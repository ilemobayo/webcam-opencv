
// CamsViewer.java
// Andrew Davison, September 2012, ad@fivedots.coe.psu.ac.th

/* A threaded server which spawns a CamReceiver thread to deal with 
   each CamReader client that connects to it. Each receiver is paired 
   with a CamViewerPanel
   which displays the latest image received from the client.

   At one time there can be at most NUM_PANELS receivers and panels. 
   If a client terminates a connection, then the receiver terminates, and
   its panel can be used for another client when it connects. The new client
   is matched to a newly invoked CamReceiver, but the old CamPanel is reused.
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import java.util.*;



public class CamsViewer extends JFrame implements Runnable
{
  private static final int PORT = 4444; 
  private static final int NUM_PANELS = 3;


  private CamReceiver[] receivers;  // receives image parts from a CamReader client

  private CamViewerPanel[] camsPan;    /* each one displays the reconstituted image
                                       for a matching CamReceiver */

  private boolean[] usedCams;       // which cam panels are currently in use?



  public CamsViewer()
  {
    super("Remote Cams Viewer");
    Container c = getContentPane();
    c.setLayout( new BoxLayout(c, BoxLayout.X_AXIS));   
          // the panels are laid out across the screen

    camsPan = new CamViewerPanel[NUM_PANELS];
    usedCams = new boolean[NUM_PANELS];
    receivers = new CamReceiver[NUM_PANELS];

    for (int i=0; i < NUM_PANELS; i++) {
      camsPan[i] = new CamViewerPanel(); // the reconstructed images appear in these panels
      usedCams[i] = false;    // all the panels are available (i.e. not in use) at the start
      c.add( camsPan[i]);
    }

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { for(CamReceiver cr : receivers)
          if (cr != null)
            cr.closeDown();    // stop receiving images from CamReader clients
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  

    // position this window at the bottom middle of the screen
    Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (scrSize.width  - getSize().width)/2;
    int y = scrSize.height - getSize().height - 30;
    setLocation(x, y);

    setVisible(true);

    new Thread(this).start();   // start waiting for client connections
  } // end of CamsViewer()



  public void run()
  /* wait for a client to contact the server, then pass it onto a CamReceiver
     object if there's a free cam Panel available for displaying the received
     images */
  {
    Socket sock;
    try {
      ServerSocket servsock = new ServerSocket(PORT);
      printHostInfo();
      while (true) {
        System.out.println("Waiting for a cam connection...");
	    sock = servsock.accept();
      
        // if there's a free cam panel then pass it to a new receiver for the client
        int camIdx = grabCam();
        if (camIdx != -1) {
          receivers[camIdx] = new CamReceiver(sock, camIdx, camsPan[camIdx], this);
          System.out.println("--assigned new cam to panel " + camIdx);
        }
        else {
          System.out.println("Not enough panels to accept a new cam");
          sock.close();
        }
      }
    }
    catch (IOException e)
    {  System.out.println("CamsViewer network problem");  
       System.exit(0);
    }
  } // of run()



  private void printHostInfo() 
  /* print the server's IP address and port, which is useful when invoking
     CamReader clients on other machines */
  {
    try {
      InetAddress localAddr = InetAddress.getLocalHost();
      System.out.println("Viewer's IP address: " + localAddr.getHostAddress());
      System.out.println("Port: " + PORT);
    } 
    catch (UnknownHostException e) 
    {  System.out.println("Could not lookup host information"); }
  }  // end of printHostInfo()



  public synchronized int grabCam()
  // return the index of a free CamPanel, or -1
  {
    for (int i=0; i < NUM_PANELS; i++)
      if (!usedCams[i]) {
        usedCams[i] = true;
        return i;
      }
    return -1;
  }  // end of grabCam()



  public synchronized void releaseCam(int idx)
  /* Release the CamPanel with the specified index.
     This method is called from a terminating CamReceiver object 
    (from a separate thread, which is why it is synchronized).
  */
  { if ((idx >= 0) && (idx < NUM_PANELS)) {
      usedCams[idx] = false;
      receivers[idx] = null;
    }
  }  // end of releaseCam()


  // ----------------------------------------------

  public static void main( String args[] )
  {  new CamsViewer();  }

} // end of CamsViewer
