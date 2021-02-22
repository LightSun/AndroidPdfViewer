package com.github.barteksc.pdfviewer.mark;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.SparseArray;

import com.github.barteksc.pdfviewer.listener.OnDrawListener2;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 10.0.3
 */
public class PdfMarkManager extends OnDrawListener2 {

    private final SparseArray<List<ImageMark>> mImageMarks = new SparseArray<>();
    private final ImageOwner mImageOwner;

    public PdfMarkManager(ImageOwner imageOwner) {
        this.mImageOwner = imageOwner;
    }
    public boolean hasImageMark(ImageMark mark){
        List<ImageMark> marks = mImageMarks.get(mark.getPageIndex());
        return marks != null && marks.contains(mark);
    }
    public void addImageMark(ImageMark mark, boolean unique){
        List<ImageMark> marks = mImageMarks.get(mark.getPageIndex());
        if(marks == null){
            marks = new ArrayList<>(3);
            mImageMarks.put(mark.getPageIndex(), marks);
        }
        if(unique && marks.contains(mark)){
            return;
        }
        marks.add(mark);
    }
    public boolean removeImageMark(ImageMark mark){
        List<ImageMark> marks = mImageMarks.get(mark.getPageIndex());
        if(marks == null){
            return false;
        }
        return marks.remove(mark);
    }
    public void clearImageMarks(int pageIndex){
        if(pageIndex < 0){
            mImageMarks.clear();
        }else {
            mImageMarks.remove(pageIndex);
        }
    }
    public SparseArray<List<ImageMark>> getImageMarks(){
        return mImageMarks;
    }
    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        List<ImageMark> marks = mImageMarks.get(displayedPage);
        if(marks != null){
            for (ImageMark mark: marks){
                Bitmap bitmap = mImageOwner.getBitmap(mark);
                onDrawImageMark(canvas, mark, pageWidth, pageHeight, bitmap);
            }
        }
    }

    protected void onDrawImageMark(Canvas canvas, ImageMark mark, float pageWidth, float pageHeight, Bitmap bitmap){
        canvas.drawBitmap(bitmap, mark.getLeft() - getTranslateX(), mark.getTop() - getTranslateY(), null);
    }

    public interface ImageOwner{
        Bitmap getBitmap(ImageMark mark);
    }
}
