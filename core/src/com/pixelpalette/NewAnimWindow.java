package com.pixelpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelpalette.PixelApp.MANAGER;
import static com.pixelpalette.PixelApp.pixelSize;

public class NewAnimWindow extends Actor {
    private Sprite spr;
    private com.pixelpalette.TextPrinter textPrinter;
    private String windowName;
    protected String animName;
    private boolean newFrame;

    public NewAnimWindow(final DrawStage parent, final AnimProcessor processor, String name, final boolean newFrame) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        textPrinter = new com.pixelpalette.TextPrinter(true);
        animName = "";
        this.windowName = name;
        this.newFrame = newFrame;

        setSize(149 * pixelSize, 75 * pixelSize);
        setPosition((com.pixelpalette.PixelApp.width - getWidth()) / 2f, (com.pixelpalette.PixelApp.height - getHeight()) / 2f);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(y > 59 * pixelSize && x < 146 * pixelSize && x > 130 * pixelSize) {
                    remove();
                }
                if(x > 9 * pixelSize && x < 140 * pixelSize && y > 27 * pixelSize && y < 41 * pixelSize) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            animName = text;
                        }

                        @Override
                        public void canceled() {

                        }
                    }, "Set Anim Name", animName, "");
                }
                if(y < 16 * pixelSize) {
                    if(newFrame) {
                        if(x > 16 * pixelSize && x < 77 * pixelSize) {
                            newFrame(processor);
                        } else if(x > 80 * pixelSize && x < 146 * pixelSize) {
                            copyFrame(processor);
                        }
                    } else {
                        addAnim(processor);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(426, 200, 149, 75);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);

        if(!newFrame) {
            spr.setRegion(426, 276, 149, 17);
            spr.setSize(getWidth(), 17 * pixelSize);
            spr.setPosition(getX(), getY());
            spr.draw(batch, parentAlpha);
        }

        textPrinter.setColor(Color.WHITE);
        textPrinter.drawText(getX() + 4 * pixelSize, getY() + 62 * pixelSize, windowName, batch);
        textPrinter.setColor(Color.BLACK);
        textPrinter.drawText(getX() + 11 * pixelSize, getY() + 29 * pixelSize, animName, batch);
    }

    private void newFrame(AnimProcessor processor) {
        processor.addFrame();
        addAnim(processor);
    }

    private void copyFrame(AnimProcessor processor) {
        processor.copyFrame(processor.getFramePos());
        addAnim(processor);
    }

    protected void addAnim(AnimProcessor processor) {
        processor.addAnim(animName);
        remove();
    }
}
