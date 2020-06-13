package com.github.barteksc.sample;

import android.graphics.PointF;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.PdfFile;
import com.heaven7.core.util.WeakHandler;
import com.shockwave.pdfium.util.SizeF;

public class SimpleTouchListener implements View.OnTouchListener {

    public static final int MSG_NORMAL = 0 ;
    public static final int MSG_PRINT  = 1 ;

    private final PDFView mPdfView;
    private final View mView;
    private final GestureDetectorCompat gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;

    private boolean scrolling = false;

    private final Handler0 mHandler = new Handler0(this);

    public SimpleTouchListener(PDFView mPdfView, View view) {
        this.mPdfView = mPdfView;
        this.mView = view;
        this.gestureDetector = new GestureDetectorCompat(view.getContext(), new Gesture0());
        this.scaleGestureDetector = new ScaleGestureDetector(view.getContext(), new ScaleGesture0());
    }

    public static void attach(PDFView mPdfView, View view){
        SimpleTouchListener listener = new SimpleTouchListener(mPdfView, view);
        view.setOnTouchListener(listener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        boolean retVal = scaleGestureDetector.onTouchEvent(event);
        retVal = gestureDetector.onTouchEvent(event) || retVal;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (scrolling) {
                scrolling = false;
                onScrollEnd(event);
            }
        }
        return retVal;
    }
    private void onScrollEnd(MotionEvent event) {
        mHandler.obtainMessage(MSG_PRINT).sendToTarget();
    }

    private class ScaleGesture0 implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            float dLeft = (focusX - lp.leftMargin);
           // float dRight = mView.getWidth() - dLeft;
            float dTop = focusY - lp.topMargin;
           // float dBottom = mView.getHeight() - dTop;
            //compute new margin left and top
            float val = dLeft * scaleFactor;
            int dmarginLeft = (int) (dLeft - val);

            //compute new margin left and top
            val = dTop * scaleFactor;
            int dmarginTop = (int) (dTop - val);

            Info info = new Info(dmarginLeft, dmarginTop, (int) (mView.getWidth() * scaleFactor), (int) (mView.getHeight() * scaleFactor));
            mHandler.obtainMessage(0, info).sendToTarget();
            return true;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mHandler.obtainMessage(MSG_PRINT).sendToTarget();
        }
    }

    private class Gesture0 implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scrolling = true;
            Info info = Info.obtain((e2.getX() - e1.getX()), (e2.getY() - e1.getY()));
            mHandler.obtainMessage(0, info).sendToTarget();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private static class Handler0 extends WeakHandler<SimpleTouchListener> {

        public Handler0(SimpleTouchListener l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            SimpleTouchListener listener = get();
            switch (msg.what){
                case MSG_NORMAL:{
                    Info info = (Info) msg.obj;
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) listener.mView.getLayoutParams();
                    lp.leftMargin += info.deltaLeftMargin;
                    lp.topMargin += info.deltaTopMargin;

                    if (info.isWHChanged()) {
                        lp.width += info.deltaWidth;
                        lp.height += info.deltaHeight;
                    }
                    listener.mView.setLayoutParams(lp);
                }
                    break;

                case MSG_PRINT:
                    final PDFView mPdfView = listener.mPdfView;
                    PdfFile pdfFile = mPdfView.getPdfFile();

                    float mappedX = -mPdfView.getCurrentXOffset();
                    float mappedY = -mPdfView.getCurrentYOffset();
                    int page = pdfFile.getPageAtOffset(mPdfView.isSwipeVertical() ? mappedY : mappedX, mPdfView.getZoom());
                    SizeF pageSize = pdfFile.getScaledPageSize(page, mPdfView.getZoom());

                    int pageX, pageY;
                    if (mPdfView.isSwipeVertical()) {
                        pageX = (int) pdfFile.getSecondaryPageOffset(mPdfView.getCurrentPage(), mPdfView.getZoom());
                        pageY = (int) pdfFile.getPageOffset(page, mPdfView.getZoom());
                    } else {
                        pageY = (int) pdfFile.getSecondaryPageOffset(page, mPdfView.getZoom());
                        pageX = (int) pdfFile.getPageOffset(page, mPdfView.getZoom());
                    }

                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) listener.mView.getLayoutParams();
                    PointF p = pdfFile.mapDeviceCoordsToPage(mPdfView.getCurrentPage(), pageX, pageY,
                            (int) pageSize.getWidth(), (int) pageSize.getHeight(), 0, lp.leftMargin, lp.topMargin);
                    System.out.println("image pos >>> " + p);
                    break;
            }
        }
    }

    public static class Info {
        int deltaLeftMargin;
        int deltaTopMargin;
        int deltaWidth;
        int deltaHeight;

        public Info(int deltaLeftMargin, int deltaTopMargin) {
            this.deltaLeftMargin = deltaLeftMargin;
            this.deltaTopMargin = deltaTopMargin;
        }

        public Info(int deltaLeftMargin, int deltaTopMargin, int deltaWidth, int deltaHeight) {
            this.deltaLeftMargin = deltaLeftMargin;
            this.deltaTopMargin = deltaTopMargin;
            this.deltaWidth = deltaWidth;
            this.deltaHeight = deltaHeight;
        }

        public boolean isWHChanged() {
            return deltaHeight != 0 || deltaWidth != 0;
        }

        public static Info obtain(float dlm, float dtm) {
            return new Info((int) dlm, (int) dtm);
        }
    }
}
