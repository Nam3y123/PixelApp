package com.pixelpalette;

/**
 * Created by jnich on 12/15/2017.
 */

public interface Tool {
    void touchDown(float pointerX, float pointerY);
    void touchUp(float pointerX, float pointerY);
    void touchDragged(float pointerX, float pointerY);
    void cancelTouch();
    ToolOptions getOptions();
    void setOptions(final ToolOptions options);
}
