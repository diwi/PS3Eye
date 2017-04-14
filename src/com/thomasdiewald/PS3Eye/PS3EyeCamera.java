package com.thomasdiewald.ps3eye;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * PS3 Eye Camera
 *
 * Implemented by Florian Bruggisser (https://github.com/cansik)
 */
public class PS3EyeCamera implements PConstants {
    private static int DEFAULT_CAMERA = 0;
    private static int DEFAULT_FRAMERATE = 60;
    private static PS3Eye.Format DEFAULT_FORMAT = PS3Eye.Format.RGB;
    private static PS3Eye.Resolution DEFAULT_RESOLUTION = PS3Eye.Resolution.VGA;

    private PApplet parent;
    private PS3Eye cam;
    private PImage frame;
    private boolean autoRead = true;
    private boolean running = false;

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye Camera reference.
     * @param framerate Frame rate of the PS3 Eye camera (60 or 120).
     * @param format Image format of the PS3 Eye camera.
     * @param resolution Resolution of the PS3 Eye camera (VGA or QVGA).
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye, int framerate, PS3Eye.Format format, PS3Eye.Resolution resolution) {
        this.parent = parent;

        cam = ps3Eye;
        cam.init(framerate, format, resolution);

        frame = parent.createImage(cam.getFrameWidth(), cam.getFrameHeight(), ARGB);

        parent.registerMethod("dispose", this);
        parent.registerMethod("pre", this);
    }

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye camera reference.
     * @param framerate Frame rate of the PS3 Eye camera (60 or 120).
     * @param format Image format of the PS3 Eye camera.
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye, int framerate, PS3Eye.Format format)
    {
        this(parent, ps3Eye, framerate, format, DEFAULT_RESOLUTION);
    }

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye camera reference.
     * @param framerate Frame rate of the PS3 Eye camera (60 or 120).
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye, int framerate)
    {
        this(parent, ps3Eye, framerate, DEFAULT_FORMAT);
    }

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye camera reference.
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye)
    {
        this(parent, ps3Eye, DEFAULT_FRAMERATE);
    }

    /**
     * Disposes the camera and cleans up the cache.
     */
    public void dispose() {
        parent.g.removeCache(frame);
        cam.release();
        PS3Eye.usb.release();
    }

    /**
     * Start the camera.
     */
    public void start()
    {
        cam.start();
        running = true;
    }

    /**
     * Stop the camera.
     */
    public void stop()
    {
        cam.stop();
        running = false;
    }

    /**
     * Read the next frame from the camera.
     */
    public void readFrame() {
        // transfer pixel data
        frame.loadPixels();
        cam.getFrame(frame.pixels);
        frame.updatePixels();
    }

    public void pre()
    {
        if(autoRead && running)
            readFrame();
    }

    /**
     * Get the native ps3 eye camera reference.
     * @return Returns the native ps3 eye camera reference.
     */
    public PS3Eye getPS3Eye() {
        return cam;
    }

    /**
     * Get the current frame.
     * @return Returns the current frame.
     */
    public PImage getFrame() {
        return frame;
    }

    /**
     * Returns true, if auto frame reading is on.
     * @return Returns if auto read is on.
     */
    public boolean isAutoRead() {
        return autoRead;
    }

    /**
     * Set to enable auto frame reading.
     * @param autoRead If true, auto frame reading is on.
     */
    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

    /**
     * Returns true, if the camera is running.
     * @return Value that indicates if the camera is running.
     */
    public boolean isRunning() {
        return running;
    }

    public static PS3EyeCamera getDevice(PApplet parent){
        return getDevice(parent, DEFAULT_CAMERA);
    }

    public static PS3EyeCamera getDevice(PApplet parent, int index){
        return getDevice(parent, index, DEFAULT_FRAMERATE, DEFAULT_FORMAT, DEFAULT_RESOLUTION);
    }

    public static PS3EyeCamera getDevice(PApplet parent, int index, int framerate, PS3Eye.Format format, PS3Eye.Resolution resolution){
        PS3Eye camera = PS3Eye.getDevice(index);

        // check if camera is available
        if(camera == null)
            return null;

        return new PS3EyeCamera(parent, camera, framerate, format, resolution);
    }
}
