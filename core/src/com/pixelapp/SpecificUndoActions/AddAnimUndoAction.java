package com.pixelapp.SpecificUndoActions;

import com.pixelapp.AnimProcessor;
import com.pixelapp.Canvas;
import com.pixelapp.UndoAction;

public class AddAnimUndoAction extends UndoAction {
    private AnimProcessor processor;
    private String anim;

    public AddAnimUndoAction(Canvas canvas, AnimProcessor parent) {
        this.canvas = canvas;
        this.processor = parent;

        anim = canvas.getAnim();
    }

    @Override
    public void undo() {
        processor.addToRedo(new AddAnimUndoAction(canvas, processor));
        canvas.setAnim(anim);
    }

    @Override
    public void redo() {
        processor.addToUndo(new AddAnimUndoAction(canvas, processor));
        canvas.setAnim(anim);
    }
}
