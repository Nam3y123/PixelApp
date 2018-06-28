package com.pixelpalette.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelpalette.Canvas;

public class CanvasFlipUndoAction extends com.pixelpalette.UndoAction {
    private com.pixelpalette.AnimProcessor parent;
    private Pixmap[] layers;

    public CanvasFlipUndoAction(com.pixelpalette.AnimProcessor parent, Canvas canvas)
    {
        this.canvas = canvas;
        this.parent = parent;
        int len = canvas.layers.size();
        this.layers = new Pixmap[len];
        for (int i = 0; i < len; i++)
        {
            layers[i] = Canvas.copyPixmap(canvas.layers.get(i));
        }
    }

    @Override
    public void undo() {
        CanvasFlipUndoAction action = new CanvasFlipUndoAction(parent, canvas);
        parent.addToRedo(action);
        action();
    }

    @Override
    public void redo() {
        CanvasFlipUndoAction action = new CanvasFlipUndoAction(parent, canvas);
        parent.addToUndo(action);
        action();
    }

    private void action()
    {
        for (int i = 0; i < layers.length; i++)
        {
            canvas.layers.set(i, Canvas.copyPixmap(layers[i]));
            layers[i].dispose();
            canvas.updateLayerDrawOnly(i);
        }
        canvas.updateCombinedDraw();
    }
}
