package com.pixelpalette;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;

public class MenuList extends Actor {
    private final com.pixelpalette.TextPrinter printer = new com.pixelpalette.TextPrinter(true);
    private final ArrayList<Button> buttons = new ArrayList<>();
    private float boxWidth;
    private Sprite spr;
    private boolean visible;
    private Actor block;

    public MenuList(String[] names, Runnable[] actions)
    {
        if(names.length != actions.length)
        {
            throw new GdxRuntimeException("There are mismatching names and actions in this MenuList: "
                    + names.length + "names, "
                    + actions.length + "actions");
        }

        int len = names.length;
        boxWidth = -1;
        for (int i = 0; i < len; i++)
        {
            final Button button = new Button();
            button.name = names[i];
            button.action = actions[i];
            button.width = printer.getTextWidth(button.name);
            if (button.width > boxWidth)
            {
                boxWidth = button.width;
            }

            buttons.add(button);
        }
        boxWidth += 22 * PixelApp.pixelSize;

        spr = new Sprite((Texture) PixelApp.MANAGER.get("GUI.png"));
        setHeight(17 * PixelApp.pixelSize * len);
        setWidth(boxWidth);

        addCaptureListener(new ClickListener()
        {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (visible)
                {
                    int pos = (int)Math.floor(y / (17 * PixelApp.pixelSize));
                    buttons.get(pos).action.run();
                    hide();
                }
                return true;
            }
        });

        block = new Actor();
        block.setBounds(0, 0, PixelApp.width, PixelApp.height);
        block.addCaptureListener(new ClickListener()
        {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (visible)
                {
                    hide();
                }
                return true;
            }
        });
        visible = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (visible)
        {
            float middleWidth = boxWidth - 22 * PixelApp.pixelSize;
            for (int i = 0; i < buttons.size(); i++)
            {
                int yPos = 165;
                if (i == 0)
                {
                    yPos = 182;
                }
                else if (i == buttons.size() - 1)
                {
                    yPos = 148;
                }

                float y = getY() + (17 * PixelApp.pixelSize * i);

                spr.setRegion(394, yPos, 11, 17);
                spr.setSize(11 * PixelApp.pixelSize, 17 * PixelApp.pixelSize);
                spr.setPosition(getX(), y);
                spr.draw(batch, parentAlpha);

                spr.setRegion(397, yPos, 18, 17);
                spr.setSize(middleWidth, 17 * PixelApp.pixelSize);
                spr.setPosition(getX() + 11 * PixelApp.pixelSize, y);
                spr.draw(batch, parentAlpha);

                spr.setRegion(407, yPos, 11, 17);
                spr.setSize(11 * PixelApp.pixelSize, 17 * PixelApp.pixelSize);
                spr.setPosition(getX() + middleWidth + 11 * PixelApp.pixelSize, y);
                spr.draw(batch, parentAlpha);

                float x = getX() + (boxWidth - buttons.get(i).width) / 2.0f;
                printer.drawText(x, y + 3 * PixelApp.pixelSize, buttons.get(i).name, batch);
            }
        }
    }

    private void add(Stage parent)
    {
        parent.addActor(block);
        parent.addActor(this);
    }

    public void show(float x, float y, Stage stage)
    {
        add(stage);
        if (x + boxWidth > PixelApp.width)
        {
            x -= boxWidth;
        }
        if (y + getHeight() > PixelApp.height)
        {
            y -= getHeight();
        }
        setX(x);
        setY(y);
        visible = true;
    }

    public void hide()
    {
        visible = false;
        block.remove();
        remove();
    }


    private class Button // POD
    {
        public String name;
        public Runnable action;
        public float width;
    }
}
