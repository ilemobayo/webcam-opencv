
// DrumsManager.java
// Andrew Davison, June 2011, ad@fivedots.coe.psu.ac.th

/* A grid of drums is drawn, each of which plays a different
   kind of beat. The percussion sounds are generated
   by the PercussionPlayer class.
*/

import java.awt.*;


public class DrumsManager
{
  private final static int NUM_ROWS = 3;
  private final static int NUM_COLS = 3;

  private PercussionPlayer player;
  private Drum[] drums;

  private int numSticks;
  private Drum[] sticks;    // points to current drum for each stick
     // replaces currDrum global of the DrumPad version of this class



  public DrumsManager(int width, int height, int num)
  // width and height are the dimensions of the total drawing area
  {
    int colWidth = width/NUM_COLS;    // size of the drawing area for one drum
    int rowWidth = height/NUM_ROWS;
    numSticks = num;

    player = new PercussionPlayer();

    // initialize each drum 
    drums = new Drum[NUM_ROWS*NUM_COLS];
    int xCoord = 0;      // (xCoord, yCoord) is the top-left of each drum drawing area
    int yCoord = 0; 
    int i = 0;
    for (int row=0; row < NUM_ROWS; row++) {
      xCoord = 0;
      for (int cols=0; cols < NUM_COLS; cols++) {
        drums[i] = new Drum( PercussionPlayer.getInstrumentName(i), 
                              xCoord, yCoord,
                              colWidth, rowWidth, player);
        drums[i].start();
        xCoord += colWidth;
        i++;
      }
      yCoord += rowWidth;
    }

    // initialize drum sticks array
    sticks = new Drum[numSticks];
    for (int j=0; j < numSticks; j++)
      sticks[j] = null;   // no drums assigned as yet
  } // end of DrumsManager()



  public void draw(Graphics g)
  { for(Drum drum: drums)
      drum.draw(g);
  }



  public void stopPlaying()
  // stop the drums and the PercussionPlayer
  {  
    for(Drum drum: drums)
      drum.stopPlaying();
    player.close();
  }


  public void startBeating(int sIdx, int x, int y)
  // start beating a drum at (x,y) with the specified stick;
  {
   if ((sIdx < 0) || (sIdx >= numSticks)) {
     System.out.println("No stick with that index (" + sIdx+ ")");
     return;
    }

    if ((sticks[sIdx] != null) && 
        (sticks[sIdx].contains(x, y)))  // still inside old drum area?
      sticks[sIdx].startBeating(x, y);
    else {
      if (sticks[sIdx] != null) {  // has just left old drum area?
        sticks[sIdx].stopBeating();
        sticks[sIdx] = null;
      }
      for(Drum drum: drums) {     // inside a new drum area?
        if (drum.startBeating(x, y)) {
          sticks[sIdx] = drum;
          break;
        }
      }
    }
  }  // end of startBeating()



  public void stopBeating(int sIdx) 
  // stop beating the drum assigned to the specified stick
  {
   if ((sIdx < 0) || (sIdx >= numSticks)) {
     System.out.println("No stick with that index (" + sIdx+ ")");
     return;
    }

    if (sticks[sIdx] != null) {
      sticks[sIdx].stopBeating();
      sticks[sIdx] = null;
    }
  }  // end of stopBeating()


} // end of DrumsManager
