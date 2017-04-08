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


/**
 * 
 * @author Thomas Diewald
 *
 */
public class FrameQueue {

  protected int        frame_size;
  protected int        num_frames = 2;
  protected byte[]     frame_buffer = new byte[0];
  protected int        frame_buffer_ptr = 0;
  protected int        head = 0;
  protected int        tail = 0;
  protected int        available = 0;

  public FrameQueue(int frame_size){
    this.frame_size = frame_size;
    this.frame_buffer = new byte[frame_size * num_frames];
  }


  protected int GetFrameBufferStart(){
    return frame_buffer_ptr;
  }

  protected int Enqueue(){
//    System.out.println("Enqueue");
    int new_frame_ptr = 0;
    synchronized(this) {
      
      // Unlike traditional producer/consumer, we don't block the producer if the 
      // buffer is full (ie. the consumer is not reading data fast enough).
      // Instead, if the buffer is full, we simply return the current 
      // frame pointer, causing the producer to overwrite the previous frame.
      // This allows performance to degrade gracefully: if the consumer is not 
      // fast enough (< Camera FPS), it will miss frames, but if it is fast 
      // enough (>= Camera FPS), it will see everything.
      //
      // Note that because the the producer is writing directly to the 
      // ring buffer, we can only ever be a maximum of num_frames-1 ahead of the 
      // consumer, otherwise the producer could overwrite the frame the consumer 
      // is currently reading (in case of a slow consumer)
      if (available >= num_frames - 1) {
        return frame_buffer_ptr + head * frame_size;
      }
      
      try {
        while(dequeuing) wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      // Note: we don't need to copy any data to the buffer since the USB packets 
      // are directly written to the frame buffer.
      // We just need to update head and available count to signal to the consumer 
      // that a new frame is available
      head = (head + 1) % num_frames;
      available++;
  
      // Determine the next frame pointer that the producer should write to
      new_frame_ptr = frame_buffer_ptr + head * frame_size;
  
      // Signal consumer that data became available
      notify();
    }
    return new_frame_ptr;
  }
  
  boolean dequeuing = false;
  
  protected boolean isAvailable(){
    return available > 0;
  }
  
  boolean wait_for_frame_to_be_available = true;

  protected void Dequeue(byte[] new_frame, int frame_width, int frame_height, PS3Eye.Format format){   
    
    synchronized(this) {
      
      if(wait_for_frame_to_be_available){
        try {
          while(!isAvailable()) wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      
      dequeuing = true;
      
      // Copy from internal buffer
      int source_ptr = frame_buffer_ptr + frame_size * tail;
  
      if (format == PS3Eye.Format.Bayer){
        System.arraycopy(frame_buffer, source_ptr, new_frame, 0, frame_size);
      }
      else if (format == PS3Eye.Format.BGR ||  format == PS3Eye.Format.RGB){
        Debayer(frame_width, frame_height, source_ptr, new_frame, format == PS3Eye.Format.BGR);
      }

      // Update tail and available count
      tail = (tail + 1) % num_frames;
      available--;
      dequeuing = false;
      
      notify();
   }
  }

  static final private int UB = 0xFF;

  protected void Debayer(int frame_width, int frame_height, int inBayer_ptr, byte[] outBuffer, boolean inBGR){
    // PSMove output is in the following Bayer format (GRBG):
    //
    // G R G R G R
    // B G B G B G
    // G R G R G R
    // B G B G B G
    //
    // This is the normal Bayer pattern shifted left one place.
    
    int num_output_channels = 3;
    int source_stride       = frame_width;
    int source_row          = inBayer_ptr;                            // Start at first bayer pixel
    int dest_stride         = frame_width * num_output_channels;
    int dest_row            = dest_stride + num_output_channels + 1;  // We start outputting at the second pixel of the second row's G component
    int swap_br             = inBGR ? 1 : -1;
    
    byte[] buffer = frame_buffer;
    
   
    
    // Fill rows 1 to height-1 of the destination buffer. First and last row are filled separately (they are copied from the second row and second-to-last rows respectively)
    for (int y = 0; y < frame_height-2; source_row += source_stride, dest_row += dest_stride, ++y){
//      try {
//        if(y%5 == 0){
//          wait(2);
////          Thread.sleep(2);
//        }
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
      int source     = source_row;
      int source_end = source + (source_stride-2); // -2 to deal with the fact that we're starting at the second pixel of the row and should end at the second-to-last pixel of the row (first and last are filled separately)
      int dest       = dest_row;
      
 
      // Row starting with Green
      if (y % 2 == 0){
        // Fill first pixel (green)
        outBuffer[dest - swap_br] = (byte) ((((buffer[source + source_stride    ]&UB) + (buffer[source + source_stride * 1 + 2]&UB) + 1) >> 1)&UB);
        outBuffer[dest]           =            buffer[source + source_stride + 1];
        outBuffer[dest + swap_br] = (byte) ((((buffer[source + 1                ]&UB) + (buffer[source + source_stride * 2 + 1]&UB) + 1) >> 1)&UB);

        source++;
        dest += num_output_channels;

        // Fill remaining pixel
        for (; source <= source_end - 2; source += 2, dest += num_output_channels * 2){
          // Blue pixel
          int cur_pixel  = dest;
          outBuffer[cur_pixel - swap_br] =          buffer[source + source_stride + 1];
          outBuffer[cur_pixel]           = (byte) ( (((buffer[source + 1]&UB) + (buffer[source + source_stride]&UB) + (buffer[source+source_stride + 2]&UB) + (buffer[source+source_stride * 2 + 1]&UB) + 2) >> 2)&UB);
          outBuffer[cur_pixel + swap_br] = (byte) ( (((buffer[source    ]&UB) + (buffer[source + 2            ]&UB) + (buffer[source+source_stride * 2]&UB) + (buffer[source+source_stride * 2 + 2]&UB) + 2) >> 2)&UB);       

          //  Green pixel
          int next_pixel = cur_pixel + num_output_channels;
          outBuffer[next_pixel - swap_br] = (byte) ( (((buffer[source + source_stride + 1]&UB) + (buffer[source+source_stride + 3    ]&UB) + 1) >> 1)&UB);         
          outBuffer[next_pixel]           =          buffer[source + source_stride + 2];
          outBuffer[next_pixel + swap_br] = (byte) ( (((buffer[source + 2                ]&UB) + (buffer[source+source_stride * 2 + 2]&UB) + 1) >> 1)&UB);
        }
      } else {
        for (; source <= source_end - 2; source += 2, dest += num_output_channels * 2) {
          // Red pixel
          int cur_pixel = dest;
          outBuffer[cur_pixel - swap_br] = (byte) ( (((buffer[source + 0]&UB) + (buffer[source + 2            ]&UB) + (buffer[source+source_stride * 2]&UB) + (buffer[source+source_stride * 2 + 2]&UB) + 2) >> 2)&UB);
          outBuffer[cur_pixel]           = (byte) ( (((buffer[source + 1]&UB) + (buffer[source + source_stride]&UB) + (buffer[source+source_stride + 2]&UB) + (buffer[source+source_stride * 2 + 1]&UB) + 2) >> 2)&UB);
          outBuffer[cur_pixel + swap_br] =          buffer[source+source_stride + 1];

          // Green pixel
          int next_pixel = cur_pixel+num_output_channels;
          outBuffer[next_pixel - swap_br] = (byte) ( (((buffer[source + 2                ]&UB) + (buffer[source+source_stride * 2 + 2]&UB) + 1) >> 1)&UB);
          outBuffer[next_pixel]           =          buffer[source + source_stride + 2];
          outBuffer[next_pixel + swap_br] = (byte) ( (((buffer[source + source_stride + 1]&UB) + (buffer[source+source_stride + 3    ]&UB) + 1) >> 1)&UB);
        }
      }

      if (source < source_end){
        outBuffer[dest - swap_br] =          buffer[source + source_stride + 1];
        outBuffer[dest]           = (byte) ( (((buffer[source + 1                ]&UB) + (buffer[source + source_stride]&UB) + (buffer[source + source_stride + 2]&UB) + (buffer[source + source_stride * 2 + 1]&UB) + 2) >> 2)&UB);     
        outBuffer[dest + swap_br] = (byte) ( (((buffer[source + 0                ]&UB) + (buffer[source + 2            ]&UB) + (buffer[source + source_stride * 2]&UB) + (buffer[source + source_stride * 2 + 2]&UB) + 2) >> 2)&UB);    

        source++;
        dest += num_output_channels;
      }

      // Fill first pixel of row (copy second pixel)
      int first_pixel = dest_row - num_output_channels;
      outBuffer[first_pixel - swap_br] = outBuffer[dest_row - swap_br];
      outBuffer[first_pixel]           = outBuffer[dest_row];
      outBuffer[first_pixel + swap_br] = outBuffer[dest_row + swap_br];
    
      // Fill last pixel of row (copy second-to-last pixel). Note: dest row starts at the *second* pixel of the row, so dest_row + (width-2) * num_output_channels puts us at the last pixel of the row
      int last_pixel           = dest_row + (frame_width - 2) * num_output_channels;
      int second_to_last_pixel = last_pixel - num_output_channels;
      
      outBuffer[last_pixel-swap_br] = outBuffer[second_to_last_pixel - swap_br];
      outBuffer[last_pixel]         = outBuffer[second_to_last_pixel];
      outBuffer[last_pixel+swap_br] = outBuffer[second_to_last_pixel + swap_br];
    }

    // Fill first & last row
    for (int i = 0; i < dest_stride; i++){
      outBuffer[i]                                  = outBuffer[i + dest_stride];
      outBuffer[i + (frame_height - 1)*dest_stride] = outBuffer[i + (frame_height - 2)*dest_stride];
    }
  }

}

