package com.github.barteksc.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.heaven7.core.util.Logger;

/**
 * the sticker view
 * . should match_parent, match_parent.
 */
public class StickerView extends View {

    private static final String TAG = "StickerView";
    private static final int GROWXY = 10;
    //drag directions
    private static final int DRAG_DIRECTION_LEFT_TOP     = 1;
    private static final int DRAG_DIRECTION_LEFT_BOTTOM  = 2;
    private static final int DRAG_DIRECTION_RIGHT_BOTTOM = 3;
    private static final int DRAG_DIRECTION_RIGHT_TOP    = 4;

    private final Params mParams = new Params();
    private final Rect mRect = new Rect();
    private final RectF mRectF = new RectF();

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTextArea = new RectF();

    private final GestureDetectorCompat mGestureDetector;

    private PathEffect mEffect;
    private Bitmap mSticker;
    private OnClickTextListener mOnClickTextListener;


    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StickerView);
        try {
            mParams.init(ta);
        }finally {
            ta.recycle();
        }
        mGestureDetector = new GestureDetectorCompat(context, new Gesture0());

        reset();
    }

    public void reset(){
        mEffect = new DashPathEffect(new float[]{mParams.linePathInterval, mParams.linePathInterval}, mParams.linePathPhase);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                adjustMargin();
                return true;
            }
        });
    }

    public void setOnClickTextListener(OnClickTextListener onClickTextListener) {
        this.mOnClickTextListener = onClickTextListener;
    }

    public int getMarginStart() {
        return mParams.marginStart;
    }
    public void setMarginStart(int mMarginStart) {
        mParams.marginStart = mMarginStart;
        invalidate();
    }
    public int getMarginTop() {
        return mParams.marginTop;
    }
    public void setMarginTop(int mMarginTop) {
        mParams.marginTop = mMarginTop;
        invalidate();
    }

    public void setSticker(int drawableId){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        if(bitmap == null){
            throw new IllegalArgumentException();
        }
        setSticker(bitmap);
    }
    public void setSticker(Bitmap bitmap){
        mSticker = bitmap;
        mParams.setStickerWidthHeight(bitmap);
        //zoom-eq. we need reset sticker width and height.
        fitZoomEqual(false);
        invalidate();
    }
    private void fitZoomEqual(boolean minOrMax){
        if(mParams.proportionalZoom && mParams.stickerWidth > 0 && mParams.stickerHeight > 0){
            float scaleX = mParams.stickerWidth * 1f / mSticker.getWidth();
            float scaleY = mParams.stickerHeight * 1f / mSticker.getHeight();
            float scale = minOrMax ? Math.min(scaleX, scaleY) : Math.max(scaleX, scaleY);
            mParams.stickerWidth = (int) (mSticker.getWidth() * scale);
            mParams.stickerHeight = (int) (mSticker.getHeight() * scale);
        }
    }
    private void onTouchRelease() {
        Logger.d(TAG, "onTouchRelease");
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);
        if(event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            onTouchRelease();
        }
        return result || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mSticker == null){
            return;
        }
        int paddingStart = getPaddingStart();
        int paddingTop = getPaddingTop();
        //int paddingEnd = getPaddingEnd();
        //int paddingBottom = getPaddingBottom();
        final int stickerWidth = mParams.stickerWidth <=0 ? mSticker.getWidth() : mParams.stickerWidth;
        final int stickerHeight = mParams.stickerHeight <=0 ? mSticker.getHeight() : mParams.stickerHeight;

        canvas.save();
        canvas.translate(paddingStart + mParams.marginStart, paddingTop + mParams.marginTop);
        //draw sticker
        mRect.set(0, 0, mSticker.getWidth(), mSticker.getHeight());
        mRectF.set(0, 0, stickerWidth, stickerHeight);
        canvas.drawBitmap(mSticker, mRect, mRectF, null);
        //line range
        mLinePaint.setPathEffect(mEffect);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setColor(mParams.lineColor);
        canvas.drawLine(0, 0, stickerWidth, 0, mLinePaint);
        canvas.drawLine(stickerWidth, 0, stickerWidth, stickerHeight, mLinePaint);
        canvas.drawLine(0, stickerHeight, stickerWidth, stickerHeight, mLinePaint);
        canvas.drawLine(0, 0, 0 ,stickerHeight, mLinePaint);
        //four dot
        //(0, 0), (stickerWidth, 0), (stickerWidth, stickerHeight), (0, stickerHeight)
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setPathEffect(null);
        mLinePaint.setColor(mParams.dotColor);
        canvas.drawCircle(0, 0, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(stickerWidth, 0, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(stickerWidth, stickerHeight, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(0, stickerHeight, mParams.dotRadius, mLinePaint);
        //---------- texts' --------------
        if(mParams.textEnabled){
            //text prepare
            measureText0();
            final int textWidth = mRect.width();
            final int textHeight = mRect.height();
            int left = stickerWidth + mParams.textMarginStart;
            int top = (stickerHeight - textHeight - mParams.textPaddingTop - mParams.textPaddingBottom) / 2;
            int right = left + textWidth + mParams.textPaddingStart + mParams.textPaddingEnd;
            int bottom = top + textHeight + mParams.textPaddingTop + mParams.textPaddingBottom;
            //text bg
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setColor(mParams.textBgColor);
            mRectF.set(left, top, right, bottom);
            mTextArea.set(mRectF);
            canvas.drawRoundRect(mRectF, mParams.textBgRoundSize, mParams.textBgRoundSize, mTextPaint);
            //text
            mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint.setColor(mParams.textColor);

            mRect.set(left + mParams.textPaddingStart,
                    top + mParams.textPaddingTop,
                    left + mParams.textPaddingStart + textWidth,
                    top + mParams.textPaddingTop + textHeight);
            DrawingUtils.computeTextDrawingCoordinate(mParams.text, mTextPaint, mRect, mRectF);
            canvas.drawText(mParams.text, mRectF.left, mRectF.top - mTextPaint.ascent(), mTextPaint);
        }

        canvas.restore();
    }

    /**
     * indicate the target x, y is in target rect or not .you should note the growX and growY.
     *
     * @param target                  the target rect
     * @param x                       the x position
     * @param y                       the y position
     * @param growX                   the growX area with left and right.
     * @param growY                   the growY area with top and bottom.
     * @return true if contains in rect.
     */
    private boolean containsInRect(RectF target, float x, float y, int growX, int growY) {
        mRectF.set(target);
        mRectF.offset(getPaddingStart() + mParams.marginStart, getPaddingTop() + mParams.marginTop);
        mRectF.set(mRectF.left - growX,
                mRectF.top - growY,
                mRectF.right + growX,
                mRectF.bottom + growY);
        return mRectF.contains(x, y);
    }
    private int getDragDirection(MotionEvent e){
        if(containsXY(0, 0, e)){
            return DRAG_DIRECTION_LEFT_TOP;
        }
        final int stickerWidth = mParams.stickerWidth <=0 ? mSticker.getWidth() : mParams.stickerWidth;
        final int stickerHeight = mParams.stickerHeight <=0 ? mSticker.getHeight() : mParams.stickerHeight;
        if(containsXY(stickerWidth, 0, e)){
            return DRAG_DIRECTION_RIGHT_TOP;
        }
        if(containsXY(stickerWidth, stickerHeight, e)){
            return DRAG_DIRECTION_RIGHT_BOTTOM;
        }
        if(containsXY(0, stickerHeight, e)){
            return DRAG_DIRECTION_LEFT_BOTTOM;
        }
        return -1;
    }
    private boolean containsXY(int expectX, int expectY, MotionEvent e){
        mRectF.set(expectX - mParams.dotRadius,
                expectY - mParams.dotRadius,
                expectX + mParams.dotRadius,
                expectY + mParams.dotRadius
        );
        return containsInRect(mRectF, e.getX(), e.getY(), GROWXY * 2, GROWXY * 2);
    }
    private void measureText0(){
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextSize(mParams.textSize);
        mTextPaint.getTextBounds(mParams.text, 0, mParams.text.length(), mRect);
    }
    private int getContentWidth(){
        final int stickerWidth = mParams.stickerWidth <=0 ? mSticker.getWidth() : mParams.stickerWidth;
        if(!mParams.textEnabled){
            return stickerWidth;
        }
        measureText0();
        final int textWidth = mRect.width();
        return stickerWidth + mParams.textMarginStart + textWidth + mParams.textPaddingStart + mParams.textPaddingEnd;
    }
    private int getContentHeight(){
        return  mParams.stickerHeight <=0 ? mSticker.getHeight() : mParams.stickerHeight;
    }
    private void adjustMargin(){
        if(mParams.marginStart < 0){
            mParams.marginStart = getWidth() - getContentWidth() - getPaddingStart()
                    - getPaddingEnd() - Math.abs(mParams.marginStart);
        }
        if(mParams.marginTop < 0){
            mParams.marginTop = getHeight() - getContentHeight() - getPaddingTop()
                    - getPaddingBottom() - Math.abs(mParams.marginTop);
        }
    }
    private boolean reachScaleBound(){
        float sw = mParams.stickerWidth * 1f / mParams.rawStickerWidth;
        float sh = mParams.stickerHeight * 1f / mParams.rawStickerHeight;
        if(sw < mParams.minScale || sh < mParams.minScale){
            return true;
        }
        if(sw > mParams.maxScale || sh > mParams.maxScale){
            return true;
        }
        return false;
    }

    private class Gesture0 implements GestureDetector.OnGestureListener{
        private int mDragDirection;
        private int mTmpStickerWidth;
        private int mTmpStickerHeight;

        private int mTmpMarginTop;
        private int mTmpMarginStart;

        private void moveInternal(int dx, int dy){
            mParams.marginStart = mTmpMarginStart + dx;
            mParams.marginTop = mTmpMarginTop + dy;
            invalidate();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mDragDirection = getDragDirection(e);
            if(mDragDirection > 0){
                mTmpStickerWidth = mParams.stickerWidth > 0 ? mParams.stickerWidth : mSticker.getWidth();
                mTmpStickerHeight = mParams.stickerHeight > 0 ? mParams.stickerHeight : mSticker.getHeight();
            }
            mTmpMarginStart = mParams.marginStart;
            mTmpMarginTop = mParams.marginTop;
            return true;
        }
        @Override
        public void onShowPress(MotionEvent e) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(mParams.textEnabled && containsInRect(mTextArea, e.getX(), e.getY(), GROWXY, GROWXY)){
                if(mOnClickTextListener != null){
                    mOnClickTextListener.onClickTextArea(StickerView.this);
                }
                return true;
            }
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx = (int) (e2.getX() - e1.getX());
            int dy = (int) (e2.getY() - e1.getY());
            final int oldWidth = mParams.stickerWidth;
            final int oldHeight = mParams.stickerHeight;

            switch (mDragDirection){
                case DRAG_DIRECTION_LEFT_BOTTOM:{
                    int oldRight = mParams.marginStart + getPaddingStart() + mParams.stickerWidth;
                    //w, h, mx ---  fix- right-top
                    mParams.stickerWidth = mTmpStickerWidth - dx;
                    mParams.stickerHeight = mTmpStickerHeight + dy;
                    //for better performance
                    fitZoomEqual(true);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }

                    int newRight = mTmpMarginStart + dx + getPaddingStart() + mParams.stickerWidth;
                    if(newRight != oldRight){
                        dx -= newRight - oldRight;
                    }
                    moveInternal(dx, 0);
                }break;

                case DRAG_DIRECTION_LEFT_TOP:{
                    int oldRight = mParams.marginStart + getPaddingStart() + mParams.stickerWidth;
                    int oldBottom = mParams.marginTop + getPaddingTop() + mParams.stickerHeight;
                    //w, h. mx, my ------ fix=right-bottom
                    mParams.stickerWidth = mTmpStickerWidth - dx;
                    mParams.stickerHeight = mTmpStickerHeight - dy;
                    fitZoomEqual(false);
                    //check min scale
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }

                    int newRight = mTmpMarginStart + dx + getPaddingStart() + mParams.stickerWidth;
                    int newBottom = mTmpMarginTop + dy + getPaddingTop() + mParams.stickerHeight;
                    if(newRight != oldRight){
                        dx -= newRight - oldRight;
                    }
                    if(newBottom != oldBottom){
                        dy -= newBottom - oldBottom;
                    }
                    moveInternal(dx, dy);
                }break;

                case DRAG_DIRECTION_RIGHT_BOTTOM:{
                    //w,h
                    mParams.stickerWidth = mTmpStickerWidth + dx;
                    mParams.stickerHeight = mTmpStickerHeight + dy;
                    fitZoomEqual(false);
                    //check min scale
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }
                    invalidate();
                }break;

                case DRAG_DIRECTION_RIGHT_TOP:{
                    int oldBottom = mParams.marginTop + getPaddingTop() + mParams.stickerHeight;
                    //w, h, my ---- fix= left-bottom
                    mParams.stickerWidth = mTmpStickerWidth + dx;
                    mParams.stickerHeight = mTmpStickerHeight - dy;
                    fitZoomEqual(true);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }
                    int newBottom = mTmpMarginTop + dy + getPaddingTop() + mParams.stickerHeight;
                    if(newBottom != oldBottom){
                        dy -= newBottom - oldBottom;
                    }
                    moveInternal(0, dy);
                }break;

                default:
                    //not drag
                    moveInternal(dx, dy);
            }
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

    public static class Params{
        int rawStickerWidth;
        int rawStickerHeight;
        int stickerWidth;
        int stickerHeight;
        float stickerScaleRatio;
        //path effect
        int lineColor;
        float linePathInterval;
        float linePathPhase;
        //dot
        float dotRadius;
        int dotColor;
        //text area
        int textBgColor;
        int textBgRoundSize;
        int textColor;
        float textSize;
        int textMarginStart;
        int textPaddingStart;
        int textPaddingTop;
        int textPaddingEnd;
        int textPaddingBottom;
        String text;
        boolean textEnabled;

        boolean proportionalZoom;

        //content margin top and bottom
        int marginStart = 0;
        int marginTop = 0 ;
        float minScale = 0.5f;
        float maxScale = 1000000;

        private Params() { }

        public void init(TypedArray ta){
            rawStickerWidth = stickerWidth = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_init_width, 0);
            rawStickerHeight = stickerHeight = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_init_height, 0);
            stickerScaleRatio = ta.getFloat(R.styleable.StickerView_stv_sticker_init_scale_ratio, 0);

            lineColor = ta.getColor(R.styleable.StickerView_stv_line_color, Color.BLACK);
            linePathInterval = ta.getFloat(R.styleable.StickerView_stv_line_pe_interval, 0);
            linePathPhase = ta.getFloat(R.styleable.StickerView_stv_line_pe_phase, 0);

            dotRadius = ta.getDimensionPixelSize(R.styleable.StickerView_stv_dotRadius, 0);
            dotColor = ta.getColor(R.styleable.StickerView_stv_dotColor, Color.BLACK);

            text = ta.getString(R.styleable.StickerView_stv_text);
            textEnabled = ta.getBoolean(R.styleable.StickerView_stv_text_enable, true);
            textBgColor = ta.getColor(R.styleable.StickerView_stv_text_bg_color, Color.BLACK);
            textBgRoundSize = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_bg_round, 20);

            textColor = ta.getColor(R.styleable.StickerView_stv_text_color, Color.WHITE);
            textSize = ta.getDimension(R.styleable.StickerView_stv_text_size, 15);
            textMarginStart = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_marginStart, 30);

            textPaddingStart = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_start, 0);
            textPaddingTop = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_top, 0);
            textPaddingEnd = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_end, 0);
            textPaddingBottom = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_bottom, 0);

            proportionalZoom = ta.getBoolean(R.styleable.StickerView_stv_proportional_zoom, true);
            marginStart = ta.getDimensionPixelSize(R.styleable.StickerView_stv_content_margin_start, 0);
            marginTop = ta.getDimensionPixelSize(R.styleable.StickerView_stv_content_margin_top, 0);
            minScale = ta.getFloat(R.styleable.StickerView_stv_min_scale, minScale);
            maxScale = ta.getFloat(R.styleable.StickerView_stv_max_scale, maxScale);
        }
        void setStickerWidth0(int width){
            rawStickerWidth = stickerWidth = width;
        }
        void setStickerHeight0(int height){
            rawStickerHeight = stickerHeight = height;
        }
        void setStickerWidthHeight(Bitmap sticker){
            if(stickerScaleRatio > 0){
                setStickerWidth0((int) (sticker.getWidth() * stickerScaleRatio));
                setStickerHeight0((int) (sticker.getHeight() * stickerScaleRatio));
            }else {
                if(stickerWidth <= 0){
                    setStickerWidth0(sticker.getWidth());
                }
                if(stickerHeight <= 0){
                    setStickerHeight0(sticker.getHeight());
                }
            }
        }
    }

    public interface OnClickTextListener{
        void onClickTextArea(StickerView view);
    }

}
