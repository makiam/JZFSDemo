package model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import java.util.List;

@JsonIncludeProperties({"nodes", "materials"})
public sealed interface Scene permits SceneImpl {
    List<SceneNode> getNodes();
    void addNode(SceneNode node);

    void add(SceneNode node);
    void add(SceneNode node, SceneNode parent);

    List<Material> getMaterials();
    void addMaterial(Material material);
}
