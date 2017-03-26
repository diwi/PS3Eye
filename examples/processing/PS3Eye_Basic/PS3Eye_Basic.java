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


package processing.PS3Eye_Basic;

import com.thomasdiewald.PS3Eye.PS3Eye;

import processing.core.PApplet;
import processing.core.PImage;


public class PS3Eye_Basic extends PApplet{
  
  PS3Eye ps3eye;
  PImage ps3eye_frame;

  public void settings(){
    size(640, 480);
    smooth(0);
  }
  
  public void setup(){
    PS3Eye[] ps3eye_list = PS3Eye.getDevices();
    
    if(ps3eye_list.length == 0){
      System.out.println("No PS3Eye connected. Good Bye!");
      exit();
      return;
    } 
    
    // pick the first device in the list
    ps3eye = ps3eye_list[0];
    // init, optionally with parameter ... ps3eye.init(60, Format.RGB);
    ps3eye.init();
    // register "dispose" to cleanup on program exit.
    registerMethod("dispose", ps3eye);
    // start
    ps3eye.start();

    // create frame for pixel transfer
    ps3eye_frame = createImage(ps3eye.getFrameWidth(), ps3eye.getFrameHeight(), ARGB);
      
  }
  
  public void draw(){
    
    // transfer pixel data
    ps3eye_frame.loadPixels();
    ps3eye.getFrame(ps3eye_frame.pixels);
    ps3eye_frame.updatePixels();
    
    // display frame
    image(ps3eye_frame, 0, 0);
    
    // info
    String txt_fps = String.format(getClass().getName()+ "  [fps %6.2f]", frameRate);
    surface.setTitle(txt_fps);
  }
  

  public static void main(String[] args) {
    PApplet.main(new String[] { PS3Eye_Basic.class.getName() });
  }

}
