package com.pixelpalette;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pixelpalette.SpecificUndoActions.AddLayerUndoAction;
import com.pixelpalette.SpecificUndoActions.RemoveLayerUndoAction;

import static com.pixelpalette.PixelApp.MANAGER;
import static com.pixelpalette.PixelApp.pixelSize;

public class LayerSelector extends SideMenu {
    private int move;
    private float scroll;
    private com.pixelpalette.TextPrinter textPrinter;
    private int layerOptionsOpen;

    public LayerSelector(final com.pixelpalette.DrawStage parent) {
        super(parent);
        move = -1;
        layerOptionsOpen = -1;
        textPrinter = new com.pixelpalette.TextPrinter(false);
        spr = new Sprite((Texture)MANAGER.get("GUI.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width * pixelSize, height * pixelSize);
            }
        };
        setBounds(com.pixelpalette.PixelApp.width - 105 * pixelSize, 0, 105 * pixelSize, com.pixelpalette.PixelApp.height);
        addCaptureListener(new ClickListener() {
            private boolean scrollChanged, canScroll;
            private float storeY, storeScroll;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = parent.rightMinimized() || !minimized;
                if(ret) {
                    if(!parent.isDrawing()) {
                        if(minimized) {
                            maximize();
                            scrollChanged = true;
                            canScroll = false;
                        } else {
                            canScroll = y < com.pixelpalette.PixelApp.height - 24 * pixelSize;
                            scrollChanged = !canScroll;
                            storeY = y;
                            storeScroll = scroll;
                            if(move == -1) {
                                if(y > com.pixelpalette.PixelApp.height - 20 * pixelSize && y < com.pixelpalette.PixelApp.height - 4 * pixelSize) {
                                    com.pixelpalette.Canvas canvas = parent.getCanvas();
                                    if(x > 4 * pixelSize && x < 20 * pixelSize) {
                                        canvas.addLayer();
                                        AddLayerUndoAction action = new AddLayerUndoAction(canvas, canvas.getCurLayer());
                                        parent.getProcessor().addToUndo(action);
                                        parent.getProcessor().clearRedo();
                                    } else if(x > 24 * pixelSize && x < 40 * pixelSize) {
                                        if(parent.getCanvas().layers.size() > 1) {
                                            RemoveLayerUndoAction action = new RemoveLayerUndoAction(canvas, canvas.getCurLayer(), canvas.layers.get(canvas.getCurLayer()));
                                            parent.getProcessor().addToUndo(action);
                                            parent.getProcessor().clearRedo();
                                            parent.getCanvas().layers.remove(parent.getCanvas().getCurLayer());
                                        }
                                    } else if(x > 44 * pixelSize && x < 60 * pixelSize) {
                                        move = canvas.getCurLayer();
                                    }
                                }
                                if(x > 71 * pixelSize && y > com.pixelpalette.PixelApp.height - 17 * pixelSize) {
                                    minimize();
                                }
                            } else if(y > com.pixelpalette.PixelApp.height - 20 * pixelSize && y < com.pixelpalette.PixelApp.height - 4 * pixelSize && x > 44 * pixelSize && x < 60 * pixelSize) {
                                move = -1;
                            }
                        }
                    }
                }
                return ret;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(canScroll) {
                    scroll += y - storeY;
                    storeY = y;
                    float maxScroll = 39 * pixelSize * parent.getCanvas().layers.size() - (com.pixelpalette.PixelApp.height - 24 * pixelSize);
                    if(scroll > maxScroll) {
                        scroll = maxScroll;
                    }
                    if(scroll < 0) {
                        scroll = 0;
                    }
                    if(Math.abs(storeScroll - scroll) > 0.5f * pixelSize) {
                        scrollChanged = true;
                    }
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                move = move;
                if(!scrollChanged && canScroll) {
                    if(move == -1) {
                        float distFromTop = (com.pixelpalette.PixelApp.height - 24 * pixelSize) - y + scroll;
                        int numFromTop = (int)(distFromTop / (39 * pixelSize)) + 1;
                        com.pixelpalette.Canvas canvas = parent.getCanvas();
                        int newLayer = canvas.layers.size() - numFromTop;
                        if(y - scroll < com.pixelpalette.PixelApp.height - 24 * pixelSize && x > 22 * pixelSize && x < 58 * pixelSize) {
                            //System.out.println(newLayer);
                            if(newLayer < canvas.layers.size() && newLayer >= 0) {
                                parent.getCanvas().setCurLayer(newLayer);
                            }
                        }
                        float distFromLayerTop = distFromTop - (39 * pixelSize * (numFromTop - 1));
                        if(x > 66 * pixelSize && x < 82 * pixelSize && distFromLayerTop < 17 * pixelSize) {
                            layerOptionsOpen = newLayer;
                            parent.addActor(parent.getCanvas().getLayerOption(newLayer));
                            minimize();
                        }
                    } else if(y - scroll < com.pixelpalette.PixelApp.height - 24 * pixelSize) {
                        moveLayer(y - scroll);
                    }
                }
            }
        });
    }

    private void moveLayer(float y) {
        float distFromTop = (com.pixelpalette.PixelApp.height - 24 * pixelSize) - y;
        float posOnSegment = distFromTop - pixelSize * 39 * ((int)(distFromTop / pixelSize) / 39);
        boolean above = false;
        if(posOnSegment < 19 * pixelSize) {
            above = true;
        }
        int numFromTop = (int)(distFromTop / (39 * pixelSize)) + 1;
        com.pixelpalette.Canvas canvas = parent.getCanvas();
        int secondLayer = canvas.layers.size() - numFromTop;
        Pixmap temp = canvas.layers.get(move);
        com.pixelpalette.LayerOptions options = canvas.getLayerOption(move);
        canvas.layers.remove(move);
        int pos = secondLayer + (above ? 0 : -1);
        if (secondLayer <= move)
        {
            pos = secondLayer + (above ? 1 : 0);
        }
        canvas.setCurLayer(pos);
        canvas.layers.add(pos, temp);
        canvas.setLayerOption(pos, options);
        parent.getProcessor().addToUndo(new com.pixelpalette.SpecificUndoActions.MoveLayerUndoAction(canvas, canvas.getCurLayer(), move));
        parent.getProcessor().clearRedo();
        move = -1;
    }

    protected void drawMinimized(Batch batch, float parentAlpha) {
        setBounds(com.pixelpalette.PixelApp.width - 16 * pixelSize, 0, 16 * pixelSize, 32 * pixelSize);
        spr.setRegion(300, 39, 16, 32);
        spr.setPosition(getX(), 0);
        spr.draw(batch, parentAlpha);
    }

    protected void drawMaximized(Batch batch, float parentAlpha) {
        setBounds(com.pixelpalette.PixelApp.width - 87 * pixelSize, 0, 105 * pixelSize, com.pixelpalette.PixelApp.height);
        spr.setRegion(317, 24, 87, 3);
        spr.setSize(87 * pixelSize, com.pixelpalette.PixelApp.height - 24 * pixelSize);
        spr.setPosition(getX(), 0);
        spr.draw(batch, parentAlpha);

        Sprite[] layersDraw = parent.getCanvas().layersDraw;
        int curLayer = parent.getCanvas().getCurLayer();
        for(int i = 0; i < layersDraw.length; i++) {
            boolean isMove = (layersDraw.length - 1 - i) == move;
            spr.setRegion(317, isMove ? 105 : (curLayer == layersDraw.length - i - 1 ? 66 : 27), 87, 39);
            float y = com.pixelpalette.PixelApp.height - 24 * pixelSize - (spr.getHeight() * (i + 1)) + scroll;
            spr.setPosition(getX(), y);
            spr.draw(batch, parentAlpha);
            String num = Integer.toString(layersDraw.length - i);
            float textWidth = textPrinter.getTextWidth(num);
            textPrinter.drawText(getX() + 1 * pixelSize + (20 * pixelSize - textWidth) / 2f, y + 12 * pixelSize, num, batch);

            if(move != -1) {
                if(isMove) {
                    spr.setRegion(423, 65, 38, 38);
                    spr.setPosition(getX() - 41 * pixelSize, getHeight() - 62 * pixelSize);
                    spr.draw(batch, parentAlpha);
                } else {
                    spr.setRegion(460, 28, 17, 16);
                    spr.setPosition(getX() + 66 * pixelSize, y + 22 * pixelSize);
                    spr.draw(batch, parentAlpha);

                    spr.setRegion(460, 46, 87, 4);
                    spr.setPosition(getX(), y + 36 * pixelSize);
                    spr.draw(batch, parentAlpha);

                    spr.setRegion(460, 51, 87, 4);
                    spr.setPosition(getX(), y);
                    spr.draw(batch, parentAlpha);
                }
            }

            Sprite layerSpr = layersDraw[layersDraw.length - 1 - i];
            float ratio = 1f * parent.getCanvas().getImgW() / parent.getCanvas().getImgH();
            if(ratio > 1f) { // The image is wider than it is tall
                float height = 36 / ratio;
                layerSpr.setSize(36 * pixelSize, height * pixelSize);
                spr.setRegion(423, 28, 36, (int)height);
                spr.setSize(layerSpr.getWidth(), layerSpr.getHeight());
                spr.setPosition(getX() + (isMove ? -40 : 22) * pixelSize, (isMove ? (getHeight() - 61 * pixelSize) : (y + 2 * pixelSize)) + (36 - height) / 2f * pixelSize);
                spr.draw(batch, parentAlpha);
                layerSpr.setPosition(spr.getX(), spr.getY());
                layerSpr.draw(batch, parentAlpha);
            } else { // the image is taller than it is wide or perfectly square
                float width = 36 * ratio;
                layerSpr.setSize(width * pixelSize, 36 * pixelSize);
                spr.setRegion(423, 28, (int)width, 36);
                spr.setSize(layerSpr.getWidth(), layerSpr.getHeight());
                spr.setPosition(getX() + ((isMove ? -40 : 22) + (36 - width) / 2f) * pixelSize, isMove ? (getHeight() - 61 * pixelSize) : (y + 2 * pixelSize));
                spr.draw(batch, parentAlpha);
                layerSpr.setPosition(spr.getX(), spr.getY());
                layerSpr.draw(batch, parentAlpha);
            }
        }

        spr.setRegion(move == -1 ? 317 : 423, 0, 87, 24);
        spr.setPosition(getX(), com.pixelpalette.PixelApp.height - spr.getHeight());
        spr.draw(batch, parentAlpha);
    }

    public boolean layerOptionsClosed() {
        return layerOptionsOpen == -1;
    }

    public void closeLayerOptions() {
        layerOptionsOpen = -1;
        maximize();
    }
}
