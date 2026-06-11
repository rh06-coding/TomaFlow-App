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
 * Call {@link #setProgress(float)} with a value in [0, 1] to update the arc instantly.
 * Call {@link #animateTo(float, long)} to animate smoothly to a target progress.
 */
public class TimerView extends View {


    private static final int   DEFAULT_TRACK_COLOR    = 0xFFFFDAD6; // #FFDAD6
    private static final int   DEFAULT_PROGRESS_COLOR = 0xFFAF101A; // #AF101A
    private static final float DEFAULT_STROKE_WIDTH_DP = 12f;


    private final Paint mTrackPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mArcBounds     = new RectF();

    /** Progress in [0, 1]. 0 = empty, 1 = full circle. */
    private float mProgress = 0f;

    /** Animator cho smooth progress — cancel + restart mỗi khi animateTo() được gọi. */
    private android.animation.ValueAnimator mProgressAnimator;



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



    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        float halfStroke = mTrackPaint.getStrokeWidth() / 2f;
        mArcBounds.set(halfStroke, halfStroke, w - halfStroke, h - halfStroke);
    }



    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(mArcBounds, -90f, 360f, false, mTrackPaint);

        // Progress arc (clockwise from top)
        if (mProgress > 0f) {
            float sweepAngle = 360f * mProgress;
            canvas.drawArc(mArcBounds, -90f, sweepAngle, false, mProgressPaint);
        }
    }



    /**
     * Sets the progress instantly without animation.
     * Dùng khi reset timer (progress = 0) hoặc khởi tạo ban đầu.
     *
     * @param progress A value in [0.0, 1.0] where 0 = no progress and 1 = complete.
     */
    public void setProgress(float progress) {
        cancelAnimator();
        mProgress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /**
     * Animate progress từ giá trị hiện tại đến {@code targetProgress} trong {@code durationMs} ms.
     * Dùng LinearInterpolator để khớp chính xác với tốc độ đếm ngược của timer.
     * Mỗi lần gọi sẽ cancel animator cũ và tạo mới từ vị trí hiện tại.
     *
     * @param targetProgress Giá trị đích trong [0.0, 1.0].
     * @param durationMs     Thời lượng animation tính bằng milliseconds.
     */
    public void animateTo(float targetProgress, long durationMs) {
        float target = Math.max(0f, Math.min(1f, targetProgress));

        cancelAnimator();

        mProgressAnimator = android.animation.ValueAnimator.ofFloat(mProgress, target);
        mProgressAnimator.setDuration(durationMs);
        mProgressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        mProgressAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mProgressAnimator.start();
    }

    /**
     * Đổi màu progress arc với animation ArgbEvaluator.
     * Dùng khi chuyển pha Work ↔ Break.
     *
     * @param fromColor  Màu bắt đầu (ARGB int).
     * @param toColor    Màu đích (ARGB int).
     * @param durationMs Thời lượng animation.
     */
    public void animateProgressColor(int fromColor, int toColor, long durationMs) {
        android.animation.ValueAnimator colorAnimator =
                android.animation.ValueAnimator.ofObject(
                        new android.animation.ArgbEvaluator(), fromColor, toColor);
        colorAnimator.setDuration(durationMs);
        colorAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animation -> {
            mProgressPaint.setColor((int) animation.getAnimatedValue());
            invalidate();
        });
        colorAnimator.start();
    }

    /** Set màu arc ngay lập tức, không animation. */
    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    /** Returns the current progress in [0, 1]. */
    public float getProgress() {
        return mProgress;
    }

    private void cancelAnimator() {
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimator();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
