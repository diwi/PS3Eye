import com.thomasdiewald.PS3Eye.*;

PS3EyeCamera cam;

public void settings() {
  size(640, 480);
  smooth(0);
}

public void setup() {
  PS3Eye[] ps3eye_list = PS3Eye.getDevices();

  if (ps3eye_list.length == 0) {
    System.out.println("No PS3Eye connected. Good Bye!");
    exit();
    return;
  }

  // pick the first device in the list
  cam = new PS3EyeCamera(this, ps3eye_list[0]);
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