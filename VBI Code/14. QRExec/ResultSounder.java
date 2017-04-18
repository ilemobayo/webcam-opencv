
// ResultSounder.java
// Andrew Davison, February 2011, ad@fivedots.psu.ac.th

/* Load two sound clips, one representing success, one failure, and
   play one when requested.
*/

import java.awt.*;
import java.io.*;
import java.applet.*; 
import java.net.URL;


public class ResultSounder 
{
  private static final String BEEP_FNM = "beep.wav";   // for success
  private static final String BUZZ_FNM = "buzzer.wav";  // for failure

  private AudioClip beep, buzz;


  public ResultSounder()
  {
    beep = loadClip(BEEP_FNM);     // for success
    buzz = loadClip(BUZZ_FNM); // for failure
  } // end of ResultSounder()


  private AudioClip loadClip(String fnm)
  // load sound clip from fnm in the "sounds/" directory
  {
    AudioClip clip = null;
    try {
      clip = Applet.newAudioClip(new URL("file:sounds/" + fnm));
    }
    catch(Exception e)
    {  System.out.println("Could not load buzz sound: " + fnm);  }
    return clip;
  }  // end of loadClip()



  public void playSuccess()
  {  play(beep);  }


  public void playFailure()
  {  play(buzz);  }



  private void play(AudioClip clip)
  // play the clip (or sound a simple beep)
  {
    if (clip == null)
      Toolkit.getDefaultToolkit().beep(); 
    else {
      try {
        clip.play();
      }
      catch(Exception e)
      { // System.out.println(e);
        Toolkit.getDefaultToolkit().beep(); 
      }
    }
  }  // end of play()


} // end of ResultSounder class
