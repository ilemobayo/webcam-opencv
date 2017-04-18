
// Capturer.java
// Andrew Davison, May 2012, ad@fivedots.coe.psu.ac.th

/* Read video from a capture device, but display it as a sequence of snapshots.

   Uses VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/)
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Capturer extends JFrame 
{
  private CaptureSnapPanel cp;

  public Capturer()
  {
    super("Capturer");
	Container c = getContentPane();
    cp = new CaptureSnapPanel();
	c.add( cp );

    addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) 
       { cp.close();
         System.exit(0);
       }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);  // center the window 
    setVisible(true);
  }  // end of Capturer()


   // ---------------------------------------------------

  public static void main(String args[])
  { new Capturer(); }

} // end of Capturer

