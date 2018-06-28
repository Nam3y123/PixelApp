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

public class WidthHeightWindow extends Actor {
    private Sprite spr;
    private com.pixelpalette.TextPrinter textPrinter;
    protected int width, height;
    private String name;

    public WidthHeightWindow(final DrawStage parent, int initWidth, int initHeight, String name) {
        spr = new Sprite((Texture) PixelApp.MANAGER.get("GUI.png"));
        textPrinter = new com.pixelpalette.TextPrinter(true);
        width = initWidth;
        height = initHeight;
        this.name = name;

        setSize(104 * PixelApp.pixelSize, 75 * PixelApp.pixelSize);
        setPosition((PixelApp.width - getWidth()) / 2f, (PixelApp.height - getHeight()) / 2f);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(y > 59 * PixelApp.pixelSize && x < 101 * PixelApp.pixelSize && x > 85 * PixelApp.pixelSize) {
                    remove();
                }
                if(x > 55 * PixelApp.pixelSize && x < 84 * PixelApp.pixelSize) {
                    if(y > 23 * PixelApp.pixelSize && y < 34 * PixelApp.pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        height = Integer.valueOf(text);
                                    } else {
                                        height = 9999;
                                    }
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set New Height", Integer.toString(height), "");
                    }

                    if(y > 41 * PixelApp.pixelSize && y < 52 * PixelApp.pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        width = Integer.valueOf(text);
                                    } else {
                                        width = 9999;
                                    }
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set New Width", Integer.toString(width), "");
                    }
                }
                if(y < 16 * PixelApp.pixelSize && x < 101 * PixelApp.pixelSize && x > 58 * PixelApp.pixelSize) {
                    accept(parent);
                }
                return true;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(321, 200, 104, 75);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);

        textPrinter.setColor(Color.WHITE);
        textPrinter.drawText(getX() + 4 * PixelApp.pixelSize, getY() + 62 * PixelApp.pixelSize, name, batch);
        textPrinter.setColor(Color.BLACK);
        textPrinter.drawText(getX() + 57 * PixelApp.pixelSize, getY() + 42 * PixelApp.pixelSize, Integer.toString(width), batch);
        textPrinter.drawText(getX() + 57 * PixelApp.pixelSize, getY() + 24 * PixelApp.pixelSize, Integer.toString(height), batch);
    }

    protected void accept(DrawStage parent) {

    }
}
