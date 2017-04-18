
// SnapPics.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from two webcams, using JavaCV's 
   FrameGrabber class. If the user presses <enter>, <space> or 
   numpad '5' then the current images are saved.

   Usage:
      > java SnapPics
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;


public class SnapPics extends JFrame
{
  private DoublePicsPanel pp;

  public SnapPics()
  {
    super( "Snaps Pics (with JavaCV)" );
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );  

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    pp = new DoublePicsPanel(0, 1);    // FrameGrabber IDs for the two cameras
       // these IDs come from my running ListDevices
    c.add(pp, BorderLayout.CENTER);

	addKeyListener( new KeyAdapter() {
      public void keyPressed(KeyEvent e)
      { 
        int keyCode = e.getKeyCode();
        if ((keyCode == KeyEvent.VK_NUMPAD5) || (keyCode == KeyEvent.VK_ENTER) ||
             (keyCode == KeyEvent.VK_SPACE))
          // fire when press NUMPAD-5, enter, space
          pp.takeSnaps();
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
