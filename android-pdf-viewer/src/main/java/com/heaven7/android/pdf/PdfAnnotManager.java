package com.heaven7.android.pdf;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.shockwave.pdfium.PdfDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * the pdf annotation manager
 * @since 10.0.7
 * @author heaven7
 */
public final class PdfAnnotManager{

    private final PdfDocument document;
    private final SparseArray<AnnotPart> mPageAnnoMap = new SparseArray<>();

    public PdfAnnotManager(PdfDocument docPtr) {
        this.document = docPtr;
    }

    public PdfDocument getPdfDocument(){
        return document;
    }
    /**
     * add image to pdf.
     * @param pageIndex the page index
     * @param bitmap the bitmap to add
     * @param left the left cors
     * @param top the top cors
     * @param width the width
     * @param height the height
     * @return the real image ptr.
     */
    public long addImage(int pageIndex, Bitmap bitmap, float left, float top, int width, int height){
        return addImage(pageIndex, bitmap, left, top, width, height, true);
    }
    /**
     * add image to pdf.
     * @param pageIndex the page index
     * @param bitmap the bitmap to add
     * @param left the left cors
     * @param top the top cors
     * @param width the width
     * @param height the height
     * @param topAsBottom use top as bottom. default is true. because pdf use left-bottom
     * @return the real image ptr.
     */
    public synchronized long addImage(int pageIndex, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom){
        AnnotPart part = mPageAnnoMap.get(pageIndex);
        if(part == null){
            part = new AnnotPart(nCreateAnnot(document.getNativePtr(), pageIndex));
            mPageAnnoMap.put(pageIndex, part);
        }
        long imgPtr = nAddImage(document.getNativePtr(), pageIndex, part.getNativePtr(), bitmap, left, top, width, height, topAsBottom);
        if(imgPtr != 0){
            part.addImageObject(imgPtr);
        }
        return imgPtr;
    }
    public synchronized boolean removeImage(int pageIndex, long imgPtr){
        AnnotPart part = mPageAnnoMap.get(pageIndex);
        if(part == null){
            return false;
        }
        boolean result = nRemoveImage(document.getNativePtr(), pageIndex, part.getNativePtr(), imgPtr);
        if(result){
            return part.removeImageObject(imgPtr);
        }
        return false;
    }
    public synchronized List<Long> getImages(int pageIndex){
        AnnotPart part = mPageAnnoMap.get(pageIndex);
        return part != null ? part.imageObjPtrs : Collections.<Long>emptyList();
    }

    public boolean removeAnnotation(int pageIndex){
        AnnotPart part = mPageAnnoMap.get(pageIndex);
        if(part != null){
            return nRemoveAnnot(document.getNativePtr(), pageIndex, part.getNativePtr());
        }
        return false;
    }

    private static native boolean nRemoveAnnot(long docPtr, int pageIndex, long annoPtr);
    private static native boolean nRemoveImage(long docPtr, int pageIndex, long annoPtr, long imgPtr);

    private static native long nCreateAnnot(long docPtr, int pageIndex);
    //add an image to annot and return image object. topAsBottom default is true. because pdf use left-bottom.
    private static native long nAddImage(long docPtr, int pageIndex, long annoPtr, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom);

    private static class AnnotPart implements INativeOwner{
        private long annoPtr;
        private List<Long> imageObjPtrs;

        AnnotPart(long annoPtr) {
            this.annoPtr = annoPtr;
        }
        @Override
        public long getNativePtr() {
            return annoPtr;
        }
        public void addImageObject(long imgPtr) {
            if(imageObjPtrs == null){
                imageObjPtrs = new ArrayList<>();
            }
            if(imageObjPtrs.contains(imgPtr)){
                imageObjPtrs.add(imgPtr);
            }
        }
        public boolean removeImageObject(long imgPtr){
            return imageObjPtrs.remove(imgPtr);
        }
        public void clearImageObjects(){
            imageObjPtrs.clear();
        }
    }
}
