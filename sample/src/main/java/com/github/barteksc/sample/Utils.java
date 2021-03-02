package com.github.barteksc.sample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.mark.ImageMark;
import com.github.barteksc.pdfviewer.util.PdfViewUtils;
import com.heaven7.android.sticker.StickerView;
import com.heaven7.core.util.Logger;
import com.heaven7.core.util.Toaster;

public final class Utils {

    private static final String TAG = "Utils";

    public static Bitmap rotate(Bitmap bm, float degree) {
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (degree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        return bm1;
    }
    //test ok
    public static long addImage(PDFView view, StickerView mStickerView, Bitmap[] out){
        //locate left-bottom
        int x = mStickerView.getMarginStart() + mStickerView.getPaddingStart();
        int y = mStickerView.getMarginTop() + mStickerView.getPaddingTop() + mStickerView.getStickerHeight();
        int x_rt = x + mStickerView.getStickerWidth();
        int y_rt = mStickerView.getMarginTop() + mStickerView.getPaddingTop();
        RectF realRect = new RectF();
        realRect.set(x, y_rt, x_rt, y);

        RectF rect = view.getPageRect();
        Logger.d(TAG, "addImage", "realRect = " + realRect + " ,page rect = " + rect);
        if(!rect.contains(realRect)){
            Toaster.show(view.getContext(), "签名不能放到文档外，请重新签名！");
            return 0;
        }

        float left = realRect.left - rect.left;
        float top = realRect.top - rect.top;
        RectF srcRect = new RectF(
                left,
                top,
                left + realRect.width(),
                top + realRect.height());
        //RectF srcRect = new RectF(250, 150, 550, 550);

        RectF dstRect = PdfViewUtils.convertScreenToPdfPageRect(view, srcRect);
        System.out.println("page srcRect: " + srcRect);
        System.out.println("page dstRect: " + dstRect);
        float sx = dstRect.width() / srcRect.width();
        float sy = dstRect.height() / srcRect.height();

        Bitmap bitmap = getResultBitmap(mStickerView, (int)dstRect.width(), (int)dstRect.height());
        out[0] = bitmap;

        //sx = x / srcRect.left
        return view.getPdfFile().getPdfAnnotManager().addImage(view.getCurrentPage(),
                bitmap,
                srcRect.left * sx , srcRect.top * sy,
                (int) dstRect.width(), (int) dstRect.height(), false
        );

    }

    //test ok
    public static long addImage(PDFView view, StickerView mStickerView) {
        //locate left-bottom
        int x = mStickerView.getMarginStart() + mStickerView.getPaddingStart();
        int y = mStickerView.getMarginTop() + mStickerView.getPaddingTop() + mStickerView.getStickerHeight();
        int x_rt = x + mStickerView.getStickerWidth();
        int y_rt = mStickerView.getMarginTop() + mStickerView.getPaddingTop();
        RectF realRect = new RectF();
        realRect.set(x, y_rt, x_rt, y);

        RectF rect = view.getPageRect();
        Logger.d(TAG, "prepareData", "realRect = " + realRect + " ,page rect = " + rect);
        if(!rect.contains(realRect)){
            Toaster.show(view.getContext(), "签名不能放到文档外，请重新签名！");
            return 0;
        }

        RectF dstRectF = PdfViewUtils.convertScreenToPdfPageRect(view, realRect);
        Logger.d(TAG, "prepareData", "dstRectF = " + dstRectF);
        int rotate = (int) mStickerView.getStickerRotate() % 360;

        ImageDataHolder holder = new ImageDataHolder();
        ImageMark mark = new ImageMark.Builder()
                .setLeft(dstRectF.left)
                .setTop(dstRectF.top)
                .build();
        Bitmap bitmap = getResultBitmap(mStickerView, (int)dstRectF.width(), (int)dstRectF.height());
        if(rotate == 90 || rotate == 270){
            mark.setWidth(dstRectF.height());
            mark.setHeight(dstRectF.width());
        }else {
            mark.setWidth(dstRectF.width());
            mark.setHeight(dstRectF.height());
        }
        Logger.d(TAG, "prepareData", "result.bitmap >> w = " + bitmap.getWidth() + " ,h = " + bitmap.getHeight());
        mark.setRotate(rotate <= 0 ? rotate : 360 - rotate);
        holder.setImageMark(mark);
        holder.setBitmap(bitmap);

        return view.getPdfFile().addImage(view.getCurrentPage(),
                bitmap,
                dstRectF.left , dstRectF.top,
                (int) dstRectF.width(), (int) dstRectF.height()
        );
    }

    public static Bitmap getResultBitmap(StickerView view, int w, int h){
        final Rect mRect = new Rect();
        final RectF mRectF = new RectF();

        mRect.set(0, 0, view.getSticker().getWidth(), view.getSticker().getHeight());
        mRectF.set(0, 0, w, h);

        Bitmap bg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        canvas.drawBitmap(view.getSticker(), mRect, mRectF, null);
        return bg;
    }
}
