
// PercussionPlayer.java
// Andrew Davison, une 2011, ad@fivedots.coe.psu.ac.th

/*  A class that creates a MIDI percussion channel, and then can be called to
    play drum beats, which are identified by the instrument name

    This object is called by several threads, and so the beat playing is
    protected by synchronized methods.

    Switching on a drum beat and switching it off are separate methods
    so the on/off calls can be interleaved by several threads calling
    the player.
*/

import javax.sound.midi.*;


public class PercussionPlayer
{

  private static final int PERCUSSION_CHANNEL = 9;
     // Channel 10 is the Midi percussion channel, but we
     // number channels from 0 and use channel 9 instead.

  private static final int VELOCITY = 127;         // full volume
  // private static final int BEAT_LENGTH = 200;     // time of a drum beat (in ms)


  private static String[] instrumentNames = {
    "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "Whistle", 
    "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"
  }; 

  private int[] instrumentKeys = {
    46, 38, 49, 39, 72, 
    58, 47, 67, 63
  };   // these keys must correspond to the names in instrumentNames[]

  /* unused names and keys:
      "Bass Drum", "Closed Hi-Hat", "High Tom", "Hi Bongo",
      "Maracas", "Low Conga", "Cowbell", 
      35, 42, 50, 60, 
      70, 64, 56, 

  More info on names and keys:
     http://www.computermusicresource.com/GM.Percussion.KeyMap.html
*/

  private Synthesizer synthesizer = null;
  private MidiChannel channel = null;     // the channel the drums play on


  public PercussionPlayer()
  {
    try {
      synthesizer = MidiSystem.getSynthesizer();
      synthesizer.open();
      channel = synthesizer.getChannels()[PERCUSSION_CHANNEL];

      // wake up the channel; needed with the Gervill software synthesizer
      drumOn("Crash Cymbal");
      wait(100);    
      drumOff("Crash Cymbal");
    }
    catch(MidiUnavailableException e) {
      System.out.println("Cannot initialize MIDI synthesizer");
      System.exit(1);
    }
  }  // end of PercussionPlayer()



  public static String getInstrumentName(int i)
  // used by DrumManager
  {
    if (i < 0) {
      i = Math.abs(i)%instrumentNames.length;
      System.out.println("Name index cannot be negative; using " + i);

    }
    else if (i >= instrumentNames.length) {
      i = i%instrumentNames.length;
      System.out.println("Name index too large; using " + i);
    }

    return instrumentNames[i];
  }  // end of getInstrumentName()


  // ------------ called by drum playing threads --------------------

  synchronized public void drumOn(String name)
  { 
    int key = name2Key(name);
    if ((channel != null) && (key != -1))
      channel.noteOn(key, VELOCITY); 
  }  // end of drumOn()


  synchronized public void drumOff(String name)
  { 
    int key = name2Key(name);
    if ((channel != null) && (key != -1))
      channel.noteOff(key);  
  }  // end of drumOff()


  private int name2Key(String name)
  // convert an instrument name to its MIDI percussion key
  {
    for (int i=0; i < instrumentNames.length; i++)
      if (instrumentNames[i].equals(name))
        return instrumentKeys[i];
    return -1;
  }  // end of name2Key()


  synchronized public void close()
  {  
    if (channel != null) {
       channel.allNotesOff();
       channel = null;
    }
    if (synthesizer != null) {
      synthesizer.close();
      synthesizer = null;
    }
  }  // end of close()



  // ------------------- test rig ------------------------------

  public static void main(String[] args) 
  {
    PercussionPlayer player = new PercussionPlayer();

    player.drumOn("Whistle");
    player.drumOn("Crash Cymbal");
    wait(5000);    
          // does NOT play for 5 secs (but there is an 'echo' in the whistle)
    player.drumOff("Whistle");
    player.drumOff("Crash Cymbal");

    wait(500);

    player.drumOn("Low-mid Tom");
    wait(200);    
    player.drumOff("Low-mid Tom");

    wait(500);
    player.close();
  }  // end of main()


  private static void wait(int delay)
  {
    try {
      Thread.sleep(delay);
    }
    catch (InterruptedException e) {}
  }  // end of wait()

}  // end of PercussionPlayer class