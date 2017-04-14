import com.thomasdiewald.ps3eye.*;

PS3EyeCamera cam;

public void settings() {
  size(640, 480);
  smooth(0);
}

public void setup() {
  // pick the first device in the list
  cam = PS3EyeCamera.getDevice(this);
  cam.start();

  // set auto gain
  cam.getPS3Eye().setAutogain(true);
}

public void draw() {
  // display frame
  image(cam.getFrame(), 0, 0);

  // info
  String txt_fps = String.format(getClass().getName()+ "  [fps %6.2f]", frameRate);
  surface.setTitle(txt_fps);
}