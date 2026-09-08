package view;


import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

import java.awt.*;

@Slf4j
public class ZebraTreeCellRenderer extends DefaultTreeCellRenderer {
    //private final Icon nodeIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(aoi.Main.class.getResource("icons8-node-16.png")));


    public ZebraTreeCellRenderer() {
        setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
        setBackgroundSelectionColor(new Color(0, 0, 0, 0));
    }


    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (selected) {
            setBackgroundSelectionColor(new Color(51, 153, 255, 100));
        }

        return c;
    }
}
