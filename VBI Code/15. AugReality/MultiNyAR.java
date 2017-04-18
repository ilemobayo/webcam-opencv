
// MultiNyAR.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, July 2013

/*  An Java3D NyARToolkit example using multiple markers and models,
    that reports position and rotation information.
    NYARToolkit is available at:
           http://nyatla.jp/nyartoolkit/wiki/index.php?NyARToolkit%20for%20Java.en

    NCSA Portfolio is used to load the models. It is available at:
      http://fivedots.coe.psu.ac.th/~ad/jg/ch9/

    --------------------
    Usage:
      > compile *.java
      > run MultiNyAR
*/


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.java3d.utils.*;



public class MultiNyAR extends JFrame
{
  private final String PARAMS_FNM = "Data/camera_para.dat";

  private static final int WIDTH = 640;   // size of panel
  private static final int HEIGHT = 480; 

  private static final double SHAPE_SIZE = 0.02; 

  private static final int BOUNDSIZE = 100;  // larger than world

  private J3dNyARParam cameraParams;
  private NyARMarkersBehavior nyaBeh;
  private JTextArea statusTA;



  public MultiNyAR()
  {
    super("Multiple markers NyARToolkit Example");

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    cameraParams = readCameraParams(PARAMS_FNM);

    Container cp = getContentPane();

    // create a JPanel in the center of JFrame
    JPanel p = new JPanel();
    p.setLayout( new BorderLayout() );
    p.setPreferredSize( new Dimension(WIDTH, HEIGHT) );
    cp.add(p, BorderLayout.CENTER);

    // put the 3D canvas inside the JPanel
    p.add(createCanvas3D(), BorderLayout.CENTER);

    // add status field to bottom of JFrame
    statusTA = new JTextArea(7, 10);   // updated by DetectMarkers object (see createSceneGraph())
    statusTA.setEditable(false);
    cp.add(statusTA, BorderLayout.SOUTH);

    // configure the JFrame
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { nyaBeh.stop();
        System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  }  // end of MultiNyAR()



  private J3dNyARParam readCameraParams(String fnm)
  {
    J3dNyARParam cameraParams = null;  
    try {
      cameraParams = J3dNyARParam.loadARParamFile( new FileInputStream(fnm));
      cameraParams.changeScreenSize(WIDTH, HEIGHT);
    }
    catch(Exception e)
    {  System.out.println("Could not read camera parameters from " + fnm);
       System.exit(1);
    }
    return cameraParams;
  }  // end of readCameraParams()



  private Canvas3D createCanvas3D()
  /* Build a 3D canvas for a Universe which contains
     the 3D scene and view 
            univ --> locale --> scene BG
                          |
                           ---> view BG  --> Canvas3D
                              (set up using camera cameraParams)
   */
  { 
    Locale locale = new Locale( new VirtualUniverse() );
    locale.addBranchGraph( createSceneGraph() );   // add the scene

    // get the preferred graphics configuration for the default screen
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

    Canvas3D c3d = new Canvas3D(config);
    locale.addBranchGraph( createView(c3d) );  // add view branch

    return c3d;
  }  // end of createCanvas3D()



  private BranchGroup createSceneGraph()
  /* The scene graph:
         sceneBG 
               ---> lights
               |
               ---> bg
               |
               -----> tg1 ---> model1  
               -----> tg2 ---> model2 
               |
               ---> behavior  (controls the bg and the tg's of the models)
  */
  { 
    BranchGroup sceneBG = new BranchGroup();  
    lightScene(sceneBG);              // add lights

    Background bg = makeBackground();
    sceneBG.addChild(bg);             // add background
    

    DetectMarkers detectMarkers = new DetectMarkers(this);

    // the "hiro" marker uses a robot model, scaled by 0.15 units, with no coords file
    MarkerModel mm1 = new MarkerModel("patt.hiro", "robot.3ds", 0.15, false);
    if (mm1.getMarkerInfo() != null) {    // creation was successful
      sceneBG.addChild( mm1.getMoveTg() );
      detectMarkers.addMarker(mm1);
    }

    // the "kanji" marker uses a cow model, scaled by 0.12 units, with coords file
    MarkerModel mm2 = new MarkerModel("patt.kanji", "cow.obj", 0.12, true);
    if (mm2.getMarkerInfo() != null) {
      sceneBG.addChild( mm2.getMoveTg() );
      detectMarkers.addMarker(mm2);
    }

    // initialise detector once all markers added
    detectMarkers.createDetector(cameraParams);   

    // create a NyAR multiple marker behaviour
    nyaBeh = new NyARMarkersBehavior(bg, detectMarkers);
    sceneBG.addChild(nyaBeh);

    sceneBG.compile();       // optimize the sceneBG graph
    return sceneBG;
  }  // end of createSceneGraph()



  private void lightScene(BranchGroup sceneBG)
  /* One ambient light, 2 directional lights */
  {
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
    BoundingSphere bounds = new BoundingSphere(new Point3d(0,0,0), BOUNDSIZE); 

    // Set up the ambient light
    AmbientLight ambientLightNode = new AmbientLight(white);
    ambientLightNode.setInfluencingBounds(bounds);
    sceneBG.addChild(ambientLightNode);

    // Set up the directional lights
    Vector3f light1Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);
       // left, down, backwards 
    Vector3f light2Direction  = new Vector3f(1.0f, -1.0f, 1.0f);
       // right, down, forwards

    DirectionalLight light1 =  new DirectionalLight(white, light1Direction);
    light1.setInfluencingBounds(bounds);
    sceneBG.addChild(light1);

    DirectionalLight light2 = new DirectionalLight(white, light2Direction);
    light2.setInfluencingBounds(bounds);
    sceneBG.addChild(light2);
  }  // end of lightScene()



  private Background makeBackground()
  // the background will be the current image captured by the camera
  { 
    Background bg = new Background();
    BoundingSphere bounds = new BoundingSphere();
    bounds.setRadius(10.0);
    bg.setApplicationBounds(bounds);
    bg.setImageScaleMode(Background.SCALE_FIT_ALL);
    bg.setCapability(Background.ALLOW_IMAGE_WRITE);  // so can change image

    return bg;
  }  // end of makeBackground()



  private BranchGroup createView(Canvas3D c3d)
  // create a view graph using the camera parameters
  {
    View view = new View();
    ViewPlatform viewPlatform = new ViewPlatform();
    view.attachViewPlatform(viewPlatform);
    view.addCanvas3D(c3d);

    view.setPhysicalBody(new PhysicalBody());
    view.setPhysicalEnvironment(new PhysicalEnvironment());

    view.setCompatibilityModeEnable(true);
    view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
    view.setLeftProjection( cameraParams.getCameraTransform() );   // camera projection

    TransformGroup viewGroup = new TransformGroup();
    Transform3D viewTransform = new Transform3D();
    viewTransform.rotY(Math.PI);   // rotate 180 degrees
    viewTransform.setTranslation(new Vector3d(0.0, 0.0, 0.0));   // start at origin
    viewGroup.setTransform(viewTransform);
    viewGroup.addChild(viewPlatform);

    BranchGroup viewBG = new BranchGroup();
    viewBG.addChild(viewGroup);

    return viewBG;
  }  // end of createView()



  public void setStatus(String msg)
  // called from DetectMarkers
  {
    statusTA.setText(msg);
  }  // end of setStatus()



  // ------------------------------------------------------------

  public static void main(String args[])
  {  new MultiNyAR();  }
    
    
} // end of MultiNyAR class
