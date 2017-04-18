
// TargetMover.java
// Andrew Davison, May 2013, ad@fivedots.coe.psu.ac.th

/* A JFrame containing a panel which displays a crosshairs image
   (the 'target'). Calls to setTarget() move the target
   image inside the panel.
   If the boolean argument of setTraget() is set to true then
   the 'fired' version of the image will be displayed instead.

   The size of the panel (and the window) are supplied as constructor
   arguments.

   This code is derived from the TargetMover class in NUI chapter 8.5
   on eye tracking.
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

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setResizable(false);
    setVisible(true);
  }  // end of TargetMover()


  public void setTarget(double x, double y, boolean isPressed)
  {  movPanel.setTarget(x, y, isPressed); }    
       // code in the panel actually moves the target


  // ---------------------------------------------------

  public static void main(String args[])
  {  new TargetMover(480, 320); }


} // end of TargetMover