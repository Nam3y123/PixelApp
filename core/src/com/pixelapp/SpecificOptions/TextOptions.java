package com.pixelapp.SpecificOptions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pixelapp.AnimProcessor;
import com.pixelapp.TextPrinter;
import com.pixelapp.ToolOptions;

import static com.pixelapp.PixelApp.pixelSize;

public class TextOptions extends ToolOptions {
    private TextPrinter.PrintType size;
    private boolean sizeMenu;
    private int maxWidth;
    private String message;
    private Pixmap textPixmap;
    private Sprite textSprite;
    private boolean drawText;
    private TextPrinter textPrinter;
    private AnimProcessor processor;
    private int textColor;

    public TextOptions(AnimProcessor processor) {
        super();
        setSize(54, 18);
        size = TextPrinter.PrintType.kSmall;
        sizeMenu = false;
        textPrinter = new TextPrinter(false);
        textSprite = new Sprite();
        this.processor = processor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(textPixmap != null && drawText) {
            textSprite.draw(batch, parentAlpha);
        }

        if(sizeMenu) {
            setSize(66, 34);
        } else {
            setSize(54, 18);
        }
        spr.setRegion(38, 293, 54, 18);
        super.draw(batch, parentAlpha);

        switch (size)
        {
            case kExtraSmall:
                spr.setRegion(125, 294, 16, 16);
                break;

            case kSmall:
                spr.setRegion(109, 294, 16, 16);
                break;

            case kBig:
                spr.setRegion(93, 294, 16, 16);
                break;

            default:
                break;
        }
        spr.setPosition(1, 1);
        spr.draw(batch, parentAlpha);

        if(sizeMenu) {
            spr.setRegion(93, 294, 48, 16);
            spr.setPosition(18, -16);
            spr.draw(batch, parentAlpha);
        }
    }

    @Override
    protected boolean touchDown(float pointerX, float pointerY) {
        boolean ret = false;
        if(pointerY > getHeight() - 18 * pixelSize) {
            if(pointerX < 18 * pixelSize) {
                sizeMenu = !sizeMenu;
            } else if(pointerX < 36 * pixelSize) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            int uncheckedInput = Integer.parseInt(text);
                            if(uncheckedInput < 0 || uncheckedInput > 999) {
                                throw new NumberFormatException();
                            }
                            maxWidth = uncheckedInput;
                            updateTextPixmap();
                        } catch(NumberFormatException e) {
                            Gdx.input.getTextInput(this, "Invalid input. Set max text width", Integer.toString(maxWidth), "");
                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set max text width", Integer.toString(maxWidth), "");
            } else {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        message = text;
                        updateTextPixmap();
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Set new message", message, "");
            }
            ret = true;
        } else if(sizeMenu && pointerX > 18 * pixelSize) {
            if(pointerX < 34 * pixelSize) {
                size = TextPrinter.PrintType.kBig;
            } else if(pointerX < 50 * pixelSize) {
                size = TextPrinter.PrintType.kSmall;
            } else {
                size = TextPrinter.PrintType.kExtraSmall;
            }
            updateTextPixmap();
            sizeMenu = false;
            ret = true;
        }
        return ret;
    }

    @Override
    protected void dispose() {
        if(textPixmap != null) {
            textPixmap.dispose();
            textPixmap = null;
        }
    }

    public void updateTextPixmap() {
        if(textPixmap != null) {
            textPixmap.dispose();
            textPixmap = null;
        }
        if(message != null) { // Trying to change draw type for writing text
            textPixmap = textPrinter.getTextPixmap(message, size, maxWidth);
            textSprite = new Sprite(new Texture(textPixmap));
        }
    }

    public Pixmap getTextPixmap() {
        return textPixmap;
    }

    public Sprite getTextSprite() {
        return textSprite;
    }

    public void setDrawText(boolean drawText) {
        this.drawText = drawText;

        if(textPixmap != null) {
            float canvasWidth = processor.getWidth();
            float widthRatio = 1.0f * textPixmap.getWidth() / processor.getImgW();
            float canvasHeight = processor.getHeight();
            float heightRatio = 1.0f * textPixmap.getHeight() / processor.getImgH();
            textSprite.setSize(canvasWidth * widthRatio, canvasHeight * heightRatio);
        }
    }

    public boolean getDrawText() {
        return drawText;
    }

    public TextPrinter.PrintType getSize() {
        return size;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        Color col = new Color();
        Color.rgba8888ToColor(col, textColor);
        textSprite.setColor(col);
    }
}
