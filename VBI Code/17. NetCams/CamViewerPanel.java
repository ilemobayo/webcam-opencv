
// CamViewerPanel.java
// Andrew Davison, September 2012, ad@fivedots.psu.ac.th

/*  Shows an image received from a CamReader client
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import javax.swing.border.*;


class CamViewerPanel extends JPanel
{
  private static final int PANEL_WIDTH = 320;
  private static final int PANEL_HEIGHT = 240;


  private BufferedImage image = null;
  private int numParts = 0;
  private Font msgFont;


  public CamViewerPanel()
  { setBackground(Color.white); 
    setBorder( LineBorder.createBlackLineBorder());
    msgFont = new Font("SansSerif", Font.BOLD, 18);
  } 


  public Dimension getPreferredSize()
  {   return new Dimension(PANEL_WIDTH, PANEL_HEIGHT); }



  public void setImage(BufferedImage im, int num)
  // called from CamReceiver object that's currently using this panel
  {  image = im;  
     numParts = num;
     repaint();
  }


  public void paintComponent(Graphics g)
  { 
    g.setFont(msgFont);
    if (image != null) {    // center the image
      int x = (int)(PANEL_WIDTH - image.getWidth())/2;
      int y = (int)(PANEL_HEIGHT - image.getHeight())/2;
      g.drawImage(image, x, y, null);   // draw the snap
      g.setColor(Color.YELLOW);
	  g.drawString("Changes: " + numParts, 5, PANEL_HEIGHT-10);
    }
    else {  // no image
      g.setColor(Color.WHITE);    // white background
      g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
      g.setColor(Color.BLUE);
	  g.drawString("Waiting...", 5, PANEL_HEIGHT-10);
    }
  }  // end of paintComponent()


} // end of CamViewerPanel class

