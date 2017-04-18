
// CaptureTest.java
// Andrew Davison, June 2013, ad@fivedots.coe.psu.ac.th

/* Play video coming from a capture device.

   Uses VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/)

   Based on examples at 
      http://code.google.com/p/vlcj/wiki/SimpleExamples
   including CaptureTest.java ("Capture").

*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;


public class CaptureTest extends JFrame
{
  private static final String CAP_DEVICE = "dshow://";        // for Windows
                                   // "v4l2:///dev/video0"    // for Linux
                                   // "qtcapture://"          // for MAC OS X

  private static final String CAMERA_NAME = "USB2.0 Camera";
                        //  "Video Blaster WebCam 3 (WDM)";     // win 7 webcam
                        //  "USB Video Device";                 // xp laptop

  private static final String[] VLC_ARGS = {
    "--no-audio",                   // no audio decoding
    "--no-video-title-show",        // do not display title
    "--live-caching=50",            // reduce capture lag/latency
    "--quiet",                      // turn off VLC warnings and info messages
  };


  private EmbeddedMediaPlayer mPlayer;


  public CaptureTest()
  {
    super("VLC Capture Test");
	Container c = getContentPane();
	c.add( playerPanel() );

    addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) 
       { mPlayer.release();
         System.exit(0);
       }
    });

    pack();
    setResizable(false);
    setLocationRelativeTo(null);  // center the window
    setVisible(true);

    startPlayer();
   }  // end of CaptureTest()


  private JPanel playerPanel()
  // create player and video surface inside a JPanel
  {
    MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
    mPlayer = factory.newEmbeddedMediaPlayer();  // create media player

    Canvas canvas = new Canvas();
    canvas.setSize(640, 480);
    CanvasVideoSurface vidSurface = factory.newVideoSurface(canvas);   // create video surface

    mPlayer.setVideoSurface(vidSurface);  // connect player and surface

    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.add(canvas, BorderLayout.CENTER);   // add surface to a panel
    return p;
  }  // end of playerPanel()



  private void startPlayer()
  {
    String[] options = {
      ":dshow-vdev=" + CAMERA_NAME,
      ":dshow-size=640x480",
      ":dshow-adev=none",    // no audio device required
    };
    mPlayer.playMedia(CAP_DEVICE, options);
  }  // end of startPlayer()



  // -----------------------------------------------

   public static void main(String args[])
   {  new CaptureTest(); }

}  // end of CaptureTest class
