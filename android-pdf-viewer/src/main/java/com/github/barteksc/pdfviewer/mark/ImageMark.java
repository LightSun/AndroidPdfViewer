package com.github.barteksc.pdfviewer.mark;

import java.util.Arrays;
/**
 * @since 10.0.3
 */
public class ImageMark {
    private int pageIndex;
    private int width; //result width
    private int height;
    private int left;
    private int top;
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

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLeft() {
        return this.left;
    }

    public int getTop() {
        return this.top;
    }

    public float getRotate() {
        return this.rotate;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setRotate(float rotate) {
        this.rotate = rotate;
    }

    public static class Builder {
        private int pageIndex;
        private int width; //result width
        private int height;
        private int left;
        private int top;
        private float rotate; //current only support 0-90-180-270 and etc.

        public Builder setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setLeft(int left) {
            this.left = left;
            return this;
        }

        public Builder setTop(int top) {
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