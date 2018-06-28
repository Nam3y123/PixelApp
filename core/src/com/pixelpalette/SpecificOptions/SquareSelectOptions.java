package com.pixelpalette.SpecificOptions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pixelpalette.ToolOptions;

import static com.pixelpalette.PixelApp.pixelSize;

public class SquareSelectOptions extends ToolOptions {
    public boolean multiClick;
    public int type;
    public int[] x = new int[2], y = new int[2];
    public boolean drawSquare;
    public com.pixelpalette.AnimProcessor processor;
    private boolean typeMenu;
    private Sprite square;
    public boolean secondTouch;

    public SquareSelectOptions() {
        super();
        setSize(36, 18);
        type = 0;
        multiClick = false;
        typeMenu = false;
        drawSquare = false;
        square = new Sprite(spr.getTexture());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(typeMenu) {
            setSize(82, 34);
        } else {
            setSize(36, 18);
        }
        spr.setRegion(38, 239, 36, 18);
        super.draw(batch, parentAlpha);
        spr.setRegion(multiClick ? 91 : 75, 240, 16, 16);
        spr.setPosition(1, 1);
        spr.draw(batch);
        spr.setRegion(108 + (16 * type), 240, 16, 16);
        spr.setPosition(19, 1);
        spr.draw(batch);
        if(typeMenu) {
            spr.setRegion(108, 240, 64, 16);
            spr.setPosition(18, -16);
            spr.draw(batch);
        }

        if(drawSquare) {
            int lowerX = (x[0] < x[1]) ? x[0] : x[1];
            int lowerY = (y[0] > y[1]) ? y[0] : y[1];
            int higherX = (x[0] > x[1]) ? x[0] : x[1];
            int higherY = (y[0] < y[1]) ? y[0] : y[1];

            int subtract = multiClick ? 19 : 0;
            square.setRegion(194 - subtract, 256, 14, 1);
            square.setSize(getCanvasX(higherX + 1) - getCanvasX(lowerX), 1 * pixelSize);
            square.setPosition(getCanvasX(lowerX), getCanvasY(lowerY + 1));
            square.draw(batch);
            square.setPosition(getCanvasX(lowerX), getCanvasY(higherY) - 1 * pixelSize);
            square.draw(batch);

            square.setRegion(192 - subtract, 241, 1, 14);
            square.setSize(1 * pixelSize, getCanvasY(higherY) - getCanvasY(lowerY + 1));
            square.setPosition(getCanvasX(lowerX), getCanvasY(lowerY + 1));
            square.draw(batch);
            square.setPosition(getCanvasX(higherX + 1) - 1 * pixelSize, getCanvasY(lowerY + 1));
            square.draw(batch);
        }

        if(multiClick && secondTouch) {
            spr.setRegion(211, 240, 16, 16);
            spr.setPosition(1, 1);
            spr.draw(batch, parentAlpha);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        boolean ret = false;
        if((!typeMenu) || pointerY > 16 * pixelSize) {
            if(pointerX < 36 * pixelSize) {
                ret = true;
                if(pointerX < 18 * pixelSize) {
                    if(multiClick && secondTouch) {
                        drawSquare = false;
                        secondTouch = false;
                    } else {
                        multiClick = !multiClick;
                    }
                } else {
                    typeMenu = !typeMenu;
                }
            }
        } else if(pointerX > 18 * pixelSize) {
            float x = pointerX / pixelSize;
            type = (int)((x - 18) / 16);
            typeMenu = false;
            ret = true;
        }
        return ret;
    }

    private float getCanvasX(int x) {
        float ret = x;
        ret /= processor.getImgW();
        ret *= processor.getWidth();
        ret += processor.getX();
        return ret;
    }

    private float getCanvasY(int y) {
        float ret = processor.getImgH() - y;
        ret /= processor.getImgH();
        ret *= processor.getHeight();
        ret += processor.getY();
        return ret;
    }
}
