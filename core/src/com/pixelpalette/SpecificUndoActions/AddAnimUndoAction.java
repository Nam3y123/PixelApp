package com.pixelpalette.SpecificUndoActions;

import com.pixelpalette.Canvas;

public class AddAnimUndoAction extends com.pixelpalette.UndoAction {
    private com.pixelpalette.AnimProcessor processor;
    private String anim;
    private boolean cascade;

    public AddAnimUndoAction(Canvas canvas, com.pixelpalette.AnimProcessor parent, boolean cascade) {
        this.canvas = canvas;
        this.processor = parent;
        this.cascade = cascade;

        anim = canvas.getAnim();
    }

    @Override
    public void undo() {
        processor.addToRedo(new AddAnimUndoAction(canvas, processor, cascade));
        setAnim();
    }

    @Override
    public void redo() {
        processor.addToUndo(new AddAnimUndoAction(canvas, processor, cascade));
        setAnim();
    }

    private void setAnim()
    {
        if(cascade)
        {
            if (canvas.getAnim() == null)
            {
                processor.addAnimCascade(anim, processor.getPosOfFrame(canvas));
            }
            else
            {
                processor.renameAnim(anim, processor.getPosOfFrame(canvas));
            }
        }
        else
        {
            canvas.setAnim(anim);
        }
    }
}
