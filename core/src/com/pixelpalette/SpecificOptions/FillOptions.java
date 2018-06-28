package com.pixelpalette.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.pixelpalette.ToolOptions;

import static com.pixelpalette.PixelApp.pixelSize;

public class FillOptions extends ToolOptions {
    public boolean blend;
    public int mode;
    private boolean modeMenu;

    public FillOptions() {
        super();
        mode = 0;
        blend = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(modeMenu) {
            setSize(66, 34);
        } else {
            setSize(36, 18);
        }
        spr.setRegion(38, 203, 36, 18);
        super.draw(batch, parentAlpha);
        spr.setRegion(75 + (16 * mode), 204, 16, 16);
        spr.setPosition(1, 1);
        spr.draw(batch);
        spr.setRegion(blend ? 124 : 140, 204, 16, 16);
        spr.setPosition(19, 1);
        spr.draw(batch);
        if(modeMenu) {
            spr.setRegion(75, 204, 48, 16);
            spr.setPosition(18, -16);
            spr.draw(batch);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        boolean ret = false;
        if(!modeMenu || pointerY > 16 * pixelSize) {
            if(pointerX < 36 * pixelSize) {
                ret = true;
                if(pointerX < 18 * pixelSize) {
                    modeMenu = !modeMenu;
                } else {
                    blend = !blend;
                }
            }
        } else if(pointerX > 18 * pixelSize) {
            float x = pointerX / pixelSize;
            mode = (int)((x - 18) / 16);
            modeMenu = false;
            ret = true;
        }
        return ret;
    }
}
