package com.pixelpalette.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelpalette.Canvas;
import com.pixelpalette.LayerOptions;
import com.pixelpalette.UndoAction;

public class MoveLayerUndoAction extends UndoAction {
    private int movedLayer, layerDest;
    private Canvas canvas;

    public MoveLayerUndoAction(Canvas canvas, int movedLayer, int layerDest) {
        this.movedLayer = movedLayer;
        this.layerDest = layerDest;
        this.canvas = canvas;
    }

    @Override
    public void undo()
    {
        swap();

        MoveLayerUndoAction action = new MoveLayerUndoAction(canvas, layerDest, movedLayer);
        canvas.getProcessor().addToRedo(action);
    }

    @Override
    public void redo()
    {
        swap();

        MoveLayerUndoAction action = new MoveLayerUndoAction(canvas, layerDest, movedLayer);
        canvas.getProcessor().addToUndo(action);
    }

    private void swap()
    {
        final LayerOptions options = canvas.getLayerOption(movedLayer);

        Pixmap temp = canvas.layers.get(movedLayer);
        canvas.layers.remove(movedLayer);
        canvas.layers.add(layerDest, temp);
        canvas.setLayerOption(layerDest, options);
    }
}
