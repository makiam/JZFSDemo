package aoi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.pf4j.DefaultPluginManager;
import view.SceneViewer;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

@Slf4j
public class Application {

    static {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.atError().setCause(e).log("Error: {}", e );
            JOptionPane.showMessageDialog(null, e, "Tokonga ZIP FS Demo", JOptionPane.ERROR_MESSAGE);
        });
    }

    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    static {
        SimpleModule sm = new SimpleModule("Geometry Module", new Version(1,0,0, ""));
        objectMapper.registerModule(sm);

    }


    /**
     * Serialize Scene to JSON string
     */
    public static String serializeScene(Scene scene) throws JsonProcessingException {
        return objectMapper.writeValueAsString(scene);
    }

    /**
     * Deserialize JSON string to Scene
     */
    public static SceneImpl deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SceneImpl.class);
    }

    public static SceneImpl deserialize(InputStream input) throws IOException {
        return objectMapper.readValue(input, SceneImpl.class);
    }

    public static void serialize(OutputStream out, Scene scene) throws IOException {
        objectMapper.writeValue(out, scene);
    }

    public static void main(String... args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
        }

        var pm = new DefaultPluginManager();
        pm.loadPlugins();
        pm.startPlugins();


        pm.getExtensionClasses(Material.class).forEach(material -> {
            log.info("Found Material {}", material.getClasses());
            objectMapper.registerSubtypes(new NamedType(material.getClass(), material.getClass().getSimpleName()));
        });

        CompilerConfiguration cc = new CompilerConfiguration();
        ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports(JFrame.class.getPackage().getName());
        ic.addStarImports(Application.class.getPackage().getName());
        cc.addCompilationCustomizers(ic);
        var shell = new GroovyShell(cc);

        try {
            // Create a scene with nested nodes
            SceneNode root = new SceneNode("RootNode");
            SceneNode child1 = new SceneNode("ChildNode1");
            SceneNode child2 = new SceneNode("ChildNode2");
            SceneNode grandchild1 = new SceneNode("GrandchildNode1");
            SceneNode grandchild2 = new SceneNode("GrandchildNode2");


            // Build hierarchy (parent is automatically set)
            root.addNode(child1);
            root.addNode(child2);
            child1.addNode(grandchild1);
            child2.addNode(grandchild2);


            Scene scene = new SceneImpl();

            scene.add(root);
            scene.addNode(child1);
            scene.addNode(child2);
            scene.addNode(grandchild1);
            scene.addNode(grandchild2);
            scene.addNode(new SceneNode("Camera"));
            scene.addNode(new SceneNode("Light"));

            scene.addMaterial(new SimpleMaterial("Simple Material"));
            scene.addMaterial(new GraphMaterial("Graph Material"));

            // Serialize
            String json = serializeScene(scene);
            System.out.println("Serialized JSON:");
            System.out.println(json);

            // Deserialize
            SceneImpl deserializedScene = deserialize(json);
            System.out.println("\nDeserialized scene has " + deserializedScene.getNodes().size() + " nodes");

            // Verify the structure is preserved
            SceneNode deserializedRoot = deserializedScene.getNodes().getFirst();
            System.out.println("Root node has " + deserializedRoot.getNodes().size() + " children");



            try(var loader = Executors.newSingleThreadExecutor()) {
                loader.submit(() -> SwingUtilities.invokeLater(() -> new SceneViewer(new SceneImpl()).setVisible(true)));
            }


        } catch (JsonProcessingException e) {
            log.atError().setCause(e).log("Error processing JSON");
        }
    }


}