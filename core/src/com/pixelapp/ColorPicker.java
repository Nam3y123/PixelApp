package com.pixelapp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class ColorPicker extends Actor {
    private Sprite spr;
    private int[] col;
    private boolean curCol;
    private int backupCol;
    private DrawStage parent;
    private boolean editingColor;
    private TextPrinter textPrinter;
    private int dragNum;
    private HSVColor hsv;
    private boolean displayHSV;

    private final int R_MULTIPLY = 16777216; //   16 ^ 6
    private final int G_MULTIPLY = 65536; //      16 ^ 4
    private final int B_MULTIPLY = 256; //        16 ^ 2

    public ColorPicker(DrawStage parent) {
        this.parent = parent;
        spr = new Sprite((Texture)MANAGER.get("GUI.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width, height);
            }

            @Override
            public void setSize(float width, float height) {
                super.setSize(pixelSize * width, pixelSize * height);
            }

            @Override
            public void setPosition(float x, float y) {
                super.setPosition(pixelSize * x, pixelSize * y);
            }
        };
        col = new int[]{
                0x000000ff,
                0xffffffff
        };
        editingColor = false;
        dragNum = -1;
        textPrinter = new TextPrinter(false);
        hsv = new HSVColor();
        displayHSV = false;

        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ColorPicker.this.touchDown(x, y);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                ColorPicker.this.touchUp(x, y);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                ColorPicker.this.touchDragged(x, y);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(editingColor) {
            int regionY = displayHSV ? 348 : 38;
            spr.setRegion(0, regionY, 177, 85);
            spr.setPosition(0, 0);
            spr.setColor(Color.WHITE);
            setBounds(0, 0, 177 * pixelSize, 85 * pixelSize);
            spr.draw(batch, parentAlpha);

            int selCol = curCol ? col[1] : col[0];
            spr.setRegion(1, 124, 175, 24);
            spr.setPosition(1, 60);
            spr.setColor(new Color(selCol));
            spr.draw(batch, parentAlpha);

            if(displayHSV) {
                textPrinter.drawInt(19 * pixelSize, 3 * pixelSize, (int)hsv.getA(), batch);
                textPrinter.drawInt(19 * pixelSize, 17 * pixelSize, (int)hsv.getV(), batch);
                textPrinter.drawInt(19 * pixelSize, 31 * pixelSize, (int)hsv.getS(), batch);
                textPrinter.drawInt(19 * pixelSize, 45 * pixelSize, (int)hsv.getH(), batch);
            } else {
                textPrinter.drawInt(19 * pixelSize, 3 * pixelSize, getA(selCol), batch);
                textPrinter.drawInt(19 * pixelSize, 17 * pixelSize, getB(selCol), batch);
                textPrinter.drawInt(19 * pixelSize, 31 * pixelSize, getG(selCol), batch);
                textPrinter.drawInt(19 * pixelSize, 45 * pixelSize, getR(selCol), batch);
            }

            spr.setColor(Color.WHITE);
            for(int i = 0; i < 4; i++) {
                float yPos = 6 + 14 * i;
                float xPos = 0;
                if(displayHSV) {
                    xPos = i == 0 ? (int)hsv.getA() : (i == 1 ? (int)hsv.getV() : (i == 2 ? (int)hsv.getS() : (int)hsv.getH()));
                    if(i == 3) {
                        xPos *= 96 / 360f;
                    } else {
                        xPos *= 96 / 100f;
                    }
                } else {
                    xPos = i == 0 ? getA(selCol) : (i == 1 ? getB(selCol) : (i == 2 ? getG(selCol) : getR(selCol)));
                    xPos *= 96 / 255f;
                }
                xPos += 52;
                if(displayHSV) {
                    spr.setRegion(48, 32, 5, 5);
                } else {
                    spr.setRegion(48 + 6 * i, 32, 5, 5);
                }
                spr.setPosition(xPos, yPos);
                spr.draw(batch, parentAlpha);
            }
        } else {
            spr.setRegion(0, 0, 47, 38);
            spr.setPosition(0, 0);
            spr.setColor(Color.WHITE);
            setBounds(0, 0, 47 * pixelSize, 38 * pixelSize);
            spr.draw(batch, parentAlpha);

            spr.setRegion(70, 0, 18, 12);
            spr.setPosition(16, 22);
            spr.setColor(new Color(col[0]));
            spr.draw(batch, parentAlpha);

            spr.setPosition(16, 4);
            spr.setColor(new Color(col[1]));
            spr.draw(batch, parentAlpha);

            spr.setRegion(47, 0, 22, 16);
            spr.setPosition(14, curCol ? 2 : 20);
            spr.setColor(Color.WHITE);
            spr.draw(batch, parentAlpha);
        }
    }

    private void touchDown(float x, float y) {
        if(editingColor) {
            touchDown_Editor(x, y);
        } else {
            touchDown_Selector(x, y);
        }
    }

    private void touchDown_Selector(float x, float y) {
        if(x >= 16 * pixelSize && x <=  34 * pixelSize) {
            if(y >= 4 * pixelSize && y <= 16 * pixelSize) {
                editingColor = curCol;
                curCol = true;
            } else if(y >= 22 * pixelSize && y <= 34 * pixelSize) {
                editingColor = !curCol;
                curCol = false;
            }
        }
        updateCol();
        if(editingColor) {
            backupCol = curCol ? col[1] : col[0];
        }
    }

    private void touchDown_Editor(float x, float y) {
        if(x >= 158 * pixelSize && x <= 174 * pixelSize) {
            if(y >= 1 * pixelSize && y <= 17 * pixelSize) {
                displayHSV = !displayHSV;
                //hsv.RGBAtoHSV(col[curCol ? 1 : 0]);
            } else if(y >= 21 * pixelSize && y <= 37 * pixelSize) {
                editingColor = false;
                col[curCol ? 1 : 0] = backupCol;
                updateCol();
            } else if(y >= 41 * pixelSize && y <= 57 * pixelSize) {
                editingColor = false;
            }
        }
        if(x >= 15 * pixelSize && x <= 53 * pixelSize) {
            if(y > 45 * pixelSize) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try{
                            int i = Integer.parseInt(text);
                            if(i >= 0 && i <= 255) {
                                int temp = col[curCol ? 1 : 0] & 0x00ffffff;
                                col[curCol ? 1 : 0] = temp + (i * R_MULTIPLY);
                                updateCol();
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set new R value", Integer.toString(getR(col[curCol ? 1 : 0])), "");
            } else if(y > 31 * pixelSize) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try{
                            int i = Integer.parseInt(text);
                            if(i >= 0 && i <= 255) {
                                int temp = col[curCol ? 1 : 0] & 0xff00ffff;
                                col[curCol ? 1 : 0] = temp + (i * G_MULTIPLY);
                                updateCol();
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set new G value", Integer.toString(getG(col[curCol ? 1 : 0])), "");
            } else if(y > 17 * pixelSize) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try{
                            int i = Integer.parseInt(text);
                            if(i >= 0 && i <= 255) {
                                int temp = col[curCol ? 1 : 0] & 0xffff00ff;
                                col[curCol ? 1 : 0] = temp + (i * B_MULTIPLY);
                                updateCol();
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set new B value", Integer.toString(getB(col[curCol ? 1 : 0])), "");
            } else if(y > 3 * pixelSize) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try{
                            int i = Integer.parseInt(text);
                            if(i >= 0 && i <= 255) {
                                int temp = col[curCol ? 1 : 0] & 0xffffff00;
                                col[curCol ? 1 : 0] = temp + i;
                                updateCol();
                            }
                        } catch (NumberFormatException e) {

                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set new A value", Integer.toString(getA(col[curCol ? 1 : 0])), "");
            }
        }
        if(x > 53 * pixelSize && x < 151 * pixelSize) {
            if(y > 45 * pixelSize) {
                dragNum = 0;
            } else if(y > 31 * pixelSize) {
                dragNum = 1;
            } else if(y > 17 * pixelSize) {
                dragNum = 2;
            } else if(y > 3 * pixelSize) {
                dragNum = 3;
            }
        }
    }

    private void touchUp(float x, float y) {
        if(editingColor) {
            touchUp_Editor();
        } else {
            //touchUp_Selector(x, y);
        }
    }

    private void touchUp_Editor() {
        dragNum = -1;
    }

    private void touchDragged(float x, float y) {
        if(editingColor) {
            touchDragged_Editor(x, y);
        } else {
            //touchDragged_Selector(x, y);
        }
    }

    private void touchDragged_Editor(float x, float y) {
        if(displayHSV) {
            int newAmt;
            if(dragNum == 0) {
                newAmt = (int)((x - 54 * pixelSize) / (96f * pixelSize) * 360);
            } else {
                newAmt = (int)((x - 54 * pixelSize) / (96f * pixelSize) * 100);
            }
            if(newAmt < 0) {
                newAmt = 0;
            }
            if(newAmt > (dragNum == 0 ? 360 : 100)) {
                newAmt = dragNum == 0 ? 360 : 100;
            }
            switch (dragNum) {
                case 0:
                    hsv.setH(newAmt);
                    break;
                case 1:
                    hsv.setS(newAmt);
                    break;
                case 2:
                    hsv.setV(newAmt);
                    break;
                case 3:
                    hsv.setA(newAmt);
                    break;
                default:
                    break; // Intentionally blank
            }
            col[curCol ? 1 : 0] = hsv.toRGBA();
        } else {
            int newAmt = (int)((x - 54 * pixelSize) / (96f * pixelSize) * 255);
            if(newAmt < 0) {
                newAmt = 0;
            }
            if(newAmt > 255) {
                newAmt = 255;
            }
            int selCol = col[curCol ? 1 : 0];
            switch (dragNum) {
                case 0:
                    selCol = (selCol & 0x00ffffff) + newAmt * R_MULTIPLY;
                    break;
                case 1:
                    selCol = (selCol & 0xff00ffff) + newAmt * G_MULTIPLY;
                    break;
                case 2:
                    selCol = (selCol & 0xffff00ff) + newAmt * B_MULTIPLY;
                    break;
                case 3:
                    selCol = (selCol & 0xffffff00) + newAmt; // No A_MULTIPLY because it would be 1
                    break;
                default:
                    break; // Intentionally blank
            }
            col[curCol ? 1 : 0] = selCol;
        }
        updateCol();
    }

    public void updateCol() {
        parent.getCanvas().setColor(curCol ? col[1] : col[0]);
        if(!displayHSV) {
            hsv.RGBAtoHSV(curCol ? col[1] : col[0]);
        }
    }

    public void setCol(int newCol) {
        col[curCol ? 1 : 0] = newCol;
        hsv.RGBAtoHSV(newCol);
        updateCol();
    }

    private int getR(int selCol) {
        int r = (selCol & 0xff000000) / R_MULTIPLY; // R gets its own variable because it is signed and will appear negative if it is over 127. This is the fix.
        if(r < 0)
            r = 256 + r;
        return r;
    }

    private int getG(int selCol) {
        return (selCol & 0x00ff0000) / G_MULTIPLY;
    }

    private int getB(int selCol) {
        return (selCol & 0x0000ff00) / B_MULTIPLY;
    }

    private int getA(int selCol) {
        return (selCol & 0x000000ff);
    }
}
