
NUI Chapter 5. DrumPad

From the website:

  Killer Game Programming in Java
  http://fivedots.coe.psu.ac.th/~ad/jg

  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat Yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention my name, and include a link
to the website.

Thanks,
  Andrew

============================

This directory contains 7 Java files:
  * DrumPad.java, DPPanel.java,
    DrumsManager.java, Drum.java,
    PercussionPlayer.java,
    ListMidiDevices.java, ListSoundbank.java

There is 1 image file:
  * jazz.png
       // Ronnie Scott's in London

----------------------------
Compilation

> javac *.java

----------------------------
Execution

There are 4 separate programs here:

> java ListMidiDevices
     -- this lists all the MIDI devices found on your machine


> java ListSoundbank
     -- this lists details about the default synthesizer, including
        the instruments it supports

ListMidiDevices and ListSoundbank are not explained in the chapter;
they are included here as a simple way of testing your machine 
for Java Sound support *without* generating sounds


> java PercussionPlayer
     -- this contains a short test of the default MIDI synthesizer for
        generating sounds


> java DrumPad

This example isn't explained in the chapter. It's a mouse-driven version
of the Drumming application without any OpenCV blob recognition. I've
included it as a way of testing MIDI percussion synthesis without the complexity
of computer vision.

A drum beat occurs when you press and drag the mouse. The tempo increases
as the mouse gets closer to the center of a circle. 

Since a user only has one mouse, only one drum can be beating at once

----------------------------
Problem with JMF and Java Sound


I discarded the Java Media Framework (JMF) back in Chapter 2 due 
to its platform problems. Another reason for avoiding it arises here – 
it causes the MIDI synthesizer in Java Sound to crash (at least in JDK 7). 

The raised exception is an IllegalAccessError to the AbstractMidiDevice class 
when MidiSystem.getSequencer() is called.

The solution is to uninstall all traces of JMF. I used the freeware version 
of Revo Uninstaller (http://www.revouninstaller.com/revo_uninstaller_free_download.html) 
which also cleans out the registry on my Windows machine. If the error persists, 
then the only certain solution is to uninstall Java completely, and reinstall 
it from scratch.

----------------------------
Last updated: 6th July 2013
