package com.github.barteksc.sample;

import android.graphics.Bitmap;

import com.github.barteksc.pdfviewer.mark.ImageMark;

public class ImageDataHolder {

    private ImageMark imageMark;
    private Bitmap bitmap; //pdf's


    public ImageMark getImageMark() {
        return imageMark;
    }

    public void setImageMark(ImageMark imageMark) {
        this.imageMark = imageMark;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
