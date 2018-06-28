package com.pixelpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelpalette.PixelApp.pixelSize;

public class SelectOptions extends SideMenu {
    private Pixmap clip;

    public SelectOptions(final DrawStage parent) {
        super(parent);
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!hidden() && parent.getDrawMask() && !parent.isDrawing()) {
                    if(minimized || !parent.getMaskVisible()) {
                        if(x > 13 * pixelSize) {
                            maximize();
                        } else {
                            if(parent.getMaskVisible()) {
                                clearSelection();
                            } else {
                                parent.setSelectionVisible(true);
                            }
                        }
                    } else {
                        int pos = (int)((com.pixelpalette.PixelApp.height - y) / (18 * pixelSize));
                        switch (pos) {
                            case 0: // Close
                                minimize();
                                break;
                            case 1: // Lift Selection & Move It
                                parent.getCanvas().liftSelection();
                                minimize();
                                break;
                            case 2: // Make the selection mask invisible
                                parent.setSelectionVisible(false);
                                break;
                            case 3: // Clear Selection
                                clearSelection();
                                break;
                            case 4:
                                eraseArea();
                                break;
                            case 5:
                                copyLocally(true);
                                break;
                            case 6: // Cut to clipboard
                                copyToClipboard(true);
                                break;
                            case 7:
                                copyLocally(false);
                                break;
                            case 8: // Copy to clipboard
                                copyToClipboard(false);
                                break;
                            default:
                                break;
                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(!hidden()) {
            boolean sel = parent.getDrawMask();
            if(!sel) {
                drawMaskInvisible(batch, parentAlpha);
            } else if(minimized || !parent.getMaskVisible()) {
                drawMinimized(batch, parentAlpha);
            } else {
                drawMaximized(batch, parentAlpha);
            }
        } else {
            setX(com.pixelpalette.PixelApp.width);
        }
    }

    private void drawMaskInvisible(Batch batch, float parentAlpha) {
        setBounds(com.pixelpalette.PixelApp.width - 16 * pixelSize, 32 * pixelSize, 16 * pixelSize, 32 * pixelSize);
        spr.setRegion(256, 89, 16, 32);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);
        minimize();
    }

    @Override
    protected void drawMinimized(Batch batch, float parentAlpha) {
        setBounds(com.pixelpalette.PixelApp.width - 29 * pixelSize, 32 * pixelSize, 29 * pixelSize, 32 * pixelSize);
        spr.setRegion(287, 89, 29, 32);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch, parentAlpha);

        if(!parent.getMaskVisible()) {
            spr.setRegion(273, 91, 13, 28);
            spr.setSize(13 * pixelSize, 28 * pixelSize);
            spr.setPosition(getX(), getY() + 2 * pixelSize);
            spr.draw(batch, parentAlpha);
        }
    }

    @Override
    protected void drawMaximized(Batch batch, float parentAlpha) {
        setBounds(com.pixelpalette.PixelApp.width - 18 * pixelSize, 0, 18 * pixelSize, com.pixelpalette.PixelApp.height);
        spr.setRegion(317, 276, 18, 18);
        spr.setSize(getWidth(), getHeight());
        spr.setPosition(getX(), getY());
        spr.draw(batch);

        final int numTools = 9;
        spr.setRegion(317, 294, 18, 18 * numTools);
        spr.setSize(getWidth(), 18 * pixelSize * numTools);
        spr.setPosition(getX(), com.pixelpalette.PixelApp.height - spr.getHeight());
        spr.draw(batch);
    }

    private void eraseArea() {
        com.pixelpalette.Canvas canvas = parent.getCanvas();
        Pixmap layer = canvas.layers.get(canvas.getCurLayer());
        com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(canvas, layer, canvas.getCurLayer());
        parent.getProcessor().addToUndo(action);
        parent.getProcessor().clearRedo();
        layer.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < canvas.getImgW(); xi++) {
            for(int yi = 0; yi < canvas.getImgH(); yi++) {
                if(parent.getMaskPosition(xi, yi)) {
                    layer.drawPixel(xi, yi, 0);
                }
            }
        }
        layer.setBlending(Pixmap.Blending.SourceOver);
        canvas.updateLayerDraw(canvas.getCurLayer());
    }

    public void clearSelection() {
        int w = parent.getCanvas().getImgW();
        int h = parent.getCanvas().getImgH();
        boolean[][] oldMask = new boolean[w][h];
        for(int xi = 0; xi < w; xi++) {
            System.arraycopy(parent.getSelectionMask()[xi], 0, oldMask[xi], 0, h);
        }
        parent.setSelectionMask(new boolean[w][h]); // The new boolean has false for everything, so this sets it to no selection
        parent.setSelectionVisible(true);
        parent.getProcessor().clearRedo();
        com.pixelpalette.SpecificUndoActions.SelectUndoAction action = new com.pixelpalette.SpecificUndoActions.SelectUndoAction(oldMask, parent.getSelectionMask(), parent, parent.getCanvas());
        parent.getProcessor().addToUndo(action);
    }

    private void copyLocally(boolean cut) {
        Pixmap newClip = copy(cut);
        if(newClip != null) {
            clip = newClip;
        }
    }

    private void copyToClipboard(boolean cut) {
        Pixmap clip = copy(cut);
        if(clip != null) {
            String str = Clipboard.clipboardHandle.path() + "/";
            FileHandle[] list = FileMenu.organizeByName(Clipboard.clipboardHandle.list());

            int num = 0;
            String numStr = "000.png";
            boolean handleFound = false;
            while(!handleFound) {
                handleFound = true;
                for(FileHandle handle : list) {
                    if(handle.name().equals(numStr)) {
                        ++num;
                        handleFound = false;
                        int numZeroes = 3 - String.valueOf(num).length();
                        numStr = "";
                        for(int i = 0; i < numZeroes; i++) {
                            numStr += "0";
                        }
                        numStr += num + ".png";
                    }
                }
            }
            str += numStr;

            FileHandle target = Gdx.files.external(str);
            PixmapIO.writePNG(target, clip);
            clip.dispose();

            parent.getClipboard().grabFiles();
        }
    }

    private Pixmap copy(boolean cut) {
        Pixmap layer = parent.getCanvas().layers.get(parent.getCanvas().getCurLayer());
        Pixmap temp = parent.getCanvas().newCanvasPixmap();
        temp.setBlending(Pixmap.Blending.None);
        int lowestX = temp.getWidth();
        int lowestY = temp.getHeight();
        int highestX = 0;
        int highestY = 0;
        com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(parent.getCanvas(), layer, parent.getCanvas().getCurLayer());
        layer.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < temp.getWidth(); xi++) {
            for(int yi = 0; yi < temp.getHeight(); yi++) {
                if(parent.getMaskPosition(xi, yi) && layer.getPixel(xi, yi) != 0) {
                    temp.drawPixel(xi, yi, layer.getPixel(xi, yi));
                    if(xi < lowestX) {
                        lowestX = xi;
                    } else if(xi > highestX) {
                        highestX = xi;
                    }
                    if(yi < lowestY) {
                        lowestY = yi;
                    } else if(yi > highestY) {
                        highestY = yi;
                    }
                    if(cut) {
                        layer.drawPixel(xi, yi, 0x00000000);
                    }
                }
            }
        }
        layer.setBlending(Pixmap.Blending.SourceOver);
        parent.getCanvas().updateLayerDraw();
        if(cut) {
            parent.getProcessor().addToUndo(action);
            parent.getProcessor().clearRedo();
        } else {
            action.dispose();
        }
        int width = highestX - lowestX + 1;
        int height = highestY - lowestY + 1;
        Pixmap clip = null;
        if(width > 0 && height > 0) {
            clip = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            clip.setBlending(Pixmap.Blending.None);
            for(int xi = 0; xi < clip.getWidth(); xi++) {
                for(int yi = 0; yi < clip.getHeight(); yi++) {
                    clip.drawPixel(xi, yi, temp.getPixel(xi + lowestX, yi + lowestY));
                }
            }
            temp.dispose();
        }
        clearSelection();
        return clip;
    }

    public Pixmap getClip() {
        return clip;
    }
}
