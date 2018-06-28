package com.pixelpalette;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class TextPrinter extends Sprite {
    private boolean smallText;
    public static Pixmap fontPixmap;
    public enum PrintType { kExtraSmall, kSmall, kBig }

    private final byte[] extraSmallWidth = {
            4, 1, 3, 4, 4, 4, 4, 1, 2, 2, 4, 3, 1, 3, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, 1, 3, 4, 3, 4,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 2, 4, 2, 3, 4,
            2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 3, 1, 3, 4, 4
    };
    private final byte[] smallWidth = {
            7, 3, 5, 7, 7, 7, 7, 3, 4, 4, 7, 7, 4, 7, 3, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 3, 4, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 5, 7, 5, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 4, 4, 7, 4, 7, 7, 7, 7, 7, 6, 7, 7, 7, 7, 7, 7, 7, 7, 5, 3, 5, 7, 7
    };

    public TextPrinter(boolean smallText) {
        super((Texture) PixelApp.MANAGER.get("GUI.png"));
        if(fontPixmap == null) {
            if(!getTexture().getTextureData().isPrepared()) {
                getTexture().getTextureData().prepare();
            }
            fontPixmap = getTexture().getTextureData().consumePixmap();
        }
        this.smallText = smallText;
    }

    public void drawInt(float x, float y, int number, Batch batch) {
        String str = Integer.toString(number);
        drawText(x, y, str, batch);
    }

    public void drawText(float x, float y, String str, Batch batch) {
        if(str != null) {
            if(smallText) {
                drawSmallText(x, y, str, batch);
            } else {
                drawBigText(x, y, str, batch);
            }
        }
    }

    private void drawSmallText(float x, float y, String str, Batch batch) {
        int ofs = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(c >= 32) {
                c -= 32;
                byte size = 7;
                if(c < smallWidth.length) {
                    size = smallWidth[c];
                }
                setRegion(577 + 8 * (c / 32), 10 * (c % 32), size, 9);
                setPosition(x + ofs * PixelApp.pixelSize, y);
                setSize(size * PixelApp.pixelSize, PixelApp.pixelSize * 9);
                draw(batch);
                ofs += size - 1;
            }
        }
    }

    private void drawBigText(float x, float y, String str, Batch batch) {
        float drawWidth = PixelApp.pixelSize * 10;
        float drawHeight = PixelApp.pixelSize * 12;
        float ofs = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int pos = 0;

            if(c == 32) {
                ofs += 10 * PixelApp.pixelSize;
            } if(c >= 48 && c <= 57) {
                pos = c - 48;

                setRegion(178 + 11 * pos, 0, 10, 12);
                setPosition(x + ofs, y);
                setSize(drawWidth, drawHeight);
                draw(batch);

                ofs += 10 * PixelApp.pixelSize;
            } else if(c >= 65 && c <= 90) {
                pos = c - 65;
                ofs += drawBigChar(x, y, pos, ofs, batch);
            } else if(c >= 97 && c <= 122) {
                pos = c - 97;
                ofs += drawBigChar(x, y, pos, ofs, batch);
            }
        }
    }

    public Pixmap getTextPixmap(String str, PrintType printType, int maxWidth) {
        Pixmap ret;
        switch (printType) {
            case kSmall:
            {
                ret = getSmallTextPixmap(str, maxWidth);
            }
            break;

            case kBig:
            {
                ret = getBigTextPixmap(str, maxWidth);
            }
            break;

            default:
            {
                ret = getExtraSmallTextPixmap(str, maxWidth);
            }
            break;
        }
        return ret;
    }

    private Pixmap getExtraSmallTextPixmap(String str, int maxWidth) {
        int width = 0;
        int height = 5;
        String[] words = str.split(" ");
        {
            int pos = 0;
            for(String word : words) {
                int len = getTextPixelWidth(word, PrintType.kExtraSmall);
                pos += len;
                if(pos > maxWidth) {
                    if(len > maxWidth) {
                        width = getTextPixelWidth(str, PrintType.kExtraSmall);
                    } else {
                        pos = len + 5;
                        height += 6;
                    }
                } else {
                    pos += 5;
                    if(width < pos)
                    {
                        width = pos;
                    }
                }
            }
        }

        Pixmap ret = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        int xOfs = 0;
        int yOfs = 0;
        for(String word : words)
        {
            if(xOfs + getTextPixelWidth(word, PrintType.kExtraSmall) > width) {
                xOfs = 0;
                yOfs += 6;
            }
            for(int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if(c >= 32) {
                    c -= 32;
                    int size = 4;
                    if(c < extraSmallWidth.length) {
                        size = extraSmallWidth[c];
                    }
                    ret.drawPixmap(fontPixmap, xOfs, yOfs, 604 + 6 * (c / 32), 290 + 6 * (c % 32), size, 5);
                    xOfs += size + 1;
                }
            }
            xOfs += 5;
        }
        return ret;
    }

    private Pixmap getSmallTextPixmap(String str, int maxWidth) {
        int width = 0;
        int height = 9;
        //boolean maxSize = false;
        String[] words = str.split(" ");
        {
            int pos = 0;
            for(String word : words) {
                int len = getTextPixelWidth(word, PrintType.kSmall);
                pos += len;
                if(pos > maxWidth) {
                    if(len > maxWidth) {
                        width = getTextPixelWidth(str, PrintType.kSmall);
                    } else {
                        pos = len + 6;
                        height += 8;
                    }
                } else {
                    pos += 6;
                    if(width < pos)
                    {
                        width = pos;
                    }
                }
            }
        }

        Pixmap ret = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        int xOfs = 0;
        int yOfs = 0;
        for(String word : words)
        {
            if(xOfs + getTextPixelWidth(word, PrintType.kSmall) > width) {
                xOfs = 0;
                yOfs += 8;
            }
            for(int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if(c >= 32) {
                    c -= 32;
                    int size = 7;
                    if(c < smallWidth.length) {
                        size = smallWidth[c];
                    }
                    ret.drawPixmap(fontPixmap, xOfs, yOfs, 577 + 8 * (c / 32), 1 + 10 * (c % 32), size, 9);
                    xOfs += size - 1;
                }
            }
            xOfs += 6;
        }
        return ret;
    }

    private Pixmap getBigTextPixmap(String str, int maxWidth) {
        Pixmap ret = new Pixmap(getTextPixelWidth(str, PrintType.kBig), getTextPixelHeight(str, PrintType.kBig), Pixmap.Format.RGBA8888);
        int xOfs = 0;
        int yOfs = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int pos;

            if(c == 32) {
                xOfs += 10;
            } if(c >= 48 && c <= 57) {
                pos = c - 48;

                ret.drawPixmap(fontPixmap, xOfs, yOfs, 178 + 11 * pos, 0, 10, 12);

                xOfs += 10;
            } else if(c >= 65 && c <= 90) {
                pos = c - 65;
                xOfs += drawBigChar(pos, xOfs, yOfs, ret);
            } else if(c >= 97 && c <= 122) {
                pos = c - 97;
                xOfs += drawBigChar(pos, xOfs, yOfs, ret);
            }
        }
        return ret;
    }

    private int getTextPixelWidth(String str) {
        int maxWidth = 0;
        int width = 0;
        int height = 0;
        if(smallText) {
            for(int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if(c >= 32) {
                    c -= 32;
                    byte size = 7;
                    if(c < this.smallWidth.length) {
                        size = this.smallWidth[c];
                    }
                    width += (size - 1);
                }
                if(c == 10) {
                    height++;
                    width = 0;
                }
                if(width > maxWidth) {
                    maxWidth = width;
                }
            }
        } else {
            for(int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if(c == 77 || c == 87 || c == 109 || c == 119) { // 'm' or 'w'
                    width += 12;
                } else if(c != 10) {
                    width += 10;
                } else {
                    height++;
                    width = 0;
                }
                if(maxWidth < width) {
                    maxWidth = width;
                }
            }
        }
        return maxWidth;
    }

    private int getTextPixelWidth(String str, PrintType printType) {
        int maxWidth = 0;
        int width = 0;
        switch (printType) {
            case kExtraSmall:
            {
                for(int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(c >= 32) {
                        c -= 32;
                        byte size = 7;
                        if(c < this.extraSmallWidth.length) {
                            size = this.extraSmallWidth[c];
                        }
                        width += (size + 1);
                    }
                    if(c == 10) {
                        width = 0;
                    }
                    if(width > maxWidth) {
                        maxWidth = width;
                    }
                }
            }
            break;

            case kSmall:
            {
                for(int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(c >= 32) {
                        c -= 32;
                        byte size = 7;
                        if(c < this.smallWidth.length) {
                            size = this.smallWidth[c];
                        }
                        width += (size - 1);
                    }
                    if(c == 10) {
                        width = 0;
                    }
                    if(width > maxWidth) {
                        maxWidth = width;
                    }
                }
            }
            break;

            case kBig:
            {
                for(int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(c == 77 || c == 87 || c == 109 || c == 119) { // 'm' or 'w'
                        width += 12;
                    } else if(c != 10) {
                        width += 10;
                    } else {
                        width = 0;
                    }
                    if(maxWidth < width) {
                        maxWidth = width;
                    }
                }
            }
            break;
        }
        return maxWidth;
    }

    private int getTextPixelHeight(String str, PrintType printType) {
        int height = 0;
        for(int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == 10) {
                switch (printType) {
                    case kExtraSmall:
                    {
                        height += 6;
                    }
                    break;

                    case kSmall:
                    {
                        height += 8;
                    }
                    break;

                    case kBig:
                    {
                        height += 13;
                    }
                    break;
                }
            }
        }
        switch (printType) {
            case kExtraSmall:
            {
                height += 6;
            }
            break;

            case kSmall:
            {
                height += 8;
            }
            break;

            case kBig:
            {
                height += 13;
            }
            break;
        }
        return height;
    }

    public float getTextWidth(String str) {
        return getTextPixelWidth(str) * PixelApp.pixelSize;
    }

    private float drawBigChar(float x, float y, int pos, float ofs, Batch batch) {
        float drawWidth = PixelApp.pixelSize * 10;
        float drawHeight = PixelApp.pixelSize * 12;

        if(pos < 12) { // Before 'm'
            setRegion(178 + 11 * pos, 13, 10, 12);
        } else if(pos == 12) { // 'm'
            setRegion(178, 39, 12, 12);
            drawWidth = PixelApp.pixelSize * 12;
        } else if(pos < 22) { // After 'm' and before 'w'
            setRegion(178 + 11 * (pos - 13), 26, 10, 12);
        } else if(pos == 22) { // 'w'
            setRegion(191, 39, 12, 12);
            drawWidth = PixelApp.pixelSize * 12;
        } else { // After 'w'
            setRegion(178 + 11 * (pos - 14), 26, 10, 12);
        }

        setPosition(x + ofs, y);
        setSize(drawWidth, drawHeight);
        draw(batch);
        return drawWidth;
    }

    private int drawBigChar(int pos, int xOfs, int yOfs, Pixmap pixmap) {
        int drawWidth = 10;
        int srcX = 0;
        int srcY = 0;

        if(pos < 12) { // Before 'm'
            srcX = 178 + 11 * pos;
            srcY = 13;
        } else if(pos == 12) { // 'm'
            srcX = 178;
            srcY = 39;
            drawWidth = 12;
        } else if(pos < 22) { // After 'm' and before 'w'
            srcX = 178 + 11 * (pos - 13);
            srcY = 26;
        } else if(pos == 22) { // 'w'
            srcX = 191;
            srcY = 39;
            drawWidth = 12;
        } else { // After 'w'
            srcX = 178 + 11 * (pos - 14);
            srcY = 26;
        }

        pixmap.drawPixmap(fontPixmap, xOfs, yOfs, srcX, srcY, drawWidth, 12);
        return drawWidth;
    }

    private int drawExtraSmallChar(int pos, int xOfs, int yOfs, Pixmap pixmap) {
        int drawWidth = 4;
        int srcX = 0;

        if(pos < 12) { // Before 'm'
            srcX = 604 + 5 * pos;
        } else if(pos == 12) { // 'm'
            srcX = 664;
            drawWidth = 5;
        } else if(pos < 22) { // After 'm' and before 'w'
            srcX = 605 + 5 * pos;
        } else if(pos == 22) { // 'w'
            srcX = 715;
            drawWidth = 5;
        } else { // After 'w'
            srcX = 606 + 5 * pos;
        }

        pixmap.drawPixmap(fontPixmap, xOfs, yOfs, srcX, 392, drawWidth, 5);
        return drawWidth + 1;
    }
}
