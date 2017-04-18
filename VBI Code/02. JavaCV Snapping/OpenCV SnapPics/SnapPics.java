
// SnapPics.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from a webcam, using OpenCV's 
   VideoCapture class. If the user presses <enter>, <space> or 
   numpad '5' then the current images are saved.

   Usage:
      > java SnapPics
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class SnapPics extends JFrame
{
  static {
    System.loadLibrary("opencv_java245");
  }


  private PicsPanel pp;


  public SnapPics()
  {
    super( "Snaps Pics (with OpenCV)" );
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );  

    pp = new PicsPanel(); 
    c.add(pp, BorderLayout.CENTER);

	addKeyListener( new KeyAdapter() {
      public void keyPressed(KeyEvent e)
      { 
        int keyCode = e.getKeyCode();
        if ((keyCode == KeyEvent.VK_NUMPAD5) || (keyCode == KeyEvent.VK_ENTER) ||
             (keyCode == KeyEvent.VK_SPACE))
          // fire when press NUMPAD-5, enter, space
          pp.takeSnap();
      }
     });


    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { pp.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of SnapPics()



  // --------------------------------------------------

  public static void main( String args[] )
  {  new SnapPics(); }  

} // end of SnapPics class
