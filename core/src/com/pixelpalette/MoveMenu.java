package com.pixelpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelpalette.PixelApp.MANAGER;
import static com.pixelpalette.PixelApp.pixelSize;

public class MoveMenu extends Actor {
    private com.pixelpalette.DrawStage parent;
    private Sprite spr;
    private boolean loop;

    public MoveMenu(final com.pixelpalette.DrawStage parent) {
        this.parent = parent;
        loop = false;
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        setSize(61 * pixelSize, 38 * pixelSize);
        setPosition(com.pixelpalette.PixelApp.width - getWidth(), 0);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = parent.getCanvas().isMoving();
                if(ret) {
                    if(y > 19 * pixelSize) {
                        if(x < 20 * pixelSize) {
                            parent.getCanvas().finishMove();
                        } else if(x < 39 * pixelSize) {
                            parent.getCanvas().cancelMove();
                        } else {
                            Gdx.input.getTextInput(new Input.TextInputListener() {
                                @Override
                                public void input(String text) {
                                    try {
                                        float widthMultiplier = Float.parseFloat(text);
                                        parent.getCanvas().resizeMoveWidth(widthMultiplier);
                                    } catch (NumberFormatException e) {

                                    }
                                }

                                @Override
                                public void canceled() {

                                }
                            }, "Multiply move left by how much?", "1", "");
                        }
                    } else {
                        if(x < 20 * pixelSize) {
                            loop = !loop;
                        } else if(x < 39 * pixelSize) {
                            Gdx.input.getTextInput(new Input.TextInputListener() {
                                @Override
                                public void input(String text) {
                                    try {
                                        float widthMultiplier = Float.parseFloat(text);
                                        parent.getCanvas().resizeMoveWidth(widthMultiplier);
                                        parent.getCanvas().resizeMoveHeight(widthMultiplier);
                                    } catch (NumberFormatException e) {

                                    }
                                }

                                @Override
                                public void canceled() {

                                }
                            }, "Multiply move size by how much?", "1", "");
                        } else {
                            Gdx.input.getTextInput(new Input.TextInputListener() {
                                @Override
                                public void input(String text) {
                                    try {
                                        float widthMultiplier = Float.parseFloat(text);
                                        parent.getCanvas().resizeMoveHeight(widthMultiplier);
                                    } catch (NumberFormatException e) {

                                    }
                                }

                                @Override
                                public void canceled() {

                                }
                            }, "Multiply move height by how much?", "1", "");
                        }
                    }
                }
                return ret;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(parent.getCanvas().isMoving()) {
            spr.setRegion(336, 276, 61, 38);
            spr.setSize(getWidth(), getHeight());
            spr.setPosition(getX(), getY());
            spr.draw(batch);

            if(loop) {
                spr.setRegion(371, 315, 16, 16);
                spr.setSize(16 * pixelSize, 16 * pixelSize);
                spr.setPosition(getX() + 4 * pixelSize, getY() + 2 * pixelSize);
                spr.draw(batch);
            }
        }
    }

    public boolean looping() {
        return loop;
    }
}
