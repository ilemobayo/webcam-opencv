
// CaptureSnapPanel.java
// Andrew Davison, July 2013, ad@fivedots.coe.psu.ac.th

/* Read video from a capture device, but display it as
   a sequence of snapshots.

   Uses VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/)

   Based on my CaptureTest.java and examples at 
      http://code.google.com/p/vlcj/wiki/SimpleExamples
   including CaptureTest.java ("Capture"),
   SnapshotTest.java ("SnapShot"), and
   ThumbsTest.java ("Thumbs").
*/


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;

import uk.co.caprica.vlcj.player.*;


public class CaptureSnapPanel extends JPanel implements ActionListener
{
  // dimensions of the panel
  private static final int P_WIDTH = 640;
  private static final int P_HEIGHT = 480;

  private static final int SNAP_INTERVAL = 100;   // ms
      // rate at which snaps are taken and displayed in the panel


  private static final String CAP_DEVICE = "dshow://";     // for Windows
                                   // "v4l2:///dev/video0"    // for Linux
                                   // "qtcapture://"          // for MAC OS X

  private static final String CAMERA_NAME =  "USB2.0 Camera";
                        //"Video Blaster WebCam 3 (WDM)";     // win 7 webcam
                        //  "USB Video Device";               // xp laptop


  private static final String[] VLC_ARGS = {
    "--intf", "dummy",              // no interface
    "--vout", "dummy",              // no video output
    "--no-audio",                   // no audio decoding
    "--no-video-title-show",        // do not display title
    "--no-stats",                   // no stats
    "--no-sub-autodetect-file",     // no subtitles
    "--no-snapshot-preview",        // no snapshot previews
    "--live-caching=50",            // reduce capture lag/latency
    "--quiet",                      // turn off VLC warnings and info messages
  };


  private MediaPlayer mPlayer;
  private Timer timer;     // for updating the current image
  private BufferedImage snapIm;


  public CaptureSnapPanel()
  {
    MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
    mPlayer = factory.newHeadlessMediaPlayer();
       // player is not intended to display video (but VLC might still still spawn a display)
       // http://www.capricasoftware.co.uk/wiki/index.php?title=Vlcj_Media_Players

    String[] options = {
      ":dshow-vdev=" + CAMERA_NAME,
      ":dshow-size=" + P_WIDTH+"x"+P_HEIGHT,
      ":dshow-adev=none",    // no audio device required
    };
    mPlayer.startMedia(CAP_DEVICE, options);

    // use to update the current image every SNAP_INTERVAL ms
	timer = new Timer(SNAP_INTERVAL, this);
    timer.start();
  } // end of CaptureSnapPanel() constructor



  public Dimension getPreferredSize()
  {   return new Dimension(P_WIDTH, P_HEIGHT); }



  public void paintComponent(Graphics g)
  // put the current snapshot in the middle of the panel
  {
    super.paintComponent(g);   // repaint standard stuff first
    if (snapIm != null)
      g.drawImage(snapIm, (P_WIDTH-snapIm.getWidth())/2, 
                          (P_HEIGHT-snapIm.getHeight())/2, null);
  }



  public void actionPerformed(ActionEvent e)
  // called by the timer to take a snapshot
  {
    snapIm = mPlayer.getSnapshot(P_WIDTH, 0);   // panel width x ?? with aspect ratio maintained
    if (snapIm == null)
      return;

    if (snapIm.getWidth() > 0)
      repaint();
  }  // end of actionPerformed()


  public void close()
  {
    timer.stop();
    mPlayer.release();
  }

} // end of CaptureSnapPanel class
