package com.pixelpalette.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelpalette.Canvas;

public class ResizeUndoAction extends com.pixelpalette.UndoAction {
    private com.pixelpalette.AnimProcessor processor;
    private Canvas[] frames;
    private int imgW, imgH;
    private Pixmap[][] layers;

    public ResizeUndoAction(com.pixelpalette.AnimProcessor processor, int width, int height) {
        imgW = width;
        imgH = height;
        frames = processor.getFrames();
        this.processor = processor;

        layers = new Pixmap[frames.length][];
        for(int frameI = 0; frameI < frames.length; frameI++) {
            Canvas canvas = frames[frameI];
            layers[frameI] = new Pixmap[canvas.layers.size()];
            for(int i = 0; i < canvas.layers.size(); i++) {
                Pixmap layer = canvas.layers.get(i);
                layers[frameI][i] = canvas.newCanvasPixmap();
                Canvas.copyPixmap(layer, layers[frameI][i]);
            }
        }
    }

    @Override
    public void undo() {
        ResizeUndoAction action = new ResizeUndoAction(processor, processor.getImgW(), processor.getImgH());
        processor.addToRedo(action);
        moveLayers();
    }

    @Override
    public void redo() {
        ResizeUndoAction action = new ResizeUndoAction(processor, processor.getImgW(), processor.getImgH());
        processor.addToUndo(action);
        moveLayers();
    }

    private void moveLayers() {
        for(int frameI = 0; frameI < frames.length; frameI++) {
            for(int i = 0; i < layers[frameI].length; i++) {
                frames[frameI].layers.get(i).dispose();
                frames[frameI].layers.set(i, new Pixmap(imgW, imgH, Pixmap.Format.RGBA8888));
                Canvas.copyPixmap(layers[frameI][i], frames[frameI].layers.get(i));
                frames[frameI].updateLayerDraw(i);
            }
        }
        processor.setImgSizes(imgW, imgH);
    }

    @Override
    public void dispose() {
        for(int frameI = 0; frameI < frames.length; frameI++) {
            for(int i = 0; i < layers[frameI].length; i++) {
                layers[frameI][i].dispose();
            }
        }
        super.dispose();
    }
}
