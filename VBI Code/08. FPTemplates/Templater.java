
// Templater.java
// Andrew Davison, February 2013, ad@fivedots.coe.psu.ac.th

/* Templater displays a webcam image, and allows the user to find a fingerprint by
   pressing "Find Print", and then to analyze it by pressing "Analyze Print".

   The fingerprint image, its template data, and a labelled fingerprint image are
   saved in the prints/ subdirectory, and the two images are displayed in separate
   panels in this application.

   If the fingerprint image is stored in XXX.png (in prints/), then the template
   information will be in XXXTemplate.txt, and the labelled image in XXXLabelled.png
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;


public class Templater extends JFrame 
                       implements ActionListener
{
  // initial sizes of the ImagePanels
  private static final int PANEL_WIDTH = 300;
  private static final int PANEL_HEIGHT = 260;


  // GUI components
  private ScanPanel scanPanel;    // to display the webcam image
  private ImagePanel fpPanel;     // to display the fingerprint extracted from the webcam image
  private ImagePanel skelPanel;   // to display the skeletinized fingerprint

  private JButton findBut, tmpltBut;



  public Templater()
  {
    super("Fingerprint Analyzer");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);


    // displays the labelled fingerprint image after it has been analyzed
    skelPanel = new ImagePanel(PANEL_WIDTH, PANEL_HEIGHT);   
    JScrollPane scrollPane1 = new JScrollPane(skelPanel);
    scrollPane1.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    scrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    c.add(scrollPane1, BorderLayout.EAST);

     // displays the extracted fingerprint image
    fpPanel = new ImagePanel(PANEL_WIDTH, PANEL_HEIGHT);   
    JScrollPane scrollPane2 = new JScrollPane(fpPanel);
    scrollPane2.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    c.add(scrollPane2, BorderLayout.CENTER);

    // displays the current webcam pictures, and finds/analyzes fingerprints
    scanPanel = new ScanPanel(fpPanel, skelPanel); 
    c.add( scanPanel, BorderLayout.WEST);

    JPanel p = new JPanel();   // buttons panel

    findBut = new JButton("Find Print");
    findBut.addActionListener(this);
    p.add(findBut);

    tmpltBut = new JButton("Analyze Print");
    tmpltBut.addActionListener(this);
    p.add(tmpltBut);

    c.add(p, BorderLayout.SOUTH);


    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { scanPanel.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    pack();  
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of Templater()



  public void actionPerformed(ActionEvent e)
  // deal with button presses
  {
    if (e.getSource() == findBut)    //find a print in the webcam image
      scanPanel.findPrint();
    else if (e.getSource() == tmpltBut)   // analyze the fingerprint image
      scanPanel.analyzePrint();
  }  // end of actionPerformed()



  // -------------------------------------------------------

  public static void main( String args[] )
  {  new Templater();  }

} // end of Templater class
