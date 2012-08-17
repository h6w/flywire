package org.bentokit.flywire.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Map;
import java.util.HashMap;

import javax.swing.border.*;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.event.MediaListener;
import org.bentokit.flywire.media.MediaInfo;
import org.bentokit.flywire.media.MediaInfoListener;
import org.bentokit.flywire.media.MediaItem;

public class MediaPanel extends JPanel implements MediaInfoListener, MediaListener, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8810075324873187385L;

	enum CollectionTag { 
		SUMMARY(new String[] {"filename","duration"}), 
		XSPF(new String[] { "filename", "duration", "libnum", "item", "description" }),
		VERBOSE(new String[] { });
		
		String[] fields;
		
		private CollectionTag(String[] fields) {
			this.fields = fields;
		}
		
		public String[] getFields() { return this.fields; }
	}
	
	protected MediaItem item;
	//MediaInfo info;
    Map<String,JLabel> fields;
	
	public MediaPanel(MediaItem mediaItem) {
        super();
        setOpaque(true);
		this.item = mediaItem;
        this.fields = new HashMap<String,JLabel>();
		this.item.addListener(this);
        this.item.getInfo().addListener(this);

        //this.setBorder(new CompoundBorder(new LineBorder(Color.RED,1,true),new EmptyBorder(5,5,5,5)));        
	}
	
/*
	public MediaPanel(MediaInfo info, String[] fields) {
		
	}
*/
	
	public MediaItem getMediaItem() { return(this.item); }

    public JLabel addField(String name) {
        JLabel label = new JLabel();
        label.setBorder(new CompoundBorder(new LineBorder(Color.RED,1), new EmptyBorder(3,3,3,3)));        
        this.fields.put(name,label);
        //System.err.println("Label created and added to map.");
        label.setText(this.item.getInfo().get(name));
        //System.err.println("Label text obtained and added.");
        return(label);
    }

    public JLabel getField(String name) { return(this.fields.get(name)); }
	    
	//public abstract void layoutFields();

	//public void setInfo(MediaInfo info) {
	//	this.info = info;
	//}
	
	//public MediaInfo getInfo() { return this.info; }
	
    ///////////////////////////////////////////////////////////////////////////
    // MediaListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	public void stateChanged(MediaItemEvent event) {
        String message = event.getSource().getURI()+":MediaPanel:Received MediaItemEvent:";
		switch(event.getState()) {
			case NONEXISTENT: message += "Nonexistent"; setParams(Color.WHITE); break;
			case UNREALIZED: message += "Unrealized"; setParams(Color.WHITE); break;
			case REALIZING: message += "Realizing"; setParams(Color.YELLOW); break;
			case REALIZED: message += "Realized"; setParams(Color.YELLOW.brighter()); break;
			case PREFETCHING: message += "Prefetching"; setParams(Color.ORANGE); break;
			case PREFETCHED: message += "Prefetched"; setParams(Color.ORANGE.brighter()); break;
			case STARTED: message += "Started"; setParams(Color.GREEN); break;	
			default: message += "UNKNOWN!"; setParams(Color.RED); break;
		}
        ErrorHandler.info(message);
        animate();
	}

    public void mediaReady(MediaItem item) {
        //We only care about information changes to the media, so ignore readiness.
    }

    public void mediaPlaying(MediaItem item) {
        //We only care about information changes to the media, so ignore readiness.
    }

    public void mediaPlayed(MediaItem item) {
        //We only care about information changes to the media, so ignore playback.
    }

    public void mediaChanged(MediaItem item) {
        //We only care about information changes to the media, so ignore actual
        //Media changes.  We should get a separate infoChanged() event instead.
    }

    public void mediaUnplayable(MediaItem item) {
        ErrorHandler.info(item.getInfo().get("filename")+":MediaPanel:Received Media Unplayable event.");
        setParams(Color.RED);
        animate();
    }

    public void mediaDeleted(MediaItem item) {
        //Allow the ListPanel to handle the deletion of the display of the item.
    }

    public void disappeared(MediaItem item) {
        //Allow the ListPanel to handle the deletion of the display of the item.
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaInfoListener interface ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void infoChanged() {
        for (String field : this.fields.keySet()) {
            ErrorHandler.info(item.getInfo().get("filename")+":MediaPanel:Setting "+field+" to "+this.item.getInfo().get(field));
            this.fields.get(field).setText(this.item.getInfo().get(field));
            if (this.fields.get(field).getParent() != null)
                this.fields.get(field).getParent().validate();
            else
                this.fields.get(field).validate();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change Animation ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	/** 
	 * The last value of the label.
	 */
	private String lastValue = "";
	/** 
	 * True when an animation is in progress. 
	 */
	private boolean animating = false;
	/** 
	 * Fade value.
	 */
	private float fade = 0;
	/**
	 * Initial fade value.
	 */
	private float initFade = .1f;
	/** 
	 * This variable controls how match we 
	 * will fade in each animation loop. 
	 */
	private float fadeStep = 0.1f;
	/**
	 * Red component of the fade color.
	 */
	private float red = 0.6f;
	/**
	 * Green component of the fade color.
	 */
	private float green = 0.6f;
	/**
	 * Blue component of the fade color.
	 */
	private float blue = 1f;
	/**
	 * When a fade is initiated, the label will have this color.
	 */ 
	private Color fadeColor = new Color(red,green,blue,fade);
	/**
	 * Flag to control the fade process.
	 */ 
	private boolean paintCalled = false;
	/**
	 * If this label doesn't control it's painting, we must set
	 * the Container responsible for it. 
	 */
	private Container repaintCont = null;

	/**
	 * Sets animation parameters. Will only accept values in the
	 * range [0,1] for color components and initial fade,
	 * and in (-1,0) for fade step.
	 * @param r - float value of the red color component.
	 * @param g	- float value of the green color component.
	 * @param b	- float value of the blue color component.
	 * @param fStep	- float value of the fade step.
	 * @param iFade	- float value of the initial fade.
	 */
	public void setParams(float r, float g, float b,
							float fStep, float iFade){
		if(r >= 0 && r <= 1)		red = r;
		if(g >= 0 && g <= 1)		green = g;
		if(b >= 0 && b <= 1)		blue = b;
		if(fStep > 0 && fStep < 1)	fadeStep = fStep;
		if(iFade >= 0 && iFade <= 1)initFade = iFade;
	}


	public void setParams(Color c) {
        float[] colors = c.getComponents(null);
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];
		if(r >= 0 && r <= 1)		red = r;
		if(g >= 0 && g <= 1)		green = g;
		if(b >= 0 && b <= 1)		blue = b;
	}
	
	/**
	 * Sets the Container that controls this label's painting.
	 * Call this method if the label is a child component of a container
	 * that does not control the painting process.
	 * @param c - Container.
	 */
	public void setRepaintContainer(Container c){
		repaintCont = c;
	}

    private void animate() {
        // Only animate if there's not another animation in progress
        // and this Label is actually added to a parent Container, i.e.
        // if it is visible.
        if(!animating && this.getParent()!=null){
          Thread t = new Thread(this);
          t.start();
        }
    }

    public void paintComponent(Graphics g){
        // Let the Label perform its normal painting.
        super.paintComponent(g);
        // Now make the fade effect.
        if(fade != 0){
            Insets i = this.getInsets();
            g.setColor(fadeColor);
            g.fillRect(i.left, i.top, 
              getWidth() - i.left - i.right, 
              getHeight() - i.top - i.bottom);
        }
        // paintComponent() called, we can continue to the next
        // animation frame.
        paintCalled = true;
    }

	/**
	 * The core of the animation.
	 */
	public void run() {
		animating = true;
		fade = initFade;
		try {
			while (fade<=1) {
				fadeColor = new Color(red,green,blue,fade);
				fade += fadeStep;
				if (fade > 1) {
					fade = 1;
				}
				paintCalled = false;
				if(repaintCont == null) 
					repaint(); // This label controls it's painting.
				else 
					repaintCont.repaint(); // Ask the container to repaint.
				// Now wait until paintComponent() gets called.
				while(!paintCalled && fade!=0){
					Thread.sleep(100);
				}
			}
			animating = false;
		}
		catch (Exception e) {
			animating = false;
			ErrorHandler.error("MediaPanel:FadeOnChangeMediaPanel encountered an error: "
					+ e.getMessage());
		}
	}
}
