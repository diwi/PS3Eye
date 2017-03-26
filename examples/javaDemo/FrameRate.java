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


package javaDemo;

import java.util.Locale;

public class FrameRate {
  private long count;
  private float framerate = 0;
  float smooth = 0.95f; // [0,1]

  private int num_timers = 30;
  private long[] timer_history = new long[num_timers];
  private int timer_idx = 0;
  

  public FrameRate(){
  }
  
  public float fps(){
    return framerate;
  }
  public long counter(){
    return count;
  }
  
  public FrameRate update() {
    int idx_cur = timer_idx%num_timers;
    int idx_old = (timer_idx+num_timers+1)%num_timers;

    timer_history[idx_cur] = System.nanoTime();
    timer_idx++;
    count++;

    long duration = timer_history[idx_cur] - timer_history[idx_old];
    
    float framerate_cur = num_timers/(duration/1E09f);
    framerate =  framerate * smooth + framerate_cur*(1.0f-smooth);

    return this;
  }
  
  @Override
  public String toString(){
    return String.format(Locale.ENGLISH, "%5.2f", framerate);
  }

}
