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



package com.thomasdiewald.PS3Eye;

import java.nio.ByteBuffer;

import org.usb4java.ConfigDescriptor;
import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.EndpointDescriptor;
import org.usb4java.Interface;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;


public class URBDesc {

  // Values for bmHeaderInfo (Video and Still Image Payload Headers, 2.4.3.3)
  static final private int UVC_STREAM_EOH = (1 << 7);
  static final private int UVC_STREAM_ERR = (1 << 6);
  static final private int UVC_STREAM_STI = (1 << 5);
  static final private int UVC_STREAM_RES = (1 << 4);
  static final private int UVC_STREAM_SCR = (1 << 3);
  static final private int UVC_STREAM_PTS = (1 << 2);
  static final private int UVC_STREAM_EOF = (1 << 1);
  static final private int UVC_STREAM_FID = (1 << 0);

  static final private int TRANSFER_SIZE = 65536;
  static final private int NUM_TRANSFERS = 5;

  // packet types when moving from iso buf to frame buf
  static private enum gspca_packet_type {
    DISCARD_PACKET, FIRST_PACKET, INTER_PACKET, LAST_PACKET
  };

  private int num_active_transfers = 0;

  private gspca_packet_type last_packet_type = gspca_packet_type.DISCARD_PACKET;
  private int last_pts = 0;
  private int last_fid = 0;
  private Transfer[] xfr = new Transfer[NUM_TRANSFERS]; // NULL

  private int cur_frame_start = 0;
  private int cur_frame_data_len = 0;
  private int frame_size = 0;
  
  protected FrameQueue frame_queue = null;
  
  public URBDesc() {
  }

  protected void release() {
    close_transfers();
  }

  //
  // look for an input transfer endpoint in an alternate setting
  // libusb_endpoint_descriptor
  //
  static protected byte find_ep(Device device) {

    byte ep_addr = 0;

    ConfigDescriptor config = new ConfigDescriptor();
    int rval = LibUsb.getActiveConfigDescriptor(device, config);
    if (rval != 0) {
      throw new LibUsbException("error LibUsb.getActiveConfigDescriptor", rval);
    }

    InterfaceDescriptor altsetting = null;

    for (int i = 0; i < config.bNumInterfaces(); i++) {
      Interface[] iface = config.iface();
      altsetting = iface[i].altsetting()[0];
      if (altsetting.bInterfaceNumber() == 0) {
        break;
      }
    }

    EndpointDescriptor ep;
    for (int i = 0; i < altsetting.bNumEndpoints(); i++) {
      ep = altsetting.endpoint()[i];
      if ((ep.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) == LibUsb.TRANSFER_TYPE_BULK && ep.wMaxPacketSize() != 0) {
        ep_addr = ep.bEndpointAddress();
        break;
      }
    }
    LibUsb.freeConfigDescriptor(config);

    return ep_addr;
  }


  private TransferCallback transfer_completed_callback = new TransferCallback() {
    @Override
    public void processTransfer(Transfer xfr) {
      TranferUserData userdata = (TranferUserData) xfr.userData();
      URBDesc urb = userdata.parent;
      
      int status = xfr.status();
      if (status != LibUsb.TRANSFER_COMPLETED) {
        if (status == LibUsb.TRANSFER_CANCELLED) {
          LibUsb.freeTransfer(xfr);
          urb.transfer_canceled(); 
        } else {
          urb.close_transfers();
        }
        return;
      } 

      urb.pkt_scan(xfr);

      if (LibUsb.submitTransfer(xfr) != 0) {
        System.out.printf("error re-submitting URB\n");
        urb.close_transfers();
      }
      
    }
  };


  protected void close_transfers() {
 
    synchronized(this) {
      try {
  
        if (num_active_transfers == 0){
          return;
        }

        // Cancel any pending transfers
        for (int index = 0; index < NUM_TRANSFERS; ++index) {
          try{
            if(xfr[index].getPointer() != 0){
              int rval = LibUsb.cancelTransfer(xfr[index]);
              if(rval != 0){
                System.out.println("error close_transfers");
              }
            } 
            xfr[index] = null;
          } catch(Exception e){
//            e.printStackTrace();
            // getting a IllegalStateException sometimes, dunno why
          }
        }

        while(num_active_transfers != 0){
          this.wait();
        }
   
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    PS3Eye.usb.cameraStopped();
    frame_queue = null;
  }
  
  
  private void transfer_canceled() {
    synchronized(this) 
    {
      --num_active_transfers;
      this.notify();
    }
  }
  
  
  static class TranferUserData{
    URBDesc parent;
    int idx;
    int flag = 0;
    TranferUserData(URBDesc parent, int idx){
      this.parent = parent;
      this.idx = idx;
    }
  }
  
  
  protected boolean start_transfers(DeviceHandle handle, int curr_frame_size) {
    // Initialize the frame queue
    frame_size = curr_frame_size;
    frame_queue = new FrameQueue(frame_size);

    // Initialize the current frame pointer to the start of the buffer; it will
    // be updated as frames are completed and pushed onto the frame queue
    cur_frame_start = frame_queue.GetFrameBufferStart();
    cur_frame_data_len = 0;

    // Find the bulk transfer endpoint
    byte bulk_endpoint = find_ep(LibUsb.getDevice(handle));
    LibUsb.clearHalt(handle, bulk_endpoint);

    int res = 0;
    for (int index = 0; index < NUM_TRANSFERS; ++index) {
      // Create & submit the transfer
      TranferUserData user_data = new TranferUserData(this, index);
      xfr[index] = LibUsb.allocTransfer(0);
      
      // Java GC takes care of memory freeing
      ByteBuffer transfer_buffer = ByteBuffer.allocateDirect(TRANSFER_SIZE);
      
      LibUsb.fillBulkTransfer(xfr[index], handle, bulk_endpoint, transfer_buffer, transfer_completed_callback, user_data, 0);
      res |= LibUsb.submitTransfer(xfr[index]);
      
      num_active_transfers++;
    }

    last_pts = 0;
    last_fid = 0;

    PS3Eye.usb.cameraStarted();

    return res == 0;
  }
  


  private void frame_add(gspca_packet_type packet_type, byte[] src, int src_ptr, int len) {
    if (packet_type == gspca_packet_type.FIRST_PACKET) {
      cur_frame_data_len = 0;
    } else {
      switch (last_packet_type) {
      case DISCARD_PACKET:
        if (packet_type == gspca_packet_type.LAST_PACKET) {
          last_packet_type = packet_type;
          cur_frame_data_len = 0;
        }
        return;
      case LAST_PACKET:
        return;
      default:
        break;
      }
    }
    
    // append the packet to the frame buffer
    if (len > 0) {
      if (cur_frame_data_len + len > frame_size) {
        packet_type = gspca_packet_type.DISCARD_PACKET;
        cur_frame_data_len = 0;
      } else {
        byte[] dst = frame_queue.frame_buffer;
        int dst_ptr = cur_frame_start + cur_frame_data_len;
//        try {
////          Thread.sleep(2);
//          wait(2);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
        
//        for(int i = 0; i < 10000; i++)
        System.arraycopy(src, src_ptr, dst, dst_ptr, len);
        cur_frame_data_len += len;
      }
    }

    last_packet_type = packet_type;

    if (packet_type == gspca_packet_type.LAST_PACKET) {
      cur_frame_data_len = 0;
      cur_frame_start = frame_queue.Enqueue();
//      System.out.printf("URBDesc.frame_add frame completed %d\n", cur_frame_start);
    }
  }
  
  
  
  
  
  private final byte[][] transfer_buffer_tmp = new byte[NUM_TRANSFERS][TRANSFER_SIZE];
  
  
  private void pkt_scan(Transfer xfr) {
    
    ByteBuffer buffer = xfr.buffer(); 
    int len = xfr.actualLength();
    
    TranferUserData userdata = (TranferUserData) xfr.userData();
    
    // get data from bytebuffer to local array
    byte[] data = transfer_buffer_tmp[userdata.idx];
    buffer.get(data);
    buffer.rewind();
    
    
    final int payload_len = 2048; // bulk type
    int this_pts = 0;
    int this_fid = 0;
    int remaining_len = len;
    int ptr = 0;
    
    do {
      
      len = Math.min(remaining_len, payload_len);

      SCAN_NEXT: 
      {

        DISCARD: 
        {

          // Payloads are prefixed with a UVC-style header. We consider a frame
          // to start when the FID toggles, or the PTS changes.
          // A frame ends when EOF is set, and we've received the correct number
          // of bytes.

          // Verify UVC header. Header length is always 12
          if ((data[ptr+0]&0xFF) != 12 || len < 12) {
//            System.out.printf("URBDesc.pkt_scan: bad header\n");
            break DISCARD;
          }

          // Check errors
          if (((data[ptr+1]&0xFF) & UVC_STREAM_ERR) != 0) {
//            System.out.printf("URBDesc.pkt_scan: payload error\n");
            break DISCARD;
          }

          // Extract PTS and FID
          if (((data[ptr+1]&0xFF) & UVC_STREAM_PTS) == 0) {
//            System.out.printf("URBDesc.pkt_scan: PTS not present\n");
            break DISCARD;
          }

          this_pts = ((data[ptr+5]&0xFF) << 24) | ((data[ptr+4]&0xFF) << 16) | ((data[ptr+3]&0xFF) << 8) | (data[ptr+2]&0xFF);
          this_fid = ((data[ptr+1]&0xFF) & UVC_STREAM_FID) != 0 ? 1 : 0;

          // If PTS or FID has changed, start a new frame.
          if (this_pts != last_pts || this_fid != last_fid) {
            if (last_packet_type == gspca_packet_type.INTER_PACKET) {
              // The last frame was incomplete, so don't keep it or we will glitch
              frame_add(gspca_packet_type.DISCARD_PACKET, null, 0, 0);
            }
            last_pts = this_pts;
            last_fid = this_fid;
            frame_add(gspca_packet_type.FIRST_PACKET, data, ptr + 12, len - 12);
          } else if (((data[ptr+1]&0xFF) & UVC_STREAM_EOF) != 0) { // If this packet is marked as EOF, end the frame
            last_pts = 0;
            if (cur_frame_data_len + len - 12 != frame_size) {
              break DISCARD;
            }
            frame_add(gspca_packet_type.LAST_PACKET, data, ptr + 12, len - 12);
          } else {
            // Add the data from this payload
            frame_add(gspca_packet_type.INTER_PACKET, data, ptr + 12, len - 12);
          }

          break SCAN_NEXT;

        } // END DISCARD

        frame_add(gspca_packet_type.DISCARD_PACKET, null, 0, 0);

      } // END SCAN_NEXT

      remaining_len -= len;
      ptr += len;
    } while (remaining_len > 0);
    
  }
  
  

}
