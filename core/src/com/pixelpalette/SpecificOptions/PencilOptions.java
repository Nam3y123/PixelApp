package com.pixelpalette.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.pixelpalette.ToolOptions;

public class PencilOptions extends ToolOptions {
    int brush;

    public PencilOptions() {
        super();
        setSize(18, 18);
        brush = 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(38, 167, 18, 18);
        super.draw(batch, parentAlpha);
    }

    public int getBrush() {
        return brush;
    }
}
