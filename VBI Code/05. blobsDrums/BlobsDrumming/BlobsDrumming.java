
// BlobsDrumming.java
// Andrew Davison, Ju1y 2013, ad@fivedots.psu.ac.th

/* The user holds two coloured rectangles. By moving them in front of 
   a webcam, the bounded boxes for the rectangles (blobs) are detected, and
   translated into 'drum sticks' that are used to make drums beat.
   The drums are drawn on the webcam image, so the user can see where
   to place the rectangles. Turning the rectangles increases the
   beat rate.

   Usage:
   > run BlobsDrumming
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;


public class BlobsDrumming extends JFrame 
{
  // GUI components
  private BDPanel drummingPanel;


  public BlobsDrumming()
  {
    super("Blobs Drumming");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    drummingPanel = new BDPanel(); // the webcam pictures and drums appear here
    c.add( drummingPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { drummingPanel.closeDown();    // stop snapping pics, and any drum playing
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of BlobsDrumming()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  new BlobsDrumming();  }

} // end of BlobsDrumming class
