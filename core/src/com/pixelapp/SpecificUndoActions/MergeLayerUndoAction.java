package com.pixelapp.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelapp.Canvas;
import com.pixelapp.DrawStage;
import com.pixelapp.UndoAction;

public class MergeLayerUndoAction extends UndoAction {
    private RemoveLayerUndoAction removeAction;
    private PixmapUndoAction pixmapAction;
    private Canvas canvas;
    private int upperLayerNum;

    public MergeLayerUndoAction(Canvas canvas, int upperLayerNum) {
        this.canvas = canvas;
        this.upperLayerNum = upperLayerNum;
        if(upperLayerNum > 0) {
            Pixmap upperPixmap = canvas.newCanvasPixmap();
            Canvas.copyPixmap(canvas.layers.get(upperLayerNum), upperPixmap);

            Pixmap lowerPixmap = canvas.newCanvasPixmap();
            Canvas.copyPixmap(canvas.layers.get(upperLayerNum - 1), lowerPixmap);
            removeAction = new RemoveLayerUndoAction(canvas, upperLayerNum, upperPixmap);
            pixmapAction = new PixmapUndoAction(canvas, lowerPixmap, upperLayerNum - 1);
        }
    }

    @Override
    public void undo() {
        canvas.getProcessor().setLockUndoStream(true);
        removeAction.undo();
        pixmapAction.undo();
        canvas.getProcessor().setLockUndoStream(false);

        MergeLayerRedoAction action = new MergeLayerRedoAction(canvas, upperLayerNum);
        canvas.getProcessor().addToRedo(action);
    }

    @Override
    public void dispose() {
        removeAction.dispose();
        pixmapAction.dispose();
        super.dispose();
    }

    private class MergeLayerRedoAction extends UndoAction {
        private Canvas canvas;
        private int upperLayerNum;

        public MergeLayerRedoAction(Canvas canvas, int upperLayerNum) {
            this.canvas = canvas;
            this.upperLayerNum = upperLayerNum;
        }

        @Override
        public void redo() {
            MergeLayerUndoAction action = new MergeLayerUndoAction(canvas, upperLayerNum);
            canvas.getProcessor().addToUndo(action);

            Canvas.copyPixmap(canvas.layers.get(upperLayerNum), canvas.layers.get(upperLayerNum - 1));
            canvas.layers.remove(upperLayerNum);
            canvas.setCurLayer(canvas.getCurLayer() - 1);
        }
    }
}
