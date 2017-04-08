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

import java.nio.ByteBuffer;

import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;




/**
 * 
 * PS3Eye Camera for Java Applications.
 * 
 * @author Thomas Diewald
 *
 */
public class PS3Eye {
  
                                        
  static final private int OV534_REG_ADDRESS   = 0xf1;  // sensor address 
  static final private int OV534_REG_SUBADDR   = 0xf2;
  static final private int OV534_REG_WRITE     = 0xf3;
  static final private int OV534_REG_READ      = 0xf4;
  static final private int OV534_REG_OPERATION = 0xf5;
  static final private int OV534_REG_STATUS    = 0xf6;
                                              
  static final private int OV534_OP_WRITE_3    = 0x37;
  static final private int OV534_OP_WRITE_2    = 0x33;
  static final private int OV534_OP_READ_2     = 0xf9;
  
  static final private int[][] ov534_reg_initdata = {
      { 0xe7, 0x3a },

      { OV534_REG_ADDRESS, 0x42 }, // select OV772x sensor

      { 0x92, 0x01 },
      { 0x93, 0x18 },
      { 0x94, 0x10 },
      { 0x95, 0x10 },
      { 0xE2, 0x00 },
      { 0xE7, 0x3E },
      
      { 0x96, 0x00 },
      { 0x97, 0x20 },
      { 0x97, 0x20 },
      { 0x97, 0x20 },
      { 0x97, 0x0A },
      { 0x97, 0x3F },
      { 0x97, 0x4A },
      { 0x97, 0x20 },
      { 0x97, 0x15 },
      { 0x97, 0x0B },

      { 0x8E, 0x40 },
      { 0x1F, 0x81 },
      { 0xC0, 0x50 },
      { 0xC1, 0x3C },
      { 0xC2, 0x01 },
      { 0xC3, 0x01 },
      { 0x50, 0x89 },
      { 0x88, 0x08 },
      { 0x8D, 0x00 },
      { 0x8E, 0x00 },

      { 0x1C, 0x00 },   // video data start (V_FMT)

      { 0x1D, 0x00 },   // RAW8 mode
      { 0x1D, 0x02 },   // payload size 0x0200 * 4 = 2048 bytes
      { 0x1D, 0x00 },   // payload size
                        //
      { 0x1D, 0x01 },   // frame size = 0x012C00 * 4 = 307200 bytes (640 * 480 @ 8bpp)
      { 0x1D, 0x2C },   // frame size
      { 0x1D, 0x00 },   // frame size
                        //
      { 0x1C, 0x0A },   // video data start (V_CNTL0)
      { 0x1D, 0x08 },   // turn on UVC header
      { 0x1D, 0x0E },

      { 0x34, 0x05 },
      { 0xE3, 0x04 },
      { 0x89, 0x00 },
      { 0x76, 0x00 },
      { 0xE7, 0x2E },
      { 0x31, 0xF9 },
      { 0x25, 0x42 },
      { 0x21, 0xF0 },
      { 0xE5, 0x04 }  
    };

  static final private int[][] ov772x_reg_initdata = {

      { 0x12, 0x80 },  // reset
      { 0x3D, 0x00 },

      { 0x12, 0x01 },   // Processed Bayer RAW (8bit)

      { 0x11, 0x01 },
      { 0x14, 0x40 },
      { 0x15, 0x00 },
      { 0x63, 0xAA },   // AWB  
      { 0x64, 0x87 },
      { 0x66, 0x00 },
      { 0x67, 0x02 },
      { 0x17, 0x26 },
      { 0x18, 0xA0 },
      { 0x19, 0x07 },
      { 0x1A, 0xF0 },
      { 0x29, 0xA0 },
      { 0x2A, 0x00 },
      { 0x2C, 0xF0 },
      { 0x20, 0x10 },
      { 0x4E, 0x0F },
      { 0x3E, 0xF3 },
      { 0x0D, 0x41 },
      { 0x32, 0x00 },
      { 0x13, 0xF0 },   // COM8  - jfrancois 0xf0 orig x0f7
      { 0x22, 0x7F },
      { 0x23, 0x03 },
      { 0x24, 0x40 },
      { 0x25, 0x30 },
      { 0x26, 0xA1 },
      { 0x2A, 0x00 },
      { 0x2B, 0x00 },
      { 0x13, 0xF7 },
      { 0x0C, 0xC0 },

      { 0x11, 0x00 },
      { 0x0D, 0x41 },

      { 0x8E, 0x00 },   // De-noise threshold - jfrancois 0x00 - orig 0x04
    };

  static final private int[][] bridge_start_vga = {
      {0x1c, 0x00},
      {0x1d, 0x00},
      {0x1d, 0x02},
      {0x1d, 0x00},
      {0x1d, 0x01}, // frame size = 0x012C00 * 4 = 307200 bytes (640 * 480 @ 8bpp)
      {0x1d, 0x2C}, // frame size
      {0x1d, 0x00}, // frame size
      {0xc0, 0x50},
      {0xc1, 0x3c},
    };
  static final private int[][] sensor_start_vga = {
      {0x12, 0x01},
      {0x17, 0x26},
      {0x18, 0xa0},
      {0x19, 0x07},
      {0x1a, 0xf0},
      {0x29, 0xa0},
      {0x2c, 0xf0},
      {0x65, 0x20},
    };
  @SuppressWarnings("unused")
  static final private int[][] bridge_start_qvga = {
      {0x1c, 0x00},
      {0x1d, 0x00},
      {0x1d, 0x02},
      {0x1d, 0x00}, 
      {0x1d, 0x00}, // frame size = 0x004B00 * 4 = 76800 bytes (320 * 240 @ 8bpp)
      {0x1d, 0x4b}, // frame size
      {0x1d, 0x00}, // frame size
      {0xc0, 0x28},
      {0xc1, 0x1e},
    };
  @SuppressWarnings("unused")
  static final private int[][] sensor_start_qvga = {
      {0x12, 0x41},
      {0x17, 0x3f},
      {0x18, 0x50},
      {0x19, 0x03},
      {0x1a, 0x78},
      {0x29, 0x50},
      {0x2c, 0x78},
      {0x65, 0x2f},
    };



  static public enum Format{
    Bayer(1), // Output in Bayer. Destination buffer must be width * height bytes
    BGR  (3), // Output in BGR. Destination buffer must be width * height * 3 bytes
    RGB  (3); // Output in RGB. Destination buffer must be width * height * 3 bytes
    
    public int bytes_per_pixel;
    
    private Format(int bytes_per_pixel){
      this.bytes_per_pixel = bytes_per_pixel;
    }
  };
  
  
  static final protected USB usb = new USB();
  
  // PS3Eye id's
  static final public short VENDOR_ID  = 0x1415;
  static final public short PRODUCT_ID = 0x2000;
  
  // LibUsb
  protected int device_idx;
  protected Device       usb_device;
  protected DeviceHandle usb_device_handle;
  
  // frame
  protected final int frame_w = 640;
  protected final int frame_h = 480;
  protected int framerate = 60;
  protected PS3Eye.Format format = null;
  
  // controls
  protected int     gain       =    20; // 0 <->  63
  protected int     exposure   =   120; // 0 <-> 255
  protected int     sharpness  =     0; // 0 <->  63
  protected int     hue        =   143; // 0 <-> 255
  protected int     brightness =    20; // 0 <-> 255
  protected int     contrast   =    37; // 0 <-> 255
  protected int     blueblc    =   128; // 0 <-> 255
  protected int     redblc     =   128; // 0 <-> 255
  protected int     greenblc   =   128; // 0 <-> 255
  protected boolean autogain   = false;
  protected boolean awb        = false;
  protected boolean flip_h     = false;
  protected boolean flip_v     = false;
 
  protected boolean is_streaming = false;
  
  protected URBDesc urb = new URBDesc();
  
  
  private static PS3Eye[] PS3EYE_LIST = null;
  
  
  /**
   * get a list of all devices
   * 
   * @param papplet
   * @return
   */
  public static PS3Eye[] getDevices(){
    if(PS3EYE_LIST == null){
      Device[] devices = usb.getDevices(PS3Eye.VENDOR_ID, PS3Eye.PRODUCT_ID);
      PS3EYE_LIST = new PS3Eye[devices.length];
      for(int i = 0; i < devices.length; i++){
        PS3EYE_LIST[i] = new PS3Eye(devices[i], i);
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
  public static PS3Eye[] getDevices(int framerate){
    return getDevices(framerate, PS3Eye.Format.RGB);
  }
  
  /**
    *  get a list of all devices + init(framerate, format)
    *  
    * @param papplet
    * @param framerate
    * @param format
    * @return
    */
  public static PS3Eye[] getDevices(int framerate, PS3Eye.Format format){
    PS3Eye[] list = getDevices();
    if(list != null){
      for(PS3Eye item : list){
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
  public static int getDeviceCount(){
    return getDevices().length;
  }
  
  /**
   * returns the first device
   * 
   * @param papplet
   * @return
   */
  public static PS3Eye getDevice(){
    return getDevice(0);
  }
  
  
  /**
   * returns a device with a given index
   * 
   * @param papplet
   * @param idx
   * @return
   */
  public static PS3Eye getDevice(int idx){
    PS3Eye[] list = getDevices();
    return list.length > idx ? list[idx] : null;
  }
  
  
  
  
  
  
  
  
  // cleanup
  public static void disposeAll(){
    if(PS3EYE_LIST != null){
      for (int i = 0; i < PS3EYE_LIST.length; i++) {
        PS3EYE_LIST[i].release();
      }
      PS3EYE_LIST = null;
      
      usb.release();
//      System.out.println("PS3Eye.disposeAll()");
    }
  }
  
  

  
  
  protected PS3Eye(Device device, int device_idx){
    this.usb_device = device;
    this.device_idx = device_idx;
  }
  
  // call on exit
  public void dispose(){
//    System.out.println("dispose");
    PS3Eye.disposeAll();
  }
  
  public void init(){
    init(60, Format.RGB);
  }
  
  public void init(int framerate){
    init(framerate, Format.RGB);
  }

  public void init(int framerate, PS3Eye.Format format){
    openUSB();
    
    this.framerate = ov534_set_frame_rate(framerate, true);
    this.format = format;

    // reset bridge
    ov534_reg_write(0xe7, 0x3a);
    ov534_reg_write(0xe0, 0x08);
    
    // initialize the sensor address
    ov534_reg_write(OV534_REG_ADDRESS, 0x42);

    // reset sensor
    sccb_reg_write(0x12, 0x80);
//  #ifdef _MSC_VER
//    Sleep(10);
//  #else    
//      nanosleep((const struct timespec[]){{0, 10000000}}, NULL);
//  #endif
    
    // probe the sensor
    @SuppressWarnings("unused")
    int sensor_id = 0;
    sccb_reg_read(0x0a);
    sensor_id |= sccb_reg_read(0x0a) << 8;
    sccb_reg_read(0x0b);
    sensor_id |= sccb_reg_read(0x0b);
//    System.out.printf("Sensor ID: %04x\n", sensor_id);

    // initialize
    reg_w_array(ov534_reg_initdata);
    ov534_set_led(1);
    sccb_w_array(ov772x_reg_initdata);
    ov534_reg_write(0xe0, 0x09);
    ov534_set_led(0);
  }
  
  

  
  
  public void start(){
    if(format == null){
      init(60, PS3Eye.Format.RGB);
    }
    
    
    if(is_streaming) return;
    if(usb_device_handle == null){
      System.err.println("ERROR: PS3Eye needs .init() before .start()");
      // this will crash!
    }
    
    reg_w_array(bridge_start_vga);  // 640x480
    sccb_w_array(sensor_start_vga); // 640x480
   
    ov534_set_frame_rate(framerate);
  
    setAutogain(autogain);
    setAutoWhiteBalance(awb);
    setGain(gain);
    setHue(hue);
    setExposure(exposure);
    setBrightness(brightness);
    setContrast(contrast);
    setSharpness(sharpness);
    setRedBalance(redblc);
    setBlueBalance(blueblc);
    setGreenBalance(greenblc);
    setFlip(flip_h, flip_v);
  
    ov534_set_led(1);
    ov534_reg_write(0xe0, 0x00); // start stream
  
    // init and start urb
    urb.start_transfers(usb_device_handle, frame_w * frame_h);
    is_streaming = true;
  }
  

  
  
  public void stop(){
    if(!is_streaming) return;
    is_streaming = false;
    
    // stop streaming data
    ov534_reg_write(0xe0, 0x09);
    ov534_set_led(0);
      
    // close urb
    urb.close_transfers();
  }
  
  


  

  
  
  
  public void release(){
    stop();
    closeUSB();
  }
  
  
  
  private void openUSB(){
    if(usb_device_handle == null){
      usb_device_handle = new DeviceHandle(); 
      int rval = LibUsb.open(usb_device, usb_device_handle);
      if (rval != LibUsb.SUCCESS){
        throw new LibUsbException("error LibUsb.open", rval);
      }
    }
    
    if(usb_device_handle != null){
      int rval = LibUsb.claimInterface(usb_device_handle, 0);
      if (rval != LibUsb.SUCCESS){
        throw new LibUsbException("error LibUsb.claimInterface", rval);
      } 
    }
  }
  
  private void closeUSB(){
    if(usb_device_handle != null){
      LibUsb.releaseInterface(usb_device_handle, 0);
      LibUsb.close(usb_device_handle);
      usb_device_handle = null;
    }

    if(usb_device != null){
      LibUsb.unrefDevice(usb_device);
      usb_device = null;
    }
  }
  
  
  public int getUSBPortNumber(){
    return LibUsb.getPortNumber(usb_device);
  }


  
  
  

  
  
  
  
  
  // Two bits control LED: 0x21 bit 7 and 0x23 bit 7 (direction and output)?
  private void ov534_set_led(int status){
    int data;
    
    data = ov534_reg_read(0x21);
    data |= 0x80;
    ov534_reg_write(0x21, data);

    data = ov534_reg_read(0x23);
    if (status==1)
      data |= 0x80;
    else
      data &= ~0x80;

    ov534_reg_write(0x23, data);
    
    if (status==0) {
      data = ov534_reg_read(0x21);
      data &= ~0x80;
      ov534_reg_write(0x21, data);
    }
  }
  
 
  // validate frame rate and (if not dry run) set it
  private int ov534_set_frame_rate(int framerate){
    return ov534_set_frame_rate(framerate, false);
  }
  private int ov534_set_frame_rate(int framerate, boolean dry_run){
    
    // fps, (byte)r11, (byte)r0d, (byte)re5
    int[][] rate_640x480 = {
      { 83, 0x01, 0xc1, 0x02}, // 83 FPS: video is partly corrupt
      { 75, 0x01, 0x81, 0x02}, // 75 FPS or below: video is valid
      { 60, 0x00, 0x41, 0x04},
      { 50, 0x01, 0x41, 0x02},
      { 40, 0x02, 0xc1, 0x04},
      { 30, 0x04, 0x81, 0x02},
      { 25, 0x00, 0x01, 0x02},
      { 20, 0x04, 0x41, 0x02},
      { 15, 0x09, 0x81, 0x02},
      { 10, 0x09, 0x41, 0x02},
      {  8, 0x02, 0x01, 0x02},
      {  5, 0x04, 0x01, 0x02},
      {  3, 0x06, 0x01, 0x02},
      {  2, 0x09, 0x01, 0x02},
    };
    
    int[][] rate = rate_640x480;
    
    int idx = -1;
    while(++idx < rate.length-1){
     if (rate[idx][0] <= framerate) 
       break;
    }
    
    if (!dry_run) {
      sccb_reg_write (0x11, rate[idx][1]);
      sccb_reg_write (0x0d, rate[idx][2]);
      ov534_reg_write(0xe5, rate[idx][3]);
    }


    return rate[idx][0];
  }
  
  
  

  



 
 
  private void ov534_reg_write(int reg, int val){
    ByteBuffer buffer = ByteBuffer.allocateDirect(1);
    buffer.put(0, (byte) val);
 
    int transfered = LibUsb.controlTransfer(usb_device_handle, 
        (byte)(LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_VENDOR | LibUsb.RECIPIENT_DEVICE), 
        (byte) 0x01, (byte) 0x00, (short) reg, buffer, 500L);
 
    if (transfered < 0){
      throw new LibUsbException("error ov534_reg_write, LibUsb.controlTransfer", transfered);
    }
  }
  
  
  private int ov534_reg_read(int reg){
    ByteBuffer buffer = ByteBuffer.allocateDirect(1);

    int transfered = LibUsb.controlTransfer(usb_device_handle,
        (byte) (LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_VENDOR| LibUsb.RECIPIENT_DEVICE), 
        (byte) 0x01, (byte) 0x00, (short) reg,
        buffer, 500);

    if (transfered < 0){
      throw new LibUsbException("error ov534_reg_read, LibUsb.controlTransfer", transfered);
    }
    
    return buffer.get() & 0xFF;
  }
  
  
  private boolean sccb_check_status(){
    for (int i = 0; i < 5; i++) {
      int data = ov534_reg_read(OV534_REG_STATUS);
      switch (data) {
        case 0x00: return true;
        case 0x04: return false;
        case 0x03: break;
        default:   System.out.printf("sccb_check_status 0x%02x, attempt %d/5\n", data, i + 1);
      }
    }
    return true;
  }
  
  private void sccb_reg_write(int reg, int val){
    ov534_reg_write(OV534_REG_SUBADDR  , reg);
    ov534_reg_write(OV534_REG_WRITE    , val);
    ov534_reg_write(OV534_REG_OPERATION, OV534_OP_WRITE_3);

    if (!sccb_check_status()) {
      System.out.println("sccb_reg_write failed\n");
    }
  }
  
  private int sccb_reg_read(int reg){
    ov534_reg_write(OV534_REG_SUBADDR, reg);
    ov534_reg_write(OV534_REG_OPERATION, OV534_OP_WRITE_2);
    if (!sccb_check_status()) {
      System.out.println("sccb_reg_read failed 1\n");
    }

    ov534_reg_write(OV534_REG_OPERATION, OV534_OP_READ_2);
    if (!sccb_check_status()) {
      System.out.println("sccb_reg_read failed 2\n");
    }

    return ov534_reg_read(OV534_REG_READ);
  }
  
  // output a bridge sequence (reg - val)
  private void reg_w_array(int[][] data){
    for(int i = 0; i < data.length; i++){
      ov534_reg_write(data[i][0], data[i][1]);
    }
  }
 
  // output a sensor sequence (reg - val)
  private void sccb_w_array(int[][] data){
    for(int i = 0; i < data.length; i++){
      int[] datai = data[i];
      if (datai[0] != 0xff) {
        sccb_reg_write(datai[0], datai[1]);
      } else {
        sccb_reg_read(datai[1]);
        sccb_reg_write(0xff, 0x00);
      }
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  // Controls
  //////////////////////////////////////////////////////////////////////////////
  public boolean getAutogain() {
    return autogain;
  }

  public void setAutogain(boolean val) {
    autogain = val;
    if (val) {
      sccb_reg_write(0x13, 0xf7); // AGC,AEC,AWB ON
      sccb_reg_write(0x64, sccb_reg_read(0x64) | 0x03);
    } else {
      sccb_reg_write(0x13, 0xf0); // AGC,AEC,AWB OFF
      sccb_reg_write(0x64, sccb_reg_read(0x64) & 0xFC);

      setGain(gain);
      setExposure(exposure);
    }
  }

  public boolean getAutoWhiteBalance() {
    return awb;
  }

  public void setAutoWhiteBalance(boolean val) {
    awb = val;
    if (val) {
      sccb_reg_write(0x63, 0xe0); // AWB ON
    } else {
      sccb_reg_write(0x63, 0xAA); // AWB OFF
    }
  }

  public int getGain() {
    return gain;
  }

  public void setGain(int val) {
    gain = val;
    switch (val & 0x30) {
    case 0x00:
      val &= 0x0F;
      break;
    case 0x10:
      val &= 0x0F;
      val |= 0x30;
      break;
    case 0x20:
      val &= 0x0F;
      val |= 0x70;
      break;
    case 0x30:
      val &= 0x0F;
      val |= 0xF0;
      break;
    }
    sccb_reg_write(0x00, val);
  }

  public int getExposure() {
    return exposure;
  }

  public void setExposure(int val) {
    exposure = val;
    sccb_reg_write(0x08, val >> 7);
    sccb_reg_write(0x10, val << 1);
  }

  public int getSharpness() {
    return sharpness;
  }

  public void setSharpness(int val) {
    sharpness = val;
    sccb_reg_write(0x91, val); // vga noise
    sccb_reg_write(0x8E, val); // qvga noise
  }

  public int getContrast() {
    return contrast;
  }

  public void setContrast(int val) {
    contrast = val;
    sccb_reg_write(0x9C, val);
  }

  public int getBrightness() {
    return brightness;
  }

  public void setBrightness(int val) {
    brightness = val;
    sccb_reg_write(0x9B, val);
  }

  public int getHue() {
    return hue;
  }

  public void setHue(int val) {
    hue = val;
    sccb_reg_write(0x01, val);
  }

  public int getRedBalance() {
    return redblc;
  }

  public void setRedBalance(int val) {
    redblc = val;
    sccb_reg_write(0x43, val);
  }

  public int getBlueBalance() {
    return blueblc;
  }

  public void setBlueBalance(int val) {
    blueblc = val;
    sccb_reg_write(0x42, val);
  }

  public int getGreenBalance() {
    return greenblc;
  }

  public void setGreenBalance(int val) {
    greenblc = val;
    sccb_reg_write(0x44, val);
  }

  public boolean getFlipH() {
    return flip_h;
  }

  public boolean getFlipV() {
    return flip_v;
  }
  
  public void setFlip(boolean horizontal, boolean vertical) {
    flip_h = horizontal;
    flip_v = vertical;
    int val = sccb_reg_read(0x0c);
    val &= ~0xc0;
    if (!horizontal) val |= 0x40;
    if (!vertical  ) val |= 0x80;
    sccb_reg_write(0x0c, val);
  }
  
  

  public int getFramerate(){
    return framerate;
  }
  
  public void setFrameRate(int framerate){
    this.framerate = ov534_set_frame_rate(framerate);
  }
  
  public PS3Eye.Format getFormat(){
    return format;
  }
  
  public void setFormat(PS3Eye.Format format){
    this.format = format;
  }
  
  public int getFrameWidth(){
    return frame_w;
  }
  
  public int getFrameHeight(){
    return frame_h;
  }
  
  public boolean isStreaming(){
    return is_streaming;
  }
  
  public int getDeviceIndex(){
    return device_idx;
  }
  
  public Device getUsbDevice(){
    return usb_device;
  }
  
  public DeviceHandle getUsbDeviceHandle(){
    return usb_device_handle;
  }
  
  
  
  
  
  

  /**
   * 
   * @return true if a new frame is available
   */
  public boolean isAvailable(){
    return is_streaming && urb.frame_queue.isAvailable();
  }


  /**
   * When "true", the thread waits until a new frame is available for transfer.
   * The default value is "true".
   * 
   * @param wait_for_frame_to_be_available
   * 
   */
  public void waitAvailable(boolean wait_for_frame_to_be_available){
    urb.frame_queue.wait_for_frame_to_be_available = wait_for_frame_to_be_available;
  }
  
  
  
  /**
   * 
   * Copies the available frame-buffer data into the given buffer.
   * If buffer is null or of the wrong size, it gets (re)allocated.
   * In any case a buffer containing the current frame gets returned.
   * 
   */
  public byte[] getFrame(byte[] buffer){
    int num_channels = format.bytes_per_pixel;
    int num_pixels = frame_w * frame_h;
    int num_bytes = num_pixels * num_channels;
    
    // (re)alloc
    if(buffer == null || buffer.length != num_bytes){
      buffer = new byte[num_bytes];
    }
    
    // blocking data transfer
    if(is_streaming){
      urb.frame_queue.Dequeue(buffer, frame_w, frame_h, format);
    }
    
    return buffer;
  }
  
  
  // local buffer ... lazy alloc + dynamic realloc
  protected byte[] frame_buffer;
  
  
  /**
   * 
   * Fills the given pixels-array with the current frame-buffer.
   * "pixels" needs to be allocated and resized to the full framesize.
   * The Pixels are formated either as 0xAARRGGBB or 0xAABBGGRR, depending on
   * chosen format (Format.RGB or Format.BGR)
   * 
   */
  public void getFrame(int[] pixels){
    
    if(pixels == null || pixels.length != frame_w*frame_h){
      System.out.println("error getFrame(pixels_ARGB). pixels_ARGB has wrong size!");
      return;
    }
    
    // get image data (bytes)
    frame_buffer = getFrame(frame_buffer);
    
    int bayer=0,rgb_r=0, rgb_g=0, rgb_b=0;
    
    if(format == Format.Bayer){
      for(int i = 0; i < pixels.length; i++){
        bayer = frame_buffer[i] & 0xFF;
        pixels[i] = 0xFF000000 | (bayer<<16) | (bayer<<8) | bayer;
      }
    }
    
    if(format == Format.RGB || format == Format.BGR){
      for(int i = 0, ch = 0; i < pixels.length; i++){
        rgb_r = frame_buffer[ch++] & 0xFF;
        rgb_g = frame_buffer[ch++] & 0xFF;
        rgb_b = frame_buffer[ch++] & 0xFF;
        pixels[i] = 0xFF000000 | (rgb_r<<16) | (rgb_g<<8) | rgb_b;
      }
    }
  }
  
  
  
  
  
  
  
  
  
}
