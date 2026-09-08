package model;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class SceneImpl implements Scene {

    @Getter
    @Setter
    private Path path;

    private final List<SceneNode> nodes = new ArrayList<>();


    @Override
    public List<SceneNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void addNode(SceneNode node) {
        this.nodes.add(node);
    }

    @Override
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    private final List<Material> materials = new ArrayList<>();

    @Override
    public void addMaterial(Material material) {
        materials.add(material);
    }

    @Override
    public String toString() {
        return "Scene";
    }

    public void add(SceneNode node) {
        this.nodes.add(node);
    }

    public void add(SceneNode node, SceneNode parent) {

    }
}
