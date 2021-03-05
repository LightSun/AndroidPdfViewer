package com.heaven7.android.pdf;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.shockwave.pdfium.PdfDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * the pdf annotation manager. currently only support image.
 * @since 10.0.7
 * @author heaven7
 */
public final class PdfAnnotManager{

    private final PdfDocument document;
    private final SparseArray<List<AnnotPart>> mPageAnnoMap = new SparseArray<>();

    public PdfAnnotManager(PdfDocument docPtr) {
        this.document = docPtr;
    }

    public PdfDocument getPdfDocument(){
        return document;
    }
    /**
     * read annotations from pdf. this often called first.
     * currently only support image.
     * @since 10.1.2
     */
    public void readAnnots(){
        NativeCallback cb = new NativeCallback() {
            @Override
            public void onGotImageAnnotation(int pageIndex, long annoPtr, long imgPtr) {
                List<AnnotPart> parts = mPageAnnoMap.get(pageIndex);
                if(parts == null){
                    parts = new ArrayList<>();
                    mPageAnnoMap.put(pageIndex, parts);
                }
                parts.add(new AnnotPart(annoPtr, imgPtr));
            }
        };
        nReadAll(document.getNativePtr(), cb);
    }
    /**
     * add image to pdf. bitmap's with and height should be the times of @width @height.
     * @param pageIndex the page index
     * @param bitmap the bitmap to add
     * @param left the left cors
     * @param top the top cors
     * @param width the width of real pdf image
     * @param height the height of real pdf image
     * @return the annot ptr.
     */
    public long addImage(int pageIndex, Bitmap bitmap, float left, float top, int width, int height){
        return addImage(pageIndex, bitmap, left, top, width, height, true);
    }
    /**
     * add image to pdf.  bitmap's with and height should be the times of @width @height.
     * @param pageIndex the page index
     * @param bitmap the bitmap to add
     * @param left the left cors
     * @param top the top cors
     * @param width the width
     * @param height the height
     * @param topAsBottom use top as bottom. default is true. because pdf use left-bottom
     * @return the annot ptr.
     */
    public synchronized long addImage(int pageIndex, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom){
        return addAnnot(pageIndex, bitmap, left, top, width, height, topAsBottom);
    }
    /**
     * add annot to pdf.  bitmap's with and height should be the times of @width @height.
     * @param pageIndex the page index
     * @param bitmap the bitmap to add
     * @param left the left cors
     * @param top the top cors
     * @param width the width
     * @param height the height
     * @param topAsBottom use top as bottom. default is true. because pdf use left-bottom
     * @return the annot ptr.
     * @since 10.1.2
     */
    public synchronized long addAnnot(int pageIndex, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom){
        List<AnnotPart> parts = mPageAnnoMap.get(pageIndex);
        if(parts == null){
            parts = new ArrayList<>();
            mPageAnnoMap.put(pageIndex, parts);
        }
        long annot = nCreateAnnot(document.getNativePtr(), pageIndex);
        long imgPtr = nAddImage(document.getNativePtr(), pageIndex, annot, bitmap, left, top, width, height, topAsBottom);
        if(imgPtr != 0){
            parts.add(new AnnotPart(annot, imgPtr));
        }
        return annot;
    }
    public synchronized boolean removeImage(int pageIndex, long annotPtr){
        return removeAnnot(pageIndex, annotPtr);
    }

    /**
     * remove annot for target page
     * @param pageIndex the page index
     * @param annotPtr the anno ptr
     * @return true if success
     * @since 10.1.2
     */
    public synchronized boolean removeAnnot(int pageIndex, long annotPtr){
        List<AnnotPart> parts = mPageAnnoMap.get(pageIndex);
        if(parts == null){
            return false;
        }
        AnnotPart old = null;
        for (AnnotPart part: parts){
            if(part.annoPtr == annotPtr){
                old = part;
                break;
            }
        }
        if(old != null){
            parts.remove(old);
            return nRemoveAnnot(document.getNativePtr(), pageIndex, old.annoPtr);
        }
        return false;
    }

    /**
     * clear images for target page
     * @param pageIndex the page index
     * @since 10.1.2
     */
    public synchronized void clearAnnots(int pageIndex){
        List<AnnotPart> parts = mPageAnnoMap.get(pageIndex);
        if(parts != null){
            for (AnnotPart part: parts){
                nRemoveAnnot(document.getNativePtr(), pageIndex, part.annoPtr);
            }
            mPageAnnoMap.remove(pageIndex);
        }
    }

    /**
     * clear all images for target pdf
     * @since 10.1.2
     */
    public synchronized void clearAnnots(){
        int size = mPageAnnoMap.size();
        for (int i = 0 ; i < size ; i ++){
            int pageIndex = mPageAnnoMap.keyAt(i);
            List<AnnotPart> parts = mPageAnnoMap.valueAt(i);
            if(parts != null){
                for (AnnotPart part: parts){
                    nRemoveAnnot(document.getNativePtr(), pageIndex, part.annoPtr);
                }
            }
        }
        mPageAnnoMap.clear();
    }

    /**
     * clear annotations lightly. that means: never clear the pdf's.
     * @since 10.1.2
     */
    public synchronized void clearAnnotsLightly(){
        mPageAnnoMap.clear();
    }

    private static native boolean nRemoveAnnot(long docPtr, int pageIndex, long annoPtr);
    private static native boolean nRemoveImage(long docPtr, int pageIndex, long annoPtr, long imgPtr);

    private static native long nCreateAnnot(long docPtr, int pageIndex);
    //add an image to annot and return image object. topAsBottom default is true. because pdf use left-bottom.
    private static native long nAddImage(long docPtr, int pageIndex, long annoPtr, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom);

    private static native void nReadAll(long docPtr, NativeCallback cb);
    //one annot -> one image
    private static class AnnotPart{
        private long annoPtr;
        private Long imgPtr;

        public AnnotPart(long annoPtr, Long imgPtr) {
            this.annoPtr = annoPtr;
            this.imgPtr = imgPtr;
        }

        public long getAnootPtr() {
            return annoPtr;
        }
        public long getImagePtr(){
            return imgPtr;
        }
    }
    public interface NativeCallback{
        void onGotImageAnnotation(int page_index, long annoPtr, long imgPtr);
    }
}
