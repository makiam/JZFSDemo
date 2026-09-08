package view;

import model.Scene;
import model.SceneImpl;
import model.SceneNode;
import view.actions.NewSceneAction;
import view.actions.OpenSceneAction;
import view.actions.SaveAction;
import view.actions.SaveAsAction;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SceneViewer extends JFrame {

    private final Scene scene;
    private JTree tree;

    public SceneViewer(Scene scene) {
        super("Scene Viewer");
        this.scene = scene;
        var sp =  ((SceneImpl)scene).getPath();
        this.setTitle("Tokonga Scene Viewer" + (sp == null ? "" : ": " + sp.getFileName()));
        var fm = this.getJMenuBar().add(new JMenu("File")); {
            fm.add(new NewSceneAction());
            fm.add(new OpenSceneAction());
            fm.add(new SaveAction((SceneImpl) scene));
            fm.add(new SaveAsAction((SceneImpl) scene));
            fm.addSeparator();
            fm.add(new JMenuItem("Quit")).addActionListener(l -> dispose());
        }

        loadModel();
    }

    private void loadModel() {
        try {
            DefaultTreeModel model = new DefaultTreeModel(new SceneTreeNode(scene));
            tree.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by the constructors to init the <code>JFrame</code> properly.
     */
    @Override
    protected void frameInit() {
        super.frameInit();
        this.setSize(1280,1024);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setContentPane(tree = new ZebraTree());
        tree.setCellRenderer(new ZebraTreeCellRenderer());
        tree.setRootVisible(true);

        this.setJMenuBar(new JMenuBar());




    }
    final class SceneItemTreeNode extends DefaultMutableTreeNode {

        private final List<SceneNode> nodes = new ArrayList<>();

        public SceneItemTreeNode(SceneNode node) {
            super(node);

            nodes.addAll(node.getNodes());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TreeNode getChildAt(int index) {
            return new SceneItemTreeNode(nodes.get(index));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getChildCount() {
            return nodes.size();
        }
    }

    final class SceneTreeNode extends DefaultMutableTreeNode {

        private final List<SceneNode> nodes;
        /**
         * Creates a tree node with no parent, no children, but which allows
         * children, and initializes it with the specified user object.
         *
         * @param userObject an Object provided by the user that constitutes the node's data
         */
        public SceneTreeNode(Scene userObject) {
            super(userObject);
            nodes = userObject.getNodes().stream().filter(n -> n.getParent() == null).toList();
        }

        /**
         * Returns the number of children of this node.
         *
         * @return an int giving the number of children of this node
         */
        @Override
        public int getChildCount() {
            return nodes.size();
        }

        /**
         * Returns this node's user object.
         *
         * @return the Object stored at this node by the user
         * @see #setUserObject
         * @see #toString
         */
        @Override
        public Scene getUserObject() {
            return (Scene) super.getUserObject();
        }

        /**
         * Returns the child at the specified index in this node's child array.
         *
         * @param index an index into this node's child array
         * @return the TreeNode in this node's child array at  the specified index
         * @throws ArrayIndexOutOfBoundsException if <code>index</code>
         *                                        is out of bounds
         */
        @Override
        public TreeNode getChildAt(int index) {
            return new SceneItemTreeNode(nodes.get(index));
        }
    }

    class ZebraTree extends JTree {

        private final Color evenRowColor = new Color(240, 248, 255); // AliceBlue
        private final Color oddRowColor = Color.WHITE;

        public ZebraTree() {
            super();
            setOpaque(false);
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        setSelectionRow(row);
                    }
                }
            });
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            // Paint stripes for each visible row
            for (int i = 0; i < getRowCount(); i++) {
                Rectangle rowBounds = getRowBounds(i);
                if (rowBounds == null) {
                    continue;
                }

                // Choose color based on row index
                Color stripeColor = (i % 2 == 0) ? evenRowColor : oddRowColor;
                g2d.setColor(stripeColor);
                // Fill the entire width of the tree for this row
                g2d.fillRect(0, rowBounds.y, getWidth(), rowBounds.height);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
