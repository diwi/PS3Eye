/**
 * 
 * PS3Eye | Copyright (C) 2017 Thomas Diewald (www.thomasdiewald.com)
 * 
 * src  - https://github.com/diwi/PS3Eye
 * 
 * A Processing/Java library for PS3Eye capture using libusb.
 * MIT License: https://opensource.org/licenses/MIT
 * 
 * 
 * _____________________________________________________________________________
 * 
 * The code is mostly a direct port of the "PS3EYEDriver" project by Eugene Zatepyakin. 
 * 
 * PS3EYEDriver project: https://github.com/inspirit/PS3EYEDriver
 * PS3EYEDriver license: https://github.com/inspirit/PS3EYEDriver/blob/master/LICENSE
 * 
 * _____________________________________________________________________________
 * 
 */



package com.thomasdiewald.ps3eye_test;

import org.usb4java.Device;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * 
 * PS3Eye Camera for Processing Applications.
 * 
 * It is only different to its super "PS3Eye" as it keeps a reference to the
 * PApplet and a PImage as a framebuffer.
 * 
 * @author Thomas Diewald
 *
 */
public class PS3EyeP5 extends PS3Eye{

  private static PS3EyeP5[] PS3EYE_LIST = null;
  
  /**
   * get a list of all devices
   * 
   * @param papplet
   * @return
   */
  public static PS3EyeP5[] getDevices(PApplet papplet){
    if(PS3EYE_LIST == null){
      Device[] devices = usb.getDevices(PS3Eye.VENDOR_ID, PS3Eye.PRODUCT_ID);
      PS3EYE_LIST = new PS3EyeP5[devices.length];
      for(int i = 0; i < devices.length; i++){
        PS3EYE_LIST[i] = new PS3EyeP5(devices[i], i, papplet);
      }
    }
    return PS3EYE_LIST;
  }
  

  /**
   * get a list of all devices + init(framerate, PS3Eye.Format.RGB)
   * 
   * @param papplet
   * @param framerate
   * @return
   */
  public static PS3EyeP5[] getDevices(PApplet papplet, int framerate){
    return getDevices(papplet, framerate, PS3Eye.Format.RGB);
  }
  
  /**
    *  get a list of all devices + init(framerate, format)
    *  
    * @param papplet
    * @param framerate
    * @param format
    * @return
    */
  public static PS3EyeP5[] getDevices(PApplet papplet, int framerate, PS3Eye.Format format){
    PS3EyeP5[] list = getDevices(papplet);
    if(list != null){
      for(PS3EyeP5 item : list){
        item.init(framerate, format);
      }
    }
    return list;
  }
  

  /**
   * returns the number of available devices
   * 
   * @param papplet
   * @return
   */
  public static int getDeviceCount(PApplet papplet){
    return getDevices(papplet).length;
  }
  
  /**
   * returns the first device
   * 
   * @param papplet
   * @return
   */
  public static PS3EyeP5 getDevice(PApplet papplet){
    return getDevice(papplet, 0);
  }
  
  
  /**
   * returns a device with a given index
   * 
   * @param papplet
   * @param idx
   * @return
   */
  public static PS3EyeP5 getDevice(PApplet papplet, int idx){
    PS3EyeP5[] list = getDevices(papplet);
    return list.length > idx ? list[idx] : null;
  }
  
  
  
  
  
  
  
  // local PImage ... lazy alloc + dynamic realloc
  protected PImage frame;
  protected PApplet papplet;

  protected PS3EyeP5(Device device, int devide_idx, PApplet papplet) {
    super(device, devide_idx);
    this.papplet = papplet;
    
    papplet.registerMethod("dispose", this);
  }
  

  public PImage getFrame(){
    if(frame == null){
      frame = papplet.createImage(frame_w, frame_h, PConstants.ARGB);
    }
    
    frame.loadPixels();
    getFrame(frame.pixels);
    frame.updatePixels();

    return frame;
  }
  
  
}
