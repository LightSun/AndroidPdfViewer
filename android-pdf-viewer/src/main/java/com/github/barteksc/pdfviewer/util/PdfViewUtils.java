package com.github.barteksc.pdfviewer.util;

import android.graphics.PointF;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.PdfFile;
import com.shockwave.pdfium.util.SizeF;

/**
 * the pdf view utils.
 * @author heaven7
 * @since 1.0.0
 */
public final class PdfViewUtils {

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

  /*  public static void convert(final PDFView mPdfView,final int x, final int y){
        mPdfView.post(new Runnable() {
            @Override
            public void run() {
                PointF p = convertScreenPointToPdfPagePoint(mPdfView, x, y);
                String msg = String.format(Locale.getDefault(), "before: x = %d, y = %d. after: x = %f, y = %f",
                        x, y, p.x, p.y);
                System.out.println("mapDeviceCoordsToPage >>> " + msg);
            }
        });

    }*/
}
