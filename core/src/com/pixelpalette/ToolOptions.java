package com.pixelpalette;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelpalette.PixelApp.MANAGER;
import static com.pixelpalette.PixelApp.pixelSize;

public class ToolOptions extends Actor {
    protected Sprite spr;

    public ToolOptions() {
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

            @Override
            public void setPosition(float x, float y) {
                super.setPosition(x * pixelSize,  (com.pixelpalette.PixelApp.height - 18 * pixelSize) + y * pixelSize);
            }
        };
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = ToolOptions.this.touchDown(x, y);
                return ret;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                ToolOptions.this.touchDragged(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                ToolOptions.this.touchUp(x, y);
            }
        });
        setVisible(false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setPosition(0, com.pixelpalette.PixelApp.height - getHeight());

        spr.setPosition(0, (int)(getHeight() - (18 * pixelSize)));
        spr.draw(batch, parentAlpha);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width * pixelSize, height * pixelSize);
    }

    protected boolean touchDown(float pointerX, float pointerY) {
        return true;
    }

    protected void touchDragged(float pointerX, float pointerY) {

    }

    protected void touchUp(float pointerX, float pointerY) {

    }

    protected void dispose() {

    }
}
