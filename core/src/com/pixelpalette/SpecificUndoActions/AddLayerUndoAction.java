package com.pixelpalette.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelpalette.Canvas;

public class AddLayerUndoAction extends com.pixelpalette.UndoAction {
    private int layerNum;
    private Pixmap layer;

    public AddLayerUndoAction(Canvas canvas, int layerNum) {
        this.canvas = canvas;
        this.layerNum = layerNum;
    }

    public AddLayerUndoAction(Canvas canvas, int layerNum, Pixmap layer) {
        this.canvas = canvas;
        this.layerNum = layerNum;
        this.layer = layer;
    }

    @Override
    public void undo() {
        AddLayerUndoAction action = new AddLayerUndoAction(canvas, layerNum, canvas.layers.get(layerNum));
        canvas.getProcessor().addToRedo(action);
        canvas.layers.remove(layerNum);
    }

    @Override
    public void redo() {
        canvas.layers.add(layerNum, canvas.newCanvasPixmap());
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.None);
        canvas.layers.get(layerNum).drawPixmap(layer, 0, 0);
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.SourceOver);
        AddLayerUndoAction action = new AddLayerUndoAction(canvas, layerNum);
        canvas.getProcessor().addToUndo(action);
    }

    @Override
    public void dispose() {
        if(layer != null) {
            layer.dispose();
        }
    }
}
