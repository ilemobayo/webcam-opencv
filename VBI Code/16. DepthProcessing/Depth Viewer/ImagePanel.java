     
// ImagePanel.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/*  Displays a fixed-size image, which can be updated and redrawn.
    Mouse pressing on the image causes the depth at that (x,y) position
    to be retrieved from DepthCalc and displayed on the image.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;


public class ImagePanel extends JPanel 
                        implements MouseListener
{
  // dimensions of this panel == dimensions of depth image
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private BufferedImage image = null; // current depth

  private boolean isPressed = false;
  private int xPress, yPress;
  private Font msgFont;
  private DepthCalc depthCalc;   // for obtaining depth information


  public ImagePanel(DepthCalc dc)
  { 
    depthCalc = dc;
    setBackground(Color.white);
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    addMouseListener(this);
  } // end of ImagePanel()


  public void setImage(BufferedImage im)
  {  image = im;  
     this.repaint();
  }
      

  public Dimension getPreferredSize()
  {   return new Dimension(WIDTH, HEIGHT); }


  public void paintComponent(Graphics g) 
  {
    super.paintComponent(g);

    if (image != null) {
      g.drawImage(image, 0, 0, this);
      if (isPressed) {     // draw a circle, and the depth info above
        g.setColor(Color.RED);
        g.setFont(msgFont);
        g.fillOval(xPress-5, yPress-5, 10, 10);

        int depth = depthCalc.getDepth(xPress, yPress);
        g.drawString(""+depth, xPress, yPress-7);
      }
    }
    else {
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, WIDTH, HEIGHT);
    }
  } // end of paintComponent()


  // --------------- mouse processing --------------------------

  public void mousePressed(MouseEvent e) 
  {
    isPressed = true;
    xPress = e.getX();
    yPress = e.getY();
    this.repaint();
  }  // end of mousePressed()

  public void mouseReleased(MouseEvent e) 
  {  isPressed = false;  
     this.repaint();
  }


  public void mouseEntered(MouseEvent e){ }
  public void mouseExited(MouseEvent e){ }
  public void mouseClicked(MouseEvent e){ }
} // end of ImagePanel class

