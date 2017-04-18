
// DrumPad.java
// Andrew Davison, June 2011, ad@fivedots.coe.psu.ac.th

/* A grid of drums, which the user can press on to start a drum
   beating. The rate of beating depends how close the
    mouse is to the center of a drum. The mouse can also be dragged.
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class DrumPad extends JFrame 
{
  private DPPanel dpPanel;


  public DrumPad()
  {
    super("DrumPad");
    Container c = getContentPane();
    c.setLayout( new BorderLayout());

    dpPanel = new DPPanel();
    c.add( dpPanel, BorderLayout.CENTER);

    addWindowListener(new WindowAdapter() { 
      public void windowClosing(WindowEvent e) 
      { dpPanel.stopPlaying();   // stop any drum beating before exiting
        System.exit(0); 
      } 
    }); 

    setResizable(false);
    pack();
    setLocationRelativeTo(null);  // center the window 
    setVisible(true);
  }  // end of DrumPad()


   // ---------------------------------------------------

   public static void main(String args[])
   {  new DrumPad(); }

} // end of DrumPad

