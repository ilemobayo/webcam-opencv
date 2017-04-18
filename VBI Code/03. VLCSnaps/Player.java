
// Player.java
// Andrew Davison, May 2012, ad@fivedots.coe.psu.ac.th

/* Play a video file.
   A progress bar shows the progress of the video, and can be
   used to jump about inside the video (until the video reaches
   its end).

   Uses VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/)

   Based on code in
      http://www.capricasoftware.co.uk/vlcj/tutorial2.php
   and examples at 
      http://code.google.com/p/vlcj/wiki/SimpleExamples
   including MinimalTestPlayer.java ("Minimal") and
   the "Basic" player
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.*;


public class Player extends JFrame
{
  private EmbeddedMediaPlayerComponent mPlayerComp;
  private MediaPlayer mPlayer;
  private JProgressBar timeBar;

  private Player(String fnm)
  {
    super("VLC Player");

    // creation of video surface
    mPlayerComp = new EmbeddedMediaPlayerComponent();
    Canvas canvas = mPlayerComp.getVideoSurface();
    canvas.setSize(640, 480);
    mPlayer = mPlayerComp.getMediaPlayer();

	Container c = getContentPane();
	c.add(mPlayerComp, BorderLayout.CENTER);

    timeBar = new JProgressBar(0, 100);
    timeBar.setStringPainted(true);
    c.add(timeBar, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) 
       { mPlayerComp.release();
         System.exit(0);
       }
    });

    pack();
    setLocationRelativeTo(null);  // center the window
    setVisible(true);

    // update the progress bar as the video progresses
    mPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      public void positionChanged(MediaPlayer mediaPlayer, float pos) 
      { int value = Math.min(100, Math.round(pos * 100.0f));
        timeBar.setValue(value);
      }
    });

    // adjust the video position when the the slider is pressed
    timeBar.addMouseListener( new MouseAdapter() {
      public void mousePressed(MouseEvent e)
      {  float pos = ((float)e.getX())/timeBar.getWidth();
         mPlayer.setPosition(pos);
      }
    });

    System.out.println("Playing " + fnm + "...");
    mPlayer.playMedia(fnm);
  }  // end of Player()




  // -----------------------------------------------

  public static void main(String[] args)
  {  
    if (args.length != 1)
       System.out.println("Usage: run Player <media name>");
    else
      new Player(args[0]);  
  }  // end of main()


}  // end of Player class