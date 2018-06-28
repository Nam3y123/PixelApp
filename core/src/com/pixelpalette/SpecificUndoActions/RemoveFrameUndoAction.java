package com.pixelpalette.SpecificUndoActions;

import com.pixelpalette.Canvas;
import com.pixelpalette.UndoAction;

public class RemoveFrameUndoAction extends UndoAction {
    private Canvas frame;
    private int pos;
    private com.pixelpalette.AnimProcessor processor;

    public RemoveFrameUndoAction(Canvas newFrame, int pos, com.pixelpalette.AnimProcessor processor) {
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
