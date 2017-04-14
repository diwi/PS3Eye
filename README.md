# PS3Eye
A Java/Processing Library for the PS3Eye USB-Camera.

The library-core is mostly a Java-port of the [PS3EYEDriver](https://github.com/inspirit/PS3EYEDriver) project.

The library initially developed by [Thomas Diewald](https://github.com/diwi/PS3Eye).

This fork adds a simpler API for using it with processing. Currently supported are frame rates up to 187 FPS with QVGA and 75 FPS with VGA resolution.

![PS3Eye Header](http://thomasdiewald.com/processing/libraries/PS3Eye/PS3Eye_capture.jpg)

## Controls
There are a lot of controls to be set. Here are the range and limits of the values:

| Control            	| Default 	| Min   	| Max  	|
|--------------------	|---------	|-------	|------	|
| Gain               	|    20   	|   0   	|  63  	|
| Exposure           	|   120   	|   0   	|  255 	|
| Sharpness          	|    0    	|   0   	|  63  	|
| Hue                	|   143   	|   0   	|  255 	|
| Brightness         	|    20   	|   0   	|  255 	|
| Contrast           	|    37   	|   0   	|  255 	|
| Blue Balance       	|   128   	|   0   	|  255 	|
| Red Balance        	|   128   	|   0   	|  255 	|
| Green Balance      	|   128   	|   0   	|  255 	|
| Auto Gain          	|  FALSE  	| FALSE 	| TRUE 	|
| Auto White Balance 	|  FALSE  	| FALSE 	| TRUE 	|
| Flip Horizontal    	|  FALSE  	| FALSE 	| TRUE 	|
| Flip Vertical      	|  FALSE  	| FALSE 	| TRUE 	|

## Processing Example
This is an extended example for processing. It uses 120 FPS with the QVGA resolution.

```java
import com.thomasdiewald.ps3eye.PS3Eye;
import com.thomasdiewald.ps3eye.PS3EyeCamera;

PS3EyeCamera cam;

public void settings(){
    size(640, 480);
    smooth(0);
}

public void setup(){
    frameRate(120);

    // create a camera
    cam = PS3EyeCamera.getDevice(this, 0, 120, PS3Eye.Format.RGB, PS3Eye.Resolution.QVGA);
    cam.start();

    // set AWB and auto gain
    cam.getPS3Eye().setAutoWhiteBalance(true);
    cam.getPS3Eye().setAutogain(true);
}

public void draw(){
    // display frame
    image(cam.getFrame(), 0, 0);
}
```

<br>

## Installation, PS3Eye Driver

[usb4java](http://usb4java.org/) (based on [libusb](http://libusb.info/)) is used to access PS3Eye USB-device.

So, to use the PS3Eye-Library you need to install the corresponding PS3Eye driver for your OS.

[How_to_use_libusb_on_Windows](https://github.com/libusb/libusb/wiki/Windows#How_to_use_libusb_on_Windows)

#### How i did it (Windows 10, x64)
1) plugin PS3Eye-USB-Camera
2) open the Device Manager and open [Zadig](http://zadig.akeo.ie/)
3) make sure no driver is installed (uninstall if necessary) and then use Zadig to install **libus-win32**

![PS3Eye_libusb_driver_install](http://thomasdiewald.com/processing/libraries/PS3Eye/PS3Eye_libusb_driver_install.jpg)

