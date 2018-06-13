package com.pixelapp.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pixelapp.ToolOptions;

import static com.pixelapp.PixelApp.pixelSize;

public class EraserOptions extends ToolOptions {
    private int brush;
    private int alpha;
    private boolean alphaSlider, alphaDrag;

    public EraserOptions() {
        super();
        brush = 1;
        alpha = 255;
        alphaSlider = false;
        alphaDrag = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setSize(alphaSlider ? 118 : 51, alphaSlider ? 22 : 18);
        spr.setRegion(38, 185, 51, 18);
        super.draw(batch, parentAlpha);
        spr.setRegion(90, 192, 14, 4);
        spr.setPosition(35, 7);
        spr.draw(batch, 1 - (alpha / 255f));
        if(alphaSlider) {
            spr.setRegion(105, 198, 100, 5);
            spr.setPosition(18, -4);
            spr.draw(batch, parentAlpha);

            spr.setRegion(48, 32, 5, 5);
            spr.setPosition(18 + (95 * alpha / 255f), -4);
            spr.draw(batch, parentAlpha);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        boolean ret = false;
        if(pointerX > 18 * pixelSize && pointerX < 51 * pixelSize && pointerY > (alphaSlider ? 4 : 0) * pixelSize) {
            ret = true;
            alphaSlider = !alphaSlider;
        }
        if(alphaSlider && pointerX > 18 * pixelSize && pointerY < 4 * pixelSize) {
            ret = true;
            alphaDrag = true;
            touchDragged(pointerX, pointerY);
        }
        return ret;
    }

    @Override
    protected void touchDragged(float pointerX, float pointerY) {
        if(alphaDrag) {
            float newAlpha = pointerX / pixelSize;
            newAlpha -= 18;
            newAlpha *= 2.55f;
            if(newAlpha < 0) {
                newAlpha = 0;
            }
            else if(newAlpha > 255) {
                newAlpha = 255;
            }
            alpha = (int)newAlpha;
        }
    }

    @Override
    protected void touchUp(float pointerX, float pointerY) {
        alphaDrag = false;
    }

    public int getBrush() {
        return brush;
    }

    public int getAlpha() {
        return alpha;
    }
}
