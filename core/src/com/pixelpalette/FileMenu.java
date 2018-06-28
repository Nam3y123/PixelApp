package com.pixelpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.pixelpalette.PixelApp.MANAGER;
import static com.pixelpalette.PixelApp.pixelSize;

public class FileMenu extends Actor {
    private Sprite spr;
    private com.pixelpalette.TextPrinter textPrinter;
    private FileHandle directory, target;
    private FileHandle file;
    private FileHandle[] directoryList;
    private float scroll;
    private Runnable action;
    private Actor block;
    private String ioType;

    public FileMenu(final com.pixelpalette.DrawStage parent, String startingLocation, String ioType) {
        this.ioType = ioType;
        spr = new Sprite((Texture)MANAGER.get("GUI.png")) {
            @Override
            public void setRegion(int x, int y, int width, int height) {
                super.setRegion(x, y, width, height);
                setSize(width * pixelSize, height * pixelSize);
            }
        };
        textPrinter = new com.pixelpalette.TextPrinter(true);
        directory = Gdx.files.external(startingLocation);
        directoryList = organizeByName(directory.list());

        block = new Actor();
        block.setBounds(0, 0, com.pixelpalette.PixelApp.width, com.pixelpalette.PixelApp.height);
        block.addCaptureListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        parent.addActor(block);

        setBounds((com.pixelpalette.PixelApp.width / 2f) - 6f, (com.pixelpalette.PixelApp.height / 2f) - 4.5f, 12, 9);
        addCaptureListener(new ClickListener() {
            private float oldY, storeScroll;
            private boolean touchMiddle, scrollChanged;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touchMiddle = false;
                if(y > getHeight() - 16 * pixelSize) {
                    if(x > getWidth() - 19 * pixelSize) {
                        remove();
                    }
                    if(x < 19 * pixelSize && directory.exists()) {
                        setDirectory(directory.parent());
                    }
                } else if(y < 16 * pixelSize) {
                    if(x < getWidth() - 19 * pixelSize) {
                        Gdx.input.getTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                file = null;
                                for(FileHandle handle : directoryList) {
                                    if(handle.name().equals(text) || handle.nameWithoutExtension().equals(text)) {
                                        file = handle;
                                    }
                                }
                                if(file == null) {
                                    file = Gdx.files.external(directory.path() + "/" + text);
                                }
                            }

                            @Override
                            public void canceled() {

                            }
                        }, "Input File Name", (file != null) ? file.name() : "", "");
                    } else if(x > getWidth() - 19 * pixelSize) {
                        openFile();
                        remove();
                    }
                } else if(y < getHeight() - 32 * pixelSize) {
                    oldY = y;
                    touchMiddle = true;
                    scrollChanged = false;
                    storeScroll = scroll;
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(touchMiddle) {
                    scroll += y - oldY;
                    oldY = y;
                    final float maxScroll = 16 * pixelSize * directoryList.length - (getHeight() - 48 * pixelSize);
                    if(scroll > maxScroll) {
                        scroll = maxScroll;
                    }
                    if(scroll < 0) {
                        scroll = 0;
                    }
                    if(Math.abs(storeScroll - scroll) > 0.5f * pixelSize) {
                        scrollChanged = true;
                    }
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(touchMiddle && !scrollChanged) {
                    float yTap = y;
                    yTap -= getHeight() + scroll - 2;
                    yTap /= -16 * pixelSize;
                    if(yTap < directoryList.length) {
                        FileHandle file = directoryList[(int)yTap];
                        if(file.equals(FileMenu.this.file)) {
                            if(file.extension().equals("")) {
                                setDirectory(file);
                            }
                        } else {
                            FileMenu.this.file = file;
                        }
                    }
                }
            }
        });
    }

    private void openFile() {
        target = file;
        if(target != null && action != null) {
            action.run();
        }
    }

    private void setDirectory(FileHandle directory) {
        this.directory = directory;
        directoryList = organizeByName(directory.list());
        scroll = 0;
        file = null;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Middle
        float middleHeight = getHeight() - 48 * pixelSize;
        spr.setRegion(321, 168, 16, 12);
        spr.setSize(spr.getWidth(), middleHeight);
        spr.setPosition(getX(), getY() + 16 * pixelSize);
        spr.draw(batch);

        spr.setRegion(353, 168, 16, 12);
        spr.setSize(spr.getWidth(), middleHeight);
        spr.setPosition(getX() + getWidth() - 16 * pixelSize, getY() + 16 * pixelSize);
        spr.draw(batch);

        spr.setRegion(337, 168, 16, 12);
        spr.setSize(getWidth() - 32 * pixelSize, middleHeight);
        spr.setPosition(getX() + 16 * pixelSize, getY() + 16 * pixelSize);
        spr.draw(batch);

        textPrinter.setColor(Color.GRAY);
        FileHandle[] list = directoryList;
        float xOfs = getX() + 8 * pixelSize;
        for(int i = 0; i < list.length; i++) {
            boolean hasExtension = !list[i].extension().equals("");
            float yOfs = getY() + getHeight() + scroll - 16 * pixelSize * (3 + i);
            if(yOfs > getY() && yOfs < getY() + getHeight() - 16 * pixelSize) {
                if(hasExtension) {
                    spr.setRegion(307, 168, 14, 14);
                } else {
                    spr.setRegion(307, 181, 14, 14);
                }
                spr.setPosition(xOfs, yOfs);
                spr.draw(batch);
                if(list[i].equals(file)) {
                    spr.setRegion(338, 153, 14, 12);
                    spr.setSize(getWidth() - 32 * pixelSize, 16 * pixelSize);
                    spr.setPosition(xOfs + 16 * pixelSize, yOfs);
                    spr.draw(batch);
                    textPrinter.setColor(Color.WHITE);
                }

                textPrinter.drawText(xOfs + 16 * pixelSize, yOfs + 3 * pixelSize, list[i].name(), batch);
                textPrinter.setColor(Color.GRAY);
            }
        }

        // Top bar
        float topBarY = getY() + getHeight() - 32 * pixelSize;
        spr.setRegion(373, 151, 10, 16);
        spr.setPosition(getX(), topBarY);
        spr.draw(batch);

        spr.setRegion(383, 151, 10, 16);
        spr.setPosition(getX() + getWidth() - 10 * pixelSize, topBarY);
        spr.draw(batch);

        spr.setRegion(378, 151, 10, 16);
        spr.setSize(getWidth() - 20 * pixelSize, spr.getHeight());
        spr.setPosition(getX() + 10 * pixelSize, topBarY);
        spr.draw(batch);

        textPrinter.setColor(Color.WHITE);
        textPrinter.drawText(getX() + 21 * pixelSize, topBarY + 3 * pixelSize, directory.path(), batch);

        final float ioTypeY = getY() + getHeight() - 16 * pixelSize;
        spr.setRegion(318, 151, 19, 16);
        spr.setPosition(getX(), ioTypeY);
        spr.draw(batch);

        spr.setRegion(353, 151, 19, 16);
        spr.setPosition(getX() + getWidth() - 19 * pixelSize, ioTypeY);
        spr.draw(batch);

        spr.setRegion(338, 151, 14, 16);
        spr.setSize(getWidth() - 38 * pixelSize, spr.getHeight());
        spr.setPosition(getX() + 19 * pixelSize, ioTypeY);
        spr.draw(batch);

        textPrinter.setColor(Color.WHITE);
        textPrinter.drawText(getX() + 21 * pixelSize, ioTypeY + 3 * pixelSize, ioType, batch);

        //Bottom bar
        spr.setRegion(321, 183, 16, 16);
        spr.setPosition(getX(), getY());
        spr.draw(batch);

        spr.setRegion(353, 183, 35, 16);
        spr.setPosition(getX() + getWidth() - 35 * pixelSize, getY());
        spr.draw(batch);

        spr.setRegion(337, 183, 16, 16);
        spr.setSize(getWidth() - 51 * pixelSize, spr.getHeight());
        spr.setPosition(getX() + 16 * pixelSize, getY());
        spr.draw(batch);

        if(file != null) {
            textPrinter.setColor(Color.GRAY);
            textPrinter.drawText(getX() + 6 * pixelSize, getY() + 4 * pixelSize, file.name(), batch);
        }
    }

    public FileHandle getTarget() {
        return target;
    }

    public static FileHandle[] organizeByName(FileHandle[] source) {
        int length = source.length;
        FileHandle[] ret = source.clone();
        boolean sorted = false;

        while(!sorted) {
            sorted = true;
            for(int j = 0; j < length - 1; j++) {
                String name1 = ret[j].nameWithoutExtension().toLowerCase();
                String name2 = ret[j + 1].nameWithoutExtension().toLowerCase();
                if((name1.compareTo(name2) > 0 && !(!ret[j + 1].extension().equals("") && ret[j].extension().equals("")))
                        || (ret[j + 1].extension().equals("") && !ret[j].extension().equals(""))) { // If name2 < name1
                    sorted = false;
                    FileHandle temp = ret[j];
                    ret[j] = ret[j + 1];
                    ret[j + 1] = temp;
                }
            }
        }

        return ret;
    }

    @Override
    public boolean remove() {
        block.remove();
        return super.remove();
    }
}
