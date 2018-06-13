package com.pixelapp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.squareup.gifencoder.GifEncoder;
import com.squareup.gifencoder.ImageOptions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.pixelapp.PixelApp.MANAGER;
import static com.pixelapp.PixelApp.pixelSize;

public class Menu extends Actor {
    private TextPrinter textPrinter;
    private Sprite spr;
    private Group[] menuButtons;
    private boolean minimized;
    private DrawStage parent;
    private String saveLocation, loadLocation, exportLocation;

    private enum Tab { kFile, kEdit };
    private Tab tab;

    public Menu(final DrawStage parent) {
        textPrinter = new TextPrinter(false);
        this.parent = parent;
        saveLocation = "PixelApp/Projects";
        loadLocation = "PixelApp/Projects";
        exportLocation = "PixelApp/Export";
        tab = Tab.kFile;
        spr = new Sprite((Texture)MANAGER.get("Menu.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width * pixelSize, height * pixelSize);
            }
        };
        addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!parent.isDrawing()) {
                    if (!minimized && x > PixelApp.width - 33 * pixelSize && y > PixelApp.height - 16 * pixelSize)
                    {
                        int tapped = (int)Math.floor((x - (PixelApp.width - 33 * pixelSize)) / (17 * pixelSize));
                        Tab newTab = intToTab(tapped);
                        if(tab != newTab)
                        {
                            menuButtons[tabToInt(tab)].setVisible(false);
                            menuButtons[tapped].setVisible(true);
                            tab = newTab;
                        }
                    }
                    else
                    {
                        setMinimized(!minimized);
                    }
                }
                return true;
            }
        });
    }

    private int tabToInt(Tab tab)
    {
        int i = 0;
        switch (tab)
        {
            case kEdit:
                i = 1;
                break;

            case kFile: // Intentionally blank, xOfs for kFile is 0
            default:
                break;
        }
        return i;
    }

    private Tab intToTab(int i)
    {
        Tab tab = null;
        switch (i)
        {
            case 0:
                tab = Tab.kFile;
                break;

            case 1:
                tab = Tab.kEdit;
                break;
        }
        return tab;
    }

    public void initButtons() {
        menuButtons = new Group[2];

        for (int i = 0; i < menuButtons.length; i++)
        {
            menuButtons[i] = new Group();
        }

        initFileMenu(menuButtons[0]);
        initEditMenu(menuButtons[1]);

        for (Group menu : menuButtons)
        {
            parent.addActor(menu);
        }
        setMinimized(true);
    }

    private void initFileMenu(Group fileMenu)
    {
        fileMenu.addActor(new MenuButton(0, Tab.kFile, "New Image", new Runnable() {
            @Override
            public void run() {
                newImage();
            }
        }));
        fileMenu.addActor(new MenuButton(1, Tab.kFile, "Save Project", new Runnable() {
            @Override
            public void run() {
                final FileMenu fileMenu = new FileMenu(parent, saveLocation, "Save Project");
                fileMenu.setAction(new Runnable() {
                    @Override
                    public void run() {
                        FileHandle target = fileMenu.getTarget();
                        if(target.extension().equals("") && !target.isDirectory()) {
                            target = Gdx.files.external(target.path() + ".png");
                        }
                        if(target.extension().equals("pixel")) {
                            target = Gdx.files.external(target.pathWithoutExtension() + ".png");
                        }
                        saveLocation = target.parent().path();
                        parent.saveProject(target);
                    }
                });
                parent.addActor(fileMenu);
                setMinimized(true);
            }
        }));
        fileMenu.addActor(new MenuButton(2, Tab.kFile, "Load Image", new Runnable() {
            @Override
            public void run() {
                final FileMenu fileMenu = new FileMenu(parent, loadLocation, "Load Image");
                fileMenu.setAction(new Runnable() {
                    @Override
                    public void run() {
                        FileHandle target = fileMenu.getTarget();
                        for(String extension : new String[] {"png","jpg","jpeg"}) {
                            if(target.extension().toLowerCase().equals(extension)) {
                                parent.loadCanvas(target);
                            }
                        }
                        if(target.extension().toLowerCase().equals("pixel")) {
                            parent.loadProject(target);
                        }
                        loadLocation = target.parent().path();
                    }
                });
                parent.addActor(fileMenu);
                setMinimized(true);
            }
        }));
        fileMenu.addActor(new MenuButton(3, Tab.kFile, "Export Current Frame", new Runnable() {
            @Override
            public void run() {
                FileHandle exportHandle = Gdx.files.external(exportLocation);
                if(!exportHandle.exists()) {
                    // Switch into Java file handling
                    new File(Gdx.files.getExternalStoragePath() + "/" + exportLocation).mkdirs();
                }
                final FileMenu fileMenu = new FileMenu(parent, exportLocation, "Export Current Frame");
                fileMenu.setAction(new Runnable() {
                    @Override
                    public void run() {
                        FileHandle target = fileMenu.getTarget();
                        if(target.extension().equals("")) {
                            target = Gdx.files.external(target.pathWithoutExtension() + ".png");
                        }
                        PixmapIO.writePNG(target, parent.getCanvas().getFinalPixmap());
                        exportLocation = target.parent().path();

                        // Switch to Java to run media scanner
                        PixelApp.platformOperations.runScanner(target.path());
                    }
                });
                parent.addActor(fileMenu);
                setMinimized(true);
            }
        }));
        fileMenu.addActor(new MenuButton(4, Tab.kFile, "Export GIF Animation", new Runnable() {
            @Override
            public void run() {
                FileHandle exportHandle = Gdx.files.external(exportLocation);
                if(!exportHandle.exists()) {
                    // Switch into Java file handling
                    new File(Gdx.files.getExternalStoragePath() + "/" + exportLocation).mkdirs();
                }
                final FileMenu fileMenu = new FileMenu(parent, exportLocation, "Export GIF Animation");
                fileMenu.setAction(new Runnable() {
                    @Override
                    public void run() {
                        parent.addActor(new WidthHeightWindow(parent, parent.getProcessor().getImgW(), parent.getProcessor().getImgH(), "Set Export Size") {
                            @Override
                            protected void accept(DrawStage parent) {
                                FileHandle target = fileMenu.getTarget();
                                if(!target.extension().equals(".gif")) {
                                    target = Gdx.files.external(target.pathWithoutExtension() + ".gif");
                                }

                                try {
                                    AnimProcessor processor = parent.getProcessor();
                                    ImageOptions options = new ImageOptions();
                                    options.setDelay(500, TimeUnit.MILLISECONDS);
                                    OutputStream stream = Gdx.files.external(target.path()).write(false);
                                    GifEncoder encoder = new GifEncoder(stream, processor.getImgW(), processor.getImgH(), 0);

                                    ArrayList<Canvas> frames = new ArrayList<>();
                                    final String curAnim = processor.getCurFrame().getAnim();
                                    for(final Canvas frame : processor.getFrames())
                                    {
                                        if(frame.getAnim().equals(curAnim))
                                        {
                                            frames.add(frame);
                                        }
                                    }

                                    for(Canvas frame : frames)
                                    {
                                        Pixmap framePixmap = frame.getFinalPixmap();
                                        Pixmap flip = frame.newCanvasPixmap();
                                        flip.setBlending(Pixmap.Blending.None);
                                        for(int xi = 0; xi < flip.getWidth(); xi++) {
                                            for(int yi = 0; yi < flip.getHeight(); yi++) {
                                                flip.drawPixel(xi, yi, framePixmap.getPixel(xi, yi));
                                            }
                                        }
                                        framePixmap.dispose();

                                        int numColors = 0;
                                        int[] colors = {0, 0};
                                        int[][] pixels = new int[flip.getHeight()][flip.getWidth()];
                                        for(int xi = 0; xi < flip.getWidth(); xi++) {
                                            for(int yi = 0; yi < flip.getHeight(); yi++) {
                                                int pixel = flip.getPixel(xi, yi);
                                                int r = (pixel >> 24) & 0xff;
                                                int g = (pixel >> 16) & 0xff;
                                                int b = (pixel >> 8) & 0xff;
                                                int a = 0xff;
                                                pixels[yi][xi] = (a << 24) + (r << 16) + (g << 8) + b;
                                                pixel = pixels[yi][xi];
                                                if(numColors < 3) {
                                                    if(numColors == 0) {
                                                        colors[0] = pixel;
                                                        numColors = 1;
                                                    }
                                                    else if(numColors == 1 && pixel != colors[0]) {
                                                        colors[1] = pixel;
                                                        numColors = 2;
                                                    } else if(pixel != colors[0] && pixel != colors[1]) {
                                                        numColors = 3;
                                                    }
                                                }
                                            }
                                        }
                                        if(numColors < 3) {
                                            if((pixels[flip.getHeight() - 1][flip.getWidth() - 1] & 0xff) == 0xff) {
                                                pixels[flip.getHeight() - 1][flip.getWidth() - 1]--;
                                            } else {
                                                pixels[flip.getHeight() - 1][flip.getWidth() - 1]++;
                                            }
                                        }
                                        if(numColors == 1) {
                                            if((pixels[flip.getHeight() - 1][flip.getWidth() - 2] & 0xff) == 0xff) {
                                                pixels[flip.getHeight() - 1][flip.getWidth() - 2]--;
                                            } else {
                                                pixels[flip.getHeight() - 1][flip.getWidth() - 2]++;
                                            }
                                        }

                                        options.setDelay((int)(1000f * Gdx.graphics.getDeltaTime() * frame.getDuration()), TimeUnit.MILLISECONDS);
                                        encoder.addImage(pixels, options);
                                        flip.dispose();
                                    }

                                    encoder.finishEncoding();
                                    stream.close();

                                    // Switch to Java to run media scanner
                                    PixelApp.platformOperations.runScanner(target.path());
                                } catch(IOException e) {
                                    Gdx.app.log("ERROR", e.getMessage());
                                }
                            }
                        });
                    }
                });
                parent.addActor(fileMenu);
                setMinimized(true);
            }
        }));
        fileMenu.addActor(new MenuButton(5, Tab.kFile, "Export Spritesheet", new Runnable() {
            @Override
            public void run() {
                newImage();
            }
        }));
        fileMenu.addActor(new MenuButton(6, Tab.kFile, "Resize Image", new Runnable() {
            @Override
            public void run() {
                resizeImage();
            }
        }));
        fileMenu.addActor(new MenuButton(7, Tab.kFile, "Resize Canvas", new Runnable() {
            @Override
            public void run() {
                resizeCanvas();
            }
        }));
    }

    private void initEditMenu(Group editMenu)
    {
        editMenu.addActor(new MenuButton(0, Tab.kEdit, "Flip Canvas Horizontally", new Runnable() {
            @Override
            public void run() {
                horizCanvasFlip();
            }
        }));
        editMenu.setVisible(false);
    }

    public boolean getMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
        if(minimized) {
            setBounds(PixelApp.width - 16 * pixelSize, PixelApp.height - 16 * pixelSize, 16 * pixelSize, 16 * pixelSize);
            menuButtons[tabToInt(tab)].setVisible(false);
        } else {
            parent.minimizeRight();
            setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            menuButtons[0].setVisible(true);
            tab = Tab.kFile;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(minimized) {
            if(!parent.getLayerSelector().isMinimized() && parent.getLayerSelector().layerOptionsClosed() ) {
                setX(parent.getLayerSelector().getX() - getWidth());
            } else if(!parent.getSelectOptions().isMinimized()) {
                setX(parent.getSelectOptions().getX() - getWidth());
            } else if(!parent.getClipboard().isMinimized()) {
                setX(parent.getClipboard().getX() - getWidth());
            } else if(!parent.getAnimMenu().isMinimized()) {
                setX(parent.getAnimMenu().getX() - getWidth());
            } else {
                setX(PixelApp.width - getWidth());
            }

            spr.setRegion(0, 17, 16, 16);
            spr.setPosition(getX(), getY());
            spr.draw(batch);
        }
    }

    private void newImage() {
        final Canvas canvas = parent.getCanvas();
        parent.addActor(new WidthHeightWindow(parent, canvas.getImgW(), canvas.getImgH(), "New Image") {
            @Override
            protected void accept(DrawStage parent) {
                parent.newCanvas(width, height);
                remove();
            }
        });
        setMinimized(true);
    }

    private void resizeImage() {
        final Canvas canvas = parent.getCanvas();
        parent.addActor(new WidthHeightWindow(parent, canvas.getImgW(), canvas.getImgH(), "Resize Image") {
            @Override
            protected void accept(DrawStage parent) {
                parent.getProcessor().resizeImage(width, height);
                remove();
            }
        });
        setMinimized(true);
    }

    private void resizeCanvas() {
        final Canvas canvas = parent.getCanvas();
        parent.addActor(new ResizeCanvasWindow(parent, "Resize Canvas") {
            @Override
            protected void accept(DrawStage parent) {
                parent.getProcessor().resizeCanvas(up, down, left, right);
                remove();
            }
        });
        setMinimized(true);
    }

    private void horizCanvasFlip()
    {
        final Canvas canvas = parent.getCanvas();
        for (int i = 0; i < canvas.layers.size(); i++)
        {
            final Pixmap layer = canvas.layers.get(i);
            final Pixmap backup = Canvas.copyPixmap(layer);
            layer.setBlending(Pixmap.Blending.None);
            for (int xi = 0; xi < canvas.getImgW(); xi++)
            {
                for (int yi = 0; yi < canvas.getImgH(); yi++)
                {
                    layer.drawPixel(xi, yi, backup.getPixel(canvas.getImgW() - xi - 1, yi));
                }
            }
            canvas.updateLayerDrawOnly();
            backup.dispose();
        }
        canvas.updateCombinedDraw();
        setMinimized(true);
    }

    // MenuButton Class

    private class MenuButton extends Actor {
        private String text;
        private int icon;
        private float width;
        private Tab tab;

        public MenuButton(int pos, Tab tab, String text, final Runnable action) {
            this.text = text;
            this.icon = pos;
            this.tab = tab;
            width = textPrinter.getTextWidth(text) + 16 * pixelSize;
            setSize(width + 6 * pixelSize, 16 * pixelSize);
            setPosition(PixelApp.width - getWidth(), PixelApp.height - (pixelSize * (32 + 16 * pos)));
            addCaptureListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    action.run();
                    return true;
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setRegion(0, 0, 3, 16);
            spr.setPosition(getX(), getY());
            spr.draw(batch);

            spr.setRegion(3, 0, 10, 16);
            spr.setSize(width, 16 * pixelSize);
            spr.setPosition(getX() + 3 * pixelSize, getY());
            spr.draw(batch);

            spr.setRegion(13, 0, 3, 16);
            spr.setPosition(PixelApp.width - 3  * pixelSize, getY());
            spr.draw(batch);

            int xOfs = 17 * tabToInt(tab);

            spr.setRegion(17 + xOfs, 34 + (17 * icon), 16, 16);
            spr.setPosition(getX() + 3 * pixelSize, getY());
            spr.draw(batch);

            textPrinter.drawText(getX() + 19 * pixelSize, getY() + 2 * pixelSize, text, batch);
        }
    }
}
