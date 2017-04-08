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


package ProcessingDemo.PS3Eye_Basic;

import com.thomasdiewald.ps3eye_test.PS3EyeP5;

import processing.core.PApplet;

public class PS3Eye_Basic extends PApplet{
  
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
    
    ps3eye.start();  
  }
  
  public void draw(){
    image(ps3eye.getFrame(), 0, 0);
    
    surface.setTitle(String.format(getClass().getName()+ "  [fps %6.2f]", frameRate));
  }
  

  public static void main(String[] args) {
    PApplet.main(new String[] { PS3Eye_Basic.class.getName() });
  }

}

