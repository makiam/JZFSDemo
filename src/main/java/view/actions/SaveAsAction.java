package view.actions;

import aoi.Application;
import lombok.extern.slf4j.Slf4j;
import model.SceneImpl;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public final class SaveAsAction extends AbstractAction {

    static javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Tokonga Scene File", "aoiz");
    static JFileChooser jfc = new JFileChooser(); {
        jfc.addChoosableFileFilter(filter);
        jfc.setCurrentDirectory(Paths.get("C:\\Tmp").toFile());
        jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    }

    private final SceneImpl scene;

    public SaveAsAction(SceneImpl scene) {
        this.putValue(Action.NAME, "Save As");
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-save-16.png"))));
        this.scene = scene;
    }
    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Do Save As " + jfc.getSelectedFile());
            try {
                SaveAsAction.commit(scene, jfc.getSelectedFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    public static void commit(SceneImpl scene, File sceneFile) throws IOException {
        log.info("Save to {}", sceneFile);
        Map<String, Object> env = new HashMap<>();
        env.put("create", true);

        try (var fs = FileSystems.newFileSystem(sceneFile.toPath(), env)) {
            Path sceneDoc = fs.getPath("/Document.json");
            scene.setPath(sceneFile.toPath());
            try (var out = Files.newOutputStream(sceneDoc)) {
                Application.serialize(out, scene);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Done.");
    }
}
