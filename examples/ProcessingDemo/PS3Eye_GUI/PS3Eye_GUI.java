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

package ProcessingDemo.PS3Eye_GUI;


import java.util.ArrayList;

import com.thomasdiewald.ps3eye.PS3EyeP5;

import processing.core.PApplet;
import processing.core.PGraphics;


public class PS3Eye_GUI extends PApplet{
  
  PS3EyeP5 ps3eye;
  
  public void settings(){
    size(640, 480);
    smooth(0);
  }
  
  public void setup(){

    ps3eye = PS3EyeP5.getDevice(this);
    
    if(ps3eye == null){
      System.out.println("No PS3Eye connected. Good Bye!");
      exit();
      return;
    } 
    ps3eye.start();  
    createGUI();
  }
  

  public void draw(){
    image(ps3eye.getFrame(), 0, 0);

    updateGUI();
    
    surface.setTitle(String.format(getClass().getName()+ "  [fps %6.2f]", frameRate));  
  }
  

  
  public void mouseReleased(){
    gui.update(MiniGUI.EV_MOUSE_RELEASED);
  }
  
  public void mousePressed(){
    gui.update(MiniGUI.EV_MOUSE_PRESSED);
  }
  
  
  
  //////////////////////////////////////////////////////////////////////////
  //MiniGUI is just a quick and dirty GUI for this demo.
  //////////////////////////////////////////////////////////////////////////
  
  MiniGUI gui;
  
  public void updateGUI(){
    gui.update(mousePressed ? MiniGUI.EV_MOUSE_DRAGGED : MiniGUI.EV_NONE);
    gui.draw(this.g);
  }
  
  public void createGUI(){
    int px,py,sx,sy,dy,gap;
    
    gui = new MiniGUI(this);
    
    px = 10; py = 10; sx = 120; sy = 15;  gap = 3; dy = sy + gap;
    final MiniSlider slider_gain       = new MiniSlider(gui, "gain"            , px, py    , sx, sy).setRange(0,  63).setValue(ps3eye.getGain        ());
    final MiniSlider slider_exposure   = new MiniSlider(gui, "exposure"        , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getExposure    ());
    final MiniSlider slider_sharpness  = new MiniSlider(gui, "sharpness"       , px, py+=dy, sx, sy).setRange(0,  63).setValue(ps3eye.getSharpness   ());
    final MiniSlider slider_hue        = new MiniSlider(gui, "hue"             , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getHue         ());
    final MiniSlider slider_brightness = new MiniSlider(gui, "brightness"      , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getBrightness  ());
    final MiniSlider slider_contrast   = new MiniSlider(gui, "contrast"        , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getContrast    ());
    final MiniSlider slider_blueblc    = new MiniSlider(gui, "blueblc"         , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getBlueBalance ());
    final MiniSlider slider_redblc     = new MiniSlider(gui, "redblc"          , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getRedBalance  ());
    final MiniSlider slider_greenblc   = new MiniSlider(gui, "greenblc"        , px, py+=dy, sx, sy).setRange(0, 255).setValue(ps3eye.getGreenBalance());
                                                                 
    sx = sy = 20; dy = sy + gap;
    final MiniSwitch switch_autogain   = new MiniSwitch(gui, "autogain"        , px, py+=dy, sx, sy).toggle(ps3eye.getAutogain        ());
    final MiniSwitch switch_awb        = new MiniSwitch(gui, "autoWhiteBlance" , px, py+=dy, sx, sy).toggle(ps3eye.getAutoWhiteBalance());
    final MiniSwitch switch_flip_h     = new MiniSwitch(gui, "flip_h"          , px, py+=dy, sx, sy).toggle(ps3eye.getFlipH           ());
    final MiniSwitch switch_flip_v     = new MiniSwitch(gui, "flip_v"          , px, py+=dy, sx, sy).toggle(ps3eye.getFlipV           ());
    
    final MiniSwitch switch_io         = new MiniSwitch(gui, "ON"      , px, py+=dy*2, sx, sy).toggle(true);

    
    gui.addEventListener(new MiniGUIEvent(){
      @Override
      public void guiEvent(MiniControl control) {
        if(control instanceof MiniSlider){
          MiniSlider slider = (MiniSlider) control;
          if(slider == slider_gain      ) ps3eye.setGain        (slider.val);
          if(slider == slider_exposure  ) ps3eye.setExposure    (slider.val);
          if(slider == slider_sharpness ) ps3eye.setSharpness   (slider.val);
          if(slider == slider_hue       ) ps3eye.setHue         (slider.val);
          if(slider == slider_brightness) ps3eye.setBrightness  (slider.val);
          if(slider == slider_contrast  ) ps3eye.setContrast    (slider.val);
          if(slider == slider_blueblc   ) ps3eye.setBlueBalance (slider.val);
          if(slider == slider_redblc    ) ps3eye.setRedBalance  (slider.val);
          if(slider == slider_greenblc  ) ps3eye.setGreenBalance(slider.val);
        }
        
        if(control instanceof MiniSwitch){
          MiniSwitch sw = (MiniSwitch) control;
          if(sw == switch_autogain) ps3eye.setAutogain        (sw.value);
          if(sw == switch_awb     ) ps3eye.setAutoWhiteBalance(sw.value);
          if(sw == switch_flip_h  ) ps3eye.setFlip            (switch_flip_h.value, switch_flip_v.value);
          if(sw == switch_flip_v  ) ps3eye.setFlip            (switch_flip_h.value, switch_flip_v.value);
          
          if(sw == switch_io){
            if(switch_io.value) { ps3eye.start();  switch_io.name = "ON" ; } 
            else                { ps3eye.stop ();  switch_io.name = "OFF"; }
          }
        }  
      }
    });
    
  }
  

  static public interface MiniGUIEvent{
    public void guiEvent(MiniControl control);
  }
  
  
  static public class MiniGUI{
    static final public int EV_NONE           = 0;
    static final public int EV_MOUSE_PRESSED  = 1;
    static final public int EV_MOUSE_RELEASED = 2;
    static final public int EV_MOUSE_DRAGGED  = 1;
    PApplet papplet;
    ArrayList<MiniControl> controls = new ArrayList<MiniControl>();
    MiniGUIEvent gui_event;
    
    public MiniGUI(PApplet papplet){
      this.papplet = papplet;
    }
    public void addControl(MiniControl control){
      controls.add(control);
      control.parent = this;
    }
    public void update(int event){
      for(MiniControl control : controls) control.update(event);
    }
    public void draw(PGraphics pg){
      for(MiniControl control : controls) control.draw(pg);
    }
    public void addEventListener(MiniGUIEvent gui_event){
      this.gui_event = gui_event;
    }
    public void trigger(MiniControl control){
      if(gui_event != null){
        gui_event.guiEvent(control);
      }
    }
  }

  
  static public abstract class MiniControl{
    public MiniGUI parent;
    public String name;
    public int px, py, sx, sy;
    public MiniControl(MiniGUI parent, String name, int px, int py, int sx, int sy){
      parent.addControl(this);
      this.name=name; this.px = px; this.py = py; this.sx = sx; this.sy = sy;
    }
    public void draw(PGraphics pg){
      pg.noStroke();
      pg.fill(0,100,200,150);
      pg.rect(px,py,sx,sy);
      float tx = px+sx+5;
      float ty = py+sy/2 + 5;
      pg.fill(0,150);
      pg.rect(px+sx, py, pg.textWidth(name)+10, sy);
      pg.fill(255); pg.text(name, tx, ty);
    }
    public boolean mouseOver(){
      return isInside(parent.papplet.mouseX, parent.papplet.mouseY);
    }
    public boolean isInside(int x, int y){
      return (x >= px) && (x <= px+sx) && (y >= py) && (y <= py+sy);
    }
    abstract void update(int event);
  }
 
  
  static public class MiniSwitch extends MiniControl{
    boolean value = false;

    public MiniSwitch(MiniGUI parent, String name, int px, int py, int sx, int sy) {
      super(parent, name, px, py, sx, sy);
    }
    public MiniSwitch toggle(boolean value){
      this.value = value;
      parent.trigger(this);
      return this;
    }
    public MiniSwitch toggle(){
      toggle(!value);
      return this;
    }
    @Override
    public void draw(PGraphics pg){
      super.draw(pg);
      if(value){
        pg.noStroke();
        pg.fill(255,200);
        pg.rect(px,py,sx,sy);
      }
    }
    @Override
    void update(int event) {
      if(mouseOver() && event == MiniGUI.EV_MOUSE_RELEASED){
        toggle();
      }
    }
  }

  static public class MiniSlider extends MiniControl{
    public int val, val_min, val_max;
    
    public MiniSlider(MiniGUI parent, String name, int px, int py, int sx, int sy) {
      super(parent, name, px, py, sx, sy);
    }
    public MiniSlider setRange(int val_min, int val_max){
      this.val_min = val_min;
      this.val_max = val_max;
      setValue(val);
      return this;
    }
    public MiniSlider setValue(int val){
      if(val < val_min) val = val_min;
      if(val > val_max) val = val_max;
      this.val = val;
      parent.trigger(this);
      return this;
    }
    @Override
    public void draw(PGraphics pg){
      super.draw(pg);
      float val_norm = val / (float)(val_max-val_min);
      pg.noStroke();
      pg.fill(255,200);
      pg.rect(px,py,sx*val_norm,sy);
    }
    @Override
    void update(int event) {
      if(mouseOver() && event==MiniGUI.EV_MOUSE_DRAGGED){
        float val_norm = (parent.papplet.mouseX - px) / (float)(sx);
        int   val = val_min + Math.round(val_norm * (val_max-val_min)); 
        setValue(val);
      }
    }
  }
  
  
  
  
  public static void main(String[] args) {
//    System.out.println( new Object() { }.getClass().getEnclosingClass().getName() );
    PApplet.main(new String[] { PS3Eye_GUI.class.getName() });
  }

}
