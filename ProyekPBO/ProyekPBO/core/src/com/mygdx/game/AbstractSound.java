package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;

public abstract class AbstractSound {
    protected Sound sound;

    public abstract void playSound();

    public void dispose() {

    }
}
