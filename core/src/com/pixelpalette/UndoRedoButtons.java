package com.pixelpalette;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class UndoRedoButtons extends Actor {
    private Sprite spr;
    private DrawStage parent;

    public UndoRedoButtons(DrawStage parent) {
        spr = new Sprite((Texture) PixelApp.MANAGER.get("GUI.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width, height);
            }

            @Override
            public void setSize(float width, float height) {
                super.setSize(width * PixelApp.pixelSize, height * PixelApp.pixelSize);
            }
        };
        this.parent = parent;
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = false;
                if(!UndoRedoButtons.this.parent.isDrawing()) {
                    ret = true;
                    if(y > 1 * PixelApp.pixelSize && y < 17 * PixelApp.pixelSize) {
                        if(x < 16 * PixelApp.pixelSize) {
                            UndoRedoButtons.this.parent.getProcessor().undoAction();
                        } else if (x > 18 * PixelApp.pixelSize && x < 34 * PixelApp.pixelSize) {
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
        spr.setPosition(18 * PixelApp.pixelSize, parent.getColorPicker().getHeight() - 1 * PixelApp.pixelSize);
        setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
        spr.draw(batch, parentAlpha);
    }
}
