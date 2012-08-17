package org.bentokit.flywire.gui.panels;

import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.bentokit.krispi.AppPath;
import org.bentokit.krispi.PluginManager;
import info.clearthought.layout.TableLayout;

public class PanelView extends JPanel {
    PanelModel model;
    PluginManager pluginManager;

    public PanelView(PluginManager pluginManager, PanelModel model) {
        this.model = model;
        this.pluginManager = pluginManager;

        int numchildren = 1;
        double[] rows = {TableLayout.FILL};
        double[] cols = new double[numchildren];
        for (int i = 0; i < numchildren; i++) cols[i] = TableLayout.FILL;
        this.setLayout(new TableLayout(cols,rows));

        numchildren = this.convertNodeToGUI(this,this.model.getDocument().getDocumentElement());
        //System.err.println(numchildren+" counted.");
     }

    public int convertNodeToGUI(JPanel container, Node node) {
       int childCount = 0;
       for(Node childNode = node.getFirstChild();
            childNode!=null;){
            Node nextChild = childNode.getNextSibling();
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                switch(PanelModel.PanelType.parseString(childElement.getNodeName())) {
                    case CONTAINER:
                        {
                            JPanel child = new JPanel();
                            String layoutStr = childElement.getAttribute("layout");
                            if (layoutStr.equals("BoxLayout")) {
                                String axisStr = childElement.getAttribute("axis");
                                child.setLayout(new BoxLayout(child,Integer.parseInt(axisStr)));
                                this.convertNodeToGUI(child,childNode);
                            } else System.err.println("Unknown layout manager \""+layoutStr+"\"");
                            if (container == this)
                                container.add(child,"0,"+childCount++);
                            else
                                container.add(child);
                        }
                        break;
                    case PLUGIN:
                        {
                            System.err.println(childElement.getAttribute("name")+" Plugin found in layout.");                            
                            org.bentokit.krispi.Plugin plugin = this.pluginManager.getPluginByName(childElement.getAttribute("name"));
                            if (plugin != null && plugin.getActiveObject() != null) {
                                Collection<Object> results = plugin.getActiveObject().hook(new AppPath("pluginPanel"));
                                for (Object obj : results) {
                                    if (obj instanceof JPanel) {
                                        if (container == this)
                                            container.add((JPanel)obj,"0,"+childCount++);
                                        else
                                            container.add((JPanel)obj);
                                    }
                                    else System.err.println("Plugin pluginPanel method didn't return a JPanel.");
                                }
                            }
                        }
                        break;
                    default: System.err.println("Unknown xml element found: "+childElement.getNodeName());
                }
            }
            childNode = nextChild;
        }
        return(childCount);
    }
}
