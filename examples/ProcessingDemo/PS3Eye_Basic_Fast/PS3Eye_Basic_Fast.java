/**
 * 
 * PS3Eye | Copyright (C) 2017 Thomas Diewald (www.thomasdiewald.com)
 * 
 * src  - www.github.com/diwi/PS3Eye
 * 
 * A Processing/Java library for PS3Eye capture using libusb.
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */


package ProcessingDemo.PS3Eye_Basic_Fast;

import com.thomasdiewald.ps3eye.PS3EyeP5;

import processing.core.PApplet;

public class PS3Eye_Basic_Fast extends PApplet{
  
  PS3EyeP5 ps3eye;

  public void settings(){
    size(640, 480);
  }
  
  public void setup(){
    ps3eye = PS3EyeP5.getDevice(this);
    
    if(ps3eye == null){
      System.out.println("No PS3Eye connected. Good Bye!");
      exit();
      return;
    } 
    
    // start capturing with 60 fps (default)
    ps3eye.start();
    
    // if "false" Processing/PS3Eye frameRates are not "synchronized".
    // default value is "true".
    ps3eye.waitAvailable(false); 
    
    frameRate(1000);
  }
  
  public void draw(){
    // 1) draw runs at 60 fps, because ps3eye.getFrame() makes the sketch
    //    wait for a new frame to be available
    // 
    //    However, if "ps3eye.waitAvailable(false);" is set the sketch doesn't 
    //    wait either. Or use just use 2)
    // image(ps3eye.getFrame(), 0, 0);
    
    // 2) alternatively use the following code instead of 1) to not be limited 
    //    by the PS3Eye Capture-frameRate
    //    here a new frame is only copied when is available
    if(ps3eye.isAvailable()){
      image(ps3eye.getFrame(), 0, 0);
    }
    
    surface.setTitle(String.format(getClass().getName()+ "  [fps %6.2f]", frameRate));
  }
  

  public static void main(String[] args) {
    PApplet.main(new String[] { PS3Eye_Basic_Fast.class.getName() });
  }

}

