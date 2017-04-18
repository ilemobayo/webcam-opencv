
// QRExec.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from a webcam in a picture panel (QRPanel). 
   When the "Decode" button is pressed, a QRCode is extracted from
   the current image if possible, and displayed in a message textfield.
   The "Launch" button treats the message as a URL or e-mail, and loads it into
   a browser or e-mail client

   Usage:
      > java QRExec
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;


public class QRExec extends JFrame 
{
  // GUI components
  private QRPanel qrPanel;
  private JButton decoderJB, launchJB;
  private Color butColor;
  private JTextField messageJTF;

  private DesktopRun deskRun;


  public QRExec()
  {
    super("QRCode Execution");

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    deskRun = new DesktopRun();
    makeGUI();

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { qrPanel.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of QRExec()


  public void makeGUI()
  /* The GUI consists of a picture panel (QRPanel) in the center, and a row of
     controls along the bottom of the frame.

     The decode button starts the QRcode decoding of the current image shown
     in the picture panel.
     The load button passes any text in the message text field over to the
     Desktop API to launch a browser or e-mail client.
  */
  {
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    qrPanel = new QRPanel(this); // the sequence of pictures appear here
    c.add( qrPanel, BorderLayout.CENTER);

    // panel holding the decode button, label, textfield, the load button
    JPanel p1 = new JPanel();
    decoderJB = new JButton("Decode");
    butColor = decoderJB.getBackground();
    decoderJB.addActionListener( new ActionListener() {
       public void actionPerformed(ActionEvent e)
       { messageJTF.setText("");
         qrPanel.startDecoder();
         decoderJB.setBackground(Color.LIGHT_GRAY);
         decoderJB.setEnabled(false);
       }
    });
    p1.add(decoderJB);

    p1.add( new JLabel("Msg: "));

    messageJTF = new JTextField(30);
    messageJTF.setEditable(false);
    p1.add(messageJTF);

    launchJB = new JButton("Launch");
    launchJB.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) 
      {  deskRun.launch( messageJTF.getText() );  }
    });
    p1.add(launchJB);

    c.add(p1, BorderLayout.SOUTH);
  }  // end of makeGUI()



  public void showMessage(final String msg)
  // called from QRPanel to update the message textfield
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        messageJTF.setText(msg);
        decoderJB.setBackground(butColor);
        decoderJB.setEnabled(true);
      }
    });
  }  // end of showMessage();


  // -------------------------------------------------------


  public static void main( String args[] )
  {  new QRExec();  }

} // end of QRExec class
