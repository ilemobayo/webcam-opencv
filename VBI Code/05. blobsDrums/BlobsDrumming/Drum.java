
// Drum.java
// Andrew Davison, June 2011, ad@fivedots.coe.psu.ac.th

/* Play a drum beat at a varying repeat rate, and draw a 
   drum and the hit location.

   The repeat rate is based on how an angle argument to 
   startBeating(), which is the main change from the DrumPad version
   of this class.
*/

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;


public class Drum extends Thread
{
  private static final int HIT_SIZE = 12;
  private static final int BEAT_LENGTH = 200;     // time of a drum beat (in ms)

  private static final int MAX_DELAY = 250;   // for drum beats in ms

  private static final Color TRANS_PALE = new Color(255, 255, 200, 75);
                // translucent whitish color for the drum

  // drum info
  private String drumName;
  private int radius, xCenter, yCenter;
  private int xHit = -1;    // hit location on the drum
  private int yHit = -1; 

  // for drawing the drum name
  private Font msgFont;
  private FontMetrics fm = null;
  private int xNamePos, yNamePos;   


  // drum beating
  private PercussionPlayer player;
  private volatile boolean isPlaying = true;
  private volatile boolean drumIsBeating = false;
  private int repeatDelay = MAX_DELAY;     // time between drum beats (in ms)



  public Drum(String name, int x, int y, int width, int height,
                                                 PercussionPlayer p)
  {
    drumName = name;
    player = p;

    radius = (width < height) ? width/2 : height/2;
    xCenter = x + width/2;
    yCenter = y + height/2;

    msgFont = new Font("SansSerif", Font.BOLD, 18);
  } // end of Drum()




  public void draw(Graphics g)
  // draw drum and hit location (xHit,yHit)
  {
    drawDrum(g);
    
    if (drumIsBeating) { 
      g.setColor(Color.RED);      // draw hit location as a red circle
      g.fillOval(xHit-HIT_SIZE/2, yHit-HIT_SIZE/2, HIT_SIZE, HIT_SIZE);
    }
  }  // end of draw()


  private void drawDrum(Graphics g)
  // draw drum as a circle containing its name at its center
  {
    if (fm == null) {   // initialize drum name position coords using font
      fm = g.getFontMetrics(msgFont);
      xNamePos = xCenter - fm.stringWidth(drumName)/2;
      yNamePos = yCenter + fm.getAscent() - (fm.getAscent() + fm.getDescent())/2;
    }

    g.setColor(TRANS_PALE);     // draw a translucent circle
    g.fillOval(xCenter-radius, yCenter-radius, radius*2, radius*2);

    // draw name of drum in the center of the circle
    g.setColor(Color.YELLOW.brighter());   
    g.setFont(msgFont);
    g.drawString(drumName, xNamePos, yNamePos);
  }  // end of drawDrum()




  public void run()
  // keep repeating a drum beat
  {
    while (isPlaying) {
      if (drumIsBeating) {
        player.drumOn(drumName);
        wait(BEAT_LENGTH);
        player.drumOff(drumName);

        wait(repeatDelay);   // time between drum beats (this value can vary)
      }
    }
  }  // end of run()


  private void wait(int delay)
  {
    try {
      Thread.sleep(delay);
    }
    catch (InterruptedException e) {}
  }  // end of wait()


  public void stopPlaying()
  {  isPlaying = false;  }



  public boolean startBeating(int x, int y, int angle)
  /* start beating the drum, with a repeat rate based on the angle argument.
     This is a change from the DrumPad Drum class.
  */
  { 
    double ratio = radiusRatio(x, y);
    // System.out.println( " " + drumName + " dist ratio: " + ratio); 
    if (ratio > 1.0)   // outside drum circle
      return false;

    repeatDelay = angle2Delay(angle);    // adjust repeat rate
    // System.out.println( " " + drumName);  //  + " -> (" + x + "," + y + ")" ); 
    xHit = x; yHit = y;    // set hit coord
    drumIsBeating = true;
    return true;
  }  // end of startBeating()



  private double radiusRatio(int x, int y)
  // find dist of (x,y) from center / radius (so in range 0 - 1 or bigger)
  {
    int xDist = x - xCenter;
    int yDist = y - yCenter;
    return Math.sqrt(xDist*xDist + yDist*yDist)/radius; 
  }  // end of radiusRatio()



  private int angle2Delay(int angle)
  /* convert angle (into degrees) into a repeat delay so
     that the delay is less when the angle is absolutely larger
  */
  {
    int ang = Math.abs(angle);
    int rate = MAX_DELAY;    // default is slowest rate
    if (ang < 23)
      rate = MAX_DELAY;
    else if (ang < 67)
      rate = 125;
    else if (ang <= 90)
      rate = 50;
    // System.out.println("angle2Delay: " + angle + " ==> " + rate);
    return rate;
  }  // end of angle2Delay()



  public boolean contains(int x, int y)
  // is (x,y) inside the drum circle?
  {
    int xDist = x - xCenter;
    int yDist = y - yCenter;
    return ((xDist*xDist + yDist*yDist) <= (radius*radius)); 
  }  // end of contains()



  public void stopBeating() 
  // stop beating the drum
  {
    xHit = -1; yHit = -1;   // set hit coord to be outside drum circle
    drumIsBeating = false;
  }  // end of stopBeating()



} // end of Drum
