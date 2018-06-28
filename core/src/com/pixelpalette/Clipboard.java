package com.pixelpalette;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;

import static com.pixelpalette.PixelApp.pixelSize;

public class Clipboard extends SideMenu {
    public static FileHandle clipboardHandle;
    private Pixmap[] clips;
    private Sprite[] clipSprites;
    private float scroll;

    public Clipboard(final DrawStage parent) {
        super(parent);
        scroll = 0;
        if(clipboardHandle == null) {
            clipboardHandle = Gdx.files.external("PixelApp/Clipboard");
        }

        grabFiles();

        addCaptureListener(new ClickListener() {
            private boolean scrollChanged, canScroll;
            private float storeY, storeScroll;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean ret = !hidden();
                scrollChanged = true;
                canScroll = false;
                if(ret) {
                    if(minimized) {
                        maximize();
                    } else {
                        if(y > getHeight() - 17 * pixelSize && x > 71 * pixelSize) {
                            minimize();
                        } else if(y > getHeight() - 21 * pixelSize && y < getHeight() - 4 * pixelSize) {
                            if(x > 4 * pixelSize && x < 20 * pixelSize) {
                                if(parent.getSelectOptions().getClip() != null) {
                                    parent.getCanvas().paste(parent.getSelectOptions().getClip());
                                }
                            }
                        }
                        if(y < getHeight() - 24 * pixelSize) {
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
                if(canScroll && !scrollChanged && x > 62 * pixelSize) {
                    float distance = (getHeight() - 24 * pixelSize) - y + scroll;
                    int selection = (int)Math.floor(distance / (39 * pixelSize));
                    if(selection < clips.length) {
                        if((int)Math.floor(distance / (19.5f * pixelSize)) % 2 == 1) {
                            parent.getCanvas().paste(clips[selection]);
                        } else {
                            delete(selection);
                        }
                    }
                }
            }
        });
    }

    private void checkScroll() {
        float maxScroll = 39 * pixelSize * clips.length - (com.pixelpalette.PixelApp.height - 24 * pixelSize);
        if(scroll > maxScroll) {
            scroll = maxScroll;
        }
        if(scroll < 0) {
            scroll = 0;
        }
    }

    public void grabFiles() {
        if(clips != null) {
            dispose();
        }
        if(clipboardHandle != null && clipboardHandle.exists()) {
            ArrayList<FileHandle> list = new ArrayList<>();
            for(FileHandle handle : clipboardHandle.list()) {
                if(handle.extension().equals("png")) {
                    list.add(handle);
                }
            }
            FileHandle[] listArray = new FileHandle[list.size()];
            list.toArray(listArray);
            listArray = FileMenu.organizeByName(listArray);

            clips = new Pixmap[listArray.length];
            clipSprites = new Sprite[listArray.length];
            for(int i = 0; i < listArray.length; i++) {
                clips[i] = new Pixmap(listArray[i]);
                clipSprites[i] = new Sprite(new Texture(clips[i]));
            }
        }
    }

    private void delete(int selection) {
        Pixmap[] newClips = new Pixmap[clips.length - 1];
        clipSprites = new Sprite[newClips.length];
        boolean deletionReached = false;
        for(int i = 0; i < clips.length; i++) {
            if(i == selection) {
                deletionReached = true;
                clips[i].dispose();
            } else {
                int newClipPosition = i - (deletionReached ? 1 : 0);
                newClips[newClipPosition] = clips[i];
                clipSprites[newClipPosition] = new Sprite(new Texture(newClips[newClipPosition]));
            }
        }

        ArrayList<FileHandle> list = new ArrayList<>();
        for(FileHandle handle : clipboardHandle.list()) {
            if(handle.extension().equals("png")) {
                list.add(handle);
            }
        }
        FileHandle[] listArray = new FileHandle[list.size()];
        list.toArray(listArray);
        listArray = FileMenu.organizeByName(listArray);
        listArray[selection].delete();
        clips = newClips;
        checkScroll();
    }

    @Override
    protected void drawMinimized(Batch batch, float parentAlpha) {
        spr.draw(batch, parentAlpha);
    }

    @Override
    protected void drawMaximized(Batch batch, float parentAlpha) {
        spr.setRegion(604, 25, 87, 2);
        spr.setSize(getWidth(), getHeight() - 24 * pixelSize);
        spr.setPosition(getX(), 0);
        spr.draw(batch, parentAlpha);

        for(int i = 0; i < clips.length; i++) {
            Pixmap clip = clips[i];
            float yPos = com.pixelpalette.PixelApp.height - 63 * pixelSize - 39 * pixelSize * i + scroll;

            spr.setRegion(604, 27, 87, 39);
            spr.setSize(getWidth(), 39 * pixelSize);
            spr.setPosition(getX(), yPos);
            spr.draw(batch, parentAlpha);

            float ratio = 1f * clip.getHeight() / clip.getWidth();
            if(ratio < 1f) { // More wide than tall
                spr.setRegion(423, 28, 36, (int)(36 * ratio));
                spr.setSize(36 * pixelSize, 36 * ratio * pixelSize);
            } else {
                spr.setRegion(423, 28, (int)(36 / ratio), 36);
                spr.setSize(36 * pixelSize / ratio, 36 * pixelSize);
            }
            float xOfs = pixelSize * (36 - spr.getRegionWidth()) / 2f;
            float yOfs = pixelSize * (36 - spr.getRegionHeight()) / 2f;
            spr.setPosition(getX() + 16 * pixelSize + xOfs, yPos + 2 * pixelSize + yOfs);
            spr.draw(batch, parentAlpha);

            clipSprites[i].setSize(spr.getWidth(), spr.getHeight());
            clipSprites[i].setPosition(spr.getX(), spr.getY());
            clipSprites[i].draw(batch, parentAlpha);
        }

        spr.setRegion(604, 0, 87, 24);
        spr.setSize(getWidth(), 24 * pixelSize);
        spr.setPosition(getX(), com.pixelpalette.PixelApp.height - spr.getHeight());
        spr.draw(batch, parentAlpha);
    }

    @Override
    public void maximize() {
        super.maximize();
        setBounds(com.pixelpalette.PixelApp.width - 87 * pixelSize, 0, 87 * pixelSize, com.pixelpalette.PixelApp.height);
    }

    @Override
    public void minimize() {
        super.minimize();
        setBounds(com.pixelpalette.PixelApp.width - 16 * pixelSize, 64 * pixelSize, 16 * pixelSize, 23 * pixelSize);
        spr.setRegion(300, 122, 16, 23);
        spr.setPosition(com.pixelpalette.PixelApp.width - 16 * pixelSize, 64 * pixelSize);
        spr.setSize(16 * pixelSize, 23 * pixelSize);
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void dispose() {
        for(Pixmap clip: clips) {
            clip.dispose();
        }
    }
}
