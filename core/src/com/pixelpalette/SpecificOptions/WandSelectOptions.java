package com.pixelpalette.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.pixelpalette.ToolOptions;

import static com.pixelpalette.PixelApp.pixelSize;

/**
 * Created by jnich on 1/7/2018.
 */

public class WandSelectOptions extends ToolOptions {
    public int mode;
    public int type;
    private boolean typeMenu, modeMenu;

    public WandSelectOptions() {
        super();
        mode = 0;
        type = 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(modeMenu) {
            setSize(66, 34);
        } else if(typeMenu) {
            setSize(82, 34);
        } else {
            setSize(36, 18);
        }
        spr.setRegion(38, 203, 36, 18);
        super.draw(batch, parentAlpha);
        spr.setRegion(75 + (16 * mode), 204, 16, 16);
        spr.setPosition(1, 1);
        spr.draw(batch);
        spr.setRegion(108 + (16 * type), 240, 16, 16);
        spr.setPosition(19, 1);
        spr.draw(batch);
        if(modeMenu) {
            spr.setRegion(75, 204, 48, 16);
            spr.setPosition(18, -16);
            spr.draw(batch);
        } else if(typeMenu) {
            spr.setRegion(108, 240, 64, 16);
            spr.setPosition(18, -16);
            spr.draw(batch);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        boolean ret = false;
        if((!modeMenu && !typeMenu) || pointerY > 16 * pixelSize) {
            if(pointerX < 36 * pixelSize) {
                ret = true;
                if(pointerX < 18 * pixelSize) {
                    modeMenu = !modeMenu;
                    typeMenu = false;
                } else {
                    typeMenu = !typeMenu;
                    modeMenu = false;
                }
            }
        } else if(pointerX > 18 * pixelSize) {
            if(modeMenu) {
                float x = pointerX / pixelSize;
                mode = (int)((x - 18) / 16);
                modeMenu = false;
                ret = true;
            }
            if(typeMenu) {
                float x = pointerX / pixelSize;
                type = (int)((x - 18) / 16);
                typeMenu = false;
                ret = true;
            }
        }
        return ret;
    }
}
