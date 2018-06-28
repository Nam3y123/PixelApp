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

public class ResizeCanvasWindow extends Actor {
    private Sprite spr;
    private com.pixelpalette.TextPrinter textPrinter;
    protected int up, down, left, right;
    private int width, height;
    private int initWidth, initHeight;
    private String name;

    public ResizeCanvasWindow(final com.pixelpalette.DrawStage parent, String name) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        textPrinter = new com.pixelpalette.TextPrinter(true);
        up = 0;
        down = 0;
        left = 0;
        right = 0;
        initWidth = parent.getCanvas().getImgW();
        initHeight = parent.getCanvas().getImgH();
        width = initWidth;
        height = initHeight;
        this.name = name;

        setSize(175 * pixelSize, 111 * pixelSize);
        setPosition((com.pixelpalette.PixelApp.width - getWidth()) / 2f, (com.pixelpalette.PixelApp.height - getHeight()) / 2f);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(y > 95 * pixelSize && x < 172 * pixelSize && x > 156 * pixelSize) {
                    remove();
                }
                if(x > 49 * pixelSize && x < 78 * pixelSize) {
                    if(y > 23 * pixelSize && y < 34 * pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        right = Integer.valueOf(text);
                                    } else {
                                        right = 9999;
                                    }
                                    updateSizes();
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set Right Padding", Integer.toString(right), "");
                    }

                    if(y > 41 * pixelSize && y < 52 * pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        left = Integer.valueOf(text);
                                    } else {
                                        left = 9999;
                                    }
                                    updateSizes();
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set Left Padding", Integer.toString(left), "");
                    }

                    if(y > 59 * pixelSize && y < 70 * pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        down = Integer.valueOf(text);
                                    } else {
                                        down = 9999;
                                    }
                                    updateSizes();
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set Down Padding", Integer.toString(down), "");
                    }

                    if(y > 77 * pixelSize && y < 88 * pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                try {
                                    int i = Integer.valueOf(text);
                                    if(i < 9999) {
                                        up = Integer.valueOf(text);
                                    } else {
                                        up = 9999;
                                    }
                                    updateSizes();
                                } catch(NumberFormatException e) {

                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Set Up Padding", Integer.toString(up), "");
                    }
                }
                if(y < 16 * pixelSize && x < 172 * pixelSize && x > 129 * pixelSize) {
                    accept(parent);
                }
                return true;
            }
        });
    }

    private void updateSizes() {
        width = left + initWidth + right;
        height = up + initHeight + down;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(398, 348, 175, 111);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);

        textPrinter.setColor(Color.WHITE);
        textPrinter.drawText(getX() + 4 * pixelSize, getY() + 98 * pixelSize, name, batch);
        textPrinter.drawText(getX() + 128 * pixelSize, getY() + 69 * pixelSize, Integer.toString(height), batch);
        textPrinter.drawText(getX() + 128 * pixelSize, getY() + 33 * pixelSize, Integer.toString(width), batch);
        textPrinter.setColor(Color.BLACK);
        float x = getX() + 51 * pixelSize;
        textPrinter.drawText(x, getY() + 78 * pixelSize, Integer.toString(up), batch);
        textPrinter.drawText(x, getY() + 60 * pixelSize, Integer.toString(down), batch);
        textPrinter.drawText(x, getY() + 42 * pixelSize, Integer.toString(left), batch);
        textPrinter.drawText(x, getY() + 24 * pixelSize, Integer.toString(right), batch);
    }

    protected void accept(com.pixelpalette.DrawStage parent) {

    }
}
