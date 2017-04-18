
// DPPanel.java
// Andrew Davison, June 2011, ad@fivedots.coe.psu.ac.th

/*  A panel that shows a background image and a grid of drums.
    When a mouse is pressed, drum beating is carried out until the
    mouse is released. The rate of beating depends how close the
    mouse is to the center of a drum. The mouse can also be dragged.

    Only one drum can be beating at a time.
*/

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;


public class DPPanel extends JPanel implements MouseListener, MouseMotionListener
{
  private static final String BACKGROUND_IM = "jazz.png";


  private DrumsManager drummer;    // manages all the drums
  private BufferedImage bgImage = null;
  private int width, height;    // of background image, and the panel also


  public DPPanel()
  {
    setBackground(Color.WHITE);

    bgImage = loadImage(BACKGROUND_IM);
    if (bgImage == null) {
      System.out.println("Could not load " + BACKGROUND_IM);
      System.exit(1);
    }
    width = bgImage.getWidth();
    height = bgImage.getHeight();

    addMouseListener(this);
    addMouseMotionListener(this);

    drummer = new DrumsManager(width, height, 1);   // one stick
  } // end of DPPanel()



  private BufferedImage loadImage(String fnm)
  // load an image file and return it as a BufferedImage.
  {
    try {
      BufferedImage image = ImageIO.read( new File(fnm));
      if (image == null || (image.getWidth() < 0))
        // could not load the image - probably invalid file format
        return null;
      return image;
    }
    catch (IOException e) 
    {  return null; }
  }  // end of loadImage()


   
  public Dimension getPreferredSize()
  {   return new Dimension( width, height); }
    


  public void paintComponent(Graphics g)
  // draw background image and the drums
  {
    super.paintComponent(g);  // repaint standard stuff first
    g.drawImage(bgImage, 0, 0, null);
    drummer.draw(g);
  }  // end of paintComponent()


  public void stopPlaying()
  { drummer.stopPlaying();  }


  // ------------ mouse listener actions --------------------------------

  public void mousePressed(MouseEvent e)
  /* start beating a drum; the mouse location determines which drum
     and the rate */
  { 
    drummer.startBeating(0, e.getX(), e.getY());   // use stick 0
    repaint();
  } 

  public void mouseReleased(MouseEvent e) 
  // stop beating the drum
  { 
    drummer.stopBeating(0);
    repaint();
  }  

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}


  // ------------------ mouse motion listeners ------------------------

  public void mouseDragged(MouseEvent e) 
  /* continue beating, but the mouse location determines which drum
     and the rate */
  {
    drummer.startBeating(0, e.getX(), e.getY());   // use stick 0
    repaint();
  }  

  public void mouseMoved(MouseEvent e) {}

} // end of DPPanel
