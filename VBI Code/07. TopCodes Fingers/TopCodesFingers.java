
// TopCodesFingers.java
// Andrew Davison, May 2013, ad@fivedots.psu.ac.th

/* Using a webcam and topcode placement codes 
   (http://users.eecs.northwestern.edu/~mhorn/topcodes/)
   to track the movement of the user's index finger, and whether
   his middle finger is visible. This information is used to 
   move and 'press' a target image in another window.

   The webcam image includes additional topcode information,
   including its ID, a picture of the topcode,
   its (x, y, z) coordinate, and a line denoting its orientation from
   the vertical.

   Usage:
   > run TopCodesFingers
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class TopCodesFingers extends JFrame 
{
  // dimensions of target window
  private static final int TARGET_WIDTH = 800;  
  private static final int TARGET_HEIGHT = 650;

  private TCPanel topCodesPanel;


  public TopCodesFingers()
  {
    super("Topcodes Glove");

    Container c = getContentPane();

    TargetMover tm = new TargetMover(TARGET_WIDTH, TARGET_HEIGHT);
        // a window showing a target that is moved by the user's finger movement

    topCodesPanel = new TCPanel(tm); 
         /* the webcam pics and topcodes are rendered in this panel */
    c.add( topCodesPanel);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { topCodesPanel.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    setResizable(false);
    pack();

    // position at right of screen
    Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = scrDim.width - getWidth();
    int y = (scrDim.height - getHeight())/2;
    setLocation(x,y);

    setVisible(true);
  } // end of TopCodesFingers()



  // -------------------------------------------------------

  public static void main( String args[] )
  {  new TopCodesFingers();  }

} // end of TopCodesFingers class
