package com.pixelapp;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelapp.Canvas.toolLen;
import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class ToolSelector extends Actor {
    private Sprite spr;
    private DrawStage parent;
    private float scroll;

    public ToolSelector(final DrawStage parent) {
        spr = new Sprite((Texture)MANAGER.get("GUI.png"));
        this.parent = parent;
        scroll = 0;
        addCaptureListener(new ClickListener() {
            private float oldY;
            private boolean yChanged;
            private float storeScroll;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!parent.isDrawing()) {
                    oldY = y;
                    storeScroll = scroll;
                    yChanged = false;
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(!parent.isDrawing()) {
                    scroll += y - oldY;
                    oldY = y;
                    if(Math.abs(storeScroll - scroll) > pixelSize) {
                        yChanged = true;
                    }
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(!parent.isDrawing() && !yChanged) {
                    int curTool = (int)((getHeight() - y + scroll) / pixelSize) / 18 + 1;
                    if(curTool <= toolLen) {
                        ToolSelector.this.parent.getProcessor().setCurTool(curTool);
                    }
                }
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float maxSize = 18 * pixelSize * (parent.getCanvas().tools.length - 1) - (PixelApp.height - 18 * pixelSize - parent.getColorPicker().getHeight());
        if(scroll > maxSize) {
            scroll = maxSize;
        }
        if(scroll < 0) {
            scroll = 0;
        }

        spr.setPosition(0, 0);
        spr.setRegion(0, 149, 18, 18);
        spr.setSize(18 * pixelSize, PixelApp.height - 18 * pixelSize);
        setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
        spr.draw(batch, parentAlpha);

        int curTool = parent.getProcessor().getCurTool();
        for(int i = 1; i < toolLen + 1; i++) { // Starts at 1 because the default tool is unselectable
            spr.setRegion(curTool == i ? 19 : 0, 149 + 18 * i, 18, 18);
            spr.setPosition(0, getHeight() - (18 * pixelSize ) * i + scroll);
            spr.setSize(18 * pixelSize, 18 * pixelSize);
            spr.draw(batch, parentAlpha);
        }
    }
}