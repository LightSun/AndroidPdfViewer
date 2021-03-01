package com.github.barteksc.pdfviewer.util;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.PdfFile;
import com.shockwave.pdfium.util.SizeF;

/**
 * the pdf view utils.
 * @author heaven7
 * @since 10.0.0
 */
public final class PdfViewUtils {

    /**
     * add an image to pdf's target page with cors
     * @param view the pdfview
     * @param page the page
     * @param bitmap the bitmap
     * @param srcRect the screen rects in pixes. origin is pdf's left-bottom.
     * @return the image ptr
     * @since 10.0.7
     */
    public static long addImage(PDFView view, int page, Bitmap bitmap, RectF srcRect){
        //RectF srcRect = new RectF(0, 10, 40 , 60);
        RectF dstRect = PdfViewUtils.convertScreenToPdfPageRect(view, srcRect);
        //System.out.println("page srcRect: " + srcRect);
        //System.out.println("page dstRect: " + dstRect);
        float sx = dstRect.width() / srcRect.width();
        float sy = dstRect.height() / srcRect.height();
        //sx = x / srcRect.left
        return view.getPdfFile().addImage(page, bitmap, dstRect.left * sx , srcRect.top * sy,
                (int) dstRect.width(), (int) dstRect.height());
    }

    public static PointF convertScreenPointToPdfPagePoint(PDFView pdfView, float x, float y) {
        PdfFile pdfFile = pdfView.getPdfFile();
        if (pdfFile == null) {
            return null;
        }
        float mappedX = -pdfView.getCurrentXOffset() + x;
        float mappedY = -pdfView.getCurrentYOffset() + y;
        int page = pdfFile.getPageAtOffset(pdfView.isSwipeVertical() ? mappedY : mappedX, pdfView.getZoom());
        SizeF pageSize = pdfFile.getScaledPageSize(page, pdfView.getZoom());
        int pageX, pageY;
        if (pdfView.isSwipeVertical()) {
            pageX = (int) pdfFile.getSecondaryPageOffset(page, pdfView.getZoom());
            pageY = (int) pdfFile.getPageOffset(page, pdfView.getZoom());
        } else {
            pageY = (int) pdfFile.getSecondaryPageOffset(page, pdfView.getZoom());
            pageX = (int) pdfFile.getPageOffset(page, pdfView.getZoom());
        }
        return pdfFile.mapDeviceCoordsToPage(page, pageX, pageY, (int) pageSize.getWidth(),
                (int) pageSize.getHeight(), 0, (int) mappedX, (int) mappedY);
    }

    /**
     * convert screen rect to pdf's.
     * @param pdfView the pdf view
     * @param src the src rect
     * @return the rect.
     * @since 10.0.7
     */
    public static RectF convertScreenToPdfPageRect(PDFView pdfView, RectF src) {
        PointF p = PdfViewUtils.convertScreenPointToPdfPagePoint(pdfView, src.left, src.top);
        PointF p2 = PdfViewUtils.convertScreenPointToPdfPagePoint(pdfView, src.right, src.bottom);
        RectF rectF = new RectF(p.x, p.y, p2.x, p2.y);
        rectF.sort();
        return rectF;
    }
}
