package com.pixelapp.SpecificUndoActions;

import com.badlogic.gdx.graphics.Pixmap;
import com.pixelapp.Canvas;
import com.pixelapp.DrawStage;
import com.pixelapp.UndoAction;

public class PixmapUndoAction extends UndoAction {
    private Pixmap storePixmap;
    private int layerNum;

    public PixmapUndoAction(Canvas canvas, Pixmap storePixmap, int layerNum) {
        this.canvas = canvas;
        this.storePixmap = new Pixmap(canvas.getImgW(), canvas.getImgH(), Pixmap.Format.RGBA8888);
        this.storePixmap.setBlending(Pixmap.Blending.None);
        this.storePixmap.drawPixmap(storePixmap, 0, 0);
        this.layerNum = layerNum;
    }

    public void addToUndo() {

    }

    public void addToRedo() {

    }

    public void undo() {
        PixmapUndoAction action = new PixmapUndoAction(canvas, canvas.layers.get(layerNum), layerNum);
        canvas.getProcessor().addToRedo(action);
        canvas.layers.set(layerNum, new Pixmap(canvas.getImgW(), canvas.getImgH(), Pixmap.Format.RGBA8888));
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.None);
        canvas.layers.get(layerNum).drawPixmap(storePixmap, 0, 0);
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.SourceOver);
        canvas.updateLayerDraw(layerNum);
    }

    public void redo() {
        PixmapUndoAction action = new PixmapUndoAction(canvas, canvas.layers.get(layerNum), layerNum);
        canvas.getProcessor().addToUndo(action);
        canvas.layers.set(layerNum, new Pixmap(canvas.getImgW(), canvas.getImgH(), Pixmap.Format.RGBA8888));
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.None);
        canvas.layers.get(layerNum).drawPixmap(storePixmap, 0, 0);
        canvas.layers.get(layerNum).setBlending(Pixmap.Blending.SourceOver);
        canvas.updateLayerDraw(layerNum);
    }

    @Override
    public void dispose() {
        storePixmap.dispose();
    }
}
