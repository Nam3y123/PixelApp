package com.pixelapp.SpecificUndoActions;

import com.pixelapp.Canvas;
import com.pixelapp.DrawStage;
import com.pixelapp.UndoAction;

public class SelectUndoAction extends UndoAction {
    private boolean[][] newMask, oldMask;
    private DrawStage parent;
    private Canvas canvas;

    public SelectUndoAction(boolean[][] oldMask, boolean[][] newMask, DrawStage parent, Canvas canvas) {
        this.newMask = new boolean[canvas.getImgW()][canvas.getImgH()];
        this.oldMask = new boolean[canvas.getImgW()][canvas.getImgH()];
        for(int xi = 0; xi < canvas.getImgW(); xi++) {
            for(int yi = 0; yi < canvas.getImgH(); yi++) {
                this.oldMask[xi][yi] = oldMask[xi][yi];
                this.newMask[xi][yi] = newMask[xi][yi];
            }
        }
        this.parent = parent;
        this.canvas = canvas;
    }

    @Override
    public void undo() {
        SelectUndoAction action = new SelectUndoAction(newMask, oldMask, parent, canvas);
        canvas.getProcessor().addToRedo(action);

        parent.setSelectionMask(oldMask);
    }

    @Override
    public void redo() {
        SelectUndoAction action = new SelectUndoAction(newMask, oldMask, parent, canvas);
        canvas.getProcessor().addToUndo(action);

        parent.setSelectionMask(oldMask);
    }
}
