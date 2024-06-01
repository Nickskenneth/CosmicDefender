package com.mygdx.game;

import com.badlogic.gdx.utils.TimeUtils;

import java.awt.*;

public class Explosion {
    private Rectangle bounds;
    private long startTime;

    public Explosion(Rectangle bounds) {
        this.bounds = bounds;
        this.startTime = TimeUtils.nanoTime();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}