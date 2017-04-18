
// ListMidiDevices.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, July 2013

/* List all the MIDI devices available to Java sound.
   Common devices include synthesizers, sequencers, MIDI input ports, and 
   MIDI output ports.
*/


import javax.sound.midi.*;


public class ListMidiDevices 
{

  public static void main(String[] args) 
  {
    MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
    System.out.println("No. of Midi devices: " + devices.length);
    for (int i=0; i < devices.length; i++) {
      System.out.println("  " + i + ". " + devices[i] + " (" +
                              devices[i].getDescription() + ")");
    }
  }
}  // end of ListMidiDevices class