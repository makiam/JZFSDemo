# Undo Models in 3D Applications

## Overview

Different 3D applications implement undo/redo using distinct architectural patterns. This report explains each model, its trade-offs, and provides implementation guidance.

---

## 1. Snapshot-based (Blender)

**How it works:**
- Before each operation, Blender serializes the **entire affected data-block** into memory
- On undo, it deserializes the previous snapshot and replaces the current state
- On redo, it restores the snapshot taken before the undo

**Pros:**
- Simple to implement — no need for inverse operations
- Always correct — you're restoring an exact previous state
- Handles any operation uniformly

**Cons:**
- Memory-intensive — each undo step stores a full copy of modified data
- Slow for large scenes (serializing a complex mesh is expensive)
- Undo granularity is fixed per operation (can't combine steps easily)

**Used by:** Blender (`ED_undo_push()` stores `UndoStack` entries with serialized `ID` blocks)

---

## 2. Command-based (Maya, Modo, Houdini)

**How it works:**
- Each undoable operation is implemented as a **Command class** with `doIt()` and `undoIt()` methods
- `doIt()` applies the change and records what it did
- `undoIt()` reverses the change using the information recorded in `doIt()`
- The undo manager stores a stack of these command objects

**Pros:**
- Memory-efficient — stores only the command objects, not full state snapshots
- Flexible — each command knows exactly how to reverse itself
- Supports grouping (compound commands for multi-step operations)

**Cons:**
- More complex to implement — every operation needs a proper `undoIt()`
- Bug-prone — if `undoIt()` doesn't perfectly reverse `doIt()`, state becomes inconsistent
- Hard to undo operations with side effects (file I/O, network calls, etc.)

**Used by:**
- Maya: `MPxCommand` with `isUndoable()` flag
- Modo: `CLxCommand` with `undoable` flag
- Houdini: `UT_UndoCommand` subclasses

---

## 3. Operation-based / Delta capture (3ds Max)

**How it works:**
- Before an operation, the system captures a **minimal delta** (what will change)
- The `RestoreObj` stores only the affected fields/properties, not the entire object
- On undo, it applies the stored delta in reverse
- On redo, it re-applies the delta

**Pros:**
- More memory-efficient than full snapshots
- More targeted than command-based — only stores what actually changes
- Avoids the need for a separate `undoIt()` implementation

**Cons:**
- Requires the system to know which fields will change before the operation
- Less flexible than command-based — hard to handle operations with complex side effects
- The delta capture mechanism must be carefully designed for each operation type

**Used by:** 3ds Max (`RestoreObj` with `Hold()` / `Fetch()` pattern)

---

## 4. Document-based (Cinema 4D)

**How it works:**
- Each document maintains its own undo stack
- Before any modification, `AddUndo()` is called to register the change
- The undo manager can operate at the document level (affecting the entire scene) or per-object
- On undo/redo, `BaseDocument::Do()` executes the reversal

**Pros:**
- Natural fit for document-oriented workflows (multiple open documents)
- Each document's undo state is independent
- Supports document-level operations (e.g., "undo all changes since last save")

**Cons:**
- Tightly coupled to the document model — harder to use for non-document operations
- Memory usage depends on implementation (may store snapshots or deltas internally)
- Cross-document operations can be complex to undo

**Used by:** Cinema 4D (`CUndoManager` with per-document stacks)

---

## Comparison Table

| Aspect | Snapshot (Blender) | Command (Maya/Modo/Houdini) | Operation (3ds Max) | Document (Cinema 4D) |
|---|---|---|---|---|
| **What's stored** | Full data-block copy | Command object with doIt/undoIt | Minimal delta (RestoreObj) | Per-document undo stack |
| **Memory usage** | High | Low | Low-Medium | Varies |
| **Implementation complexity** | Low | High | Medium | Medium |
| **Bug risk** | Low (exact restore) | High (imperfect undoIt) | Medium | Medium |
| **Granularity** | Per-operation | Per-command | Per-operation | Per-document |
| **Best for** | Simple, correct undo | Flexible, complex operations | Targeted, efficient undo | Multi-document workflows |

---

## Efficiency for Large Scenes

**Operation-based (delta capture)** is the most efficient for large scenes because it stores only the minimal change, not the entire object.

**Memory comparison for a large scene:**

| Operation | Snapshot | Command | Operation/Delta | Document |
|---|---|---|---|---|
| Move 1 vertex on 1M mesh | ~8MB (full mesh copy) | ~few bytes (command object) | ~few bytes (delta) | Varies |
| Add 100 objects | ~100x full object copies | ~100 command objects | ~100 deltas | Full stack |
| Delete 1 material | ~full material copy | ~command object | ~delta | Varies |

**Trade-off:** Operation-based requires the system to predict what will change before the operation. Commands are slightly less memory-efficient but more flexible.

**In practice:** Most production apps (Maya, Modo, Houdini) use **command-based** as the best balance of efficiency and flexibility.

---

## Implementation Guide: Command-based Undo

### Core Interface

```java
public interface UndoableCommand {
    void execute();
    void undo();
    String getDisplayName();
}
```

### Undo Manager

```java
public class UndoManager {
    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    public void execute(UndoableCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        UndoableCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        UndoableCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
```

### Concrete Command Example

```java
public class AddMaterialCommand implements UndoableCommand {
    private final Scene scene;
    private final Material material;

    public AddMaterialCommand(Scene scene, Material material) {
        this.scene = scene;
        this.material = material;
    }

    @Override
    public void execute() {
        scene.getMaterials().add(material);
        scene.setModified(true);
    }

    @Override
    public void undo() {
        scene.getMaterials().remove(material);
        scene.setModified(true);
    }

    @Override
    public String getDisplayName() {
        return "Add Material";
    }
}
```

### Compound Commands

```java
public class CompoundCommand implements UndoableCommand {
    private final List<UndoableCommand> commands = new ArrayList<>();
    private final String name;

    public CompoundCommand(String name) { this.name = name; }

    public void add(UndoableCommand cmd) { commands.add(cmd); }

    @Override
    public void execute() {
        for (UndoableCommand cmd : commands) cmd.execute();
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String getDisplayName() { return name; }
}
```

### Integration with Scene Modified State

```java
public class UndoManager {
    private final Scene scene;
    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    public UndoManager(Scene scene) { this.scene = scene; }

    public void execute(UndoableCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        scene.setModified(true);
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        UndoableCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        scene.setModified(true);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        UndoableCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        scene.setModified(true);
    }
}
```

### Key Design Points

1. `execute()` applies the change, `undo()` reverses it
2. New actions clear the redo stack
3. `scene.setModified(true)` is called on every operation (matches behavior of all 3D apps)
4. `scene.setModified(false)` is only called by `scene.save()`
5. Compound commands group multiple operations into one undo step
