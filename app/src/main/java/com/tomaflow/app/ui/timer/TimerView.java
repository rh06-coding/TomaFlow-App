package com.tomaflow.app.ui.timer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tomaflow.app.R;

/**
 * TimerView — Custom circular progress arc.
 *
 * Draws two concentric arcs:
 *   1. Track arc  — full circle in the muted/background colour (#FFDAD6)
 *   2. Progress arc — sweeps clockwise from the top, coloured with the
 *                     primary brand colour (#AF101A)
 *
 * Usage in XML:
 *   <com.tomaflow.app.ui.timer.TimerView
 *       android:id="@+id/timer_view"
 *       android:layout_width="0dp"
 *       android:layout_height="0dp"
 *       app:trackColor="@color/timer_track"
 *       app:progressColor="@color/color_primary"
 *       app:strokeWidth="12dp" />
 *
 * Call {@link #setProgress(float)} with a value in [0, 1] to update the arc.
 */
public class TimerView extends View {

    // -------------------------------------------------------------------------
    // Default values (overridable via XML attrs)
    // -------------------------------------------------------------------------
    private static final int   DEFAULT_TRACK_COLOR    = 0xFFFFDAD6; // #FFDAD6
    private static final int   DEFAULT_PROGRESS_COLOR = 0xFFAF101A; // #AF101A
    private static final float DEFAULT_STROKE_WIDTH_DP = 12f;

    // -------------------------------------------------------------------------
    // Drawing state
    // -------------------------------------------------------------------------
    private final Paint mTrackPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mArcBounds     = new RectF();

    /** Progress in [0, 1]. 0 = empty, 1 = full circle. */
    private float mProgress = 0f;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public TimerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public TimerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        int trackColor    = DEFAULT_TRACK_COLOR;
        int progressColor = DEFAULT_PROGRESS_COLOR;
        float strokeWidthPx = dpToPx(DEFAULT_STROKE_WIDTH_DP);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimerView);
            try {
                trackColor    = ta.getColor(R.styleable.TimerView_trackColor,    trackColor);
                progressColor = ta.getColor(R.styleable.TimerView_progressColor, progressColor);
                strokeWidthPx = ta.getDimension(R.styleable.TimerView_strokeWidth, strokeWidthPx);
            } finally {
                ta.recycle();
            }
        }

        // Track paint
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackPaint.setColor(trackColor);
        mTrackPaint.setStrokeWidth(strokeWidthPx);
        mTrackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Progress paint
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStrokeWidth(strokeWidthPx);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    // -------------------------------------------------------------------------
    // Measurement
    // -------------------------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        float halfStroke = mTrackPaint.getStrokeWidth() / 2f;
        mArcBounds.set(halfStroke, halfStroke, w - halfStroke, h - halfStroke);
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Full track
        canvas.drawArc(mArcBounds, -90f, 360f, false, mTrackPaint);

        // Progress arc (clockwise from top)
        if (mProgress > 0f) {
            float sweepAngle = 360f * mProgress;
            canvas.drawArc(mArcBounds, -90f, sweepAngle, false, mProgressPaint);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Sets the progress of the arc.
     *
     * @param progress A value in [0.0, 1.0] where 0 = no progress and 1 = complete.
     */
    public void setProgress(float progress) {
        mProgress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /** Returns the current progress in [0, 1]. */
    public float getProgress() {
        return mProgress;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
