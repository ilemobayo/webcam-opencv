
// DesktopRun.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, March 2011

/* Uses the Desktop API in JavaSE 6 to start either a browser
   or e-mail client.

*/

import java.awt.*;
import java.io.*;
import java.net.*;


public class DesktopRun 
{
  private Desktop desktop;


  public DesktopRun()
  {
    desktop = null;
    if (Desktop.isDesktopSupported()) {
      desktop = Desktop.getDesktop();
      // System.out.println("Desktop API is supported");
    }
    else
      System.out.println("Desktop API is NOT supported");
  }  // end of DesktopRun()
    


  public void launch(String msg)
  // launch either a browser or e-mail client
  {
    if (desktop == null) {
      System.out.println("Desktop API is NOT supported");
      return;
    }

    String lMsg = msg.toLowerCase();
    if (lMsg.startsWith("http:") || lMsg.startsWith("https:") ||
        lMsg.startsWith("ftp:"))
      launchBrowser(msg);
    else if (lMsg.startsWith("mailto:") || lMsg.startsWith("smtp:"))
      launchMail(msg);
    else
      System.out.println("Unrecognized launch format: " + msg);
  }  // end of launch()
    


  private void launchBrowser(String url)
  // Launch the default browser with the URL provided
  {
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        URI uri = new URI(url);
        desktop.browse(uri);
        System.out.println("Opening browser for " + url + "...");
      }
      catch (IOException ioe) {
        System.out.println("Could not open browser app for " + url);
      }
      catch (URISyntaxException e) {
        System.out.println("Badly formatted url: " + url);
      }
    }
    else
      System.out.println("Desktop browsing is NOT supported");
  }  // end of launchBrowser()

  

  private void launchMail(String address)
  /* Launch the default email client using the "mailto" protocol.
     If the address is in SMTP format (as created by QuickMark), then
     translated it to mailto.
  */
  {
    if (!desktop.isSupported(Desktop.Action.MAIL)) {
      System.out.println("Desktop e-mail is NOT supported");
      return;
    }

    try {
      if (address.length() == 0) {
        System.out.println("Email address is empty");
        desktop.mail();
        System.out.println("Opening e-mail app...");
      }
      else {
        address = address.replaceAll(" ", "%20");   // URL encoding of spaces only
        URI uriMailTo = null;
        if (address.startsWith("SMTP:")) {
          System.out.println("Changing SMTP format to MAILTO");
          String mailto = SMTPtoMAILTO(address);
          uriMailTo = new URI(mailto);
        }
        else
          uriMailTo = new URI(address);
 
        if (uriMailTo == null) {
          System.out.println("URI Email address is empty");
          desktop.mail();
          System.out.println("Opening e-mail app...");
        }
        else {
          desktop.mail(uriMailTo);
          System.out.println("Opening e-mail app for " + uriMailTo + "...");
        }
      }
    }
    catch (IOException e) {
      System.out.println("Could not open mail app for " + address);
    }
    catch (Exception e) {
      System.out.println("Badly formatted e-mail address");
      System.out.println(e);
    }        
  }  // end of launchMail()



  private String SMTPtoMAILTO(String address)
  /* converts QuickMark
       SMTP:<email_address>:<subject>:<body>
     to
       MAILTO:email_address?subject=<subject>&body=<body>
       (see http://en.wikipedia.org/wiki/URI_scheme)
  */
  {
    String[] elems = address.split(":");
    if ((elems.length < 2) || (elems.length > 4)) {
      System.out.println("Incorrect SMTP address format: " + address);
      return null;
    }

    String mailto = "MAILTO:" + elems[1];
    if (elems.length == 4)
      mailto = mailto + "?subject=" + elems[2] + "&body=" + elems[3];
    else if (elems.length == 3)
      mailto = mailto + "?subject=" + elems[2];

    System.out.println("New mail form: " + mailto);
    return mailto;
  }  // end of SMTPtoMAILTO()

    
}  // end of DesktopRun class
