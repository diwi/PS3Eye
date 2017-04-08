package com.thomasdiewald.PS3Eye;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by cansik on 08.04.17.
 */
public class PS3EyeCamera implements PConstants {
    private PApplet parent;
    private PS3Eye cam;
    private PImage frame;
    private boolean autoRead = true;
    private boolean running = false;

    public PS3EyeCamera(PApplet parent, PS3Eye ps3Eye) {
        this.parent = parent;

        cam = ps3Eye;
        frame = parent.createImage(cam.getFrameWidth(), cam.getFrameHeight(), ARGB);

        parent.registerMethod("dispose", this);
        parent.registerMethod("pre", this);

        cam.init();
    }

    public void dispose() {
        parent.g.removeCache(frame);
        cam.dispose();
    }

    public void start()
    {
        cam.start();
        running = true;
    }

    public void stop()
    {
        cam.stop();
        running = false;
    }

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

    public PS3Eye getPS3Eye() {
        return cam;
    }

    public PImage getFrame() {
        return frame;
    }

    public boolean isAutoRead() {
        return autoRead;
    }

    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

    public boolean isRunning() {
        return running;
    }
}
