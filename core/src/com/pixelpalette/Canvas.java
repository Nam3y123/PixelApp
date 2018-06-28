package com.pixelpalette;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;

public class Canvas extends Actor {
    private Sprite paper;
    private int stoCount;
    private float yRatio; // height / left
    public ArrayList<Pixmap> layers;
    public Sprite[] layersDraw;
    private ArrayList<LayerOptions> layerOptions;
    private int oldX, oldY;
    private int curLayer;
    private Pixmap edit;
    private Sprite editDraw;
    private boolean drawMode;
    private int imgW, imgH;
    private int color;
    private DrawStage parent;
    private boolean moving;
    private Pixmap move;
    private Sprite moveDraw;
    private int moveX, moveY, oldMoveX, oldMoveY;
    private int duration;
    private AnimProcessor processor;
    private Pixmap combined;
    private Sprite combinedDraw;
    private String anim;
    private int sprOfsX, sprOfsY;

    private static float touchCenterX, touchCenterY, touchRatio;
    public static final int toolLen = 8;
    public final Tool[] tools = new Tool[] {
            new Tool() {
                @Override
                public void touchDown(float pointerX, float pointerY) {

                }

                @Override
                public void touchUp(float pointerX, float pointerY) {

                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {

                }

                @Override
                public void cancelTouch() {

                }

                @Override
                public ToolOptions getOptions() {
                    return null;
                }

                @Override
                public void setOptions(final ToolOptions options) {

                }
            }, // No tool selected
            new Tool() {
                private com.pixelpalette.SpecificOptions.PencilOptions pencilOptions;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    if(drawMode) {
                        if(edit == null) {
                            edit = newCanvasPixmap();
                        }
                        float actualX = (pointerX - getX()) / getWidth();
                        float actualY = 1 - (pointerY - getY()) / getHeight();
                        int width = (int)(actualX * imgW) + 1;
                        int height = (int)(actualY * imgH) + 1;
                        if(actualX < 0) {
                            width--;
                        }
                        if(actualY < 0) {
                            height--;
                        }
                        {
                            if(oldX < 0) {
                                oldX = width;
                                oldY = height;
                            }
                            edit.setBlending(Pixmap.Blending.None);
                            edit.setColor(color);
                            Pixmap brush = processor.getBrushes()[pencilOptions.getBrush()];
                            int ofsX = brush.getWidth() / 2 + 1;
                            int ofsY = brush.getWidth() / 2 + 1;
                            for(int xi = 0; xi < brush.getWidth(); xi++) {
                                for(int yi = 0; yi < brush.getWidth(); yi++) {
                                    if(brush.getPixel(xi, yi) != -256) { // -256 is 0x00 on an alpha pixmap...
                                        line(oldX + xi - ofsX, oldY + yi - ofsY, width + xi - ofsX, height + yi - ofsY);
                                    }
                                }
                            }
                            updateEditDraw();

                        }
                        oldX = width;
                        oldY = height;
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    resetDraw();
                    if(drawMode && edit != null) {
                        boolean changeMade = false;
                        Pixmap oldPixmap = newCanvasPixmap();
                        oldPixmap.setBlending(Pixmap.Blending.None);
                        oldPixmap.drawPixmap(layers.get(curLayer), 0, 0);
                        for(int xi = 0; xi < imgW; xi++) {
                            for(int yi = 0; yi < imgH; yi++) {
                                if((edit.getPixel(xi, yi) & 0x000000ff) != 0) {
                                    boolean modBlending = (edit.getPixel(xi, yi) & 0x000000ff) != 255 && (layers.get(curLayer).getPixel(xi, yi) & 0x000000ff) == 0;
                                    if(modBlending)
                                        layers.get(curLayer).setBlending(Pixmap.Blending.None);
                                    layers.get(curLayer).drawPixel(xi, yi, edit.getPixel(xi, yi));
                                    if(modBlending)
                                        layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                                    if(edit.getPixel(xi, yi) != 0) {
                                        changeMade = true;
                                    }
                                }
                            }
                        }
                        if(changeMade) {
                            UndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(Canvas.this, oldPixmap, curLayer);
                            processor.addToUndo(action);
                            processor.clearRedo();
                        }
                        updateLayerDraw(curLayer);
                        edit.dispose();
                        edit = newCanvasPixmap();
                        updateEditDraw();
                        oldPixmap.dispose();
                    }
                    drawMode = true;
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {

                }

                @Override
                public ToolOptions getOptions() {
                    return pencilOptions;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.PencilOptions) {
                        pencilOptions = (com.pixelpalette.SpecificOptions.PencilOptions)options;
                    }
                }

                // Code based on the Bresenham algorithm and is based on source code from Tech-Algorithm.com.
                // Tech-Algorithm.com does not state that it needs any credit for its code, but I would
                // rather explain that I used their code as a baseline than experience legal trouble later.
                private void line(int x0, int y0, int x1, int y1) {
                    int w = x1 - x0;
                    int h = y1 - y0;
                    int dx1 = 0;
                    int dy1 = 0;
                    int dx2 = 0;
                    int dy2 = 0;

                    if (w < 0) {
                        dx1 = -1;
                    } else if (w > 0) {
                        dx1 = 1;
                    }
                    if (h < 0) {
                        dy1 = -1;
                    } else if (h > 0) {
                        dy1 = 1;
                    }
                    if (w < 0) {
                        dx2 = -1;
                    } else if (w > 0) {
                        dx2 = 1;
                    }

                    int longest = Math.abs(w);
                    int shortest = Math.abs(h);
                    if (longest <= shortest) {
                        longest = Math.abs(h);
                        shortest = Math.abs(w);
                        if (h < 0){
                            dy2 = -1;
                        } else if (h > 0) {
                            dy2 = 1;
                        }
                        dx2 = 0;
                    }
                    int numerator = longest >> 1;
                    for (int x = 0; x <= longest; x++) {
                        if(!parent.getMaskVisible() || parent.getMaskPosition(x0, y0)) {
                            edit.drawPixel(x0, y0, color);
                        }
                        numerator += shortest;
                        if (numerator >= longest) {
                            numerator -= longest;
                            x0 += dx1;
                            y0 += dy1;
                        } else {
                            x0 += dx2;
                            y0 += dy2;
                        }
                    }
                }
            }, // Pencil tool
            new Tool() {
                private com.pixelpalette.SpecificOptions.EraserOptions eraserOptions;
                private Pixmap undoPixmap;
                private boolean changeMade;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    if(drawMode) {
                        if(undoPixmap == null) {
                            undoPixmap = newCanvasPixmap();
                            undoPixmap.setBlending(Pixmap.Blending.None);
                            undoPixmap.drawPixmap(layers.get(curLayer), 0, 0);
                        }
                        if(edit == null) {
                            edit = newCanvasPixmap();
                            updateEditDraw();
                        }
                        float actualX = (pointerX - getX()) / getWidth();
                        float actualY = 1 - (pointerY - getY()) / getHeight();
                        int width = (int)(actualX * imgW) + 1;
                        int height = (int)(actualY * imgH) + 1;
                        if(actualX < 0) {
                            width--;
                        }
                        if(actualY < 0) {
                            height--;
                        }
                        {
                            if(oldX < 0) {
                                oldX = width;
                                oldY = height;
                            }
                            layers.get(curLayer).setBlending(Pixmap.Blending.None);
                            layers.get(curLayer).setColor(0);
                            Pixmap brush = processor.getBrushes()[eraserOptions.getBrush()];
                            int ofsX = brush.getWidth() / 2 + 1;
                            int ofsY = brush.getWidth() / 2 + 1;
                            if(width >= -ofsX && width <= imgW + ofsX && height >= -ofsY && height <= imgH + ofsY) {
                                changeMade = true;
                            }
                            for(int xi = 0; xi < brush.getWidth(); xi++) {
                                for(int yi = 0; yi < brush.getWidth(); yi++) {
                                    if(brush.getPixel(xi, yi) != -256) { // -256 is 0x00 on an alpha pixmap...
                                        line(oldX + xi - ofsX, oldY + yi - ofsY, width + xi - ofsX, height + yi - ofsY);
                                    }
                                }
                            }
                            updateLayerDrawOnly(curLayer);
                            layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                        }
                        oldX = width;
                        oldY = height;
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    resetDraw();
                    if(changeMade && undoPixmap != null) {
                        processor.clearRedo();
                        UndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(Canvas.this, undoPixmap, curLayer);
                        processor.addToUndo(action);
                        undoPixmap.dispose();
                        updateLayerDraw();
                    }
                    drawMode = true;
                    undoPixmap = null;
                    changeMade = false;
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {
                    if(undoPixmap != null) {
                        layers.get(curLayer).dispose();
                        layers.set(curLayer, newCanvasPixmap());
                        layers.get(curLayer).setBlending(Pixmap.Blending.None);
                        layers.get(curLayer).drawPixmap(undoPixmap, 0, 0);
                        layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                        undoPixmap.dispose();
                        undoPixmap = null;
                        updateLayerDraw(curLayer);
                    }
                }

                @Override
                public ToolOptions getOptions() {
                    return eraserOptions;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.EraserOptions) {
                        eraserOptions = (com.pixelpalette.SpecificOptions.EraserOptions) options;
                    }
                }

                // Code based on the Bresenham algorithm and is based on source code from Tech-Algorithm.com.
                // Tech-Algorithm.com does not state that it needs any credit for its code, but I would
                // rather explain that I used their code as a baseline than experience legal trouble later.
                private void line(int x0, int y0, int x1, int y1) {
                    int w = x1 - x0;
                    int h = y1 - y0;
                    int dx1 = 0;
                    int dy1 = 0;
                    int dx2 = 0;
                    int dy2 = 0;

                    if (w < 0) {
                        dx1 = -1;
                    } else if (w > 0) {
                        dx1 = 1;
                    }
                    if (h < 0) {
                        dy1 = -1;
                    } else if (h > 0) {
                        dy1 = 1;
                    }
                    if (w < 0) {
                        dx2 = -1;
                    } else if (w > 0) {
                        dx2 = 1;
                    }

                    int longest = Math.abs(w);
                    int shortest = Math.abs(h);
                    if (longest <= shortest) {
                        longest = Math.abs(h);
                        shortest = Math.abs(w);
                        if (h < 0){
                            dy2 = -1;
                        } else if (h > 0) {
                            dy2 = 1;
                        }
                        dx2 = 0;
                    }
                    int numerator = longest >> 1;
                    for (int x = 0; x <= longest; x++) {
                        if(!parent.getMaskVisible() || parent.getMaskPosition(x0, y0)) {
                            layers.get(curLayer).drawPixel(x0, y0, 0);
                        }
                        numerator += shortest;
                        if (numerator >= longest) {
                            numerator -= longest;
                            x0 += dx1;
                            y0 += dy1;
                        } else {
                            x0 += dx2;
                            y0 += dy2;
                        }
                    }
                }
            }, // Eraser tool
            new Tool() {
                private int curPixelColor;
                ArrayList<Short> queueX, queueY;
                private com.pixelpalette.SpecificOptions.FillOptions options;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    float actualX = (pointerX - getX()) / getWidth();
                    float actualY = 1 - (pointerY - getY()) / getHeight();
                    int width = (int)(actualX * imgW);
                    int height = (int)(actualY * imgH);
                    if(actualX < 0) {
                        width--;
                    }
                    if(actualY < 0) {
                        height--;
                    }
                    oldX = width;
                    oldY = height;
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    if(drawMode) {
                        processor.clearRedo();
                        com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(Canvas.this, layers.get(curLayer), curLayer);
                        processor.addToUndo(action);
                        int width = (int)pointerX;
                        int height = (int)pointerY;
                        curPixelColor = layers.get(curLayer).getPixel(width, height);
                        switch (options.mode) {
                            case 0:
                                queueX = new ArrayList<Short>();
                                queueY = new ArrayList<Short>();
                                queueX.add((short)width);
                                queueY.add((short)height);
                                while(queueX.size() > 0) {
                                    fillPixel_Contiguous(queueX.get(0), queueY.get(0), 0);
                                    queueX.remove(0);
                                    queueY.remove(0);
                                }
                                layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                                break;
                            case 1:
                                fillPixel_NonContiguous();
                                break;
                            case 2:
                                fillPixel_All();
                                break;
                        }

                        updateLayerDraw(curLayer);
                    }
                    resetDraw();
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {

                }

                @Override
                public ToolOptions getOptions() {
                    return options;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.FillOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.FillOptions)options;
                    }
                }

                private void fillPixel_Contiguous(int x, int y, int stackNum) {
                    Pixmap layer = layers.get(curLayer);
                    if(layer.getPixel(x, y) == color && (color & 0x000000ff) == 255) {
                        return;
                    }
                    if((!parent.getMaskVisible() || parent.getMaskPosition(x, y)) && layer.getPixel(x, y) == curPixelColor && x >= 0 && x < imgW && y >= 0 && y < imgH) {
                        if(stackNum < 63) {
                            if((layer.getPixel(x, y) & 0x000000ff) == 0 || !options.blend) {
                                layer.setBlending(Pixmap.Blending.None);
                            } else {
                                layer.setBlending(Pixmap.Blending.SourceOver);
                            }
                            layer.drawPixel(x, y, color);
                            if(stackNum == 0 & layer.getPixel(x, y) == curPixelColor) { // No change occured
                                return;
                            }
                            if(layer.getPixel(x, y + 1) == curPixelColor && y + 1 < imgH) {
                                fillPixel_Contiguous(x, y + 1, stackNum + 1);
                            }
                            if(layer.getPixel(x, y - 1) == curPixelColor && y - 1 >= 0) {
                                fillPixel_Contiguous(x, y - 1, stackNum + 1);
                            }
                            if(layer.getPixel(x + 1, y) == curPixelColor && x + 1 < imgW) {
                                fillPixel_Contiguous(x + 1, y, stackNum + 1);
                            }
                            if(layer.getPixel(x - 1, y) == curPixelColor && x - 1 >= 0) {
                                fillPixel_Contiguous(x - 1, y, stackNum + 1);
                            }
                        } else {
                            queueX.add((short)x);
                            queueY.add((short)y);
                        }
                    }
                }

                private void fillPixel_NonContiguous() {
                    for(int xi = 0; xi < imgW; xi++) {
                        for(int yi = 0; yi < imgH; yi++) {
                            if((!parent.getMaskVisible() || parent.getMaskPosition(xi, yi)) && layers.get(curLayer).getPixel(xi, yi) == curPixelColor) {
                                if((layers.get(curLayer).getPixel(xi, yi) & 0x000000ff) == 0 || !options.blend) {
                                    layers.get(curLayer).setBlending(Pixmap.Blending.None);
                                } else {
                                    layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                                }
                                layers.get(curLayer).drawPixel(xi, yi, color);
                            }
                        }
                    }
                    layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                }

                private void fillPixel_All() {
                    for(int xi = 0; xi < imgW; xi++) {
                        for(int yi = 0; yi < imgH; yi++) {
                            if(!parent.getMaskVisible() || parent.getMaskPosition(xi, yi)) {
                                if(!options.blend || (layers.get(curLayer).getPixel(xi, yi) & 0x000000ff) == 0) {
                                    layers.get(curLayer).setBlending(Pixmap.Blending.None);
                                } else {
                                    layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                                }
                                layers.get(curLayer).drawPixel(xi, yi, color);
                            }
                        }
                    }
                    layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
                }
            }, // Paint tool
            new Tool() {
                private com.pixelpalette.SpecificOptions.ColorSelectOptions options;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    float actualX = (pointerX - getX()) / getWidth();
                    float actualY = 1 - (pointerY - getY()) / getHeight();
                    oldX = (int)(actualX * imgW);
                    oldY = (int)(actualY * imgH);
                    if(actualX < 0) {
                        oldX--;
                    }
                    if(actualY < 0) {
                        oldY--;
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    if(drawMode) {
                        Pixmap source = options.layerOnly ? layers.get(curLayer) : combined;
                        parent.getColorPicker().setCol(source.getPixel(oldX, oldY));
                        oldX = -1;
                        oldY = -1;
                        if(options.swap) {
                            processor.setCurTool(1);
                        }
                        options.dragDown = false;
                    }
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                    options.curColor = color;
                    Pixmap source = options.layerOnly ? layers.get(curLayer) : combined;
                    options.replaceColor = source.getPixel(oldX, oldY);
                    options.dragDown = drawMode;
                    options.pointerX = pointerX;
                    options.pointerY = pointerY;
                }

                @Override
                public void cancelTouch() {
                    options.dragDown = false;
                }

                @Override
                public ToolOptions getOptions() {
                    return options;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.ColorSelectOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.ColorSelectOptions) options;
                    }
                }
            }, // Color picker
            new Tool() {
                private com.pixelpalette.SpecificOptions.SquareSelectOptions options;
                private int[] x = new int[2], y = new int[2];

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    if(options.processor == null) {
                        options.processor = processor;
                    }

                    if(drawMode) {
                        if(options.multiClick && options.secondTouch) {
                            x[1] = decodePointerX(pointerX) - 1;
                            y[1] = decodePointerY(pointerY) - 1;
                            oldX = x[1];
                            oldY = y[1];
                            options.x[0] = x[0];
                            options.y[0] = y[0];
                            options.x[1] = x[1];
                            options.y[1] = y[1];
                        } else {
                            x[0] = decodePointerX(pointerX) - 1;
                            y[0] = decodePointerY(pointerY) - 1;
                            oldX = x[0];
                            oldY = y[0];
                            options.x[0] = x[0];
                            options.y[0] = y[0];
                            options.x[1] = x[0];
                            options.y[1] = y[0];
                        }
                        options.drawSquare = true;
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    if(drawMode) {
                        if(!options.multiClick || options.secondTouch) {
                            x[1] = oldX;
                            y[1] = oldY;
                            int xLower = (x[0] > x[1]) ? 1 : 0;
                            int yLower = (y[0] > y[1]) ? 1 : 0;
                            boolean[][] area = new boolean[imgW][imgH];
                            for(int xi = 0; xi < imgW; xi++) {
                                for(int yi = 0; yi < imgH; yi++) {
                                    if(xi >= x[xLower] && xi <= x[1 - xLower] && yi >= y[yLower] && yi <= y[1 - yLower]) {
                                        area[xi][yi] = true;
                                    }
                                }
                            }
                            boolean[][] oldMask = new boolean[getImgW()][getImgH()];
                            for(int xi = 0; xi < getImgW(); xi++) {
                                System.arraycopy(parent.getSelectionMask()[xi], 0, oldMask[xi], 0, getImgH());
                            }
                            switch (options.type) {
                                case 0: // Addition
                                    parent.addToSelectionMask(area);
                                    break;
                                case 1: // Subtraction
                                    parent.subtractFromSelectionMask(area);
                                    break;
                                case 2: // Overlap
                                    parent.overlapWithSelectionMask(area);
                                    break;
                                case 3: // Replace
                                    parent.setSelectionMask(area);
                                    break;
                            }
                            com.pixelpalette.SpecificUndoActions.SelectUndoAction action = new com.pixelpalette.SpecificUndoActions.SelectUndoAction(oldMask, parent.getSelectionMask(), parent, Canvas.this);
                            processor.addToUndo(action);
                            processor.clearRedo();
                            options.secondTouch = false;
                        } else {
                            options.secondTouch = true;
                            x[0] = oldX;
                            x[1] = oldX;
                            y[0] = oldY;
                            y[1] = oldY;
                        }
                    }
                    resetDraw();
                    options.drawSquare = options.multiClick && options.secondTouch;
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    if(drawMode) {
                        oldX = decodePointerX(pointerX) - 1;
                        oldY = decodePointerY(pointerY) - 1;
                        options.x[1] = oldX;
                        options.y[1] = oldY;
                        if(options.multiClick && !options.secondTouch) {
                            options.x[0] = oldX;
                            options.y[0] = oldY;
                        }
                    }
                }

                @Override
                public void cancelTouch() {
                    if(options.multiClick && options.secondTouch) {
                        oldX = x[0];
                        oldY = y[0];
                        options.x[1] = x[0];
                        options.y[1] = y[0];
                    } else {
                        options.drawSquare = false;
                    }
                }

                @Override
                public ToolOptions getOptions() {
                    return options;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.SquareSelectOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.SquareSelectOptions) options;
                    }
                }
            }, // Square select tool
            new Tool() {
                private int curPixelColor;
                ArrayList<Short> queueX, queueY;
                private boolean[][] area;
                private com.pixelpalette.SpecificOptions.WandSelectOptions options;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    float actualX = (pointerX - getX()) / getWidth();
                    float actualY = 1 - (pointerY - getY()) / getHeight();
                    int width = (int)(actualX * imgW);
                    int height = (int)(actualY * imgH);
                    if(actualX < 0) {
                        width--;
                    }
                    if(actualY < 0) {
                        height--;
                    }
                    oldX = width;
                    oldY = height;
                    area = new boolean[imgW][imgH];
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    if(drawMode) {
                        int width = (int)pointerX;
                        int height = (int)pointerY;
                        curPixelColor = layers.get(curLayer).getPixel(width, height);
                        switch (options.mode) {
                            case 0:
                                queueX = new ArrayList<Short>();
                                queueY = new ArrayList<Short>();
                                queueX.add((short)width);
                                queueY.add((short)height);
                                while(queueX.size() > 0) {
                                    fillPixel_Contiguous(queueX.get(0), queueY.get(0), 0);
                                    queueX.remove(0);
                                    queueY.remove(0);
                                }
                                break;
                            case 1:
                                fillPixel_NonContiguous();
                                break;
                            case 2:
                                fillPixel_All();
                                break;
                        }

                        boolean[][] oldMask = new boolean[getImgW()][getImgH()];
                        for(int xi = 0; xi < getImgW(); xi++) {
                            for(int yi = 0; yi < getImgH(); yi++) {
                                oldMask[xi][yi] = parent.getSelectionMask()[xi][yi];
                            }
                        }
                        switch (options.type) {
                            case 0: // Addition
                                parent.addToSelectionMask(area);
                                break;
                            case 1: // Subtraction
                                parent.subtractFromSelectionMask(area);
                                break;
                            case 2: // Overlap
                                parent.overlapWithSelectionMask(area);
                                break;
                            case 3: // Replace
                                parent.setSelectionMask(area);
                                break;
                        }
                        com.pixelpalette.SpecificUndoActions.SelectUndoAction action = new com.pixelpalette.SpecificUndoActions.SelectUndoAction(oldMask, parent.getSelectionMask(), parent, Canvas.this);
                        processor.addToUndo(action);
                        processor.clearRedo();
                    }
                    resetDraw();
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {

                }

                @Override
                public ToolOptions getOptions() {
                    return options;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.WandSelectOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.WandSelectOptions) options;
                    }
                }

                private void fillPixel_Contiguous(int x, int y, int stackNum) {
                    Pixmap layer = layers.get(curLayer);
                    if(layer.getPixel(x, y) == curPixelColor && x >= 0 && x < imgW && y >= 0 && y < imgH && !area[x][y]) {
                        if(stackNum < 63) {
                            area[x][y] = true;
                            if(layer.getPixel(x, y + 1) == curPixelColor && y + 1 < imgH) {
                                fillPixel_Contiguous(x, y + 1, stackNum + 1);
                            }
                            if(layer.getPixel(x, y - 1) == curPixelColor && y - 1 >= 0) {
                                fillPixel_Contiguous(x, y - 1, stackNum + 1);
                            }
                            if(layer.getPixel(x + 1, y) == curPixelColor && x + 1 < imgW) {
                                fillPixel_Contiguous(x + 1, y, stackNum + 1);
                            }
                            if(layer.getPixel(x - 1, y) == curPixelColor && x - 1 >= 0) {
                                fillPixel_Contiguous(x - 1, y, stackNum + 1);
                            }
                        } else {
                            queueX.add((short)x);
                            queueY.add((short)y);
                        }
                    }
                }

                private void fillPixel_NonContiguous() {
                    for(int xi = 0; xi < imgW; xi++) {
                        for(int yi = 0; yi < imgH; yi++) {
                            if(layers.get(curLayer).getPixel(xi, yi) == curPixelColor) {
                                area[xi][yi] = true;
                            }
                        }
                    }
                }

                private void fillPixel_All() {
                    for (int xi = 0; xi < imgW; xi++) {
                        for (int yi = 0; yi < imgH; yi++) {
                            area[xi][yi] = true;
                        }
                    }
                }
            }, // Wand select tool
            new Tool() {
                private com.pixelpalette.SpecificOptions.PenSelectOptions options;
                private boolean changeMade;
                private boolean[][] oldArea = null;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    if(drawMode) {
                        if(oldArea == null) {
                            oldArea = new boolean[imgW][imgH];
                            for(int xi = 0; xi < imgW; xi++) {
                                System.arraycopy(parent.getSelectionMask()[xi], 0, oldArea[xi], 0, imgH);
                            }
                        }
                        if(edit == null) {
                            edit = newCanvasPixmap();
                        }
                        float actualX = (pointerX - getX()) / getWidth();
                        float actualY = 1 - (pointerY - getY()) / getHeight();
                        int width = (int)(actualX * imgW) + 1;
                        int height = (int)(actualY * imgH) + 1;
                        if(actualX < 0) {
                            width--;
                        }
                        if(actualY < 0) {
                            height--;
                        }
                        {
                            if(oldX < 0) {
                                oldX = width;
                                oldY = height;
                            }
                            Pixmap brush = processor.getBrushes()[options.getBrush()];
                            int ofsX = brush.getWidth() / 2 + 1;
                            int ofsY = brush.getWidth() / 2 + 1;
                            if(width >= -ofsX && width <= imgW + ofsX && height >= -ofsY && height <= imgH + ofsY) {
                                changeMade = true;
                            }
                            for(int xi = 0; xi < brush.getWidth(); xi++) {
                                for(int yi = 0; yi < brush.getWidth(); yi++) {
                                    if(brush.getPixel(xi, yi) != -256) { // -256 is 0x00 on an alpha pixmap...
                                        line(oldX + xi - ofsX, oldY + yi - ofsY, width + xi - ofsX, height + yi - ofsY);
                                    }
                                }
                            }
                            updateEditDraw();
                        }
                        oldX = width;
                        oldY = height;
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    resetDraw();
                    if(changeMade && oldArea != null) {
                        processor.clearRedo();
                        com.pixelpalette.SpecificUndoActions.SelectUndoAction action = new com.pixelpalette.SpecificUndoActions.SelectUndoAction(oldArea, parent.getSelectionMask(), parent, Canvas.this);
                        processor.addToUndo(action);
                    }
                    parent.updateMaskTexture();
                    drawMode = true;
                    oldArea = null;
                    changeMade = false;
                    if(edit != null) {
                        edit.dispose();
                        edit = null;
                    }
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {
                    if(oldArea != null) {
                        parent.setSelectionMask(oldArea);
                        parent.updateMaskTexture();
                    }
                    if(edit != null) {
                        edit.dispose();
                        edit = null;
                    }
                    oldArea = null;
                }

                @Override
                public ToolOptions getOptions() {
                    return options;
                }

                @Override
                public void setOptions(final ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.PenSelectOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.PenSelectOptions) options;
                    }
                }

                // Code based on the Bresenham algorithm and is based on source code from Tech-Algorithm.com.
                // Tech-Algorithm.com does not state that it needs any credit for its code, but I would
                // rather explain that I used their code as a baseline than experience legal trouble later.
                private void line(int x0, int y0, int x1, int y1) {
                    int w = x1 - x0;
                    int h = y1 - y0;
                    int dx1 = 0;
                    int dy1 = 0;
                    int dx2 = 0;
                    int dy2 = 0;

                    if (w < 0) {
                        dx1 = -1;
                    } else if (w > 0) {
                        dx1 = 1;
                    }
                    if (h < 0) {
                        dy1 = -1;
                    } else if (h > 0) {
                        dy1 = 1;
                    }
                    if (w < 0) {
                        dx2 = -1;
                    } else if (w > 0) {
                        dx2 = 1;
                    }

                    int longest = Math.abs(w);
                    int shortest = Math.abs(h);
                    if (longest <= shortest) {
                        longest = Math.abs(h);
                        shortest = Math.abs(w);
                        if (h < 0){
                            dy2 = -1;
                        } else if (h > 0) {
                            dy2 = 1;
                        }
                        dx2 = 0;
                    }
                    int numerator = longest >> 1;
                    for (int x = 0; x <= longest; x++) {
                        if(x0 >= 0 && x0 < imgW && y0 >= 0 && y0 < imgH) {
                            if(options.mode) { // Addition
                                parent.addPixelToMask(x0, y0);
                            } else { // Subtraction
                                parent.subtractPixelFromMask(x0, y0);
                            }
                            edit.drawPixel(x0, y0, 0x60606060);
                        }
                        numerator += shortest;
                        if (numerator >= longest) {
                            numerator -= longest;
                            x0 += dx1;
                            y0 += dy1;
                        } else {
                            x0 += dx2;
                            y0 += dy2;
                        }
                    }
                }
            }, // Pen select tool
            new Tool() {
                private com.pixelpalette.SpecificOptions.TextOptions options;

                @Override
                public void touchDown(float pointerX, float pointerY) {
                    if(drawMode) {
                        if(!options.getDrawText()) {
                            options.setDrawText(true);
                            options.setTextColor(color);
                        }
                        if(options.getTextPixmap() != null)
                        {
                            float actualX = (pointerX - getX()) / getWidth();
                            float actualY = 1 - (pointerY - getY()) / getHeight();
                            int width = (int)(actualX * imgW);
                            int height = (int)(actualY * imgH) + 1;
                            if(actualX < 0) {
                                width--;
                            }
                            if(actualY < 0) {
                                height--;
                            }
                            oldX = width;
                            oldY = height - options.getTextPixmap().getHeight();
                            options.getTextSprite().setPosition(width * (getWidth() / imgW) + getX(), (-height * (getHeight() / imgH)) + getY() + getHeight());

                            //options.getTextSprite().setSize(getWidth(), getHeight());
                            //options.getTextSprite().setScale(getWidth() * options.getTextPixmap().getWidth() / imgW, getHeight() * options.getTextPixmap().getHeight() / imgH);
                        }
                    }
                }

                @Override
                public void touchUp(float pointerX, float pointerY) {
                    if(drawMode && options.getTextPixmap() != null)
                    {
                        com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(Canvas.this, layers.get(curLayer), curLayer);
                        final Pixmap text = options.getTextPixmap();
                        final Pixmap layer = layers.get(curLayer);
                        for(int xi = 0; xi < text.getWidth(); xi++)
                        {
                            for(int yi = 0; yi < text.getHeight(); yi++)
                            {
                                if((text.getPixel(xi, yi) & 0x000000ff) > 0)
                                {
                                    if((layer.getPixel(xi + oldX, yi + oldY) & 0x000000ff) == 0)
                                    {
                                        layer.setBlending(Pixmap.Blending.None);
                                    } else
                                    {
                                        layer.setBlending(Pixmap.Blending.SourceOver);
                                    }
                                    layer.drawPixel(xi + oldX, yi + oldY, color & text.getPixel(xi, yi));
                                }
                            }
                        }
                        updateLayerDraw(curLayer);
                        processor.addToUndo(action);
                    }
                    oldX = -1;
                    oldY = -1;
                    options.setDrawText(false);
                }

                @Override
                public void touchDragged(float pointerX, float pointerY) {
                    touchDown(pointerX, pointerY);
                }

                @Override
                public void cancelTouch() {
                    oldX = -1;
                    oldY = -1;
                    options.setDrawText(false);
                }

                @Override
                public ToolOptions getOptions() {
                    return null;
                }

                @Override
                public void setOptions(ToolOptions options) {
                    if(options instanceof com.pixelpalette.SpecificOptions.TextOptions) {
                        this.options = (com.pixelpalette.SpecificOptions.TextOptions) options;
                    }
                }
            }, // Text tool
    };

    public Canvas(DrawStage parent, AnimProcessor processor) {
        imgW = 64;
        imgH = 64;
        setup(parent, processor);
    }

    public Canvas(int width, int height, DrawStage parent, AnimProcessor processor) {
        imgW = width;
        imgH = height;
        setup(parent, processor);
    }

    public Canvas(FileHandle handle, DrawStage parent, AnimProcessor processor) {
        boolean notNull = handle != null;
        Pixmap file = null;
        if(notNull) {
            file = new Pixmap(handle);
            imgW = file.getWidth();
            imgH = file.getHeight();
        }
        setup(parent, processor);
        if(notNull) {
            layers.get(0).setBlending(Pixmap.Blending.None);
            layers.get(0).drawPixmap(file, 0, 0);
            layers.get(0).setBlending(Pixmap.Blending.SourceOver);
            file.dispose();
            updateLayerDraw(0);
        }
        curLayer = 0;
    }

    private void setup(final DrawStage parent, final AnimProcessor processor) {
        redrawPaper();
        this.parent = parent;
        this.processor = processor;
        yRatio = 1f * imgH / imgW;
        float width = 9 / yRatio;
        setBounds((PixelApp.width - width) / 2f, (PixelApp.height / 2f) - 4.5f, width, 9);
        stoCount = 0;
        oldX = -1;
        oldY = -1;
        curLayer = 0;
        duration = 1;
        moving = false;
        drawMode = true;
        color = 0x000000ff;
        layerOptions = new ArrayList<>();
        layers = new ArrayList<Pixmap>() {
            @Override
            public boolean add(Pixmap pixmap) {
                boolean ret = super.add(pixmap);
                updateLayersDraw();
                layerOptions.add(new LayerOptions(parent, size() - 1));
                return ret;
            }

            @Override
            public void add(int i, Pixmap pixmap) {
                super.add(i, pixmap);
                layerOptions.add(i, new LayerOptions(parent, i));
                updateLayersDraw();
            }

            @Override
            public Pixmap remove(int i) {
                Pixmap ret = super.remove(i);
                layerOptions.remove(i);
                updateLayersDraw();
                if(curLayer > size() - 1) {
                    curLayer = size() - 1;
                }
                if(curLayer < 0) {
                    curLayer = 0;
                }
                return ret;
            }

            private void updateLayersDraw() {
                layersDraw = new Sprite[size()];
                for(int i = 0; i < size(); i++) {
                    layersDraw[i] = new Sprite(new Texture(get(i)));
                }
            }
        };
        layers.add(newCanvasPixmap());
        edit = newCanvasPixmap();
        editDraw = new Sprite(new Texture(edit));
        updateCombinedDraw();
    }

    public Canvas clone() {
        Canvas shallowCopy;
        try {
            shallowCopy = (Canvas)super.clone();
        } catch (CloneNotSupportedException e) {
            shallowCopy = new Canvas(imgW, imgH, parent, processor);
        }
        for(int i = 0; i < layers.size(); i++) {
            if(shallowCopy.layers.size() <= i) {
                Pixmap layerCopy = copyPixmap(layers.get(i));
                shallowCopy.layers.add(layerCopy);
            } else {
                copyPixmap(layers.get(i), shallowCopy.layers.get(i));
                shallowCopy.updateLayerDraw(i);
            }
        }
        shallowCopy.duration = duration;
        return shallowCopy;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(this == processor.getCurFrame()) {
            paper.setBounds(getX() + (sprOfsX * getWidth() / imgW), getY() + (sprOfsY * getWidth() / imgW), getWidth(), getHeight());
            paper.draw(batch, parentAlpha);
        } else {
            parentAlpha *= 0.3f;
        }
        for(int i = 0; i < layersDraw.length; i++) {
            if(layerOptions.get(i).isLayerVisible()) {
                Sprite spr = layersDraw[i];
                spr.setBounds(getX() + (sprOfsX * getWidth() / imgW), getY() + (sprOfsY * getWidth() / imgW), getWidth(), getHeight());
                spr.setAlpha(layerOptions.get(i).getLayerAlpha());
                spr.draw(batch, parentAlpha);
                if(i == curLayer) {
                    if(!moving) {
                        if(edit != null) {
                            editDraw.setBounds(getX() + (sprOfsX * getWidth() / imgW), getY() + (sprOfsY * getWidth() / imgW), getWidth(), getHeight());
                            editDraw.setAlpha(layerOptions.get(i).getLayerAlpha());
                            editDraw.draw(batch, parentAlpha);
                        }
                    } else {
                        drawMove(moveX, moveY, batch);
                        if(parent.getMoveMenu().looping()) {
                            if(moveX < 0) {
                                int newX = moveX + imgW;
                                drawMove(newX, moveY, batch);
                                if(moveY < 0) {
                                    drawMove(newX, moveY + imgH, batch);
                                    if(moveY < -imgH) {
                                        moveY += imgH;
                                    }
                                } else if(moveY + move.getHeight() > imgH) {
                                    drawMove(newX, moveY - imgH, batch);
                                    if(moveY > imgH) {
                                        moveY -= imgH;
                                    }
                                }
                            } else if(moveX + move.getWidth() > imgW) {
                                int newX = moveX - imgW;
                                drawMove(newX, moveY, batch);
                                if(moveY < 0) {
                                    drawMove(newX, moveY + imgH, batch);
                                    if(moveY < -imgH) {
                                        moveY += imgH;
                                    }
                                } else if(moveY + move.getHeight() > imgH) {
                                    drawMove(newX, moveY - imgH, batch);
                                    if(moveY > imgH) {
                                        moveY -= imgH;
                                    }
                                }
                            }
                            if(moveY < 0) {
                                drawMove(moveX, moveY + imgH, batch);
                                if(moveY < -imgH) {
                                    moveY += imgH;
                                }
                            } else if(moveY + move.getHeight() > imgH) {
                                drawMove(moveX, moveY - imgH, batch);
                                if(moveY > imgH) {
                                    moveY -= imgH;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawMove(int moveX, int moveY, Batch batch) {
        moveDraw.setAlpha(layerOptions.get(curLayer).getLayerAlpha());
        if(moveX < imgW && moveX > -move.getWidth() && moveY > -move.getHeight() && moveY < imgH) {
            float pixelSize = getWidth() / imgW;
            int drawX = moveX;
            int drawY = moveY;
            moveDraw.setRegion(0, 0, move.getWidth(), move.getHeight());
            if(drawX < 0) {
                moveDraw.setRegion(-moveX, 0, moveDraw.getRegionWidth() + moveX, moveDraw.getRegionHeight());
                drawX = 0;
            }
            if(drawY < 0) {
                moveDraw.setRegion(moveDraw.getRegionX(), 0, moveDraw.getRegionWidth(), moveDraw.getRegionHeight() + moveY);
                drawY = 0;
            }
            if(moveX > imgW - move.getWidth()) {
                int cutoff = moveX - (imgW - move.getWidth());
                moveDraw.setRegion(moveDraw.getRegionX(), 0, moveDraw.getRegionWidth() - cutoff, moveDraw.getRegionHeight());
            }
            if(moveY > imgH - move.getHeight()) {
                int cutoff = moveY - (imgH - move.getHeight());
                moveDraw.setRegion(moveDraw.getRegionX(), cutoff, moveDraw.getRegionWidth(), moveDraw.getRegionHeight() - cutoff);
            }
            moveDraw.setBounds(getX() + (sprOfsX * getWidth() / imgW) + (drawX * pixelSize), getY() + (sprOfsY * getWidth() / imgW) + (drawY * pixelSize), getWidth() * moveDraw.getRegionWidth() / imgW, getHeight() * moveDraw.getRegionHeight() / imgH);

            moveDraw.draw(batch);
        }
    }

    private void redrawPaper() {
        if(paper != null && paper.getTexture() != null) {
            paper.getTexture().dispose();
        }
        Pixmap paperTexture = PixelApp.MANAGER.get("Paper.png");
        Pixmap paperPixmap = new Pixmap(imgW * 2, imgH * 2, Pixmap.Format.RGBA8888);
        for(int ix = 0; ix < imgW * 2; ix += 16) {
            for(int iy = 0; iy < imgH * 2; iy += 16) {
                paperPixmap.drawPixmap(paperTexture, ix, iy);
            }
        }
        paper = new Sprite(new Texture(paperPixmap));
    }

    @Override
    public float getX() {
        return processor.getX();
    }

    @Override
    public void setX(float x) {
        processor.setX(x);
        super.setX(x);
    }

    @Override
    public float getY() {
        return processor.getY();
    }

    @Override
    public void setY(float y) {
        processor.setY(y);
        super.setY(y);
    }

    @Override
    public float getWidth() {
        return processor.getWidth();
    }

    @Override
    public void setWidth(float width) {
        processor.setWidth(width);
        super.setWidth(width);
    }

    @Override
    public float getHeight() {
        return processor.getHeight();
    }

    @Override
    public void setHeight(float height) {
        processor.setHeight(height);
        super.setHeight(height);
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
    }

    @Override
    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void moveBy(float x, float y) {
        setX(getX() + x);
        setY(getY() + y);
    }

    public int getSprOfsX() {
        return sprOfsX;
    }

    public void setSprOfsX(int sprOfsX) {
        this.sprOfsX = sprOfsX;
    }

    public int getSprOfsY() {
        return sprOfsY;
    }

    public void setSprOfsY(int sprOfsY) {
        this.sprOfsY = sprOfsY;
    }

    public void handlePointers(int count, float[] pointerX, float[] pointerY) {
        int curTool = processor.getCurTool();
        if(stoCount != count) {
            switch (count) {
                default: // Intentionally empty; should never happen, but is here just in case something changes
                case 0:
                    if(!moving && stoCount == 1)
                        tools[curTool].touchUp(oldX, oldY);
                    drawMode = true;
                    break;
                case 1:
                    if (moving) {
                        oldMoveX = moveX;
                        oldMoveY = moveY;
                        oldX = decodePointerX(pointerX[0] - (sprOfsX * getWidth() / imgW));
                        oldY = decodePointerY(pointerY[0] - (sprOfsY * getWidth() / imgW));
                    } else {
                        tools[curTool].touchDown(pointerX[0] - (sprOfsX * getWidth() / imgW), pointerY[0] - (sprOfsY * getWidth() / imgW));
                    }
                    break;
                case 2:
                {
                    drawMode = false;
                    if(moving) {
                        moveX = oldMoveX;
                        moveY = oldMoveY;
                    } else {
                        tools[curTool].cancelTouch();
                    }
                    if(edit != null) {
                        edit.dispose();
                    }
                    edit = newCanvasPixmap();
                    resetDraw();
                    updateEditDraw();
                    float xDist = pointerX[0] - pointerX[1];
                    float yDist = pointerY[0] - pointerY[1];
                    float dist = (float)Math.sqrt(xDist * xDist + yDist * yDist);
                    touchRatio = getWidth() / dist;
                    touchCenterX = pointerX[0] + xDist / 2f;
                    touchCenterY = pointerY[0] + yDist / 2f;
                }
                break;
            }
            stoCount = count;
        }
    }

    public void doTool(float pointerX, float pointerY) {
        tools[processor.getCurTool()].touchDragged(pointerX - (sprOfsX * getWidth() / imgW), pointerY - (sprOfsY * getWidth() / imgW));
    }

    public void moveCanvas(float[] pointerX, float[] pointerY) {
        if(pointerX.length == 2 && pointerY.length == 2) { // If something goes really wrong, break out
            float xPointerDist = pointerX[0] - pointerX[1];
            float yPointerDist = pointerY[0] - pointerY[1];

            float newTouchCenterX = pointerX[0] + xPointerDist / 2f;
            float newTouchCenterY = pointerY[0] + yPointerDist / 2f;
            moveBy(newTouchCenterX - touchCenterX, newTouchCenterY - touchCenterY);
            touchCenterX = newTouchCenterX;
            touchCenterY = newTouchCenterY;

            float dist = (float)Math.sqrt(xPointerDist * xPointerDist + yPointerDist * yPointerDist);
            float size = dist * touchRatio;
            if(size < imgW / 200f)
                size = imgW / 200f;
            float xCenterDist = touchCenterX - getX();
            float yCenterDist = touchCenterY - getY();
            float sizeRatio = size / getWidth();
            moveBy(xCenterDist * (1 - sizeRatio), yCenterDist * (1 - sizeRatio));
            setSize(size, yRatio * size);

            if(getX() < -size + 0.1f)
                setX(-size + 0.1f);
            if(getX() > PixelApp.width - 0.1f)
                setX(PixelApp.width - 0.1f);
            if(getY() < -yRatio * size + 0.1f)
                setY(-yRatio * size + 0.1f);
            if(getY() > PixelApp.height - 0.1f)
                setY(PixelApp.height - 0.1f);
        }
    }

    public void addLayer() {
        layers.add(curLayer + 1, newCanvasPixmap());
        curLayer++;
    }

    public void updateLayerDraw(int i) {
        layersDraw[i].getTexture().dispose();
        layersDraw[i].setTexture(new Texture(layers.get(i)));
        updateCombinedDraw();
    }

    public void updateLayerDraw() {
        layersDraw[curLayer].getTexture().dispose();
        layersDraw[curLayer].setTexture(new Texture(layers.get(curLayer)));
        updateCombinedDraw();
    }

    public void updateLayerDrawOnly(int i) {
        layersDraw[i].getTexture().dispose();
        layersDraw[i].setTexture(new Texture(layers.get(i)));
        //updateCombinedDraw();
    }

    public void updateLayerDrawOnly() {
        layersDraw[curLayer].getTexture().dispose();
        layersDraw[curLayer].setTexture(new Texture(layers.get(curLayer)));
        //updateCombinedDraw();
    }

    private void updateEditDraw() {
        editDraw.getTexture().dispose();
        editDraw.setTexture(new Texture(edit));
    }

    public void updateCombinedDraw() {
        if(combined != null) {
            combined.dispose();
        }
        combined = getFinalPixmap();
        combinedDraw = new Sprite(new Texture(combined));
    }

    public void resetDraw() {
        oldX = -1;
        oldY = -1;
    }

    private int decodePointerX(float pointerX) {
        float actualX = (pointerX - getX()) / getWidth();
        int ret = (int)(actualX * imgW) + 1;
        if(actualX < 0) {
            ret--;
        }
        return ret;
    }

    private int decodePointerY(float pointerY) {
        float actualY = 1 - (pointerY - getY()) / getHeight();
        int ret = (int)(actualY * imgH) + 1;
        if(actualY < 0) {
            ret--;
        }
        return ret;
    }
    
    public Pixmap newCanvasPixmap() {
        return new Pixmap(imgW, imgH, Pixmap.Format.RGBA8888);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public LayerOptions getLayerOption(int layer) {
        return layerOptions.get(layer);
    }

    public void setLayerOption(int layer, LayerOptions options)
    {
        layerOptions.set(layer, options);
        updateLayerDraw(layer);
    }

    public void setCurLayer(int curLayer) {
        this.curLayer = curLayer;
    }

    public int getCurLayer() {
        return curLayer;
    }

    public Sprite getCombinedDraw() {
        return combinedDraw;
    }

    public void liftSelection() {
        int smallestX = imgW;
        int smallestY = imgH;
        int largestX = 0;
        int largestY = 0;
        edit = newCanvasPixmap();
        copyPixmap(layers.get(curLayer), edit);
        moving = true;
        Pixmap temp = newCanvasPixmap();
        temp.setBlending(Pixmap.Blending.None);
        layers.get(curLayer).setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < imgW; xi++) {
            for(int yi = 0; yi < imgH; yi++) {
                if(parent.getMaskPosition(xi, yi) && layers.get(curLayer).getPixel(xi, yi) != 0) {
                    temp.drawPixel(xi, yi, layers.get(curLayer).getPixel(xi, yi));
                    layers.get(curLayer).drawPixel(xi, yi, 0x00000000);

                    if(xi < smallestX) {
                        smallestX = xi;
                    }
                    if(xi > largestX) {
                        largestX = xi;
                    }
                    if(yi < smallestY) {
                        smallestY = yi;
                    }
                    if(yi > largestY) {
                        largestY = yi;
                    }
                }
            }
        }
        layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
        updateLayerDraw(curLayer);

        int width = largestX - smallestX + 1;
        int height = largestY - smallestY + 1;
        if(width > 0 && height > 0) {
            move = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            move.setBlending(Pixmap.Blending.None);
            for(int xi = 0; xi < width; xi++) {
                for(int yi = 0; yi < height; yi++) {
                    move.drawPixel(xi, yi, temp.getPixel(xi + smallestX, yi + smallestY));
                }
            }
            temp.dispose();

            moveDraw = new Sprite(new Texture(move));
            parent.getSelectOptions().clearSelection();
            com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(this, edit, curLayer);
            processor.addToUndo(action);
            processor.clearRedo();

            moveX = smallestX;
            moveY = imgH - smallestY - height;
            parent.addActor(parent.getMoveMenu());
            resetDraw();
        } else { // If nothing is selected
            edit.dispose();
            edit = newCanvasPixmap();
            temp.dispose();
            layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
            moving = false;
        }
    }

    public void paste(Pixmap clipPixmap) {
        move = new Pixmap(clipPixmap.getWidth(), clipPixmap.getHeight(), Pixmap.Format.RGBA8888);
        copyPixmap(clipPixmap, move);
        parent.minimizeRight();
        if(parent.getDrawMask()) {
            parent.getSelectOptions().clearSelection();
        }
        moving = true;
        moveX = 0;
        moveY = 0;
        moveDraw = new Sprite(new Texture(move));
        parent.addActor(parent.getMoveMenu());
        resetDraw();

        edit = newCanvasPixmap();
        copyPixmap(layers.get(curLayer), edit);
        parent.getSelectOptions().clearSelection();
        com.pixelpalette.SpecificUndoActions.PixmapUndoAction action = new com.pixelpalette.SpecificUndoActions.PixmapUndoAction(this, edit, curLayer);
        processor.addToUndo(action);
        processor.clearRedo();
    }

    public void cancelMove() {
        finishMove();
        processor.undoAction();
        processor.undoAction();
        processor.clearRedo();
    }

    public void finishMove() {
        Pixmap oldPixmap = newCanvasPixmap();
        copyPixmap(edit, oldPixmap);
        edit.dispose();
        edit = newCanvasPixmap();
        putMove();
        if(parent.getMoveMenu().looping()) {
            if (moveX < 0) {
                int newX = moveX + imgW;
                putMove(newX, moveY);
                if (moveY < 0) {
                    putMove(newX, moveY + imgH);
                } else if (moveY + move.getHeight() > imgH) {
                    putMove(newX, moveY - imgH);
                }
            } else if (moveX + move.getWidth() > imgW) {
                int newX = moveX - imgW;
                putMove(newX, moveY);
                if (moveY < 0) {
                    putMove(newX, moveY + imgH);
                } else if (moveY + move.getHeight() > imgH) {
                    putMove(newX, moveY - imgH);
                }
            }
            if (moveY < 0) {
                putMove(moveX, moveY + imgH);
            } else if (moveY + move.getHeight() > imgH) {
                putMove(moveX, moveY - imgH);
            }
        }
        updateLayerDraw(curLayer);
        layers.get(curLayer).setBlending(Pixmap.Blending.SourceOver);
        moving = false;
        move.dispose();
        move = null;
        parent.getMoveMenu().remove();
        resetDraw();
    }

    private void putMove() {
        putMove(moveX, moveY);
    }

    private void putMove(int moveX, int moveY) {
        Pixmap map = layers.get(curLayer);
        for(int xi = 0; xi < imgW; xi++) {
            for(int yi = 0; yi < imgH; yi++) {
                int xPos = xi - moveX;
                int yPos = yi - (imgH - move.getHeight() - moveY);
                if(xPos >= 0 && xPos < move.getWidth() && yPos >= 0 && yPos < move.getHeight()) {
                    if(map.getPixel(xi, yi) == 0) {
                        map.setBlending(Pixmap.Blending.None);
                    } else {
                        map.setBlending(Pixmap.Blending.SourceOver);
                    }
                    map.drawPixel(xi, yi, move.getPixel(xPos, yPos));
                }
            }
        }
    }

    public void resizeMoveWidth(float multiplier) {
        Pixmap temp = new Pixmap((int)(move.getWidth() * multiplier), move.getHeight(), Pixmap.Format.RGBA8888);
        temp.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < temp.getWidth(); xi++) {
            for(int yi = 0; yi < move.getHeight(); yi++) {
                temp.drawPixel(xi, yi, move.getPixel((int)(xi / multiplier), yi));
            }
        }

        move.dispose();
        move = new Pixmap(temp.getWidth(), temp.getHeight(), Pixmap.Format.RGBA8888);
        copyPixmap(temp, move);
        temp.dispose();
        moveDraw.getTexture().dispose();
        moveDraw.setTexture(new Texture(move));
    }

    public void resizeMoveHeight(float multiplier) {
        Pixmap temp = new Pixmap(move.getWidth(), (int)(move.getHeight() * multiplier), Pixmap.Format.RGBA8888);
        temp.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < move.getWidth(); xi++) {
            for(int yi = 0; yi < temp.getHeight(); yi++) {
                temp.drawPixel(xi, yi, move.getPixel(xi, (int)(yi / multiplier)));
            }
        }

        move.dispose();
        move = new Pixmap(temp.getWidth(), temp.getHeight(), Pixmap.Format.RGBA8888);
        copyPixmap(temp, move);
        temp.dispose();
        moveDraw.getTexture().dispose();
        moveDraw.setTexture(new Texture(move));
    }

    public void resizeImage(int width, int height) {
        float multiplierX = (float)width / imgW;
        float multiplierY = (float)height / imgH;
        for(int layer = 0; layer < layers.size(); layer++) {
            Pixmap temp = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            temp.setBlending(Pixmap.Blending.None);
            for(int xi = 0; xi < width; xi++) {
                for(int yi = 0; yi < height; yi++) {
                    temp.drawPixel(xi, yi, layers.get(layer).getPixel((int)(xi / multiplierX), (int)(yi / multiplierY)));
                }
            }

            layers.get(layer).dispose();
            layers.set(layer, new Pixmap(temp.getWidth(), temp.getHeight(), Pixmap.Format.RGBA8888));
            copyPixmap(temp, layers.get(layer));
            temp.dispose();
            updateLayerDraw(layer);
        }

        setImgSizes(width, height);
    }

    public void resizeCanvas(int up, int down, int left, int right) {
        int width = left + imgW + right;
        int height = up + imgH + down;
        for(int layer = 0; layer < layers.size(); layer++) {
            Pixmap temp = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            temp.setBlending(Pixmap.Blending.None);
            for(int xi = 0; xi < imgW; xi++) {
                for(int yi = 0; yi < imgH; yi++) {
                    temp.drawPixel(xi + left, yi + up, layers.get(layer).getPixel(xi, yi));
                }
            }

            layers.get(layer).dispose();
            layers.set(layer, new Pixmap(temp.getWidth(), temp.getHeight(), Pixmap.Format.RGBA8888));
            copyPixmap(temp, layers.get(layer));
            temp.dispose();
            updateLayerDraw(layer);
        }

        setImgSizes(width, height);
    }

    public void setImgSizes(int width, int height) {
        int oldImgW = imgW;
        int oldImgH = imgH;
        imgW = width;
        imgH = height;
        yRatio = 1f * imgH / imgW;
        setSize(getWidth() * (1f * imgW / oldImgW), getHeight() * (1f * imgH / oldImgH));
        redrawPaper();

        parent.resizeSelectionMask(imgW, imgH);

        if(edit != null) {
            edit.dispose();
        }
        edit = newCanvasPixmap();
    }

    public void move(float pointerX, float pointerY) {
        if(drawMode) {
            moveX = oldMoveX + (decodePointerX(pointerX) - oldX);
            moveY = oldMoveY + (oldY - decodePointerY(pointerY));
            if(parent.getMoveMenu().looping()) {
                if(moveX < -imgW) {
                    moveX += imgW;
                }
                if(moveX > imgW) {
                    moveX -= imgW;
                }

                if(moveY < -imgH) {
                    moveY += imgH;
                }
                if(moveY > imgH) {
                    moveY -= imgH;
                }
            }
        }
    }

    public boolean getDrawMode() {
        return drawMode;
    }

    public boolean isMoving() { return moving; }

    public int getImgW() {
        return imgW;
    }

    public int getImgH() {
        return imgH;
    }

    public void setDuration(int dur) {
        duration = dur;
    }

    public int getDuration() {
        return duration;
    }

    public void setAnim(String newAnim) {
        anim = newAnim;
    }

    public String getAnim() {
        return anim;
    }

    public AnimProcessor getProcessor () {
        return processor;
    }

    public Pixmap getFinalPixmap() {
        Pixmap pixmap = newCanvasPixmap();
        for(int i = 0; i < layers.size(); i++) {
            if(layerOptions.get(i).isLayerVisible()) {
                copyPixmap(layers.get(i), pixmap, layerOptions.get(i).getLayerAlpha());
            }
        }
        return pixmap;
    }

    public static void copyPixmap(Pixmap source, Pixmap dest, float visibility) {
        dest.setBlending(Pixmap.Blending.SourceOver);
        if(visibility > 1) {
            visibility = 1;
        }
        if(visibility < 0) {
            visibility = 0;
        }
        for(int xi = 0; xi < source.getWidth(); xi++) {
            for(int yi = 0; yi < source.getHeight(); yi++) {
                if((source.getPixel(xi, yi) & 0x000000ff) != 0) {
                    int pixel = dest.getPixel(xi, yi);
                    if((pixel & 0x000000ff) == 0) {
                        dest.setBlending(Pixmap.Blending.None);
                    } else {
                        dest.setBlending(Pixmap.Blending.SourceOver);
                    }
                    int srcPixel = source.getPixel(xi, yi);
                    int alpha = (int)((srcPixel & 0x000000ff) * visibility);
                    srcPixel = (srcPixel & 0xffffff00) + alpha;
                    dest.drawPixel(xi, yi, srcPixel);
                }
            }
        }
        dest.setBlending(Pixmap.Blending.SourceOver);
    }

    public static void copyPixmap(Pixmap source, Pixmap dest) {
        dest.setBlending(Pixmap.Blending.SourceOver);
        for(int xi = 0; xi < source.getWidth(); xi++) {
            for(int yi = 0; yi < source.getHeight(); yi++) {
                if((source.getPixel(xi, yi) & 0x000000ff) != 0) {
                    int pixel = dest.getPixel(xi, yi);
                    if((pixel & 0x000000ff) == 0) {
                        dest.setBlending(Pixmap.Blending.None);
                    }
                    dest.drawPixel(xi, yi, source.getPixel(xi, yi));
                    if((pixel & 0x000000ff) == 0) {
                        dest.setBlending(Pixmap.Blending.SourceOver);
                    }
                }
            }
        }
    }

    public static Pixmap copyPixmap(Pixmap source) {
        Pixmap dest = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        copyPixmap(source, dest);
        return dest;
    }

    public void dispose() {
        paper.getTexture().dispose();
        if(edit != null) {
            edit.dispose();
        }
        for(Pixmap layer : layers) {
            layer.dispose();
        }
        if(move != null) {
            move.dispose();
        }
    }
}
