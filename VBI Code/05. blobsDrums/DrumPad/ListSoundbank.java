
// ListSoundbank.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, July 2013

/* Examine the soundbank for the default synthesizer.
   A soundbank is a collection of instruments for synthesizing
   different sounds.
*/

import java.io.*;
import java.util.*;
import javax.sound.midi.*;


public class ListSoundbank
{

  public static void main(String[] args) throws Exception
  {
    Synthesizer synth = MidiSystem.getSynthesizer();
    System.out.println("Synthesizer: " + synth.getDeviceInfo());
    synth.open();
    Soundbank sb = synth.getDefaultSoundbank();
    synth.close();

    if (sb == null) {
      System.out.println("No soundbank found");
      System.exit(1);
    }

    // display information about the sb
    System.out.println("Soundbank: " + sb.getName());
    System.out.println("  Description: " + sb.getDescription());
    System.out.println("  Vendor: " + sb.getVendor());
    System.out.println("  Version: " + sb.getVersion());

    Instrument[] instrs = sb.getInstruments();
    System.out.println("Instruments (" + instrs.length + "):");   
    for (int i = 0; i < instrs.length; i++) {
      System.out.print("  " + i + ".\"" + instrs[i].getName().trim() + "\"");
      if ((i+1)%4 == 0)
       System.out.println();
    }
    System.out.println();
  }  // end of main()

}  // end of ListSoundbank class

 

