
// ExtFilter
// Andrew Davison, Feb. 2013, ad@fivedots.coe.psu.ac.th

/* A filter for JFileChooser which restricts the choice to directories
   and files that end with an extension ("."+ext)
*/


import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class ExtFilter extends FileFilter 
{
  private String ext;


  public ExtFilter(String ext) 
  {  this.ext = ext.toLowerCase();  }


  public String getDescription() 
  {  return ext; }


  public boolean accept(File file) 
  {
    if (file.isDirectory())
      return true;
    else {
      String path = file.getAbsolutePath().toLowerCase();
      if (path.endsWith("." + ext))
        return true;
    }
    return false;
  } // end of accept()


}  // end of ExtFilter class

