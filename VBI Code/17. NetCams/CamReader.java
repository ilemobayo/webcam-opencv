
// CamReader.java
// Andrew Davison, September 2012, ad@fivedots.psu.ac.th

/* Collect a stream of images from the local webcam,
   and pass those images over to CamsViewer for display. The input
   images are also shown in a panel here.

   Usage:
      > java CamReader <IP address of CamsViewer machine>
   or > java CamReader
           -- this will assume that CamsViewer is on the same machine
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class CamReader extends JFrame 
{
  private CamReaderPanel crp;   // displays images and sends thems to CamsViewer


  public CamReader(String ipAddr)
  {
    super( "Local Cam Reader" );
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    crp = new CamReaderPanel(ipAddr); // the sequence of snaps appear here
    c.add( crp, "Center");

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { crp.closeDown();    // stop capturing and sending images
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setVisible(true);
  } // end of CamReader()


  // ---------------------------------------------------------

  public static void main( String args[] )
  {  
    if (args.length == 0) {
      System.out.println("Assumming viewer is on same machine");
      new CamReader("localHost");  
    }
    else
      new CamReader(args[0]);  
  }  // end of main()

} // end of CamReader class
