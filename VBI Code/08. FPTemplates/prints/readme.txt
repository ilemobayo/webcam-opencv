
NUI Chapter 11. Fingerprint Recognition

From the website:

  Killer Game Programming in Java
  http://fivedots.coe.psu.ac.th/~ad/jg

  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention my name, and include a link
to the website.

Thanks,
  Andrew

============================

This directory is where fingerprints information is saved by
the Templater application. The Matcher application looks in
here for template data.


Each fingerprint is represented by a three files. If XXX
is the name of the fingerprint, then the files will be:

  - XXX.png          -- the fingerprint image)

  - XXXTemplate.txt  -- the fingerprint template data)

  - XXXLabelled.png  -- an image combining the thinned fingerprint 
                        and the template data


I could have left this directory empty, but these examples allow you
to test the Matcher application, and get an idea of what kind of
information is created by Templater.


--------------------------------
Last updated: 22nd February 2013
