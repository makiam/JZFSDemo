package view.actions;

import lombok.extern.slf4j.Slf4j;
import model.SceneImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Slf4j
public final class SaveAction extends AbstractAction {

    private final SceneImpl scene;

    /**
     * Creates an {@code Action}.
     */
    public SaveAction(SceneImpl scene) {
        this.putValue(Action.NAME, "Save");
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        this.scene = scene;
        this.setEnabled(scene.getPath() != null);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (scene.getPath() == null) {
            log.info("Scene was not saved");
        }
    }
}
