package view.actions;

import aoi.Application;
import view.SceneViewer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenSceneAction extends AbstractAction {

    static javax.swing.filechooser.FileFilter filter = new FileNameExtensionFilter("Tokonga Scene File", "aoiz");
    static JFileChooser jfc = new JFileChooser(); {
        jfc.addChoosableFileFilter(filter);
        jfc.setCurrentDirectory(Paths.get("C:\\Tmp").toFile());
        jfc.setDialogType(JFileChooser.OPEN_DIALOG);
    }

    /**
     * Creates an {@code Action}.
     */
    public OpenSceneAction() {
        this.putValue(Action.NAME, "Open");
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (var fs = FileSystems.newFileSystem(jfc.getSelectedFile().toPath())) {
                Path sceneDoc = fs.getPath("/Document.json");

                try (var out = Files.newInputStream(sceneDoc)) {
                    var scene  = Application.deserialize(out);
                    scene.setPath(jfc.getSelectedFile().toPath());
                    SwingUtilities.invokeLater(() -> new SceneViewer(scene).setVisible(true));
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
