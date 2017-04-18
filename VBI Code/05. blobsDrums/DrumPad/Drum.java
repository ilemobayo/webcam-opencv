
// Drum.java
// Andrew Davison, June 2011, ad@fivedots.coe.psu.ac.th

/* Play a drum beat at a varying repeat rate, and draw a 
   drum and the hit location.

   The repeat rate is based on how close the hit location
   is to the center of the drum.
*/

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;


public class Drum extends Thread
{
  private static final int HIT_SIZE = 12;
  private static final int BEAT_LENGTH = 200;     // time of a drum beat (in ms)

  // drum repeat constants
  private static final int MIN_DELAY = 50;   // in ms
  private static final int NUM_REGIONS = 5; 

  private static final Color TRANS_PALE = new Color(255, 255, 200, 75);
                          // translucent drum color
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
  private int repeatDelay = MIN_DELAY * NUM_REGIONS;   // time between drum beats (in ms)



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
      g.setColor(Color.RED);     // draw hit location as a red circle
      g.fillOval(xHit-HIT_SIZE/2, yHit-HIT_SIZE/2, HIT_SIZE, HIT_SIZE);
    }
  }  // end of draw()



  private void drawDrum(Graphics g)
  // draw drum as a circle containing its name at its center
  {
    if (fm == null) {    // initialize drum name position coords using font
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


  public boolean startBeating(int x, int y)
  /* start beating the drum, with a repeat rate based on the distance
     of the (x,y) from the center; closer to the center means a faster beat
  */
  { 
    double ratio = radiusRatio(x, y);
    if (ratio > 1.0)   // outside drum circle
      return false;

    repeatDelay = ratio2Delay(ratio);    // adjust repeat delay
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


  private int ratio2Delay(double ratio)
  /* convert ratio (0-1) into repeat delay divided into
     NUM_REGIONS regions in MIN_DELAY steps */
  {
    double regionSize = 1.0 / NUM_REGIONS;
    int region = (int) Math.ceil(ratio/regionSize);
    int delay = MIN_DELAY * region;
    return delay;
  }  // end of ratio2Delay()



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
