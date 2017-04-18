
// SliderBox.java
// Andrew Davison, May 2013, ad@fivedots.coe.psu.ac.th

/* A JSlider with a linked Textfield. The slider value can
   be changed via the JSlider or the textfield.
   Any change results in a call to valChange(), implemented using
   the Sliderwatcher interface.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;


public class SliderBox extends JPanel
{
  private static final int TITLE_WIDTH = 20;

  private String title;
  private int min, max;
  private SliderBoxWatcher ws;

  private JSlider slider;
  private JTextField valTF;



  public SliderBox(String name, int minVal, int maxVal, 
                                   SliderBoxWatcher watcher)
  {  this(name, minVal, maxVal, (minVal+maxVal)/2, watcher);  }



  public SliderBox(String name, int minVal, int maxVal, 
                                   int startVal, SliderBoxWatcher watcher)
  { 
    title = name;
    ws = watcher;

    if (minVal > maxVal) {
      System.out.println("Swapping min and max values");
      min = maxVal;
      max = minVal;
    }
    else {
      min = minVal;  
      max = maxVal;
    }

    if ((startVal < min) || (startVal > max)) {
      System.out.println("Starting value out of range; using mid range as a value");
      startVal = (min + max)/2;
    }

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    // ---- label --------------
    String labelText = String.format("<html><div WIDTH=%d>%s</div><html>", 
                                                            TITLE_WIDTH, title);
    this.add( new JLabel(labelText));

    // ----------- slider -------------
    slider = new JSlider(min, max, startVal);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) 
      {
         int val = slider.getValue();
         valTF.setText("" + val);
         ws.valChange(title, val);   // callback
      }
    });

    Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
    table.put(min, new JLabel("" + min));
    table.put(max, new JLabel("" + max));
    slider.setLabelTable(table);
    slider.setPaintLabels(true);

    slider.setMajorTickSpacing((max-min)/10);
    slider.setPaintTicks(true);
    
    slider.setPreferredSize(new Dimension(300, 50));
    this.add(slider);
    
    // -------------- text field --------------
    valTF = new JTextField(5);
    valTF.setText("" + startVal);
    valTF.addActionListener( new ActionListener() {
       public void actionPerformed(ActionEvent ae)
       {
         try {
           int val = Integer.parseInt(valTF.getText());
           if (val < min) {
             val = min;
             valTF.setText("" + val);
           }
           else if (val > max) {
             val = max;
             valTF.setText("" + val);
           }
           slider.setValue(val);
         } 
         catch(NumberFormatException e) 
         {  valTF.setText(""); }
       }
    });
    
    JPanel p = new JPanel();
    p.add(valTF);
    this.add(p);
  }  // end of SliderBox()


}  // end of SliderBox class
