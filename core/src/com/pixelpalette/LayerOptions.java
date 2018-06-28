package com.pixelpalette;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pixelpalette.SpecificUndoActions.RemoveLayerUndoAction;

public class LayerOptions extends Actor {
    private Sprite spr;
    private boolean layerVisible;
    private float layerAlpha;
    private int layerAlphaInt;
    private DrawStage parent;
    private int layer;
    private com.pixelpalette.TextPrinter textPrinter;

    public LayerOptions(final DrawStage parent, final int layer) {
        layerVisible = true;
        layerAlpha = 1f;
        layerAlphaInt = 100;
        textPrinter = new com.pixelpalette.TextPrinter(false);
        spr = new Sprite((Texture) PixelApp.MANAGER.get("GUI.png"));
        setSize(121 * PixelApp.pixelSize, 42 * PixelApp.pixelSize);
        setPosition(PixelApp.width - getWidth(), 0);
        this.parent = parent;
        this.layer = layer;

        addCaptureListener(new ClickListener() {
            private boolean alphaDown;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                com.pixelpalette.Canvas canvas = parent.getCanvas();
                alphaDown = false;
                float pixelX = x / PixelApp.pixelSize;
                float pixelY = y / PixelApp.pixelSize;
                if(pixelX > 105 && pixelY > 26) {
                    close();
                } else {
                    if(pixelY > 23 && pixelY < 39) {
                        if(pixelX > 43 && pixelX < 59) {
                            mergeLayer();
                        } else if(pixelX > 62 && pixelX < 78) {
                            RemoveLayerUndoAction action = new RemoveLayerUndoAction(canvas, canvas.getCurLayer(), canvas.layers.get(canvas.getCurLayer()));
                            parent.getProcessor().addToUndo(action);
                            parent.getProcessor().clearRedo();
                            canvas.layers.remove(canvas.getCurLayer());
                            close();
                        } else if(pixelX > 81 && pixelX < 97) {
                            layerVisible = !layerVisible;
                            parent.getCanvas().updateCombinedDraw();
                        }
                    }
                    if(pixelX > 43 && pixelX < 115 && pixelY < 10) {
                        alphaDown = true;
                    }
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(alphaDown) {
                    layerAlpha = (x / PixelApp.pixelSize - 43) / 72f;
                    if(layerAlpha < 0) {
                        layerAlpha = 0;
                    }
                    if(layerAlpha > 1f) {
                        layerAlpha = 1f;
                    }
                    layerAlphaInt = (int)(layerAlpha * 100);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                alphaDown = false;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(421, 104, 121, 42);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);

        Sprite layerSpr = parent.getCanvas().layersDraw[layer];
        float ratio = 1f * parent.getCanvas().getImgW() / parent.getCanvas().getImgH();
        if(ratio > 1f) { // The image is wider than it is tall
            float height = 36 / ratio;
            layerSpr.setSize(36 * PixelApp.pixelSize, height * PixelApp.pixelSize);
            spr.setRegion(423, 28, 36, (int)height);
            spr.setSize(layerSpr.getWidth(), layerSpr.getHeight());
            spr.setPosition(getX() + 3 * PixelApp.pixelSize, getY() + 3 * PixelApp.pixelSize + (36 - height) / 2f * PixelApp.pixelSize);
            spr.draw(batch, parentAlpha);
            layerSpr.setPosition(spr.getX(), spr.getY());
            layerSpr.draw(batch, parentAlpha);
        } else { // the image is taller than it is wide or perfectly square
            float width = 36 * ratio;
            layerSpr.setSize(width * PixelApp.pixelSize, 36 * PixelApp.pixelSize);
            spr.setRegion(423, 28, (int)width, 36);
            spr.setSize(layerSpr.getWidth(), layerSpr.getHeight());
            spr.setPosition(getX() + 3 * PixelApp.pixelSize + ((36 - width) / 2f) * PixelApp.pixelSize, getY() + 3 * PixelApp.pixelSize);
            spr.draw(batch, parentAlpha);
            layerSpr.setPosition(spr.getX(), spr.getY());
            layerSpr.draw(batch, parentAlpha);
        }

        spr.setRegion(48, 32, 5, 5);
        spr.setSize(5 * PixelApp.pixelSize, 5 * PixelApp.pixelSize);
        spr.setPosition(getX() + 41 * PixelApp.pixelSize + (72 * layerAlpha * PixelApp.pixelSize), getY() + 2 * PixelApp.pixelSize);
        spr.draw(batch, parentAlpha);

        textPrinter.drawInt(getX() + 55 * PixelApp.pixelSize, getY() + 8 * PixelApp.pixelSize, layerAlphaInt, batch);
    }

    private void mergeLayer() {
        if(layer > 0) {
            com.pixelpalette.Canvas canvas = parent.getCanvas();
            com.pixelpalette.SpecificUndoActions.MergeLayerUndoAction action = new com.pixelpalette.SpecificUndoActions.MergeLayerUndoAction(canvas, layer);
            parent.getProcessor().addToUndo(action);
            parent.getProcessor().clearRedo();

            Pixmap map = canvas.newCanvasPixmap();
            map.setBlending(Pixmap.Blending.None);
            for(int xi = 0; xi < parent.getProcessor().getImgW(); xi++) {
                for(int yi = 0; yi < parent.getProcessor().getImgH(); yi++) {
                    int col = canvas.layers.get(layer).getPixel(xi, yi);
                    int colAlpha = col & 0x000000ff;
                    colAlpha *= layerAlpha;
                    col = (col & 0xffffff00) + colAlpha;
                    map.drawPixel(xi, yi, col);
                }
            }
            com.pixelpalette.Canvas.copyPixmap(map, canvas.layers.get(layer - 1));
            map.dispose();
            if(canvas.getCurLayer() == layer) {
                canvas.setCurLayer(canvas.getCurLayer() - 1);
            }
            canvas.layers.remove(layer);
            close();
        }
    }

    private void close() {
        remove();
        parent.getLayerSelector().closeLayerOptions();
        parent.getSelectOptions().minimize();
        parent.getAnimMenu().minimize();
        parent.getClipboard().minimize();
    }

    public boolean isLayerVisible() {
        return layerVisible;
    }

    public void setLayerVisible(boolean layerVisible) {
        this.layerVisible = layerVisible;
    }

    public float getLayerAlpha() {
        return layerAlpha;
    }

    public void setLayerAlpha(float layerAlpha) {
        this.layerAlpha = layerAlpha;
        layerAlphaInt = (int)(layerAlpha * 100);
    }
}
