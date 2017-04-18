
// CamReceiver.java
// Andrew Davison, September 2012, ad@fivedots.coe.psu.ac.th

/* Each CamReceiver is associated with a CamReader client which sends image
   parts to it over a Socket link, and a CamViewerPanel for displaying the 
   combined image.
   
   CamReceiver receives a series of NUM_SEPS*NUM_SEPS subimages (or less)
   and reconstructs them into an image. An image update may send less than
   NUM_SEPS*NUM_SEPS subimages, in which case the reconstruction uses the 
   old subimage delivered in an earlier update. An update is finished when
   the receiver is sent the END_UPDATE message.

   The END_TRANS message means that this client is about to finish, and the receiver
   also terminates.
*/


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;



public class CamReceiver implements Runnable
{   
  private static final int NUM_SEPS = 4;    // separations per row and column

  private static final int DELAY = 100;  // ms, time waiting for finish

  // special message IDs
  private static final int END_UPDATE = -98;
  private static final int END_TRANS = -99;

  private Socket sock;
  private int recID;    // ID for this receiver
  private CamViewerPanel camPanel;   // the display panel for this receiver
  private CamsViewer camsViewer;    // the top-level server

  private int numPartsUpdated = 0;

  private volatile boolean isRunning;
  private volatile boolean isFinished;


  public CamReceiver(Socket s, int idx, CamViewerPanel cp, CamsViewer cv) 
  { 
    sock = s; 
    recID = idx;
    camPanel = cp;
    camsViewer = cv;
    try {
      sock.setSoTimeout(5000);    // 5 secs
    }
    catch(SocketException e) {}

    new Thread(this).start();   // start receiving client data
  }  // end of CamReceiver()



  public void run()
  /* Update the image parts, join them together as a single image, and
     pass it to the CamViewerPanel. This is done repeatedly until updateParts()
     returns false or isRunning is set to false by a call to closeDown().
  */
  {
    BufferedImage[] imParts = new BufferedImage[NUM_SEPS*NUM_SEPS];
    try {
      DataInputStream dis = new DataInputStream(sock.getInputStream());
      isRunning = true;
      isFinished = false;
      while (isRunning) {
        if (!updateParts(dis, imParts))
          break;

        BufferedImage im = ImageUtils.join(imParts, NUM_SEPS);   // join parts together
        if (im == null)
          System.out.println("No image received");
        else
          camPanel.setImage(im, numPartsUpdated);   // only update image if im contains something
      }

      // Close this connection, (not the overall server socket)
      System.out.println("Cam receiver " + recID + " terminating");
      camPanel.setImage(null, 0);
      camsViewer.releaseCam(recID);    
              // release the CamViewerPanel so it can be used with another receiver
      sock.close();
    } 
    catch(IOException ioe)
    {System.out.println(ioe); }

    System.out.println("Execution End");
    isFinished = true;
  } // end of run()



  private boolean updateParts(DataInputStream dis, BufferedImage[] imParts)
  /* Update the imParts array with updated parts until an END_UPDATE 
     message is received, then return true. The global numPartsUpdated
     is also set.

     If readMessage() returns END_TRANS or -1 then the client has disconnected,
     so return false.
  */
  {
    numPartsUpdated = 0;
    while (numPartsUpdated < imParts.length) {
      int res = readMessage(dis, imParts);
      if (res == END_UPDATE)
        break;
      else if ((res == END_TRANS) || (res == -1))
        return false;
      numPartsUpdated++;
    }
    return true;
  }  // end of updateParts()



  private int readMessage(DataInputStream dis, BufferedImage[] imParts)
  /*  The message format is:
             <part index> <byte array length>  <image data bytes ...>
        or    END_UPDATE
        or    END_TRANS
      If the ID is END_UPDATE then it means that this round of updates is finished.
      If the ID is END_TRANS then it means that this client is closing down.

      This method returns the part's index of an updated part, or END_UPDATE,
      END_TRANS, or -1 for an error (such as a socket read error).
  */
  {
    try {
      int idx = dis.readInt();
      if (idx == END_UPDATE)
        return END_UPDATE;     // no more parts to update on this round
      else if (idx == END_TRANS) {
        System.out.println("Remote client has left");
        return END_TRANS;     // time to finish reading
      }

      int len = dis.readInt();
      if (len == 0) {    // no data in this part
        System.out.println("Part " + idx + " has no data");
        return -1;   // no data read
      }
      else {    // read in the image data
        byte[] imBytes = new byte[len];
        dis.readFully(imBytes);
        imParts[idx] = ImageUtils.bytesToIm(idx, imBytes);
        return idx;   // data read for idx part
      }
    }
    catch (InterruptedIOException iioe)
    { System.out.println("Remote client connection timed out during read");
      return -1;   // time to finish
    }
    catch(IOException e)
    { System.out.println("Client socket read error");  
      return -1;   // time to finish
    }
  }  // end of readMessage()



  public void closeDown()
  /* Terminate run() and wait for it to finish.
     This stops the application from exiting until everything
     has finished. */
  { 
    isRunning = false;
    while (!isFinished) {
      try {
        Thread.sleep(DELAY);
      } 
      catch (Exception ex) {}
    }
  } // end of closeDown()


} // end of CamReceiver class
