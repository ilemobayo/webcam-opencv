
// ImagePanel.java
// Andrew Davison, February 2013, ad@fivedots.coe.psu.ac.th

/* A panel for displaying an image, which may be set/changed after the
   creation of the panel.
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;


public class ImagePanel extends JPanel
{
  private BufferedImage im;


  public ImagePanel(int width, int height)
  {
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(width, height));
  }  // end of ImagePanel()


  public void reset()
  {
    im = null;
    repaint();
    revalidate();
  }  // end of setImage()




  public void setImage(BufferedImage image)
  {
    im = image;
    setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
    repaint();
    revalidate();
  }  // end of setImage()



  public void paintComponent(Graphics g)
  {  
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    int pWidth = getWidth();
    int pHeight = getHeight();

    g2.setColor(Color.WHITE);
    g2.fillRect(0, 0, pWidth, pHeight);    // clear panel

    if (im != null) {
      int x = (pWidth - im.getWidth())/2;   // so image is centered in panel
      int y = (pHeight - im.getHeight())/2;
      g2.drawImage(im, x, y, this);
    }
  }  // end of paintComponent()


}  // end of ImagePanel class

