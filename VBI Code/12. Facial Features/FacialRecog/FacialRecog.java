
// FacialRecog.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from a webcam in a picture panel
   and outline the facial features (e.g. nose, mouth). Report on
   the user 'mood' and add a mustache.
  
   Usage:
      > java FacialRecog
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;


public class FacialRecog extends JFrame
{
  // GUI components
  private FFPanel facePanel;

  static {     // load facesdk.dll for use in FaceInfo class
    try {
      System.loadLibrary("facesdk");
    } 
    catch (UnsatisfiedLinkError e) {
      System.out.println("facesdk.dll failed to load.\n" + e);
      System.exit(1);
    }
  }


  public FacialRecog()
  {
    super("Facial Features Recognizer");
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );  
   
    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    facePanel = new FFPanel(); // the sequence of webcam pics appear here
    c.add(facePanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { facePanel.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of FacialRecog()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  new FacialRecog();  }

} // end of FacialRecog class
