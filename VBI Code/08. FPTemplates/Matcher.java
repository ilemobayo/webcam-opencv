
// Matcher.java
// Andrew Davison, February 2013, ad@fivedots.coe.psu.ac.th

/* Matcher is called after Templater has calculated template information
   and images for multiple fingerprints.

   If the fingerprint image is stored in XXX.png (in prints/), then the template
   information will be in XXXTemplate.txt, and the labelled image in XXXLabelled.png.

   Matcher is supplied with a print name, and matches its template against all the
   other analyzed fingerprints, and displays the best match.

   The GUI shows the supplied fingerprint's labelled image,
   all the match scores for the other prints, and the labelled image of the highest 
   matching other fingerprint.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.image.*;
import java.util.*;



public class Matcher extends JFrame 
{
  // size of the ImagePanels
  private static final int PANEL_WIDTH = 450;
  private static final int PANEL_HEIGHT = 600;



  public Matcher(String pName)
  {
    super("Fingerprint Matcher");

    // check if the supplied printname has associated template and labelled fingerprint info
    if (!FingerUtils.hasTemplate(pName)) {
      System.out.println("No template information found for " + pName);
      System.exit(0);
    }

    if (!FingerUtils.hasLabel(pName)) {
      System.out.println("No label information found for " + pName);
      System.exit(0);
    }

    // get the names of all the other fingerprints that can be matched against
    ArrayList<String> prints = collectPrints(pName);
    if (prints.size() == 0) {
      System.out.println("No other prints found");
      System.exit(0);
    }

    // build match info for the supplied print name
    MatchInfo testFinger = new MatchInfo(pName);

    // build match information for all the other prints, calculating their match scores
    MatchInfo[] matches = new MatchInfo[prints.size()];
    for (int i=0; i < prints.size(); i++) {
      matches[i] = new MatchInfo( prints.get(i));
      matches[i].score(testFinger);
    }

    Arrays.sort(matches);    // sorted into descending order by match scores

    makeGUI(testFinger, matches);   // display the results in a GUI

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();  
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of Matcher()




  private ArrayList<String> collectPrints(String pName) 
  /* return all filenames prefixes (prints) that end with LABEL_EXT
     and have a corresponding TEMPLATE_EXT file; excluding pName
  */
  {
    ArrayList<String> prints = new ArrayList<String>();
    String fnm, printName;

    File[] listOfFiles = new File(FingerUtils.PRINT_DIR).listFiles(); 
    for (int i = 0; i < listOfFiles.length; i++)  {
      if (listOfFiles[i].isFile()) {
        fnm = listOfFiles[i].getName();
        int labelPos = fnm.lastIndexOf(FingerUtils.LABEL_EXT);
        if (labelPos != -1) {
          printName = fnm.substring(0, labelPos);
          if (!printName.equals(pName)) {
            if (FingerUtils.hasTemplate(printName)) {
              System.out.println("Found print " + printName);
              prints.add(printName);
            }
            else
              System.out.println("File " + printName + " has labelled image but no template info");
          }
        }
      }
    }
    return prints;
  }  // end of collectPrints()



  private void makeGUI(MatchInfo testFinger, MatchInfo[] matches)
  /* The GUI consists of three parts:

     1. Display the supplied fingerprint's labelled image in an ImagePanel.

     2. Display all the match scores for the other prints in a text area
     that is scrollable. The info is sorted into descending order by score.

     3. Display the labelled image of the highest matching other fingerptint.
  */
  {
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    Border raisedBevel = BorderFactory.createRaisedBevelBorder();
    Border loweredBevel = BorderFactory.createLoweredBevelBorder();
    Border compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);

    // 1. display the labelled input print image
    ImagePanel inputPanel = new ImagePanel(PANEL_WIDTH, PANEL_HEIGHT);
    inputPanel.setImage( testFinger.getLabel());
    JScrollPane inputScroll = new JScrollPane(inputPanel);
    inputScroll.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

    TitledBorder title1 = BorderFactory.createTitledBorder(compound, 
                                        "Name: " + testFinger.getPrintName(),
                                        TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
    inputScroll.setBorder(title1);
    c.add(inputScroll, BorderLayout.WEST);


    // 2. list all the fingerprints matched against, in descending score order
    StringBuilder sb = new StringBuilder();
    for (MatchInfo mi : matches)
      sb.append("  " + mi + "\n");
    JTextArea scoresJTA = new JTextArea(sb.toString());  // , 15,40);
    scoresJTA.setEditable(false);
    JScrollPane scoresScroll = new JScrollPane(scoresJTA);
    scoresScroll.setPreferredSize(new Dimension(150, PANEL_HEIGHT));

    TitledBorder title2 = BorderFactory.createTitledBorder(compound, "Sorted Matches",
                                        TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
    scoresScroll.setBorder(title2);
    c.add(scoresScroll, BorderLayout.CENTER);


    // 3. display the labelled fingerprint of the best match
    MatchInfo bestMatch = matches[0];
    ImagePanel matchPanel = new ImagePanel(PANEL_WIDTH, PANEL_HEIGHT); 
    matchPanel.setImage( bestMatch.getLabel());
    JScrollPane matchScroll = new JScrollPane(matchPanel);
    matchScroll.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    TitledBorder title3 = BorderFactory.createTitledBorder(compound, 
                                        "Best Match: " + bestMatch,
                                        TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
    matchScroll.setBorder(title3);
    c.add(matchScroll, BorderLayout.EAST);
  }  // end of makeGUI()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  
    if (args.length != 1)
      System.out.println("Usage: Matcher <printName>");
    else
      new Matcher(args[0]);  
  }  // end of main()

} // end of Matcher class
