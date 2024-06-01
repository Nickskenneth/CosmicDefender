package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class ExplodingSound extends AbstractSound {
    public ExplodingSound() {
        sound = Gdx.audio.newSound(Gdx.files.internal("explodingSound.wav"));
    }

    @Override
    public void playSound() {
        sound.play();
    }
}
