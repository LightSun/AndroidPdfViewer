package com.github.barteksc.pdfviewer.mark;

import java.util.Arrays;
/**
 * @since 10.0.3
 */
public class ImageMark {
    private int pageIndex;
    private float width; //result width
    private float height;
    private float left;
    private float top;
    private float rotate; //current only support 0-90-180-270 and etc.

    protected ImageMark(ImageMark.Builder builder) {
        this.pageIndex = builder.pageIndex;
        this.width = builder.width;
        this.height = builder.height;
        this.left = builder.left;
        this.top = builder.top;
        this.rotate = builder.rotate;
    }

    public ImageMark(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageMark imageMark = (ImageMark) o;
        return pageIndex == imageMark.pageIndex &&
                width == imageMark.width &&
                height == imageMark.height &&
                left == imageMark.left &&
                top == imageMark.top &&
                Float.compare(imageMark.rotate, rotate) == 0;
    }

    @Override
    public int hashCode() {
        return hash(pageIndex, width, height, left, top, rotate);
    }
    private static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getLeft() {
        return this.left;
    }

    public float getTop() {
        return this.top;
    }

    public float getRotate() {
        return this.rotate;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public void setRotate(float rotate) {
        this.rotate = rotate;
    }

    public static class Builder {
        private int pageIndex;
        private float width; //result width
        private float height;
        private float left;
        private float top;
        private float rotate; //current only support 0-90-180-270 and etc.

        public Builder setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        public Builder setLeft(float left) {
            this.left = left;
            return this;
        }

        public Builder setTop(float top) {
            this.top = top;
            return this;
        }

        public Builder setRotate(float rotate) {
            this.rotate = rotate;
            return this;
        }

        public ImageMark build() {
            return new ImageMark(this);
        }
    }
}