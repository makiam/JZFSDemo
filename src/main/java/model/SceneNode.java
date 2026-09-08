package model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SceneNode {

    @JsonProperty("id")
    @Getter @Setter
    private UUID id = UUID.randomUUID();

    @JsonProperty("nodes")
    @JsonIdentityReference(alwaysAsId = true)
    private List<SceneNode> children = new ArrayList<>();

    @JsonProperty("parent")
    @JsonIdentityReference(alwaysAsId = true)
    @Getter
    private SceneNode parent;

    public void setParent(SceneNode parent) {
        this.parent = parent;
    }

    @Getter @Setter
    private String name = "";

    public SceneNode() {
    }

    public SceneNode(String name) {
        this.name = name;
    }

    public List<SceneNode> getNodes() {
        return Collections.unmodifiableList(children);
    }

    public void addNode(SceneNode node) {
        node.setParent(this);
        this.children.add(node);
    }

    @Override
    public String toString() {
        return name;
    }
}
