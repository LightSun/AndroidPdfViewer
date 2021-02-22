package com.github.barteksc.pdfviewer.listener;

/**
 * @since 10.0.3
 */
public abstract class OnDrawListener2 implements OnDrawListener{

    private float translateX;
    private float translateY;

    public void reportTranslates(float transX, float tranxY){
        this.translateX = transX;
        this.translateY = tranxY;
    }

    public float getTranslateX() {
        return translateX;
    }
    public float getTranslateY() {
        return translateY;
    }
}
