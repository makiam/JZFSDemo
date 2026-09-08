package view.actions;

import model.SceneImpl;
import view.SceneViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewSceneAction extends AbstractAction {
    /**
     * Creates an {@code Action}.
     */
    public NewSceneAction() {
        this.putValue(Action.NAME, "New");
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> new SceneViewer(new SceneImpl()).setVisible(true));
    }
}
