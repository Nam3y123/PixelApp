package com.pixelapp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class WidthHeightWindow extends Actor {
    private Sprite spr;
    private TextPrinter textPrinter;
    protected int width, height;
    private String name;

    public WidthHeightWindow(final DrawStage parent, int initWidth, int initHeight, String name) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        textPrinter = new TextPrinter(true);
        width = initWidth;
        height = initHeight;
        this.name = name;

        setSize(104 * pixelSize, 75 * pixelSize);
        setPosition((PixelApp.width - getWidth()) / 2f, (PixelApp.height - getHeight()) / 2f);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(y > 59 * pixelSize && x < 101 * pixelSize && x > 85 * pixelSize) {
                    remove();
                }
                if(x > 55 * pixelSize && x < 84 * pixelSize) {
                    if(y > 23 * pixelSize && y < 34 * pixelSize) {
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

                    if(y > 41 * pixelSize && y < 52 * pixelSize) {
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
                if(y < 16 * pixelSize && x < 101 * pixelSize && x > 58 * pixelSize) {
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
        textPrinter.drawText(getX() + 4 * pixelSize, getY() + 62 * pixelSize, name, batch);
        textPrinter.setColor(Color.BLACK);
        textPrinter.drawText(getX() + 57 * pixelSize, getY() + 42 * pixelSize, Integer.toString(width), batch);
        textPrinter.drawText(getX() + 57 * pixelSize, getY() + 24 * pixelSize, Integer.toString(height), batch);
    }

    protected void accept(DrawStage parent) {

    }
}
