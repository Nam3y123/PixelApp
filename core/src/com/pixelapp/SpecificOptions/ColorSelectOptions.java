package com.pixelapp.SpecificOptions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pixelapp.DrawStage;
import com.pixelapp.ToolOptions;

import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class ColorSelectOptions extends ToolOptions {
    private Sprite colorDisplay;
    public int curColor, replaceColor;
    public boolean dragDown;
    public float pointerX, pointerY;
    public boolean layerOnly, swap;
    public boolean swapMenu;

    public ColorSelectOptions() {
        super();
        colorDisplay = new Sprite((Texture)MANAGER.get("GUI.png"));
        dragDown = false;
        layerOnly = true;
        swap = true;
        setSize(36, 18);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //setSize(36 * pixelSize, 18 * pixelSize);
        spr.setRegion(38, 221, 36, 18);
        super.draw(batch, parentAlpha);

        if(!layerOnly) {
            spr.setRegion(91, 222, 16, 16);
            spr.setPosition(1, 1);
            spr.draw(batch, parentAlpha);
        }
        if(!swap)
        {
            spr.setRegion(124, 222, 16, 16);
            spr.setPosition(19, 1);
            spr.draw(batch, parentAlpha);
        }

        if(dragDown) {
            colorDisplay.setRegion(141, 221, 38, 18);
            colorDisplay.setSize(38 * pixelSize, 18 * pixelSize);
            colorDisplay.setPosition(pointerX - 19 * pixelSize, pointerY + 8 * pixelSize);
            colorDisplay.setColor(Color.WHITE);
            colorDisplay.draw(batch);

            colorDisplay.setRegion(180, 222, 16, 16);
            colorDisplay.setSize(16 * pixelSize, 16 * pixelSize);
            colorDisplay.setPosition(pointerX - 18 * pixelSize, pointerY + 9 * pixelSize);
            colorDisplay.setColor(new Color(curColor));
            colorDisplay.draw(batch);

            colorDisplay.setRegion(197, 222, 16, 16);
            colorDisplay.setSize(16 * pixelSize, 16 * pixelSize);
            colorDisplay.setPosition(pointerX + 2 * pixelSize, pointerY + 9 * pixelSize);
            colorDisplay.setColor(new Color(replaceColor));
            colorDisplay.draw(batch);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        if(pointerX < 18 * pixelSize) {
            layerOnly = !layerOnly;
        } else {
            swap = !swap;
        }
        return true;
    }
}
