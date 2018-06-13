package com.pixelapp;

public class HSVColor {
    private float hue;
    private float saturation;
    private float value;
    private float alpha;

    public HSVColor() {
        hue = 0;
        saturation = 0;
        value = 0;
        alpha = 100;
    }

    public void RGBAtoHSV(int rgba) {
        int[] splitRGBA = new int[4];
        splitRGBA[0] = (rgba & 0xff000000) >> 24;
        if(splitRGBA[0] < 0) {
            splitRGBA[0] += 256;
        }
        splitRGBA[1] = (rgba & 0x00ff0000) >> 16;
        splitRGBA[2] = (rgba & 0x0000ff00) >> 8;
        splitRGBA[3] = (rgba & 0x000000ff);

        int highest = 0;
        int lowest = 0;
        for(int i = 0; i < 3; i++) {
            if(splitRGBA[i] > splitRGBA[highest]) {
                highest = i;
            }
            if(splitRGBA[i] < splitRGBA[lowest]) {
                lowest = i;
            }
        }

        float[] floatRGBA = new float[4];
        for(int i = 0; i < 4; i++) {
            floatRGBA[i] = splitRGBA[i] / 255f;
        }
        float delta = floatRGBA[highest] - floatRGBA[lowest];
        alpha = Math.round(100 * floatRGBA[3]);
        if(delta > 0) {
            if(highest == 0) { // Red
                hue = 60 * (((floatRGBA[1] - floatRGBA[2]) / delta) % 6);
            } else if(highest == 1) {
                hue = 60 * (((floatRGBA[2] - floatRGBA[0]) / delta) + 2);
            } else {
                hue = 60 * (((floatRGBA[0] - floatRGBA[1]) / delta) + 4);
            }
        }
        if(floatRGBA[highest] == 0) {
            saturation = 0;
        } else {
            saturation = Math.round(100 * delta / floatRGBA[highest]);
        }
        value = Math.round(100 * floatRGBA[highest]);
    }

    public int toRGBA() {
        value /= 100f;
        saturation /= 100f;

        int rgba = 0x00000000;
        float red, green, blue;

        if(saturation > 0.91) {
            rgba = 0;
        }

        float c = value * saturation;
        float x = c * (1 - Math.abs(((hue / 60) % 2) - 1));
        if(x < 0) {
            x = 0;
        }
        float m = value - c;
        if(hue < 60) {
            red = c;
            green = x;
            blue = 0;
        } else if(hue < 120) {
            red = x;
            green = c;
            blue = 0;
        } else if(hue < 180) {
            red = 0;
            green = c;
            blue = x;
        } else if(hue < 240) {
            red = 0;
            green = x;
            blue = c;
        } else if(hue < 300) {
            red = x;
            green = 0;
            blue = c;
        } else {
            red = c;
            green = 0;
            blue = x;
        }

        int iRed = (int)((red + m) * 255);
        int iGreen = (int)((green + m) * 255);
        int iBlue = (int)((blue + m) * 255);
        int iAlpha = (int)((alpha / 100f) * 255);
        rgba += iAlpha;
        rgba += iBlue << 8;
        rgba += iGreen << 16;
        rgba += iRed << 24;

        value *= 100f;
        saturation *= 100f;

        return rgba;
    }

    public void setH(float h) {
        hue = h;
        if(hue > 360) {
            hue = 360;
        }
        if(hue < 0) {
            hue = 0;
        }
    }

    public float getH() {
        return hue;
    }

    public void setS(float s) {
        saturation = s;
        if(saturation > 100) {
            saturation = 100;
        }
        if(saturation < 0) {
            saturation = 0;
        }
    }

    public float getS() {
        return saturation;
    }

    public void setV(float v) {
        value = v;
        if(value > 100) {
            value = 100;
        }
        if(value < 0) {
            value = 0;
        }
    }

    public float getV() {
        return value;
    }

    public void setA(float a) {
        alpha = a;
        if(alpha > 100) {
            alpha = 100;
        }
        if(alpha < 0) {
            alpha = 0;
        }
    }

    public float getA() {
        return alpha;
    }
}
