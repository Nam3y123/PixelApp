package com.pixelapp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Timer;
import com.pixelapp.SpecificOptions.ColorSelectOptions;
import com.pixelapp.SpecificOptions.EraserOptions;
import com.pixelapp.SpecificOptions.FillOptions;
import com.pixelapp.SpecificOptions.PenSelectOptions;
import com.pixelapp.SpecificOptions.PencilOptions;
import com.pixelapp.SpecificOptions.SquareSelectOptions;
import com.pixelapp.SpecificOptions.TextOptions;
import com.pixelapp.SpecificOptions.WandSelectOptions;
import com.pixelapp.SpecificUndoActions.AddAnimUndoAction;
import com.pixelapp.SpecificUndoActions.AddFrameUndoAction;
import com.pixelapp.SpecificUndoActions.RemoveFrameUndoAction;
import com.pixelapp.SpecificUndoActions.ResizeUndoAction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.pixelapp.Canvas.toolLen;
import static com.pixelapp.PixelApp.pixelSize;

public class AnimProcessor {
    private Canvas[] frames;
    //private ArrayList<Anim> animations;
    private DrawStage parent;
    private int framePos;
    private Group frameWrapper;
    private Group toolOptionsGroup;
    private int imgW, imgH;
    private UndoAction[] undo, redo;
    private int undoSize;
    private boolean lockUndoStream;
    private ToolOptions[] toolOptions;
    private int curTool;
    private float x, y;
    private float width, height;
    private AnimMenu menu;
    private Pixmap[] brushes;
    private boolean animRunning;
    private boolean drawFrameBefore, drawFrameAfter;

    // REMEMBER: Clean any new parameters

    public AnimProcessor(DrawStage parent) {
        imgW = 64;
        imgH = 64;
        setup(parent);
    }

    private void setup(DrawStage parent) {
        this.parent = parent;

        lockUndoStream = false;
        animRunning = false;
        undoSize = 25;
        undo = new UndoAction[undoSize];
        redo = new UndoAction[undoSize];
        if(frameWrapper == null) {
            frameWrapper = new Group();
        }

        brushes = new Pixmap[] {
                new Pixmap(1, 1, Pixmap.Format.Alpha),
                new Pixmap(3, 3, Pixmap.Format.Alpha),
        };
        brushes[0].setColor(0xff);
        brushes[0].drawPixel(0, 0);
        brushes[1].setColor(0xff);
        brushes[1].fill();
        brushes[1].setBlending(Pixmap.Blending.None);
        brushes[1].drawPixel(0, 0, 0x00);
        brushes[1].drawPixel(0, 2, 0x00);
        brushes[1].drawPixel(2, 0, 0x00);
        brushes[1].drawPixel(2, 2, 0x00);

        if(toolOptionsGroup == null) {
            toolOptionsGroup = new Group();
            curTool = 1;
            resetToolOptions();
        } else {
            setupToolOptionsGroup();
        }
        frames = null;
        addFrame();
        addAnim(null);

        menu = new AnimMenu(parent);

        updateFrameWrapper();
    }

    public void reset(int width, int height) {
        dispose();
        clean();
        imgW = width;
        imgH = height;
        setup(parent);
    }

    public void reset(FileHandle handle) {
        dispose();
        clean();

        boolean notNull = handle != null;
        Pixmap file;
        if(notNull) {
            file = new Pixmap(handle);
            imgW = file.getWidth();
            imgH = file.getHeight();
            file.dispose();
        }
        setup(parent);
        if(notNull) {
            frames[0].dispose();
            frames[0] = new Canvas(handle, parent, this);
            addToolOptions(0);
        }
        updateFrameWrapper();
    }

    public void updateFrameWrapper() {
        frameWrapper.clearChildren();
        frameWrapper.addActor(frames[framePos]);
        String anim = frames[framePos].getAnim();
        if(anim != null) {
            if(drawFrameBefore && framePos > 0 && anim.equals(frames[framePos - 1].getAnim())) {
                frameWrapper.addActor(frames[framePos - 1]);
            }
            if(drawFrameAfter && framePos < frames.length - 1 && anim.equals(frames[framePos + 1].getAnim())) {
                frameWrapper.addActor(frames[framePos + 1]);
            }
        }

        frameWrapper.addActor(parent.getMaskActor());

        if(parent.getColorPicker() != null) {
            parent.getColorPicker().updateCol();
        }
    }

    public void resetToolOptions() {
        for(int i = 0; i < toolLen; i++) {
            if(toolOptions != null) {
                toolOptions[i].dispose();
            }
        }
        toolOptions = new ToolOptions[toolLen];
        toolOptions[0] = new PencilOptions();
        toolOptions[1] = new EraserOptions();
        toolOptions[2] = new FillOptions();
        toolOptions[3] = new ColorSelectOptions();
        toolOptions[4] = new SquareSelectOptions();
        toolOptions[5] = new WandSelectOptions();
        toolOptions[6] = new PenSelectOptions();
        toolOptions[7] = new TextOptions(this);
        setupToolOptionsGroup();
    }

    private void setupToolOptionsGroup() {
        toolOptionsGroup.clearChildren();
        for(int i = 0; i < toolLen; i++) {
            toolOptionsGroup.addActor(toolOptions[i]);
        }
        toolOptions[curTool - 1].setVisible(true);
    }

    public Group getToolOptionsGroup() {
        return toolOptionsGroup;
    }

    public void addFrame() {
        if(frames == null) {
            frames = new Canvas[1];
            frames[0] = new Canvas(imgW, imgH, parent, this);
            framePos = 0;
            addToolOptions(framePos);
        } else {
            addFrame(frames.length);
        }
    }

    public void addFrame(int pos) {
        if(frames != null) {
            int len = frames.length;
            Canvas[] stoFrames = new Canvas[len];
            System.arraycopy(frames, 0, stoFrames, 0, len);
            frames = new Canvas[len + 1];

            if(pos > len) {
                pos = len;
            }
            if(pos < 0) {
                pos = 0;
            }

            System.arraycopy(stoFrames, 0, frames, 0, pos);
            frames[pos] = new Canvas(imgW, imgH, parent, this);
            if(len - pos > 0) {
                System.arraycopy(stoFrames, pos, frames, pos + 1, len - pos);
            }
            framePos = pos;
            addToolOptions(pos);
            updateFrameWrapper();

            AddFrameUndoAction action = new AddFrameUndoAction(frames[pos], pos, this);
            addToUndo(action);
            if(!lockUndoStream) {
                clearRedo();
            }
        }
    }

    public void copyFrame(int srcPos) {
        if(srcPos >= 0 && srcPos < frames.length) {
            setLockUndoStream(true);
            addFrame();
            frames[frames.length - 1] = frames[srcPos].clone();
            addToolOptions(frames.length - 1);
            updateFrameWrapper();

            setLockUndoStream(false);
            AddFrameUndoAction action = new AddFrameUndoAction(frames[frames.length - 1], frames.length - 1, this);
            addToUndo(action);
            clearRedo();
        }
    }

    public void copyFrame(int srcPos, int destPos) {
        if(srcPos >= 0 && srcPos < frames.length && destPos >= 0 && destPos < frames.length + 1) {
            setLockUndoStream(true);
            addFrame(destPos);
            frames[destPos] = frames[srcPos].clone();
            addToolOptions(destPos);
            updateFrameWrapper();
            framePos = destPos;

            setLockUndoStream(false);
            AddFrameUndoAction action = new AddFrameUndoAction(frames[destPos], destPos, this);
            addToUndo(action);
            clearRedo();
        }
    }

    public void removeFrame(int pos) {
        int len = frames.length;
        if(len > 1) {
            RemoveFrameUndoAction action = new RemoveFrameUndoAction(frames[pos], pos, this);
            addToUndo(action);
            if(!lockUndoStream) {
                clearRedo();
            }

            Canvas[] stoFrames = new Canvas[len];
            System.arraycopy(frames, 0, stoFrames, 0, len);
            frames = new Canvas[len - 1];

            if(pos > len) {
                pos = len;
            }
            if(pos < 0) {
                pos = 0;
            }

            System.arraycopy(stoFrames, 0, frames, 0, pos);
            if(pos != len) {
                System.arraycopy(stoFrames, pos + 1, frames, pos, len - pos - 1);
            }
            if(framePos == pos) {
                framePos--;
                if(framePos < 0) {
                    framePos = 0;
                }
            }
            updateFrameWrapper();
        }
    }

    public void setFrame(int pos, Canvas frame) {
        frames[pos].dispose();
        frames[pos] = frame;
        updateFrameWrapper();
    }

    public void addAnim(String name) {
        addToUndo(new AddAnimUndoAction(frames[framePos], this, false));
        frames[framePos].setAnim(name);
    }

    public void removeAnim(int animPos) {
        renameAnim(null, animPos);
    }

    public void renameAnim(String name, int animPos) {
        addToUndo(new AddAnimUndoAction(frames[framePos], this, true));
        int animStart = animPos;
        while(animStart >= 0 && frames[animStart].getAnim() != null && frames[animStart].getAnim().equals(frames[animPos].getAnim())) {
            animStart--;
        }
        animStart++;
        int animEnd = animPos;
        while(animEnd < frames.length && frames[animEnd].getAnim() != null && frames[animEnd].getAnim().equals(frames[animPos].getAnim())) {
            animEnd++;
        }
        for(int i = animStart; i < animEnd; i++) {
            frames[i].setAnim(name);
        }
    }

    public void addAnimCascade(String name, int animPos) {
        addToUndo(new AddAnimUndoAction(frames[framePos], this, true));
        int animStart = animPos;
        while(animStart >= 0 && frames[animStart].getAnim() == null) {
            animStart--;
        }
        animStart++;
        int animEnd = animPos;
        while(animEnd < frames.length && frames[animEnd].getAnim() == null) {
            animEnd++;
        }
        for(int i = animStart; i < animEnd; i++) {
            frames[i].setAnim(name);
        }
    }

    private void addToolOptions(final int frameNum) {
        Canvas canvas = frames[frameNum];
        for(int i = 0; i < toolLen; i++) {
            canvas.tools[i + 1].setOptions(toolOptions[i]);
        }
    }

    private void addToolOptions(final Canvas canvas) {
        for(int i = 0; i < toolLen; i++) {
            canvas.tools[i + 1].setOptions(toolOptions[i]);
        }
    }

    public void setImgSizes(int width, int height) {
        imgW = width;
        imgH = height;
        for(Canvas canvas : frames) {
            canvas.setImgSizes(width, height);
        }
    }

    public void resizeImage(int width, int height) {
        ResizeUndoAction action = new ResizeUndoAction(this, imgW, imgH);
        addToUndo(action);
        clearRedo();

        for(Canvas canvas : frames) {
            canvas.resizeImage(width, height);
        }

        imgW = width;
        imgH = height;
    }

    public void resizeCanvas(int up, int down, int left, int right) {
        ResizeUndoAction action = new ResizeUndoAction(this, imgW, imgH);
        addToUndo(action);
        clearRedo();

        for(Canvas canvas : frames) {
            canvas.resizeCanvas(up, down, left, right);
        }

        imgW += left + right;
        imgH += up + down;
    }

    private void startAnimation() {
        if(frames[framePos].getAnim() == null) {
            animRunning = false;
        } else {
            animRunning = true;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    updateAnimation();
                }
            }, frames[framePos].getDuration() * Gdx.graphics.getDeltaTime());
        }
    }

    private void updateAnimation() {
        String animName = frames[framePos].getAnim();
        framePos++;
        if(framePos == frames.length || frames[framePos].getAnim() == null || !frames[framePos].getAnim().equals(animName)) {
            do {
                framePos--;
            } while(framePos >= 0 && frames[framePos].getAnim().equals(animName));
            framePos++;
        }
        updateFrameWrapper();
        startAnimation();
    }

    public void endAnimation() {
        animRunning = false;
        Timer.instance().clear();
    }

    public int getPosOfFrame(Canvas frame)
    {
        int ret = -1;
        for (int i = 0; i < frames.length && ret == -1; i++)
        {
            Canvas testFrame = frames[i];
            if (testFrame.equals(frame))
            {
                ret = i;
            }
        }
        return ret;
    }

    public Group getFrameWrapper() {
        return frameWrapper;
    }

    public Canvas getCurFrame() {
        return frames[framePos];
    }

    public int getFramePos() {
        return framePos;
    }

    public Canvas[] getFrames() {
        return frames;
    }

    public int getImgW() {
        return imgW;
    }

    public int getImgH() {
        return imgH;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public AnimMenu getAnimMenu() {
        return menu;
    }

    public Pixmap[] getBrushes()
    {
        return brushes;
    }

    public int getCurTool()
    {
        return curTool;
    }

    public void setCurTool(int tool) {
        if(tool > 0 && tool < toolLen + 1) {
            toolOptions[curTool - 1].setVisible(false);
            curTool = tool;
            toolOptions[tool - 1].setVisible(true);
        }
    }

    private int getAnimStart(Anim anim, ArrayList<Anim> animations) {
        int start = 0;
        if(animations != null) {
            int i = 0;
            while(animations.get(i) != anim) {
                start += animations.get(i).length;
                i++;
            }
        }
        return start;
    }

    public void setLockUndoStream(boolean lock) {
        lockUndoStream = lock;
    }

    public void addToUndo(UndoAction action) {
        if(!lockUndoStream) {
            int emptyPixmap = -1;
            for(int i = 0; i < undoSize && emptyPixmap == -1; i++) {
                if(undo[i] == null) {
                    emptyPixmap = i;
                }
            }
            if(emptyPixmap == -1) {
                undo[0].dispose();
                for(int i = 1; i < undoSize; i++) {
                    undo[i - 1] = undo[i];
                }
                emptyPixmap = undoSize - 1;
            }
            undo[emptyPixmap] = action;
        } else {
            try {
                action.dispose();
            } catch (GdxRuntimeException e) { }
        }
    }

    public void undoAction() {
        int emptyPixmap = undoSize - 1;
        for(int i = 0; i < undoSize && emptyPixmap == undoSize - 1; i++) {
            if(undo[i] == null) {
                emptyPixmap = i - 1;
            }
        }
        if(emptyPixmap >= 0) {
            undo[emptyPixmap].undo();
            undo[emptyPixmap].dispose();
            undo[emptyPixmap] = null;

            /*int layer = undoLayer[emptyPixmap];
            addLayerToRedo(layer);
            layers.set(layer, undo[emptyPixmap]);
            undo[emptyPixmap] = null;
            updateLayerDraw(layer);*/
        }
    }

    public void addToRedo(UndoAction action) {
        if(!lockUndoStream) {
            int emptyPixmap = -1;
            for(int i = 0; i < undoSize && emptyPixmap == -1; i++) {
                if(redo[i] == null) {
                    emptyPixmap = i;
                }
            }
            if(emptyPixmap == -1) {
                for(int i = 1; i < undoSize; i++) {
                    redo[i - 1] = redo[i];
                }
                emptyPixmap = undoSize - 1;
            }
            redo[emptyPixmap] = action;
        } else {
            try {
                action.dispose();
            } catch (GdxRuntimeException e) { }
        }
    }

    public void redoAction() {
        int emptyRedo = undoSize - 1;
        for(int i = 0; i < undoSize && emptyRedo == undoSize - 1; i++) {
            if(redo[i] == null) {
                emptyRedo = i - 1;
            }
        }
        if(emptyRedo >= 0) {
            redo[emptyRedo].redo();
            redo[emptyRedo].dispose();
            redo[emptyRedo] = null;
        }
    }

    public void clearRedo() {
        for(int i = 0; i < undoSize; i++) {
            if(redo[i] != null) {
                redo[i].dispose();
                redo[i] = null;
            }
        }
    }

    public void loadProject(FileHandle handle) {
        String fileSplit;
        String[] segments;
        String[][] animDetails;

        try {
            String line;

            BufferedReader bufferedReader =
                    handle.reader(8192);

            StringBuilder builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            bufferedReader.close();

            segments = fileSplit.split("\\s*\\r?\\n\\s*");
            animDetails = new String[segments.length][];
            for(int i = 0; i < segments.length; i++) {
                animDetails[i] = segments[i].split(" ");
            }

            dispose();
            setup(parent);
            framePos = 0;
            Pixmap drawFrom = null, draw = null;
            String curAnim = null;
            int layerCount = 1;
            boolean newFrame = false;
            int sprOfsX = 0;
            int sprOfsY = 0;
            for(String[] code : animDetails) {
                if(newFrame && !code[0].equals("SetAnim")) {
                    addFrame();
                    frames[framePos].setAnim(curAnim);
                    for(int i = 0; i < layerCount - 1; i++) {
                        frames[framePos].layers.add(frames[framePos].newCanvasPixmap());
                    }
                    frames[framePos].setSprOfsX(sprOfsX);
                    frames[framePos].setSprOfsY(sprOfsY);
                    newFrame = false;
                }
                switch (code[0]) {
                    case "Init":
                        dispose();
                        imgW = Integer.parseInt(code[1]);
                        imgH = Integer.parseInt(code[2]);
                        setup(parent);
                        break;
                    case "LayerCount":
                        layerCount = Integer.parseInt(code[1]);
                        for(int i = 0; i < layerCount - 1; i++) {
                            frames[framePos].layers.add(frames[framePos].newCanvasPixmap());
                        }
                        break;
                    case "SetSprite":
                    {
                        StringBuilder temp = new StringBuilder();
                        for(int i = 1; i < code.length; i++) {
                            if(i > 1) {
                                temp.append(" ");
                            }
                            temp.append(code[i]);
                        }
                        FileHandle spriteHandle = Gdx.files.external(temp.toString());
                        if(!spriteHandle.exists()) {
                            spriteHandle = Gdx.files.external(handle.parent().path() + "/" + temp.toString());
                        }
                        drawFrom = new Pixmap(spriteHandle);
                    }
                    break;
                    case "Frame":
                        if(drawFrom != null) {
                            if(draw != null) {
                                draw.dispose();
                            }
                            draw = new Pixmap(Integer.parseInt(code[4]), Integer.parseInt(code[5]), Pixmap.Format.RGBA8888);
                            draw.setBlending(Pixmap.Blending.None);
                            for(int xi = 0; xi < Integer.parseInt(code[4]); xi++) {
                                for(int yi = 0; yi < Integer.parseInt(code[5]); yi++) {
                                    draw.drawPixel(xi, yi, drawFrom.getPixel(xi + Integer.parseInt(code[2]), yi + Integer.parseInt(code[3])));
                                }
                            }
                        }
                        break;
                    case "LayerPosSet":
                        if(draw != null) {
                            Pixmap pixmap = frames[framePos].layers.get(Integer.parseInt(code[1]));
                            int x = Integer.parseInt(code[2]);
                            int y = frames[framePos].getImgH() - draw.getHeight() - Integer.parseInt(code[3]);
                            pixmap.setBlending(Pixmap.Blending.None);
                            pixmap.drawPixmap(draw, x, y);
                            frames[framePos].updateLayerDraw(Integer.parseInt(code[1]));
                        }
                        break;
                    case "SprPosSet":
                    {
                        sprOfsX = Integer.parseInt(code[1]);
                        sprOfsY = Integer.parseInt(code[2]);
                        frames[framePos].setSprOfsX(sprOfsX);
                        frames[framePos].setSprOfsY(sprOfsY);
                    }
                    break;
                    case "LayerAlpha":
                    {
                        int layer = Integer.parseInt(code[1]);
                        int alpha = Integer.parseInt(code[2]);
                        frames[framePos].getLayerOption(layer).setLayerAlpha(alpha / 100f);
                        frames[framePos].updateCombinedDraw();
                    }
                    break;
                    case "HideLayer":
                        frames[framePos].getLayerOption(Integer.parseInt(code[1])).setLayerVisible(false);
                        frames[framePos].updateCombinedDraw();
                        break;
                    case "Wait":
                        frames[framePos].setDuration(Integer.parseInt(code[1]));
                        newFrame = true;
                        break;
                    case "Anim":
                        curAnim = code[1];
                        frames[framePos].setAnim(curAnim);
                        sprOfsX = 0;
                        sprOfsY = 0;
                        break;
                    default:
                        break;
                }
            }
            if(draw != null) {
                draw.dispose();
            }
            if(drawFrom != null) {
                drawFrom.dispose();
            }
            for (Canvas frame : frames) {
                frame.layers.get(frame.getCurLayer()).setBlending(Pixmap.Blending.SourceOver);
                addToolOptions(frame);
            }

        } catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            handle.path() + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + handle.path() + "'");
        }
        framePos = 0;
        updateFrameWrapper();
        undo = new UndoAction[undoSize];
        menu.checkScroll();
    }

    public void saveProject(FileHandle handle) {
        FileHandle projectFile = Gdx.files.external(handle.pathWithoutExtension() + ".pixel");
        projectFile.writeString("Init " + imgW + ' ' + imgH + '\n', false); // Projects need to be initialized
        projectFile.writeString("SetSprite " + handle.name() + '\n', true);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        int distRight = 0;
        int frameNum = 0;
        String animName = null;
        int oldLayerSize = 0;
        int sprOfsX = 0;
        int sprOfsY = 0;
        for(Canvas canvas : frames) {
            int frameWidth = 0;
            int distDown = 0;
            if(frames[frameNum].getAnim() != null && !frames[frameNum].getAnim().equals(animName)) {
                projectFile.writeString("\n", true);
                projectFile.writeString("Anim " + frames[frameNum].getAnim() + '\n', true);
                animName = frames[frameNum].getAnim();
            }
            if(oldLayerSize != canvas.layers.size()) {
                oldLayerSize = canvas.layers.size();
                projectFile.writeString("LayerCount " + canvas.layers.size() + '\n', true); // How many layers are in a frame
            }
            for(int i = 0; i < canvas.layers.size(); i++) {
                Pixmap layer = canvas.layers.get(i);
                int highestX = 0, lowestX = layer.getWidth();
                int highestY = 0, lowestY = layer.getHeight();
                for(int xi = 0; xi < layer.getWidth(); xi++) {
                    for(int yi = 0; yi < layer.getHeight(); yi++) {
                        if((layer.getPixel(xi, yi) & 0x000000ff) != 0) {
                            if(xi > highestX) {
                                highestX = xi;
                            }
                            if(xi < lowestX) {
                                lowestX = xi;
                            }
                            if(yi > highestY) {
                                highestY = yi;
                            }
                            if(yi < lowestY) {
                                lowestY = yi;
                            }
                        }
                    }
                }
                highestX++; // Width needs to be one higher
                highestY++; // Same with height

                if(highestX - lowestX > 0 && highestY - lowestY > 0) {
                    Pixmap temp1 = new Pixmap(highestX - lowestX, highestY - lowestY, Pixmap.Format.RGBA8888);
                    temp1.setBlending(Pixmap.Blending.None);
                    temp1.drawPixmap(layer, -lowestX, -lowestY);

                    int newWidth = pixmap.getWidth(), newHeight = pixmap.getHeight();
                    if(pixmap.getWidth() < distRight + temp1.getWidth()) {
                        newWidth = distRight + temp1.getWidth();
                    }
                    if(pixmap.getHeight() < distDown + temp1.getHeight()) {
                        newHeight = distDown + temp1.getHeight();
                    }
                    Pixmap temp2 = pixmap;
                    pixmap = new Pixmap(newWidth, newHeight, Pixmap.Format.RGBA8888);
                    pixmap.setBlending(Pixmap.Blending.None);
                    pixmap.drawPixmap(temp2, 0, 0);
                    pixmap.drawPixmap(temp1, distRight, distDown);

                    projectFile.writeString("Frame " + i, true);
                    projectFile.writeString(" " + distRight + " " + distDown, true);
                    projectFile.writeString(" " + temp1.getWidth() + ' ' + temp1.getHeight() + '\n', true);
                    projectFile.writeString("LayerPosSet " + i, true);
                    projectFile.writeString(" " + lowestX + " " + (layer.getHeight() - temp1.getHeight() - lowestY) + '\n', true);
                    if(canvas.getSprOfsX() != sprOfsX || canvas.getSprOfsY() != sprOfsY) {
                        sprOfsX = canvas.getSprOfsX();
                        sprOfsY = canvas.getSprOfsY();
                        projectFile.writeString("SprPosSet", true);
                        projectFile.writeString(" " + sprOfsX + " " + sprOfsY + '\n', true);
                    }
                    if(!canvas.getLayerOption(i).isLayerVisible()) {
                        projectFile.writeString("HideLayer " + i + '\n', true);
                    }
                    if(canvas.getLayerOption(i).getLayerAlpha() < 0.99f) {
                        int alphaInt = (int)(canvas.getLayerOption(i).getLayerAlpha() * 100);
                        projectFile.writeString("LayerAlpha " + i + " " + alphaInt + '\n', true);
                    }

                    distDown += temp1.getHeight();
                    if(temp1.getWidth() > frameWidth) {
                        frameWidth = temp1.getWidth();
                    }
                }
            }
            projectFile.writeString("Wait " + canvas.getDuration() + '\n', true);
            frameNum++;
            if(frameNum == frames.length || (frames[frameNum].getAnim() != null && !frames[frameNum].getAnim().equals(animName))) {
                projectFile.writeString("SetAnim " + animName + '\n', true);
                sprOfsX = 0;
                sprOfsY = 0;
            }
            distRight += frameWidth;
        }
        PixmapIO.writePNG(handle, pixmap);
    }

    private void clean() {
        frames = null;
        framePos = 0;
        frameWrapper.clearChildren();
        toolOptionsGroup.clearChildren();
        undo = null;
        redo = null;
        undoSize = 0;
        lockUndoStream = false;
        animRunning = false;
        menu = null;
        brushes = null;
    }

    public void dispose() {
        for(Canvas frame : frames) {
            frame.dispose();
        }
        for(Pixmap brush : brushes) {
            brush.dispose();
        }
        for(UndoAction undoPixmap : undo) {
            if(undoPixmap != null) {
                undoPixmap.dispose();
            }
        }
        for(UndoAction redoPixmap : redo) {
            if(redoPixmap != null) {
                redoPixmap.dispose();
            }
        }
        for(int i = 0; i < toolLen; i++) {
            if(toolOptions != null) {
                toolOptions[i].dispose();
            }
        }
    }


    // Everything is public because Anim is POD
    private class Anim {
        public int length;
        public String name;
    }

    public class AnimMenu extends SideMenu {
        private int move;
        private float scroll;
        private TextPrinter textPrinter;
        private MenuList copyMenuList;
        private MenuList animOptions;

        public AnimMenu(final DrawStage parent) {
            super(parent);
            move = -1;
            textPrinter = new TextPrinter(true);
            addCaptureListener(new ClickListener() {
                private boolean scrollChanged, canScroll;
                private float storeY, storeScroll;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    boolean ret = !hidden();
                    canScroll = false;
                    scrollChanged = true;
                    if(ret) {
                        if(minimized) {
                            maximize();
                        } else {
                            if(x > getWidth() - 16 * pixelSize && y > getHeight() - 16 * pixelSize) {
                                minimize();
                            } else if(y > getHeight() - 26 * pixelSize && y < getHeight() - 10 * pixelSize) {
                                if(x > 4 * pixelSize && x < 20 * pixelSize) {
                                    final NewAnimWindow newAnimWindow = new NewAnimWindow(parent, AnimProcessor.this, "New Anim", getCurFrame().getAnim() != null);
                                    parent.addActor(newAnimWindow);
                                } else if(x > 24 * pixelSize && x < 40 * pixelSize) {
                                    removeAnim(framePos);
                                } else if(x > 44 * pixelSize && x < 60 * pixelSize) {
                                    if(animRunning) {
                                        endAnimation();
                                    } else {
                                        startAnimation();
                                    }
                                } else if(x > 64 * pixelSize && x < 80 * pixelSize) {
                                    animOptions.show(getX() + 80 * pixelSize, getHeight() - 26 * pixelSize, parent);
                                }
                            } else if(y > getHeight() - 52 * pixelSize && y < getHeight() - 36 * pixelSize) {
                                if(x > 4 * pixelSize && x < 20 * pixelSize) {
                                    copyMenuList.show(getX() + 4 * pixelSize, getHeight() - 36 * pixelSize, parent);
                                } else if(x > 24 * pixelSize && x < 40 * pixelSize) {
                                    removeFrame(framePos);
                                }
                            } else if(y < getHeight() - 53 * pixelSize) {
                                canScroll = true;
                                scrollChanged = false;
                                storeY = y;
                                storeScroll = scroll;
                            }
                        }
                    }
                    return ret;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if(canScroll) {
                        scroll += y - storeY;
                        storeY = y;
                        checkScroll();
                        if(Math.abs(storeScroll - scroll) > 0.5f * pixelSize) {
                            scrollChanged = true;
                        }
                    }
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if(!scrollChanged && canScroll) {
                        float distFromTop = (PixelApp.height - 53 * pixelSize) - y + scroll;
                        final int selFrame = (int)(distFromTop / (39 * pixelSize));
                        if(y - scroll < PixelApp.height - 24 * pixelSize) {
                            if(x > 32 * pixelSize && x < 68 * pixelSize && selFrame < frames.length && selFrame >= 0) {
                                framePos = selFrame;
                                endAnimation();
                                updateFrameWrapper();
                            } else if(x > 68 * pixelSize) {
                                Gdx.input.getTextInput(new Input.TextInputListener() {
                                    @Override
                                    public void input(String text) {
                                        try {
                                            int valueOf = Integer.valueOf(text);
                                            if(valueOf > 999 || valueOf < 0) {
                                                throw new NumberFormatException();
                                            } else {
                                                frames[selFrame].setDuration(valueOf);
                                            }
                                        } catch(NumberFormatException e) {
                                            Gdx.input.getTextInput(this, "Invalid Number. Set new frame duration", Integer.toString(frames[framePos].getDuration()), "");
                                        }
                                    }

                                    @Override
                                    public void canceled() {

                                    }
                                }, "Set new frame duration", Integer.toString(frames[selFrame].getDuration()), "");
                            }
                        }
                    }
                }
            });

            final String[] copyNames = {
                    "Copy Frame",
                    "New Frame"
            };
            final Runnable[] copyActions = {
                    () -> {
                        copyFrame(framePos, framePos + 1);
                        frames[framePos].setAnim(frames[framePos - 1].getAnim());
                    },
                    () -> {
                        addFrame(framePos + 1);
                        frames[framePos].setAnim(frames[framePos - 1].getAnim());
                    }
            };
            copyMenuList = new MenuList(copyNames, copyActions);

            final String[] animNames = {
                    "Display Image Ghosts",
                    "Copy Anim",
                    "Rename Anim"
            };
            final Runnable[] animAtions = {
                    () -> {
                        drawFrameBefore = !drawFrameBefore;
                        drawFrameAfter = !drawFrameAfter;
                        updateFrameWrapper();
                    },
                    () -> {
                        final String curAnim = getCurFrame().getAnim();
                        int begin = framePos;
                        int end = framePos;
                        while (begin > -1 && frames[begin].getAnim().equals(curAnim))
                        {
                            begin--;
                        }
                        begin++;
                        while (end < frames.length && frames[end].getAnim().equals(curAnim))
                        {
                            end++;
                        }
                        int newPos = frames.length + (framePos - begin);
                        for(int i = begin; i < end; i++)
                        {
                            copyFrame(i);
                        }
                        framePos = newPos;
                        updateFrameWrapper();
                        addAnimCascade(curAnim + "_Copy", newPos);
                    },
                    () -> {
                        final NewAnimWindow newAnimWindow = new NewAnimWindow(parent, AnimProcessor.this, "Rename Anim", false)
                        {
                            @Override
                            protected void addAnim(AnimProcessor processor) {
                                renameAnim(animName, framePos);
                                remove();
                            }
                        };
                        parent.addActor(newAnimWindow);
                    }
            };
            animOptions = new MenuList(animNames, animAtions);
        }

        void checkScroll() {
            float maxScroll = 39 * pixelSize * frames.length - (PixelApp.height - 53 * pixelSize);
            if(scroll > maxScroll) {
                scroll = maxScroll;
            }
            if(scroll < 0) {
                scroll = 0;
            }
        }

        @Override
        protected void drawMinimized(Batch batch, float parentAlpha) {
            spr.draw(batch, parentAlpha);
        }

        @Override
        protected void drawMaximized(Batch batch, float parentAlpha) {
            spr.setRegion(604, 148, 100, 3);
            spr.setSize(getWidth(), getHeight());
            spr.setPosition(getX(), getY());
            spr.draw(batch, parentAlpha);

            ArrayList<Anim> animations = new ArrayList<>();
            String animName = null;
            if(frames[0].getAnim() == null) {
                Anim anim = new Anim();
                anim.length = 0;
                animations.add(anim);
            }
            for(Canvas frame : frames) {
                if((frame.getAnim() == null && animName == null) || (frame.getAnim() != null && frame.getAnim().equals(animName))) {
                    animations.get(animations.size() - 1).length++;
                } else {
                    Anim anim = new Anim();
                    anim.name = frame.getAnim();
                    anim.length = 1;
                    animations.add(anim);
                    animName = anim.name;
                }
            }
            if(animations.size() == 0) {
                spr.setRegion(604, 67, 100, 27);
                spr.setSize(getWidth(), 27 * pixelSize);
                spr.setPosition(getX(), getHeight() - spr.getHeight());
                spr.draw(batch, parentAlpha);
            } else {
                textPrinter.setColor(0, 0, 0, 1);
                for(int i = 0; i < frames.length; i++) {
                    Sprite draw = frames[i].getCombinedDraw();
                    spr.setRegion(604, i == framePos ? 190 : 151, 100, 39);
                    spr.setSize(100 * pixelSize, 39 * pixelSize);
                    float y = getSectionY(i);
                    spr.setPosition(getX(), y);
                    spr.draw(batch, parentAlpha);

                    float ratio = 1f * parent.getCanvas().getImgW() / parent.getCanvas().getImgH();
                    if(ratio > 1f) { // The image is wider than it is tall
                        float height = 36 / ratio;
                        draw.setSize(36 * pixelSize, height * pixelSize);
                        spr.setRegion(423, 28, 36, (int)height);
                        spr.setSize(draw.getWidth(), draw.getHeight());
                        spr.setPosition(getX() + 32 * pixelSize, (y + 2 * pixelSize) + (36 - height) / 2f * pixelSize);
                        spr.draw(batch, parentAlpha);
                        draw.setPosition(spr.getX(), spr.getY());
                        draw.draw(batch, parentAlpha);
                    } else { // the image is taller than it is wide or perfectly square
                        float width = 36 * ratio;
                        draw.setSize(width * pixelSize, 36 * pixelSize);
                        spr.setRegion(423, 28, (int)width, 36);
                        spr.setSize(draw.getWidth(), draw.getHeight());
                        spr.setPosition(getX() + (32 + (36 - width) / 2f) * pixelSize, y + 2 * pixelSize);
                        spr.draw(batch, parentAlpha);
                        draw.setPosition(spr.getX(), spr.getY());
                        draw.draw(batch, parentAlpha);
                    }

                    float textWidth = textPrinter.getTextWidth(Integer.toString(frames[i].getDuration()));
                    textPrinter.drawInt(getX() + 97 * pixelSize - textWidth, y + 16 * pixelSize, frames[i].getDuration(), batch);
                }

                for(Anim anim : animations) {
                    if(anim.name != null) { // If anim.name is null, it signifies that the frames are currently not in any animation
                        int start = getAnimStart(anim, animations);
                        float y = getSectionY(start);
                        spr.setRegion(705, 67, 7, 6);
                        spr.setSize(7 * pixelSize, 6 * pixelSize);
                        spr.setPosition(getX() + 2 * pixelSize, y + 33 * pixelSize);
                        spr.draw(batch, parentAlpha);

                        start += anim.length - 1;
                        float bottomY = getSectionY(start);
                        spr.setRegion(705, 94, 7, 6);
                        spr.setSize(7 * pixelSize, 6 * pixelSize);
                        spr.setPosition(getX() + 2 * pixelSize, bottomY);
                        spr.draw(batch, parentAlpha);

                        float middleSize = y + 33 * pixelSize - bottomY - 6 * pixelSize;
                        spr.setRegion(705, 73, 4, 21);
                        spr.setSize(4 * pixelSize, middleSize);
                        spr.setPosition(getX() + 2 * pixelSize, bottomY + 6 * pixelSize);
                        spr.draw(batch, parentAlpha);

                        float nameWidth = textPrinter.getTextWidth(anim.name);
                        spr.setRegion(727, 67, 25, 11);
                        spr.setSize(nameWidth, 11 * pixelSize);
                        spr.setPosition(getX() + 2 * pixelSize - nameWidth, y + 28 * pixelSize);
                        spr.draw(batch, parentAlpha);

                        spr.setRegion(721, 67, 6, 11);
                        spr.setSize(6 * pixelSize, 11 * pixelSize);
                        spr.setPosition(getX() - 4 * pixelSize - nameWidth, y + 28 * pixelSize);
                        spr.draw(batch, parentAlpha);

                        textPrinter.setColor(Color.WHITE);
                        textPrinter.drawText(spr.getX() + spr.getWidth() - 2 * pixelSize, spr.getY() + pixelSize, anim.name, batch);
                    }
                }

                spr.setRegion(604, 95, 100, 53);
                spr.setSize(getWidth(), 53 * pixelSize);
                spr.setPosition(getX(), getHeight() - spr.getHeight());
                spr.draw(batch, parentAlpha);

                if(animRunning) {
                    spr.setRegion(705, 105, 16, 16);
                    spr.setSize(16 * pixelSize, 16 * pixelSize);
                    spr.setPosition(getX() + 44 * pixelSize, getHeight() - 26 * pixelSize);
                    spr.draw(batch, parentAlpha);
                }
            }
        }

        private float getSectionY(int i) {
            return PixelApp.height - 53 * pixelSize - (39 * pixelSize * (i + 1)) + scroll;
        }

        @Override
        public void minimize() {
            super.minimize();
            spr.setRegion(283, 123, 16, 28);
            spr.setSize(16 * pixelSize, 28 * pixelSize);
            spr.setPosition(PixelApp.width - spr.getWidth(), 87 * pixelSize);
            setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
        }

        @Override
        public void maximize() {
            super.maximize();
            setSize(100 * pixelSize, PixelApp.height);
            setPosition(PixelApp.width - getWidth(), 0);
        }
    }
}
