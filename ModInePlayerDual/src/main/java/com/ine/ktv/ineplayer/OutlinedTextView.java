package com.ine.ktv.ineplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;

public class OutlinedTextView extends androidx.appcompat.widget.AppCompatTextView {
    /* ===========================================================
     * Constants
     * =========================================================== */
    private float outlineProportion = 0.1f;
    private Typeface typeface = null;
    /* ===========================================================
     * Members
     * =========================================================== */
    private final Paint mStrokePaint = new Paint();
    private final Rect mTextBounds = new Rect();
    private int textColor;
    float  StrokeWidthOutline;
    private int outlineColor = Color.WHITE;
    private boolean defaultBaseLine = true;
    private float textSize;
    /* ===========================================================
     * Constructors
     * =========================================================== */
    public OutlinedTextView(Context context) {
        super(context);
        this.setupPaint();
        typeface = super.getTypeface();
        setTextSize(super.getTextSize());
    }
    public OutlinedTextView(Context context, boolean defaultBaseLine) {
        this(context);
        this.defaultBaseLine = defaultBaseLine;
    }
    public OutlinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setupPaint();
        this.setupAttributes(context, attrs);
        typeface = super.getTypeface();
        setTextSize(super.getTextSize());
    }

    public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setupPaint();
        this.setupAttributes(context, attrs);
        typeface = super.getTypeface();
        setTextSize(super.getTextSize());
    }
    public void setOutlineProportion(float outlineProportion){
        this.outlineProportion = outlineProportion;
        setTextSize(getTextSize());
    }
    public void setOutlineTextColor(@ColorInt int color) {
        outlineColor = color;
    }
    public @ColorInt int GetOutlineTextColor() {
        return outlineColor;
    }

    public void setDefaultBaseLine(boolean defaultBaseLine) {
        this.defaultBaseLine = defaultBaseLine;
    }
    /* ===========================================================
     * Overrides
     * =========================================================== */
    @Override
    public void setTextColor(int color){
        textColor = color;
        super.setTextColor(color);
    }
    @Override
    public void setTypeface(Typeface typeface){
        this.typeface = typeface;
        super.setTypeface(typeface);
    }
    @Override
    public void setTextSize(float size){
        int outlineSize = (int)(size * outlineProportion);
        if(outlineSize - size < 2)
            outlineSize = (int)size + 2;
        super.setTextSize(size);
        textSize = super.getTextSize();
        StrokeWidthOutline = textSize * outlineProportion;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        // Get the text to print
        final String text = super.getText().toString();

        // setup stroke
        mStrokePaint.setColor(outlineColor);
        mStrokePaint.setStrokeWidth(StrokeWidthOutline);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setTextSize(textSize);
        mStrokePaint.setFlags(super.getPaintFlags());
        mStrokePaint.setTypeface(typeface);
        // Figure out the drawing coordinates
        super.getPaint().getTextBounds(text, 0, text.length(), mTextBounds);
        int x = (int)((float)getWidth() * 0.5f);
        int y = defaultBaseLine? getBaseline() : (int)textSize;
//        // draw everything
        canvas.drawText(text, x, y, mStrokePaint);

        mStrokePaint.setColor(textColor);
        mStrokePaint.setStyle(Paint.Style.FILL);
        mStrokePaint.setStrokeWidth(1);

        canvas.drawText(text, x, y, mStrokePaint);
//        canvas.drawText(text,
//                getWidth() * 0.5f, (getHeight() + mTextBounds.height()) * 0.5f,
//                mStrokePaint);
        //super.onDraw(canvas);
    }

    /* ===========================================================
     * Private/Protected Methods
     * =========================================================== */
    private final void setupPaint() {
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setTextAlign(Paint.Align.CENTER);

    }

    private final void setupAttributes(Context context, AttributeSet attrs) {
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.OutlinedTextView);
        outlineColor = array.getColor(
                R.styleable.OutlinedTextView_outlineColor, Color.BLUE);
        array.recycle();

        // Force this text label to be centered
        super.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}