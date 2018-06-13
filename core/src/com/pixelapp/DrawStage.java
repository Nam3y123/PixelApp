package com.pixelapp;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.jndi.toolkit.url.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.pixelapp.PixelApp.height;
import static com.pixelapp.PixelApp.ppu;

public class DrawStage extends Stage{
    private AnimProcessor processor;
    private ColorPicker picker;
    private ToolSelector toolSelector;
    private LayerSelector layerSelector;
    private SelectOptions selectOptions;
    private Clipboard clipboard;
    private AnimProcessor.AnimMenu animMenu;
    private MoveMenu moveMenu;
    private Menu menu;
    private int pointersDown;
    private boolean[] pointers;
    private float[] pointerX, pointerY;
    private boolean[][] selectionMask;
    private boolean drawMask;
    private Sprite maskSprite;
    private Actor maskActor;
    private boolean selectionVisible;

    public DrawStage(Viewport viewport) {
        super(viewport);
        pointersDown = 0;
        pointers = new boolean[3];
        pointerX = new float[3];
        pointerY = new float[3];
        drawMask = false;
        selectionVisible = true;
        maskSprite = new Sprite();

        maskActor = new Actor() {

            @Override
            public void draw(Batch batch, float parentAlpha) {
                Canvas canvas = processor.getCurFrame();
                if(drawMask && selectionVisible && !canvas.isMoving()) {
                    maskSprite.setSize(canvas.getWidth(), canvas.getHeight());
                    maskSprite.setPosition(canvas.getX(), canvas.getY());
                    maskSprite.draw(batch);
                }
            }
        };
        processor = new AnimProcessor(this);
        Group canvasGroup = processor.getFrameWrapper();
        addActor(canvasGroup);
        toolSelector = new ToolSelector(this);
        addActor(toolSelector);
        Group toolOptionWrapper = processor.getToolOptionsGroup();
        addActor(toolOptionWrapper);
        UndoRedoButtons undoRedoButtons = new UndoRedoButtons(this);
        addActor(undoRedoButtons);
        picker = new ColorPicker(this);
        addActor(picker);
        layerSelector = new LayerSelector(this);
        addActor(layerSelector);
        selectOptions = new SelectOptions(this);
        addActor(selectOptions);
        clipboard = new Clipboard(this);
        addActor(clipboard);
        animMenu = processor.getAnimMenu();
        addActor(animMenu);

        moveMenu = new MoveMenu(this);

        menu = new Menu(this);
        addActor(menu);
        menu.initButtons();

        selectionMask = new boolean[processor.getImgW()][processor.getImgH()];
    }

    public void newCanvas(int width, int height) {
        processor.reset(width, height);
        getColorPicker().updateCol();
        selectionMask = new boolean[processor.getImgW()][processor.getImgH()];
        drawMask = false;
    }

    public void loadCanvas(FileHandle handle) {
        processor.reset(handle);
        getColorPicker().updateCol();
        selectionMask = new boolean[processor.getImgW()][processor.getImgH()];
        drawMask = false;
    }

    public void loadProject(FileHandle handle) {
        processor.loadProject(handle);
        getColorPicker().updateCol();
        selectionMask = new boolean[processor.getImgW()][processor.getImgH()];
        drawMask = false;
    }

    public void saveProject(FileHandle handle) {
        processor.saveProject(handle);
    }

    public void resizeSelectionMask(int width, int height) {
        selectionMask = new boolean[width][height];
    }

    public void addToSelectionMask(boolean[][] addition) {
        selectionVisible = true;
        drawMask = false;
        Pixmap maskMap = new Pixmap(processor.getImgW(), processor.getImgH(), Pixmap.Format.RGBA8888);
        maskMap.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < processor.getImgW(); xi++) {
            for(int yi = 0; yi < processor.getImgH(); yi++) {
                selectionMask[xi][yi] = selectionMask[xi][yi] || addition[xi][yi];
                if(selectionMask[xi][yi]) {
                    drawMask = true;
                } else {
                    boolean brightCol = (xi + yi) % 6 < 2;
                    maskMap.drawPixel(xi, yi, brightCol ? 0xcc000080 : 0x80000080);
                }
            }
        }

        if(maskSprite.getTexture() != null) {
            maskSprite.getTexture().dispose();
        }
        maskSprite = new Sprite(new Texture(maskMap));
    }

    public void addPixelToMask(int x, int y) {
        selectionMask[x][y] = true;
    }

    public void subtractFromSelectionMask(boolean[][] subtraction) {
        selectionVisible = true;
        drawMask = false;
        Pixmap maskMap = new Pixmap(processor.getImgW(), processor.getImgH(), Pixmap.Format.RGBA8888);
        maskMap.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < selectionMask.length; xi++) {
            for(int yi = 0; yi < selectionMask[xi].length; yi++) {
                selectionMask[xi][yi] = selectionMask[xi][yi] && !subtraction[xi][yi];
                if(selectionMask[xi][yi]) {
                    drawMask = true;
                } else {
                    boolean brightCol = (xi + yi) % 6 < 2;
                    maskMap.drawPixel(xi, yi, brightCol ? 0xcc000080 : 0x80000080);
                }
            }
        }

        if(maskSprite.getTexture() != null) {
            maskSprite.getTexture().dispose();
        }
        maskSprite = new Sprite(new Texture(maskMap));
    }

    public void subtractPixelFromMask(int x, int y) {
        selectionMask[x][y] = false;
    }

    public void overlapWithSelectionMask(boolean[][] overlap) {
        selectionVisible = true;
        drawMask = false;
        Pixmap maskMap = new Pixmap(processor.getImgW(), processor.getImgH(), Pixmap.Format.RGBA8888);
        maskMap.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < selectionMask.length; xi++) {
            for(int yi = 0; yi < selectionMask[xi].length; yi++) {
                selectionMask[xi][yi] = selectionMask[xi][yi] && overlap[xi][yi];
                if(selectionMask[xi][yi]) {
                    drawMask = true;
                } else {
                    boolean brightCol = (xi + yi) % 6 < 2;
                    maskMap.drawPixel(xi, yi, brightCol ? 0xcc000080 : 0x80000080);
                }
            }
        }

        if(maskSprite.getTexture() != null) {
            maskSprite.getTexture().dispose();
        }
        maskSprite = new Sprite(new Texture(maskMap));
    }

    public void setSelectionMask(boolean[][] mask) {
        selectionVisible = true;
        drawMask = false;
        Pixmap maskMap = new Pixmap(processor.getImgW(), processor.getImgH(), Pixmap.Format.RGBA8888);
        maskMap.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < selectionMask.length; xi++) {
            for(int yi = 0; yi < selectionMask[xi].length; yi++) {
                selectionMask[xi][yi] = mask[xi][yi];
                if(selectionMask[xi][yi]) {
                    drawMask = true;
                } else {
                    boolean brightCol = (xi + yi) % 6 < 2;
                    maskMap.drawPixel(xi, yi, brightCol ? 0xcc000080 : 0x80000080);
                }
            }
        }

        if(maskSprite.getTexture() != null) {
            maskSprite.getTexture().dispose();
        }
        maskSprite = new Sprite(new Texture(maskMap));
    }

    public boolean getMaskPosition(int x, int y) {
        boolean ret;
        if(x < 0 || x >= selectionMask.length) {
            ret = false;
        } else if(y < 0 || y >= selectionMask[0].length) {
            ret = false;
        } else {
            ret = selectionMask[x][y];
        }
        return ret;
    }

    public void updateMaskTexture() {
        drawMask = false;
        Pixmap maskMap = new Pixmap(processor.getImgW(), processor.getImgH(), Pixmap.Format.RGBA8888);
        maskMap.setBlending(Pixmap.Blending.None);
        for(int xi = 0; xi < selectionMask.length; xi++) {
            for(int yi = 0; yi < selectionMask[xi].length; yi++) {
                if(selectionMask[xi][yi]) {
                    drawMask = true;
                } else {
                    boolean brightCol = (xi + yi) % 6 < 2;
                    maskMap.drawPixel(xi, yi, brightCol ? 0xcc000080 : 0x80000080);
                }
            }
        }

        if(maskSprite.getTexture() != null) {
            maskSprite.getTexture().dispose();
        }
        maskSprite = new Sprite(new Texture(maskMap));
    }

    @Override
    public void act() {
        handlePointers();
        super.act();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean ret = super.touchDown(screenX, screenY, pointer, button);
        if(!ret) {
            if(pointer < pointers.length && !pointers[pointer]) {
                pointersDown++;
                pointers[pointer] = true;
                pointerX[pointer] = screenX / ppu;
                pointerY[pointer] = height - screenY / ppu;
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean ret = super.touchDragged(screenX, screenY, pointer);
        if(pointer < pointers.length && pointers[pointer]) {
            pointerX[pointer] = screenX / ppu;
            pointerY[pointer] = height - screenY / ppu;
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean ret = super.touchUp(screenX, screenY, pointer, button);
        if(!ret && pointer < pointers.length && pointers[pointer]) {
            pointersDown--;
            pointers[pointer] = false;
            ret = true;
        }
        return ret;
    }

    private void handlePointers() {
        int count = 0;
        for(int i = 0; i < 3; i++) {
            if(pointers[i]) {
                count++;
            }
        }
        float[] usedPointerX = new float[count];
        float[] usedPointerY = new float[count];
        for(int i = 0, j = 0; i < 3; i++) {
            if(pointers[i]) {
                usedPointerX[j] = pointerX[i];
                usedPointerY[j] = pointerY[i];
                j++;
            }

        }
        processor.getCurFrame().handlePointers(count, usedPointerX, usedPointerY);
        switch (count) {
            default: // Intentionally empty; should never happen, but is here just in case something changes
            case 0:
                break;
            case 1:
                if(processor.getCurFrame().isMoving()) {
                    processor.getCurFrame().move(usedPointerX[0], usedPointerY[0]);
                } else {
                    processor.getCurFrame().doTool(usedPointerX[0], usedPointerY[0]);
                }
                break;
            case 2:
                processor.getCurFrame().moveCanvas(usedPointerX, usedPointerY);
                break;
        }
    }

    public boolean rightMinimized() {
        return layerSelector.isMinimized()
                && selectOptions.isMinimized()
                && clipboard.isMinimized()
                && animMenu.isMinimized()
                && !processor.getCurFrame().isMoving();
    }

    public void minimizeRight() {
        layerSelector.minimize();
        selectOptions.minimize();
        clipboard.minimize();
        animMenu.minimize();
    }

    public void setSelectionVisible(boolean selectionVisible) {
        this.selectionVisible = selectionVisible;
    }

    public Menu getMenu() {
        return menu;
    }

    public Canvas getCanvas() {
        return processor.getCurFrame();
    }

    public AnimProcessor getProcessor() {
        return processor;
    }

    public ColorPicker getColorPicker() { return picker; }

    public LayerSelector getLayerSelector() { return layerSelector; }

    public SelectOptions getSelectOptions() { return selectOptions; }

    public Clipboard getClipboard() { return clipboard; }

    public AnimProcessor.AnimMenu getAnimMenu() {
        return animMenu;
    }

    public MoveMenu getMoveMenu() { return moveMenu; }

    public boolean[][] getSelectionMask() { return selectionMask; }

    public boolean getMaskVisible() {
        return drawMask && selectionVisible;
    }

    public boolean getDrawMask() {
        return drawMask;
    }

    public Actor getMaskActor() {
        return maskActor;
    }

    public boolean isDrawing() {
        return (processor.getCurFrame().getDrawMode() && pointersDown == 1) || processor.getCurFrame().isMoving();
    }

    @Override
    public void dispose() {
        super.dispose();
        clipboard.dispose();
        processor.dispose();
        TextPrinter.fontPixmap.dispose();
    }


    private void galleryAddPic() {

    }
}
