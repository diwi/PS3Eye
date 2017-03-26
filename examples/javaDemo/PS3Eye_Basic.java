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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.thomasdiewald.PS3Eye.PS3Eye;
import com.thomasdiewald.PS3Eye.PS3Eye.Format;


public class PS3Eye_Basic extends JPanel implements Runnable {
  
  JFrame jframe;
  
  BufferedImage ps3eye_frame;
  PS3Eye ps3eye;
  
  public PS3Eye_Basic() {

    PS3Eye[] devices = PS3Eye.getDevices();
    if (devices.length == 0) {
      System.out.println("No PS3Eye connected. Good Bye!");
      System.exit(0);
    }

    ps3eye = devices[0];
    ps3eye.init(60, Format.RGB);
    ps3eye.start();
    
    int frame_w = ps3eye.getFrameWidth();
    int frame_h = ps3eye.getFrameHeight();

    ps3eye_frame = new BufferedImage(frame_w, frame_h, BufferedImage.TYPE_INT_ARGB);

    setPreferredSize(new Dimension(frame_w, frame_h));
    
    jframe = new JFrame("PS3Eye Capture");
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jframe.setResizable(false);
    jframe.add(this);
    jframe.pack();
    jframe.setVisible(true);
    jframe.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        PS3Eye.disposeAll(); // cleanup, shut-off led
      }
    });
     
    // start capturing
    new Thread(this).start();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(ps3eye_frame, 0, 0, this);
  }


  @Override
  public void run() {
    FrameRate framerate = new FrameRate();
    int[] pixels = ((DataBufferInt) ps3eye_frame.getRaster().getDataBuffer()).getData();
//    ps3eye.waitAvailable(false);
    while (true) {
      
//      if(ps3eye.isAvailable()){
        ps3eye.getFrame(pixels);
        repaint();
        jframe.setTitle(""+framerate.update());
//      }
        
//      try {
//        Thread.sleep(0);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
    }
    
  }
  
  
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new PS3Eye_Basic();
      }
    });
  }

}
