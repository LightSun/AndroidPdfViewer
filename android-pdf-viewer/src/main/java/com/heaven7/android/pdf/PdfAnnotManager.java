package com.heaven7.android.pdf;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.shockwave.pdfium.PdfDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * the pdf annotation manager
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
        return imgPtr;
    }
    public synchronized boolean removeImage(int pageIndex, long imgPtr){
        List<AnnotPart> parts = mPageAnnoMap.get(pageIndex);
        if(parts == null){
            return false;
        }
        long annoPtr = 0;
        for (AnnotPart part: parts){
            if(part.imgPtr == imgPtr){
                annoPtr = part.getAnootPtr();
                break;
            }
        }
        if(annoPtr != 0){
            return nRemoveAnnot(document.getNativePtr(), pageIndex, annoPtr);
        }
        return false;
    }

    private static native boolean nRemoveAnnot(long docPtr, int pageIndex, long annoPtr);
    private static native boolean nRemoveImage(long docPtr, int pageIndex, long annoPtr, long imgPtr);

    private static native long nCreateAnnot(long docPtr, int pageIndex);
    //add an image to annot and return image object. topAsBottom default is true. because pdf use left-bottom.
    private static native long nAddImage(long docPtr, int pageIndex, long annoPtr, Bitmap bitmap, float left, float top, int width, int height, boolean topAsBottom);

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
}
