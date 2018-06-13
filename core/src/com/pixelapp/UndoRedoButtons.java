package com.pixelapp;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class UndoRedoButtons extends Actor {
    private Sprite spr;
    private DrawStage parent;

    public UndoRedoButtons(DrawStage parent) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width, height);
            }

            @Override
            public void setSize(float width, float height) {
                super.setSize(width * pixelSize, height * pixelSize);
            }
        };
        this.parent = parent;
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = false;
                if(!UndoRedoButtons.this.parent.isDrawing()) {
                    ret = true;
                    if(y > 1 * pixelSize && y < 17 * pixelSize) {
                        if(x < 16 * pixelSize) {
                            UndoRedoButtons.this.parent.getProcessor().undoAction();
                        } else if (x > 18 * pixelSize && x < 34 * pixelSize) {
                            UndoRedoButtons.this.parent.getProcessor().redoAction();
                        }
                    }
                }
                return ret;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(178, 52, 35, 18);
        spr.setPosition(18 * pixelSize, parent.getColorPicker().getHeight() - 1 * pixelSize);
        setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
        spr.draw(batch, parentAlpha);
    }
}
