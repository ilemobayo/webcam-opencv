
// VLCCapture.java
// Andrew Davison, July 2013, ad@fivedots.coe.psu.ac.th

/* Web snapping  using VLC (http://www.videolan.org/developers/) and 
   vlcj (https://github.com/caprica/vlcj; http://code.google.com/p/vlcj/) instead.

   The interface is the similar to JavaCV's FrameGrabber, so can be used with
   minimal change in applications requiring snaps from a capture device.

   One simplification is that the use of "dshow" is hardwired into this code.
*/


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;

import uk.co.caprica.vlcj.player.*;


public class VLCCapture
{
  private static final String CAP_DEVICE = "dshow://";     // for Windows
                                   // "v4l2:///dev/video0"    // for Linux
                                   // "qtcapture://"          // for MAC OS X


  private static final String[] VLC_ARGS = {
    "--intf", "dummy",              // no interface
    "--vout", "dummy",              // no video output
    "--no-audio",                   // no audio decoding
    "--no-video-title-show",        // do not display title
    "--no-stats",                   // no stats
    "--no-sub-autodetect-file",     // no subtitles
    "--no-snapshot-preview",        // no snapshot previews
    "--live-caching=50",            // reduce capture lag/latency
    "--quiet",                      // turn off warnings and info messages
  };


  private MediaPlayerFactory factory = null;
  private MediaPlayer mPlayer = null;

  private String cameraName;
  private int imWidth;


  public VLCCapture(String camName, int width, int height)
  {
    factory = new MediaPlayerFactory(VLC_ARGS);
    mPlayer = factory.newHeadlessMediaPlayer();
    cameraName = camName;
    imWidth = width;

    System.out.println("Initializing grabber for \"" + cameraName + "\" ...");
    String[] options = {
      ":dshow-vdev=" + cameraName,
      ":dshow-size=" + width+"x"+height,
      ":dshow-adev=none",    // no audio device required
    };
    mPlayer.startMedia(CAP_DEVICE, options);
  } // end of VLCCapture() constructor



  public BufferedImage grab()
  {
    BufferedImage im = mPlayer.getSnapshot(imWidth, 0);   
                 // get image width x ?? with aspect ratio maintained

    if ((im == null) || (im.getWidth() == 0)) {
      System.out.println("No snap available");
      return null;
    }
    return im;
  }  // end of grab()



  public void close()
  {  
    System.out.println("Closing grabber for \"" + cameraName + "\" ...");
    if(mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
    
    if(factory != null) {
      factory.release();
      factory = null;
    }
  }  // end of close()


} // end of VLCCapture class
