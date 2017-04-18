
// SnapPics.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from a webcam.

   Uses VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/).

   If the user presses <enter>, <space> or 
   numpad '5' then the current image is saved.

   Usage:
      > java SnapPics
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class SnapPics extends JFrame 
{
  private PicsPanel pp;

  public SnapPics()
  {
    super( "Snap Pics (using VLC)" );
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    pp = new PicsPanel(); // the sequence of snaps appear here
    c.add(pp, BorderLayout.CENTER);

    addKeyListener( new KeyAdapter() {
      public void keyPressed(KeyEvent e)
      { 
        int keyCode = e.getKeyCode();
        if ((keyCode == KeyEvent.VK_NUMPAD5) || (keyCode == KeyEvent.VK_ENTER) ||
             (keyCode == KeyEvent.VK_SPACE))
          // take a snap when press NUMPAD-5, enter, or space is pressed
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
  {  new SnapPics();  }

} // end of SnapPics class
