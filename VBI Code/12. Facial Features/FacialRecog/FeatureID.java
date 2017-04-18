
// FeatureID.java
// Andrew Davison, May 2013, ad@fivedots.coe.psu.ac.th

/* enum version of the FaceSDK IDs for face features;
   See p.46-49 of the SDK documentation.
   **NOTE**: I've left off the "FSDKP_" prefix of each ID.

   Includes support for mapping from ID to integer and integer to ID.

   Prints the ID string in a more 'Java'-like style
   e.g. NOSE_TIP ==> noseTip

   There are a series of static methods for returning arrays of IDs
   representing all the points that make up a face part
   e.g. FeatureID.nose() returns an array of all the points in the nose
   The points are ordered so they can be used to draw the outline
   of the face part.

*/

import java.util.*;
import java.util.regex.*;


public enum FeatureID
{
  // **NOTE**: I've left off the "FSDKP_" prefix of each ID

  LEFT_EYE(0),     // left pupil
  RIGHT_EYE(1),    // right pupil

  LEFT_EYE_INNER_CORNER(24),
  LEFT_EYE_OUTER_CORNER(23),
  LEFT_EYE_LOWER_LINE1(38),
  LEFT_EYE_LOWER_LINE2(27),
  LEFT_EYE_LOWER_LINE3(37),
  LEFT_EYE_UPPER_LINE1(35),
  LEFT_EYE_UPPER_LINE2(28),
  LEFT_EYE_UPPER_LINE3(36),
  LEFT_EYE_LEFT_IRIS_CORNER(29),      // left iris (left edge)
  LEFT_EYE_RIGHT_IRIS_CORNER(30),     // left iris (right edge)

  RIGHT_EYE_INNER_CORNER(25),
  RIGHT_EYE_OUTER_CORNER(26),
  RIGHT_EYE_LOWER_LINE1(41),
  RIGHT_EYE_LOWER_LINE2(31),
  RIGHT_EYE_LOWER_LINE3(42),
  RIGHT_EYE_UPPER_LINE1(40),
  RIGHT_EYE_UPPER_LINE2(32),
  RIGHT_EYE_UPPER_LINE3(39),
  RIGHT_EYE_LEFT_IRIS_CORNER(33),     // right iris  (left edge)
  RIGHT_EYE_RIGHT_IRIS_CORNER(34),    // right iris  (right edge)

  LEFT_EYEBROW_INNER_CORNER(13),
  LEFT_EYEBROW_MIDDLE(16),
  LEFT_EYEBROW_MIDDLE_LEFT(18),
  LEFT_EYEBROW_MIDDLE_RIGHT(19),
  LEFT_EYEBROW_OUTER_CORNER(12),

  RIGHT_EYEBROW_INNER_CORNER(14),
  RIGHT_EYEBROW_MIDDLE(17),
  RIGHT_EYEBROW_MIDDLE_LEFT(20),
  RIGHT_EYEBROW_MIDDLE_RIGHT(21),
  RIGHT_EYEBROW_OUTER_CORNER(15),

  NOSE_TIP(2),      // nose tip
  NOSE_BOTTOM(49),
  NOSE_BRIDGE(22),
  NOSE_LEFT_WING(43),
  NOSE_LEFT_WING_OUTER(45),
  NOSE_LEFT_WING_LOWER(47),
  NOSE_RIGHT_WING(44),
  NOSE_RIGHT_WING_OUTER(46),
  NOSE_RIGHT_WING_LOWER(48),

  MOUTH_RIGHT_CORNER(3),        // right mouth corner
  MOUTH_LEFT_CORNER(4),         // left mouth corner
  MOUTH_TOP(54),
  MOUTH_TOP_INNER(61),
  MOUTH_BOTTOM(55),
  MOUTH_BOTTOM_INNER(64),
  MOUTH_LEFT_TOP(56),
  MOUTH_LEFT_TOP_INNER(60),
  MOUTH_RIGHT_TOP(57),
  MOUTH_RIGHT_TOP_INNER(62),
  MOUTH_LEFT_BOTTOM(58),
  MOUTH_LEFT_BOTTOM_INNER(63),
  MOUTH_RIGHT_BOTTOM(59),
  MOUTH_RIGHT_BOTTOM_INNER(65),

  NASOLABIAL_FOLD_LEFT_UPPER(50),
  NASOLABIAL_FOLD_LEFT_LOWER(52),
  NASOLABIAL_FOLD_RIGHT_UPPER(51),
  NASOLABIAL_FOLD_RIGHT_LOWER(53),

  CHIN_BOTTOM(11),
  CHIN_LEFT(9),
  CHIN_RIGHT(10),

  FACE_CONTOUR1(7),
  FACE_CONTOUR2(5),
  FACE_CONTOUR12(6),
  FACE_CONTOUR13(8);


  private int index;
 
  
  private static Map<Integer, FeatureID> map = new HashMap<Integer, FeatureID>();
       // map index --> FeatureID name
  static {
    for (FeatureID fp : FeatureID.values()) {
      map.put(fp.index, fp);
    }
  }



  private FeatureID(int i) 
  {  index = i; }
 

  public int getIndex() 
  {  return index;  }



  public String toString()
  // change to lowercase, remove _'s and capitalize afterwards
  // e.g. MOUTH_TOP ==> mouthTop
  {
    String s = super.toString();
    s = s.toLowerCase();

    Matcher matcher = Pattern.compile("_(.)").matcher(s);   // find "_ch"
    StringBuffer out = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(out, matcher.group(1).toUpperCase());  // add "CH"
    }
    s = matcher.appendTail(out).toString();   // add remaining

    return s;
  }  // end of toString()


  // -------------------- static methods --------------------


  public static FeatureID valueOf(int index) 
  // convert index integer to a FeatureID
  {  return map.get(index);  }


  /* 10 static methods returning an array of IDs for a face area;
     the IDs are in drawing order */

  public static FeatureID[] leftEyeBrow()
  {  return new FeatureID[] { 
        LEFT_EYEBROW_INNER_CORNER,   // 13
        LEFT_EYEBROW_MIDDLE_RIGHT,   // 19
        LEFT_EYEBROW_MIDDLE,         // 16
        LEFT_EYEBROW_MIDDLE_LEFT,    // 18
        LEFT_EYEBROW_OUTER_CORNER,   // 12
                             }; 
  }  // end of leftEyeBrow()


  public static FeatureID[] leftEye()
  {  return new FeatureID[] { 
        LEFT_EYE_INNER_CORNER,     // 24
        LEFT_EYE_LOWER_LINE1,      // 38
        LEFT_EYE_LOWER_LINE2,      // 27
        LEFT_EYE_LOWER_LINE3,      // 37
        LEFT_EYE_OUTER_CORNER,     // 23
        LEFT_EYE_UPPER_LINE1,      // 35
        LEFT_EYE_UPPER_LINE2,      // 28
        LEFT_EYE_UPPER_LINE3,       // 36
                             }; 
  }  // end of leftEye()


  public static FeatureID[] rightEyeBrow()
  {  return new FeatureID[] { 
        RIGHT_EYEBROW_INNER_CORNER,   // 14
        RIGHT_EYEBROW_MIDDLE_LEFT,    // 20
        RIGHT_EYEBROW_MIDDLE,         // 17
        RIGHT_EYEBROW_MIDDLE_RIGHT,   // 21
        RIGHT_EYEBROW_OUTER_CORNER,   // 15
                             }; 
  }  // end of rightEyeBrow()


  public static FeatureID[] rightEye()
  {  return new FeatureID[] { 
        RIGHT_EYE_INNER_CORNER,     // 25
        RIGHT_EYE_LOWER_LINE1,      // 41
        RIGHT_EYE_LOWER_LINE2,      // 31
        RIGHT_EYE_LOWER_LINE3,      // 42
        RIGHT_EYE_OUTER_CORNER,     // 26
        RIGHT_EYE_UPPER_LINE1,      // 40
        RIGHT_EYE_UPPER_LINE2,      // 32
        RIGHT_EYE_UPPER_LINE3,      // 39
                             }; 
  }  // end of rightEye()


  public static FeatureID[] nose()
  {  return new FeatureID[] { 
        NOSE_BRIDGE,           // 22
        NOSE_LEFT_WING,        // 43
        NOSE_LEFT_WING_OUTER,  // 45
        NOSE_LEFT_WING_LOWER,  // 47
        NOSE_BOTTOM,           // 49
        NOSE_RIGHT_WING_LOWER, // 48
        NOSE_RIGHT_WING_OUTER, // 46
        NOSE_RIGHT_WING,       // 44
                             }; 
  }  // end of nose()


  public static FeatureID[] leftCheek()
  {  return new FeatureID[] { 
        NASOLABIAL_FOLD_LEFT_UPPER,   // 50
        NASOLABIAL_FOLD_LEFT_LOWER,   // 52
                             }; 
  }  // end of leftCheek()


  public static FeatureID[] rightCheek()
  {  return new FeatureID[] { 
        NASOLABIAL_FOLD_RIGHT_UPPER,    // 51
        NASOLABIAL_FOLD_RIGHT_LOWER,    // 53
                             }; 
  }  // end of rightCheek()


  public static FeatureID[] topLip()
  {  return new FeatureID[] { 
        MOUTH_RIGHT_CORNER,    // 3
        MOUTH_LEFT_TOP,        // 56
        MOUTH_TOP,             // 54
        MOUTH_RIGHT_TOP,       // 57
        MOUTH_LEFT_CORNER,     // 4
        MOUTH_LEFT_TOP_INNER,  // 60
        MOUTH_TOP_INNER,       // 61
        MOUTH_RIGHT_TOP_INNER, // 62
                             }; 
  }  // end of topLip()


  public static FeatureID[] bottomLip()
  {  return new FeatureID[] { 
        MOUTH_RIGHT_CORNER,        // 3
        MOUTH_LEFT_BOTTOM_INNER,   // 63
        MOUTH_BOTTOM_INNER,        // 64
        MOUTH_RIGHT_BOTTOM_INNER,  // 65
        MOUTH_LEFT_CORNER,         // 4
        MOUTH_RIGHT_BOTTOM,        // 59
        MOUTH_BOTTOM,              // 55
        MOUTH_LEFT_BOTTOM,         // 58
                             }; 
  }  // end of bottomLip()


  public static FeatureID[] chin()
  {  return new FeatureID[] { 
        FACE_CONTOUR2,       // 5
        FACE_CONTOUR1,       // 7
        CHIN_LEFT,           // 9
        CHIN_BOTTOM,         // 11
        CHIN_RIGHT,          // 10
        FACE_CONTOUR13,      // 8     
        FACE_CONTOUR12,      // 6
                             }; 
  }  // end of chin()


  // ------------------------ test --------------------

  public static void main(String[] args) 
  {
    // print all the IDs in ID order
    for(FeatureID fp : FeatureID.values()) {
      System.out.println( fp + " = " + fp.getIndex());
    }

    System.out.println();
    System.out.println();

    // print all the IDs in integer order
    for(int i=0; i < 66; i++) {
      System.out.println( i + " = " + FeatureID.valueOf(i));
    }

    // print all the IDs making up the nose
    System.out.println();
    System.out.println("The nose points");
    FeatureID[] nose = FeatureID.nose();
    for(FeatureID fp : nose) {
      System.out.println( fp + " = " + fp.getIndex());
    }
  }  // end of main()
 
}  // end of FeatureID enum