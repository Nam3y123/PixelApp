package com.pixelapp;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.pixelapp.PixelApp.MANAGER;

public class SideMenu extends Actor {
    protected Sprite spr;
    protected boolean minimized;
    protected DrawStage parent;

    public SideMenu(final DrawStage parent) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        this.parent = parent;
        minimize();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(hidden()) { // If something is open that is not this menu
            setX(PixelApp.width);
        } else {
            setX(PixelApp.width - getWidth());
            if(minimized) {
                drawMinimized(batch, parentAlpha);
            } else {
                drawMaximized(batch, parentAlpha);
            }
        }
    }

    protected void drawMinimized(Batch batch, float parentAlpha) {

    }

    protected void drawMaximized(Batch batch, float parentAlpha) {

    }

    public boolean isMinimized() {
        return minimized;
    }

    public void minimize() {
        minimized = true;
    }

    public void maximize() {
        minimized = false;
    }

    protected boolean hidden() {
        return !parent.rightMinimized() && minimized;
    }
}
