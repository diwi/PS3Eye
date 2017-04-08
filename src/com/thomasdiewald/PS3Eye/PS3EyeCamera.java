package com.thomasdiewald.PS3Eye;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * PS3 Eye Camera
 *
 * Implemented by Florian Bruggisser (https://github.com/cansik)
 */
public class PS3EyeCamera implements PConstants {
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
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye, int framerate, PS3Eye.Format format) {
        this.parent = parent;

        cam = ps3Eye;
        cam.init(framerate, format);

        frame = parent.createImage(cam.getFrameWidth(), cam.getFrameHeight(), ARGB);

        parent.registerMethod("dispose", this);
        parent.registerMethod("pre", this);
    }

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye camera reference.
     * @param framerate Frame rate of the PS3 Eye camera (60 or 120).
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye, int framerate)
    {
        this(parent, ps3Eye, framerate, PS3Eye.Format.RGB);
    }

    /**
     * Create a new PS3 Eye camera.
     * @param parent Parent Processing sketch.
     * @param ps3Eye PS3 Eye camera reference.
     */
    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye)
    {
        this(parent, ps3Eye, 60);
    }

    /**
     * Disposes the camera and cleans up the cache.
     */
    public void dispose() {
        parent.g.removeCache(frame);
        cam.dispose();
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
}
