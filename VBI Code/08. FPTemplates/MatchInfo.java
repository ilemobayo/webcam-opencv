
// MatchInfo.java
// Andrew Davison, Feb 2013, ad@fivedots.coe.psu.ac.th

/* Given a fingerprint name (printName), its corresponding template information
   is loaded. 

   If the fingerprint image is stored in XXX.png (in prints/), then the template
   information will be in XXXTemplate.txt, and the labelled image in XXXLabelled.png

   When score() is called, the Biometric SDK match() method is used to compare the 
   template argument with this object's template, and the score is saved.

   MatchInfo uses score to implement Comparable.compareTo() so an array of MatchInfo
   objects will be sorted into descending order.

   If necessary the labelled fingerprint image can be loaded by this object.

*/

import java.awt.image.*;


public class MatchInfo implements Comparable<MatchInfo>
{
  private String printName;
  private double[] template;
  private int score;     
      // the result when this object's template and another template are matched 

  private BufferedImage labelledPrint = null;


  public MatchInfo(String pName)
  {
    printName = pName;
    template = FingerUtils.loadTemplate(printName);
    if (template == null)
      System.out.println("No template found for " + printName);
  }  // end of MatchInfo()



  public void score(MatchInfo mi)
  {
    double[] tmplt = mi.getTemplate();
    if ((tmplt == null) || (template == null)) {
      System.out.println("Could not match templates for " + mi.getPrintName() + 
                               " and " + printName);
      score = 0;
    }
    else
      score = FingerUtils.match(tmplt, template, 65, false); 
                                            // threshold and whether a quick match is performed
  }  // end of score()


  public String getPrintName()
  {  return printName;  }


  public double[] getTemplate()
  {  return template;  }


  public int getScore()
  {  return score;  }


  public int compareTo(MatchInfo mi) 
  { return mi.getScore() - score;    
    // means that an array of MatchInfo objects will be sorted into descending order
  }


  public String toString()
  {  return printName + "; score = " + score;  }


  public BufferedImage getLabel()
  // load (if necessary) and return the labelled fingerprint image
  {
    if (labelledPrint == null) {
      labelledPrint = FingerUtils.loadLabel(printName);
      if (labelledPrint == null)
        System.out.println("No labelled image found for " + printName);
    }
    return labelledPrint;
  }  // end of getLabel()


}  // end of MatchInfo class