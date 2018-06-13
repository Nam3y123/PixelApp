package com.pixelapp.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelapp.Canvas;
import com.pixelapp.UndoAction;

public class MoveLayerUndoAction extends UndoAction {
    private int movedLayer, layerDest;
    private Canvas canvas;

    public MoveLayerUndoAction(Canvas canvas, int movedLayer, int layerDest) {
        this.movedLayer = movedLayer;
        this.layerDest = layerDest;
        this.canvas = canvas;
    }

    @Override
    public void undo() {
        Pixmap temp = canvas.layers.get(movedLayer);
        canvas.layers.remove(movedLayer);
        canvas.layers.add(layerDest - 0, temp);

        MoveLayerUndoAction action = new MoveLayerUndoAction(canvas, layerDest, movedLayer);
        canvas.getProcessor().addToRedo(action);
    }

    @Override
    public void redo() {
        Pixmap temp = canvas.layers.get(movedLayer);
        canvas.layers.remove(movedLayer);
        canvas.layers.add(layerDest - 0, temp);

        MoveLayerUndoAction action = new MoveLayerUndoAction(canvas, layerDest, movedLayer);
        canvas.getProcessor().addToUndo(action);
    }
}
