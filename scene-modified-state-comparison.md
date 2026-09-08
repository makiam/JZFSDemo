# Scene Modified State Comparison Across 3D Applications

## Overview

How different 3D applications track and manage scene modified state when objects are added, undo/redo is performed, and files are saved.

---

## Blender

**Modified Flag System:**
- Uses `is_dirty` global flag (`G.is_dirty`)
- Per-data-block dirty tracking via `ID.tag` (e.g., `LIB_TAG_INDIRECT`, `ID_TAG_DIRTY`)
- The `*` asterisk in the title bar is driven by `G.is_dirty`

**Object Add:**
- `BKE_id_tag_set(id, ID_TAG_DIRTY)` marks data as dirty
- `G.is_dirty = 1` is set globally
- The depsgraph is tagged for update

**Undo/Redo:**
- Snapshot-based: `ED_undo_push()` saves full state before each operation
- On undo, restores previous state and marks scene as modified
- `G.is_dirty` stays `1` — undo is itself an operation on the undo stack

**Save:**
- `BLO_write_file()` / `WM_save_as_mainfile()` writes the file
- Clears all `ID.tag` dirty flags on data-blocks
- Resets `G.is_dirty` to 0
- `WM_event_add_notifier()` refreshes UI indicators

**Key code paths:**
- `source/blender/editors/screen/screen_ops.c` — save operations
- `source/blender/blenkernel/intern/blenloader.c` — undo snapshots
- `source/blender/windowmanager/intern/wm.c` — `G.is_dirty` global flag

---

## Autodesk Maya

**Modified Flag System:**
- Uses `MFnDagNode` and `MItDag` with dirty flags on nodes
- `MDGModifier` tracks changes to the DAG (Directed Acyclic Graph)
- Each `MObject` has an internal `isModified` state managed by the DG

**Object Add:**
- `MDagModifier::createNode()` or `MFnTransform::create()` modifies the DAG
- Maya marks the DG as dirty and triggers `MEvent::kDGModified` events
- The undo queue (`MGlobal::executeCommand` with `undoable=true`) snapshots the state

**Undo/Redo:**
- `MGlobal::undo()` / `MGlobal::redo()` operate on a command stack (`MCommandHistory`)
- Each undoable command stores before/after state via `MDGModifier`
- After undo/redo, Maya re-evaluates the DG and marks dependent nodes dirty

**Save:**
- `MFileIO::save()` writes `.mb`/`.ma` file and clears dirty flags on all `MObjects`
- The `MDGContext` is reset after save
- `MSceneMessage::kAfterSave` callback fires

**Key differences from Blender:**
- Maya's DG is fully node-based (everything flows through connections), so dirty propagation is automatic
- Uses `MCallbackId` for event hooks (`kNodeAdded`, `kBeforeFileWrite`, etc.)
- Undo is command-based (each operation is an `MPxCommand`), not snapshot-based

---

## Autodesk 3ds Max

**Modified Flag System:**
- Uses `INode` objects with `IsModified()` / `SetModifiedFlag()` methods
- The `Scene` class maintains a global dirty flag
- `NotifyChanged()` propagates changes through the node hierarchy

**Object Add:**
- `INode* node = GetCOREInterface()->CreateObjectNode()` sets the scene's modified state
- `GetCOREInterface()->NodeAdded()` fires a notification
- The scene's `IsModified()` returns true immediately

**Undo/Redo:**
- Uses `Hold()` / `Fetch()` pattern (MacroRecorder / undo system)
- `GetCOREInterface()->GetUndoManager()` manages command stacks
- Each undoable operation stores a `RestoreObj` with before/after state
- After undo/redo, `NotifyChanged()` is called to update viewport and dependents

**Save:**
- `GetCOREInterface()->SaveToFile()` writes `.max` file and clears all `INode::IsModified()` flags
- `GetCOREInterface()->FileSaveCompleted()` fires post-save notifications
- The title bar asterisk is driven by `Scene::IsModified()`

**Key differences from Blender:**
- 3ds Max uses a more callback-heavy approach (`RegisterNotification`)
- Undo granularity is coarser (operates on entire operations, not individual data changes)
- The `Scene` class acts as a centralized state manager, unlike Blender's distributed `G.is_dirty`

---

## Modo (Luxology/Foundry)

**Modified Flag System:**
- Uses `CLxScene` and `CLxItem` with per-item dirty flags
- The `SceneService` manages a global modified state via `LXtSceneStats`
- Each `CLxItem` has `IsModified()` / `SetModified()` methods

**Object Add:**
- `CLxScene::ItemAdd()` creates the item and marks the scene dirty
- `LXe_SCENE_MODIFIED` event is fired
- The undo stack (`CLxUndoStack`) records the operation

**Undo/Redo:**
- Uses command-based undo (`CLxCommand` with `undoable` flag)
- `LXtUndoState` stores before/after state for each operation
- After undo/redo, `CLxScene::SetModified(true)` is called — undo does NOT clear the modified flag

**Save:**
- `CLxScene::Save()` writes `.lxo` file and clears all dirty flags
- `LXe_SCENE_SAVED` notification is sent
- The modified indicator in the UI is cleared

**Key characteristics:**
- Modo's undo granularity is finer than Maya's (closer to Blender's snapshot approach)
- The scene graph is more procedural, so dirty propagation is partially automatic
- `CLxUndoStack::GetState()` can query if undo has entries (but doesn't affect modified flag)

---

## Cinema 4D (Maxon)

**Modified Flag System:**
- Uses `BaseDocument` with `IsModified()` / `SetModified()` methods
- Each `BaseObject` has `IsDirty()` / `SetDirty()` flags
- The `EventAdd()` function propagates changes through the scene graph

**Object Add:**
- `BaseDocument::InsertObject()` adds the object and calls `SetModified(true)`
- `EventAdd()` triggers viewport and UI updates
- The undo system (`CUndoManager`) records the command

**Undo/Redo:**
- Uses `CUndoManager` with `AddUndo()` before each modification
- `BaseDocument::Do()` executes the undo/redo operation
- After undo/redo, `SetModified(true)` remains set — the scene is still dirty

**Save:**
- `BaseDocument::Save()` / `BaseDocument::SaveCopy()` writes `.c4d` file
- `SetModified(false)` clears the dirty flag
- `EventAdd()` refreshes the UI to remove the modified indicator

**Key characteristics:**
- Cinema 4D's undo system is document-based (each document has its own undo stack)
- `IsModified()` checks both the document and all contained objects
- The `EventAdd()` function is the central notification mechanism (similar to Blender's `WM_event_add_notifier()`)

---

## Houdini (SideFX)

**Modified Flag System:**
- Uses `OP_Node` with per-node dirty flags (`OP_Node::needsToCook()`)
- The `CH_Manager` (Channel Manager) tracks parameter changes
- The scene-level dirty state is managed by `OP_Network` and propagated upward through the node hierarchy

**Object Add:**
- `OP_Node::createNode()` adds the node and marks it dirty
- The dependency graph (`OP_Depend`) is invalidated for affected nodes
- The undo system (`UT_UndoManager`) records the operation

**Undo/Redo:**
- Uses command-based undo (`UT_UndoCommand` subclasses)
- `UT_UndoManager` manages a per-context undo stack
- After undo/redo, the affected nodes are marked for recooking (`OP_Node::setNeedsToCook()`)
- The scene's modified state remains set — undo does not clear it

**Save:**
- `OP_Director::save()` / `OP_Node::save()` writes `.hip` / `.hiplc` files
- Clears dirty flags on all nodes after serialization
- The modified indicator in the UI is cleared

**Key characteristics:**
- Houdini's node-based procedural architecture means dirty propagation is automatic — if a node changes, all downstream nodes are marked for recooking
- The undo system is integrated with the cooking (evaluation) pipeline, so undo triggers a re-evaluation of the dependency graph
- `OP_Node::isDirty()` can check if a node needs recooking, but this is separate from the "modified since last save" state
- Houdini distinguishes between "dirty for cooking" (recomputation needed) and "modified since save" (user-facing dirty flag)

**Key differences from other apps:**
- Houdini's procedural nature means even after undo, nodes may need to recook to reflect the restored state
- The undo system is more tightly coupled to the dependency graph than in other apps
- The `CH_Manager` provides a separate layer of change tracking for animation parameters

---

## Summary Comparison

| Aspect | Blender | Maya | 3ds Max | Modo | Cinema 4D | Houdini |
|---|---|---|---|---|---|---|
| **Dirty tracking** | Per-data-block (`ID.tag`) | Per-node in DG (`MObject`) | Per-node (`INode`) + global `Scene` | Per-item (`CLxItem`) + `LXtSceneStats` | Per-object (`BaseObject`) + `BaseDocument` | Per-node (`OP_Node`) + dependency graph |
| **Undo model** | Snapshot-based | Command-based (`MPxCommand`) | Operation-based (`RestoreObj`) | Command-based (`CLxCommand`) | Document-based (`CUndoManager`) | Command-based (`UT_UndoCommand`) |
| **Prop change propagation** | Manual `recalc` flags | Automatic via DG connections | `NotifyChanged()` callbacks | Partially automatic (procedural graph) | `EventAdd()` notifications | Automatic via dependency graph + recooking |
| **Post-save cleanup** | Clear `G.is_dirty` + `ID.tag` | Clear `MObject` dirty + callbacks | Clear `INode::IsModified()` | Clear `LXtSceneStats` | `SetModified(false)` + `EventAdd()` | Clear `OP_Node` dirty flags + save file |
| **After add+undo** | Still dirty | Still dirty | Still dirty | Still dirty | Still dirty | Still dirty |

---

## Key Insight

In **all six applications**, the scene remains **modified (dirty)** after an add + undo operation. This is because:

1. **Dirty flags are set by operations, cleared only by save.** Undo is an operation (it pushes to the undo stack), so it doesn't clear the flag.
2. **Reverting to the exact saved state would require expensive state comparison.** None of these apps implement this optimization.
3. **The user expectation is consistent:** if you undid something, you still "did work" in the session, and the scene should remain marked as modified until explicitly saved.
