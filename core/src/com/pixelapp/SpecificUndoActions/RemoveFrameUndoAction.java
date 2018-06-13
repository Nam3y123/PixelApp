package com.pixelapp.SpecificUndoActions;

import com.pixelapp.AnimProcessor;
import com.pixelapp.Canvas;
import com.pixelapp.UndoAction;

public class RemoveFrameUndoAction extends UndoAction {
    private Canvas frame;
    private int pos;
    private AnimProcessor processor;

    public RemoveFrameUndoAction(Canvas newFrame, int pos, AnimProcessor processor) {
        frame = newFrame;
        this.processor = processor;
        this.pos = pos;
    }

    @Override
    public void undo() {
        processor.setLockUndoStream(true);
        processor.addFrame(pos);
        processor.setFrame(pos, frame);
        processor.setLockUndoStream(false);

        processor.addToRedo(new RemoveFrameUndoAction(frame, pos, processor));
    }

    @Override
    public void redo() {
        processor.setLockUndoStream(true);
        processor.removeFrame(pos);
        processor.setLockUndoStream(false);

        processor.addToUndo(new RemoveFrameUndoAction(frame, pos, processor));
    }

    @Override
    public void dispose() {
        //frame.dispose();
    }
}
