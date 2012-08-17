package org.bentokit.flywire.gui.panels;

import org.bentokit.krispi.options.types.UnknownOptionTypeException;
import org.bentokit.flywire.errorutils.ErrorHandler;

import org.bentokit.krispi.PluginManager;
import org.bentokit.krispi.options.ui.swing.*;

import java.awt.BorderLayout;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.StringWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.EventQueue;
import javax.swing.border.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PanelEditor extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2491221767027137874L;


	class Panel { };
    class Plugin { };

    //public static PanelEditor SELF;

    ActionIcon rowIcon, columnIcon; //, tableIcon, addIcon, deleteIcon;
    TrashIcon trashIcon;
    DNDContainer workingPanel;
    JPanel iconPanel;
    PanelModel model;
    PluginManager pluginManager;
    public static final DataFlavor containerFlavor = new DataFlavor(org.bentokit.flywire.gui.panels.PanelEditor.Panel.class, 
                                                "X-flywire/panel; class=<org.bentokit.flywire.gui.panels.PanelEditor.Panel>;");
    public static final DataFlavor pluginFlavor = new DataFlavor(org.bentokit.flywire.gui.panels.PanelEditor.Plugin.class, 
                                                "X-flywire/plugin; class=<org.bentokit.flywire.gui.panels.PanelEditor.Plugin>;");

    public PanelEditor(PluginManager pluginManager, PanelModel model) {
        this.model = model;
        this.pluginManager = pluginManager;

        //SELF = this;
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(5,5,5,5));

        DNDContainer rowTemplate = new DNDContainer(this.model);
        rowTemplate.setLayout(new BoxLayout(rowTemplate,BoxLayout.X_AXIS));
        DNDContainer colTemplate = new DNDContainer(this.model);
        colTemplate.setLayout(new BoxLayout(colTemplate,BoxLayout.Y_AXIS));

        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));

        rowIcon = new ActionIcon(getIcon("org/bentokit/flywire/resource/row.png"),null,"Insert row panel",rowTemplate);
        columnIcon = new ActionIcon(getIcon("org/bentokit/flywire/resource/column.png"),null,"Insert column panel",colTemplate);

        iconPanel.add(rowIcon);
        iconPanel.add(columnIcon);

        for (org.bentokit.krispi.Plugin plugin : this.pluginManager.getAvailablePlugins()) {
            DNDObject pluginTemplate = new DNDPlugin(new org.bentokit.krispi.Plugin(plugin),this.model); //Duplicate plugin to get an inactive copy.
            ActionIcon pluginIcon = new ActionIcon(plugin.getIcon(),plugin.getName(),pluginTemplate);
            iconPanel.add(pluginIcon);
        }

        trashIcon = new TrashIcon(getIcon("org/bentokit/flywire/resource/trash.png"),null,"Trash Object");

        iconPanel.add(trashIcon);

        workingPanel = new DNDContainer(this.model);
        workingPanel.setLayout(new BoxLayout(workingPanel,BoxLayout.X_AXIS));
        if (this.model.getDocument().getDocumentElement() == null) {
            System.err.println("this.model.getDocument().getDocumentElement() is null.  Can't open PanelEditor.");
            return;
        }
        workingPanel.setElement(this.model.getDocument().getDocumentElement());
        this.convertNodeToGUI(workingPanel,this.model.getDocument().getDocumentElement());

        this.add(workingPanel,BorderLayout.CENTER);
        this.add(iconPanel,BorderLayout.EAST);

    }

    public static Image getIcon(String iconName) {
        URL pathShell;
        ClassLoader cl = PanelEditor.class.getClassLoader();
        pathShell = cl.getResource(iconName);

        Image icon = Toolkit.getDefaultToolkit().getImage(pathShell);
        return(icon);
    }


    public void convertNodeToGUI(DNDContainer container, Node node) {
        for(Node childNode = node.getFirstChild();
            childNode!=null; childNode = childNode.getNextSibling()){
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                switch(PanelModel.PanelType.parseString(childElement.getNodeName())) {
                    case CONTAINER:
                        {
                            DNDContainer child = new DNDContainer(this.model);
                            child.setParent(container);
                            if (childElement == null) System.err.println("ChildElement is null!");
                            child.setElement(childElement);
                            String layoutStr = childElement.getAttribute("layout");
                            if (layoutStr.equals("BoxLayout")) {
                                String axisStr = childElement.getAttribute("axis");
                                child.setLayout(new BoxLayout(child,Integer.parseInt(axisStr)));
                                this.convertNodeToGUI(child,childNode);
                            } else System.err.println("Unknown layout manager \""+layoutStr+"\"");
                            container.addDNDObject(child,this.model);
                        }
                        break;
                    case PLUGIN:
                        {
                            org.bentokit.krispi.Plugin plugin = this.pluginManager.getPluginByName(childElement.getAttribute("name"));
                            DNDPlugin child = new DNDPlugin(plugin,this.model);
                            child.setParent(container);
                            child.setElement(childElement);
                            container.addDNDObject(child,this.model);
                        }
                        break;
                    default: System.err.println("Unknown xml element found: "+childElement.getNodeName());
                }
            }
        }
    }


    public static void showInWindow(PluginManager pluginManager) {
        JFrame frame = new JFrame("PanelEditor");
        PanelModel model = new PanelModel(new File(System.getProperty("user.home")+File.separator+".bripper"+File.separator+"layout.xml"));
        frame.getContentPane().add(new PanelEditor(pluginManager,model));

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);    
    }


    public static void main(String[] argv) {
        final PluginManager pluginManager;
    
    	pluginManager = new PluginManager((new File(System.getProperty("user.home")+File.separator+".bripper"+File.separator+"plugins")).toURI(),"BRipper");

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                showInWindow(pluginManager);
            }
        });
    }

    class ActionIcon extends JPanel implements DragGestureListener, DragSourceListener {   
        /**
		 * 
		 */
		private static final long serialVersionUID = 4542079886979465187L;
		
		DragSource dragSource;
        DNDObject templateComponent;
     
        public ActionIcon(String label, DNDObject templateComponent) {
            this.setLayout(new BorderLayout());
        
            if (label != null) {
                this.add(new JLabel(label),BorderLayout.SOUTH);
            }

            dragSource = new DragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_COPY, this);

            this.templateComponent = templateComponent;
        }

        public ActionIcon(Icon icon, String label, DNDObject templateComponent) {
            this(label, templateComponent);
            if (icon != null) {
                this.add(new JLabel(icon),BorderLayout.CENTER);
            }
        }

        public ActionIcon(Image image, String label, String tooltip, DNDObject templateComponent) {
            this(new ImageIcon(image,tooltip), label, templateComponent);
        }

        public ActionIcon(String filename, String label, String tooltip, DNDObject templateComponent) {
            this(new ImageIcon(filename,tooltip), label, templateComponent);
        }

        public void dragGestureRecognized(DragGestureEvent evt) {
            //Serialize object
            //byte[] bytes = null;
            ByteArrayOutputStream baos = null;
            ObjectOutputStream out = null;
            DNDObject newobj = null;

            try
            {
                baos = new ByteArrayOutputStream();
                out = new ObjectOutputStream(baos);
                System.err.println("Duplicating "+this.templateComponent.getClass().getName());
                                
                out.writeObject(this.templateComponent);
                out.close();

                //Reinflate object
                ByteArrayInputStream bais = null;
                ObjectInputStream in = null;
                try
                {

                    bais = new ByteArrayInputStream(baos.toByteArray());
                    in = new ObjectInputStream(bais);
                    newobj = (DNDObject)in.readObject();
                    in.close();


                    Transferable t = new TransferableComponent(new PanelEvent(newobj,PanelEditor.this.model));
                    dragSource.startDrag (evt, DragSource.DefaultCopyDrop, t, this);

                }
                catch(IOException ex)
                {
                  ex.printStackTrace();
                }
                catch(ClassNotFoundException ex)
                {
                  ex.printStackTrace();
                }


            }
            catch(java.io.NotSerializableException nse)
            {
              javax.swing.JOptionPane.showMessageDialog(this,this.templateComponent.getClass().getName()+" is not serializable because it contains "+nse.getMessage());
              nse.printStackTrace();
            }
            catch(IOException ex)
            {
              ex.printStackTrace();
            }
        }
        public void dragEnter(DragSourceDragEvent evt) {
            // Called when the user is dragging this drag source and enters
            // the drop target.
        }
        public void dragOver(DragSourceDragEvent evt) {
            // Called when the user is dragging this drag source and moves
            // over the drop target.
        }
        public void dragExit(DragSourceEvent evt) {
            // Called when the user is dragging this drag source and leaves
            // the drop target.
        }
        public void dropActionChanged(DragSourceDragEvent evt) {
            // Called when the user changes the drag action between copy or move.
        }
        public void dragDropEnd(DragSourceDropEvent evt) {
            // Called when the user finishes or cancels the drag operation.
        }

    }


    class TrashIcon extends JPanel implements DropTargetListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1096144519198428750L;
		
		Border highlightBorder;

        public TrashIcon(String label) {
            new DropTarget(this,this);
            this.setLayout(new BorderLayout());
        
            if (label != null) {
                this.add(new JLabel(label),BorderLayout.SOUTH);
            }

            this.highlightBorder = new javax.swing.border.LineBorder(java.awt.Color.GREEN,1,true);
        }

        public TrashIcon(Icon icon, String label) {
            this(label);
            if (icon != null) {
                this.add(new JLabel(icon),BorderLayout.CENTER);
            }

        }

        public TrashIcon(Image image, String label, String tooltip) {
            this(new ImageIcon(image,tooltip), label);
        }

        public TrashIcon(String filename, String label, String tooltip) {
            this(new ImageIcon(filename,tooltip), label);
        }

        /* METHODS FOR DROPTARGETLISTENER */

        public void dragEnter(DropTargetDragEvent evt) {
            // Called when the user is dragging and enters this drop target. 
            this.setBorder(this.highlightBorder);
        }
        public void dragOver(DropTargetDragEvent evt) {
            // Called when the user is dragging and moves over this drop target.
        }
        public void dragExit(DropTargetEvent evt) {
            // Called when the user is dragging and leaves this drop target.
            this.setBorder(null);
        }
        public void dropActionChanged(DropTargetDragEvent evt) {
            // Called when the user changes the drag action between copy or move.
        }
        public void drop(DropTargetDropEvent evt) {
            try {
                Transferable t = evt.getTransferable();

                if (t.isDataFlavorSupported(PanelEditor.containerFlavor)) {
                    evt.acceptDrop(DnDConstants.ACTION_MOVE);
                    PanelEvent data = (PanelEvent)t.getTransferData(PanelEditor.containerFlavor);
                    evt.getDropTargetContext().dropComplete(true);
                    DNDContainer parent = data.getObject().getParentDNDContainer();
                    if (parent == null) {
                        javax.swing.JOptionPane.showMessageDialog(this,"Cannot remove topmost container.");
                        return;
                    }
                    parent.removeDNDObject(data.getObject(),data.getModel());
                    this.setBorder(null);
                    parent.layout();
                    parent.validate();
                    parent.setVisible(true);
                    System.err.println("Removed component from panel");
                } else {
                    System.err.println("Flavour not supported.  What's ya flava?  Tell me what's ya flava. Ooooo!");
                    evt.rejectDrop();
                }
            } catch (IOException e) {
                evt.rejectDrop();
            } catch (UnsupportedFlavorException e) {
                evt.rejectDrop();
            }
        }
    }
}

abstract class DNDObject extends JPanel implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -41392875309157931L;
	
	Element element;
    DNDContainer parent;
    final PanelModel panelModel;

    public DNDObject(PanelModel panelModel) {
        super();
        this.panelModel = panelModel;
    }
    
    public Element getElement() { return(this.element); }

    public void setElement(Element element) { this.element = element; }

    public void setParent(DNDContainer parent) { this.parent = parent; }

    public DNDContainer getParentDNDContainer() { return(this.parent); }

    //Generate this node and add it to its parent.
    protected abstract void generateElement(Element parent);

    public PanelModel getModel() { return(this.panelModel); }

}

/*
class DNDSpace extends DNDObject implements DropTargetListener {
    Border defaultBorder;
    Border highlightBorder;

    public DNDSpace() {
        super();
        new DropTarget(this,this);
        this.add(new JLabel("Space"));
        this.defaultBorder = new CompoundBorder(new EmptyBorder(5,5,5,5),new javax.swing.border.LineBorder(java.awt.Color.BLUE,1,true));
        this.highlightBorder = new CompoundBorder(new EmptyBorder(5,5,5,5),new javax.swing.border.LineBorder(java.awt.Color.GREEN,1,true));
        this.setBorder(defaultBorder);
    }
    
    public Element getElement() { return(null); }

    //Generate this node and add it to its parent.
    protected void generateElement(Element parent) { ; }

    // ==== METHODS FOR DROPTARGETLISTENER =====

    public void dragEnter(DropTargetDragEvent evt) {
        // Called when the user is dragging and enters this drop target.
        this.setBorder(this.highlightBorder);
    }
    public void dragOver(DropTargetDragEvent evt) {
        // Called when the user is dragging and moves over this drop target.
    }
    public void dragExit(DropTargetEvent evt) {
        // Called when the user is dragging and leaves this drop target.
        final DNDSpace thisF = this;
        final Border defaultBorderF = this.defaultBorder;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                thisF.setBorder(defaultBorderF);
            }
        });
    }
    public void dropActionChanged(DropTargetDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void drop(DropTargetDropEvent evt) {
        try {
            Transferable t = evt.getTransferable();

            if (t.isDataFlavorSupported(PanelEditor.containerFlavor)) {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                DNDObject data = (DNDObject)t.getTransferData(PanelEditor.containerFlavor);
                evt.getDropTargetContext().dropComplete(true);                
                this.parent.addDNDObject(data,this);            //Add created component to this panel.
                data.setParent(this.parent);
                this.setBorder(this.defaultBorder);
                this.revalidate();

                System.err.println("Added component between space and component or space and wall");
            } else {
                System.err.println("Flavour nor supported");
                evt.rejectDrop();
            }
        } catch (IOException e) {
            evt.rejectDrop();
        } catch (UnsupportedFlavorException e) {
            evt.rejectDrop();
        }
    }

}
*/

class DNDPlugin extends DNDObject implements DragGestureListener, DragSourceListener, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2259290908428627401L;
	
	protected DragSource dragSource;
    org.bentokit.krispi.Plugin plugin;

    public DNDPlugin(org.bentokit.krispi.Plugin plugin, PanelModel panelModel) {
        super(panelModel);

        this.dragSource = new DragSource();
        this.dragSource.createDefaultDragGestureRecognizer(
            this, DnDConstants.ACTION_MOVE, this);

        this.setLayout(new BorderLayout());

        if (plugin.getIcon() != null) {
            this.add(new JLabel(plugin.getIcon()),BorderLayout.CENTER);
        }
        this.add(new JLabel(plugin.getName()),BorderLayout.SOUTH);

        final org.bentokit.krispi.Plugin finalPlugin = plugin;

        JMenuItem optionsMenuItem = new JMenuItem("Properties");
        if (finalPlugin.getSettings() == null) optionsMenuItem.setEnabled(false);
        optionsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JDialog optionsDlg = new JDialog((java.awt.Dialog)null,finalPlugin.getName());
                    optionsDlg.getContentPane().add(new ConfigPanel(finalPlugin));
                    optionsDlg.pack();
                    optionsDlg.setVisible(true);
                } catch(UnknownOptionTypeException uote) {
                    ErrorHandler.error(uote.getMessage());
                }
            }
        });

        JPopupMenu menu = new JPopupMenu(plugin.getName());
        menu.add(optionsMenuItem);
        this.setComponentPopupMenu(menu);


        this.plugin = plugin;
    }

    /* DNDObject Abstract Methods Implementation */

    protected void generateElement(Element parent) {
        this.element = parent.getOwnerDocument().createElement("Plugin");
        this.element.setAttribute("name",this.plugin.getName());
        parent.appendChild(this.element);
    }

    /* DragGestureListener Methods Implementation */

    public void dragGestureRecognized(DragGestureEvent evt) {
        Transferable t = new TransferableComponent(new PanelEvent(this,this.getModel()));
        this.dragSource.startDrag (evt, DragSource.DefaultCopyDrop, t, this);
    }

    /* DragSourceListener Methods Implementation */

    public void dragEnter(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and enters
        // the drop target.
    }
    public void dragOver(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and moves
        // over the drop target.
    }
    public void dragExit(DragSourceEvent evt) {
        // Called when the user is dragging this drag source and leaves
        // the drop target.
    }
    public void dropActionChanged(DragSourceDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void dragDropEnd(DragSourceDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    }
    
}

class DNDContainer extends DNDObject implements DragGestureListener, DragSourceListener, DropTargetListener, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4735353948736431574L;
	
	protected DragSource dragSource;
    Border defaultBorder;
    Border highlightBorder;
    Vector<DNDObject> children;

    public DNDContainer(PanelModel panelModel) {
        super(panelModel);
        this.add(Box.createRigidArea(new java.awt.Dimension(100,40)));

        this.dragSource = new DragSource();
        this.dragSource.createDefaultDragGestureRecognizer(
            this, DnDConstants.ACTION_MOVE, this);

        new DropTarget(this,this);

        //this.add(new JLabel("Container"));
        this.defaultBorder = new CompoundBorder(new EmptyBorder(5,5,5,5),new javax.swing.border.LineBorder(java.awt.Color.BLUE,1,true));
        this.highlightBorder = new CompoundBorder(new EmptyBorder(5,5,5,5),new javax.swing.border.LineBorder(java.awt.Color.GREEN,1,true));
        this.setBorder(defaultBorder);
        
        this.children = new Vector<DNDObject>();

        //DNDSpace space = new DNDSpace();
        //this.add(space);
        //space.setParent(this);        
    }


    /* DNDObject Abstract Methods Implementation */

    protected void generateElement(Element parent) {
        if (this.getLayout() instanceof BoxLayout) {
            if (parent == null) System.err.println("Parent is null");
            if (parent.getOwnerDocument() == null) System.err.println("Parent.getOwnerDocument() is null");
            this.element = parent.getOwnerDocument().createElement("Panel");
            this.element.setAttribute("layout","BoxLayout");
            
            this.element.setAttribute("axis",(new Integer(((BoxLayout)this.getLayout()).getAxis())).toString());
            parent.appendChild(this.element);
        }
    }


    public void addDNDObject(DNDObject child, PanelModel panelModel) {
        this.addDNDObject(child,null,panelModel);
    }

    public void addDNDObject(DNDObject child, DNDObject after, PanelModel panelModel) {
        final int index;
        //final int spaceindex;
        if (after == null) { 
            index = -1; 
            //spaceindex = -1; 
        }
        else { 
            index = this.children.indexOf(after);
            //spaceindex = index + 1;  //Because of the add, the space will need to be 1 more than where it was.
        }

        //DNDSpace space = new DNDSpace();
        //space.setParent(this);
        
        final JPanel panel = this;
        final DNDObject childF = child;
        //final DNDSpace spaceF = space;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel.add(childF,index);
//                panel.add(spaceF,spaceindex);
            }
        });
        this.children.add(child);
//        this.children.add(space);

        //child.generateElement(this.element);
        panelModel.saveXMLtoFile();
    }

    public Vector<DNDObject> getChildrenDNDObjects() {
        return(children);
    }

    //Remove a child node from this node.
    public void removeDNDObject(DNDObject child, PanelModel panelModel) {
        //Get the space first
        //DNDSpace space = (DNDSpace) this.children.get(this.children.indexOf(child)+1);

    
        //Remove both from the panel
        final JPanel panel = this;
        final DNDObject childF = child;
        //final DNDSpace spaceF = space;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel.remove(childF);
                //panel.remove(spaceF);
            }
        });

        //Remove both from the container's children
        this.children.remove(child);
        //this.children.remove(space);

        //Remove the element from the XML tree
        this.element.removeChild(child.getElement());

        panelModel.saveXMLtoFile();
    }


    /* DragGestureListener Methods Implementation */

    public void dragGestureRecognized(DragGestureEvent evt) {
        Transferable t = new TransferableComponent(new PanelEvent(this,this.getModel()));
        this.dragSource.startDrag (evt, DragSource.DefaultMoveDrop, t, this);
    }

    /* DragSourceListener Methods Implementation */

    public void dragEnter(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and enters
        // the drop target.
    }
    public void dragOver(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and moves
        // over the drop target.
    }
    public void dragExit(DragSourceEvent evt) {
        // Called when the user is dragging this drag source and leaves
        // the drop target.
    }
    public void dropActionChanged(DragSourceDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void dragDropEnd(DragSourceDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    }
    

    
    /* METHODS FOR DROPTARGETLISTENER */

    public void dragEnter(DropTargetDragEvent evt) {
        // Called when the user is dragging and enters this drop target.
        this.setBorder(this.highlightBorder);
    }
    public void dragOver(DropTargetDragEvent evt) {
        // Called when the user is dragging and moves over this drop target.
    }
    public void dragExit(DropTargetEvent evt) {
        // Called when the user is dragging and leaves this drop target.
        final DNDContainer thisF = this;
        final Border defaultBorderF = this.defaultBorder;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                thisF.setBorder(defaultBorderF);
            }
        });
    }
    public void dropActionChanged(DropTargetDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void drop(DropTargetDropEvent evt) {
        try {
            Transferable t = evt.getTransferable();

            if (t.isDataFlavorSupported(PanelEditor.containerFlavor)) {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                PanelEvent data = (PanelEvent)t.getTransferData(PanelEditor.containerFlavor);
                evt.getDropTargetContext().dropComplete(true);
                data.getObject().generateElement(this.element);
                this.addDNDObject(data.getObject(),data.getModel());            //Add created component to this panel.
                data.getObject().setParent(this);
                this.setBorder(this.defaultBorder);
                this.revalidate();
                System.err.println("Added component to panel");
            } else {
                System.err.println("Flavour not supported.  What's ya flava?  Tell me what's ya flava. Ooooo!");
                evt.rejectDrop();
            }
        } catch (IOException e) {
            evt.rejectDrop();
        } catch (UnsupportedFlavorException e) {
            evt.rejectDrop();
        }
    }
}


class PanelEvent {
    DNDObject object;
    PanelModel panelModel;

    public PanelEvent(DNDObject object, PanelModel panelModel) {
        this.object = object;
        this.panelModel = panelModel;
    }

    public DNDObject getObject() { return(this.object); }
    public PanelModel getModel() { return(this.panelModel); }
}

class TransferableComponent implements Transferable {
    PanelEvent component;

    public TransferableComponent(PanelEvent c) {
        this.component = c;
    }

    // Returns an object which represents the data to be transferred.
    public Object getTransferData(DataFlavor flavor) {
        return(this.component);
    }
    // Returns an array of DataFlavor objects indicating the flavors the data can be provided in.
    public DataFlavor[]	getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[1];
        flavors[0] = PanelEditor.containerFlavor;
        return(flavors);
    }
    // Returns whether or not the specified data flavor is supported for this object.
    public boolean	isDataFlavorSupported(DataFlavor flavor) {
        return (flavor == PanelEditor.containerFlavor);
    }
}

