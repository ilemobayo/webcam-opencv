
// TargetMover.java
// Andrew Davison, March 2013, ad@fivedots.coe.psu.ac.th

/* A JFrame containing a panel which displays a crosshairs image
   (the 'target'). Calls to setTarget() move the target
   image inside the panel.

   The size of the panel (and the window) are supplied as constructor
   arguments.
*/


import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;



public class TargetMover extends JFrame 
{ 
  private MoverPanel movPanel;


  public TargetMover(int winWidth, int winHeight)
  {
    super("Target Mover");

	Container c = getContentPane();
    movPanel = new MoverPanel(winWidth, winHeight);
	c.add(movPanel);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       // use the other JFrame to close things down
    pack();
    setResizable(false);
    setVisible(true);
  }  // end of TargetMover()



  public void setTarget(double x, double y)
  {  movPanel.setTarget(x, y); }    // code in the panel actually moves the target


  // ---------------------------------------------------

  public static void main(String args[])
  {  
    JDialog.setDefaultLookAndFeelDecorated(true);

     new TargetMover(480, 320); 
}


} // end of TargetMover