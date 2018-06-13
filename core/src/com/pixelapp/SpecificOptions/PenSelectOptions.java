package com.pixelapp.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pixelapp.ToolOptions;

import static com.pixelapp.PixelApp.pixelSize;

public class PenSelectOptions extends ToolOptions {
    private int brush;
    public boolean mode;

    public PenSelectOptions() {
        super();
        brush = 1;
        mode = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setSize(36, 18);
        spr.setRegion(38, 275, 36, 18);
        super.draw(batch, parentAlpha);
        spr.setRegion(mode ? 75 : 91, 276, 16, 16);
        spr.setPosition(19, 1);
        spr.draw(batch);
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        if(pointerX > 18 * pixelSize) {
            mode = !mode;
        }
        return true;
    }

    public int getBrush() {
        return brush;
    }
}
