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
 * "PS3Eye" is a java-port of the "PS3EYEDriver" project by Eugene Zatepyakin. 
 * 
 * src: https://github.com/inspirit/PS3EYEDriver
 *      https://github.com/inspirit/PS3EYEDriver/blob/master/LICENSE
 * 
 * _____________________________________________________________________________
 * 
 * 
 */




package com.thomasdiewald.ps3eye_test;

import java.util.ArrayList;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * 
 * @author Thomas Diewald
 *
 */
public class USB{
  Context context;
  
  Thread   update_thread;
  boolean  exit_signaled;
  int      active_camera_count;
  
  
  public USB(){
    init(LibUsb.LOG_LEVEL_INFO);
  }
  
  public void init(int log_level){
    if(context != null){
      return;
    }

    int result = LibUsb.init(context);
    if (result != LibUsb.SUCCESS) {
      throw new LibUsbException("Unable to initialize libusb.", result);
    }
    
    LibUsb.setDebug(context, log_level);
  }

  
  
  public Device[] getDevices(int vendor_id, int product_id){
    
    DeviceList usb_device_list = new DeviceList();
    int result = LibUsb.getDeviceList(context, usb_device_list);
    if (result < 0){
      throw new LibUsbException("Unable to get device list", result);
    }
    
    ArrayList<Device> ps3_device_list = new ArrayList<Device>();
    
    try {

      for (Device usb_device : usb_device_list) {
        DeviceDescriptor descriptor = new DeviceDescriptor();
        result = LibUsb.getDeviceDescriptor(usb_device, descriptor);
        if (result != LibUsb.SUCCESS){
          throw new LibUsbException("Unable to read device descriptor", result);
        }

        if(descriptor.idVendor() == vendor_id && descriptor.idProduct() == product_id){
          DeviceHandle handle = new DeviceHandle();
          result = LibUsb.open(usb_device, handle);
          if (result != LibUsb.SUCCESS){
            throw new LibUsbException("Unable to open USB device", result);
          }
          
          LibUsb.close(handle);
          LibUsb.refDevice(usb_device);
          ps3_device_list.add(usb_device);
        }
  
      }
    } finally {
      LibUsb.freeDeviceList(usb_device_list, true);
    }
    
    return ps3_device_list.toArray(new Device[ps3_device_list.size()]);
  }
  
//  
//  public PS3Eye[] listDevices(){
//    
//    DeviceList usb_device_list = new DeviceList();
//    int result = LibUsb.getDeviceList(null, usb_device_list);
//    if (result < 0){
//      throw new LibUsbException("Unable to get device list", result);
//    }
//    
//    ArrayList<PS3Eye> ps3_list = new ArrayList<PS3Eye>();
//    
//    try {
//
//      for (Device usb_device : usb_device_list) {
//        DeviceDescriptor descriptor = new DeviceDescriptor();
//        result = LibUsb.getDeviceDescriptor(usb_device, descriptor);
//        if (result != LibUsb.SUCCESS){
//          throw new LibUsbException("Unable to read device descriptor", result);
//        }
//
//        short vid = descriptor.idVendor();
//        short pid = descriptor.idProduct();
//        
//        if(vid == PS3Eye.VENDOR_ID && pid == PS3Eye.PRODUCT_ID){
//          DeviceHandle handle = new DeviceHandle();
//          result = LibUsb.open(usb_device, handle);
//          if (result != LibUsb.SUCCESS){
//            throw new LibUsbException("Unable to open USB device", result);
//          }
//          
//          LibUsb.close(handle);
//          LibUsb.refDevice(usb_device);
//          
//          
//          
//          ps3_list.add(new PS3Eye(usb_device));
//        }
//  
//      }
//    } finally {
//      LibUsb.freeDeviceList(usb_device_list, true);
//    }
//    
//    return ps3_list.toArray(new PS3Eye[ps3_list.size()]);
//  }
  
  
  synchronized protected void cameraStarted(){
    if (active_camera_count++ == 0){
      startTransferThread();
    }
      
  }

  
  synchronized protected void cameraStopped(){
    if (--active_camera_count == 0){
      stopTransferThread();
    }
  }
  
  
  protected void startTransferThread(){
    update_thread = new Thread(new TranferThread());
    update_thread.setName("PS3EyeDriver Transfer Thread");
    update_thread.start();
  }
  
  
  synchronized protected void stopTransferThread(){
    exit_signaled = true;
    try {
      update_thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Reset the exit signal flag.
    // If we don't and we call startTransferThread() again, transferThreadFunc will exit immediately.
    exit_signaled = false;    
  }


  private class TranferThread implements Runnable {
    public void run() {
       while (!exit_signaled){
         long timeout = 50 * 1000;
         LibUsb.handleEventsTimeoutCompleted(context, timeout, null);
       }
    }
  }
  
  
  public void release(){
    if(context != null){
      LibUsb.exit(context);
      context = null;
    }
  }
  
}

