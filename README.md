![PS3Eye Header](http://thomasdiewald.com/processing/libraries/PS3Eye/PS3Eye_header.jpg)

# PS3Eye
A Java/Processing Library for the PS3Eye USB-Camera.

The library-core is mostly a Java-port of the [PS3EYEDriver](https://github.com/inspirit/PS3EYEDriver) project.


![PS3Eye Header](http://thomasdiewald.com/processing/libraries/PS3Eye/PS3Eye_capture.jpg)


<br>

## Download
+ [Releases](https://github.com/diwi/PS3Eye/releases)
+ [PS3Eye Website](http://thomasdiewald.com/processing/libraries/PS3Eye/)
+ Processing IDE -> Library Manager

JavaDoc: http://thomasdiewald.com/processing/libraries/PS3Eye/reference/index.html

## Installation, Processing IDE

- Download [Processing 3](https://processing.org/download/?processing)
- Install PS3Eye via the Library Manager.
- Or manually, unzip and put the extracted PS3Eye folder into the libraries folder of your Processing sketches. Reference and examples are included in the PS3Eye folder. 

#### Platforms
Windows, Linux, MacOSX

<br>

## Installation, PS3Eye Driver

[usb4java](http://usb4java.org/) (based on [libusb](http://libusb.info/)) is used to access PS3Eye USB-device.

So, to use the PS3Eye-Library you need to install the corresponding PS3Eye driver for your OS.

[How_to_use_libusb_on_Windows](https://github.com/libusb/libusb/wiki/Windows#How_to_use_libusb_on_Windows)

#### How i did it (Windows 10, x64)
1) plugin PS3Eye-USB-Camera
2) open the Device Manager and open [Zadig](http://zadig.akeo.ie/)
3) make sure no driver is installed (de-install if necessary) and then use Zadig to install **libus-win32**

![PS3Eye_libusb_driver_install](http://thomasdiewald.com/processing/libraries/PS3Eye/PS3Eye_libusb_driver_install.jpg)

