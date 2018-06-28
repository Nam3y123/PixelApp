package com.pixelpalette.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;

public class RemoveLayerUndoAction extends com.pixelpalette.UndoAction {
    private int layerNum;
    private Pixmap layer;

    public RemoveLayerUndoAction(com.pixelpalette.Canvas canvas, int layerNum, Pixmap layer) {
        this.canvas = canvas;
        this.layerNum = layerNum;
        this.layer = layer;
    }

    public RemoveLayerUndoAction(com.pixelpalette.Canvas canvas, int layerNum) {
        this.canvas = canvas;
        this.layerNum = layerNum;
    }

    @Override
    public void undo() {
        canvas.layers.add(layerNum, canvas.newCanvasPixmap());
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.None);
        canvas.layers.get(layerNum).drawPixmap(layer, 0, 0);
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.SourceOver);
        canvas.updateLayerDraw(layerNum);
        RemoveLayerUndoAction action = new RemoveLayerUndoAction(canvas, layerNum);
        canvas.getProcessor().addToRedo(action);
        canvas.setCurLayer(layerNum);
    }

    @Override
    public void redo() {
        RemoveLayerUndoAction action = new RemoveLayerUndoAction(canvas, layerNum, canvas.layers.get(layerNum));
        canvas.getProcessor().addToUndo(action);
        canvas.layers.remove(layerNum);
    }

    @Override
    public void dispose() {
        if(layer != null) {
            layer.dispose();
        }
    }
}
