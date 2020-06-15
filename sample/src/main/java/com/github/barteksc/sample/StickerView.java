package com.github.barteksc.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * the sticker view
 * . should match_parent, match_parent.
 */
public class StickerView extends View {

    private static final int GROWXY = 10;
    private final Params mParams = new Params();
    private final Rect mRect = new Rect();
    private final RectF mRectF = new RectF();
    private final DashPathEffect mEffect;

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTextArea = new RectF();

    private final GestureDetectorCompat mGestureDetector;

    private Bitmap mSticker;
    private OnClickTextListener mOnClickTextListener;
    private int mMarginStart;
    private int mMarginTop;


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
        mEffect = new DashPathEffect(new float[]{mParams.pe_interval, mParams.pe_interval}, mParams.pe_phase);

        mGestureDetector = new GestureDetectorCompat(context, new Gesture0());
    }

    public void setOnClickTextListener(OnClickTextListener onClickTextListener) {
        this.mOnClickTextListener = onClickTextListener;
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
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mGestureDetector.onTouchEvent(event)){
            return true;
        }
        return super.onTouchEvent(event);
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
        canvas.translate(paddingStart + mMarginStart, paddingTop + mMarginTop);
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
        //text prepare
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextSize(mParams.textSize);
        mTextPaint.getTextBounds(mParams.text, 0, mParams.text.length(), mRect);
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
        mRectF.offset(getPaddingStart() + mMarginStart, getPaddingTop() + mMarginTop);
        mRectF.set(mRectF.left - growX,
                mRectF.top - growY,
                mRectF.right + growX,
                mRectF.bottom + growY);
        return mRectF.contains(x, y);
    }

    private class Gesture0 implements GestureDetector.OnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            //TODO
            return true;
        }
        @Override
        public void onShowPress(MotionEvent e) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(containsInRect(mTextArea, e.getX(), e.getY(), GROWXY, GROWXY)){
                if(mOnClickTextListener != null){
                    mOnClickTextListener.onClickTextArea(StickerView.this);
                }
                return true;
            }
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            return false;
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
        int stickerWidth;
        int stickerHeight;
        //path effect
        int lineColor;
        float pe_interval;
        float pe_phase;
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
        String text = "旋转";

        public void init(TypedArray ta){
            stickerWidth = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_width, 0);
            stickerHeight = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_height, 0);

            lineColor = ta.getColor(R.styleable.StickerView_stv_line_color, Color.BLACK);
            pe_interval = ta.getFloat(R.styleable.StickerView_stv_pe_interval, 0);
            pe_phase = ta.getFloat(R.styleable.StickerView_stv_pe_phase, 0);

            dotRadius = ta.getDimensionPixelSize(R.styleable.StickerView_stv_dotRadius, 0);
            dotColor = ta.getColor(R.styleable.StickerView_stv_dotColor, Color.BLACK);

            textBgColor = ta.getColor(R.styleable.StickerView_stv_text_bg_color, Color.BLACK);
            textBgRoundSize = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_bg_round, 20);

            textColor = ta.getColor(R.styleable.StickerView_stv_text_color, Color.WHITE);
            textSize = ta.getDimension(R.styleable.StickerView_stv_text_size, 15);
            textMarginStart = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_marginStart, 30);

            textPaddingStart = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_start, 0);
            textPaddingTop = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_top, 0);
            textPaddingEnd = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_end, 0);
            textPaddingBottom = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_bottom, 0);
        }
    }
    public interface OnClickTextListener{
        void onClickTextArea(StickerView view);
    }
}
