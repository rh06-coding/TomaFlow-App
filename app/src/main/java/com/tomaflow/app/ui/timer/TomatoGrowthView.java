package com.tomaflow.app.ui.timer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tomaflow.app.R;

/**
 * TomatoGrowthView — Custom View hiển thị cây cà chua lớn dần theo tiến trình timer.
 *
 * Giai đoạn:
 *   Stage 0 (0–20%)   → Hạt giống   (ic_toma_seed)
 *   Stage 1 (20–40%)  → Mầm nhú     (ic_toma_sprout)
 *   Stage 2 (40–60%)  → Cây có lá   (ic_toma_plant)
 *   Stage 3 (60–80%)  → Ra hoa      (ic_toma_flower)
 *   Stage 4 (80–100%) → Quả chín 🍅  (ic_toma_ripe)
 *   Stage -1          → Cây héo 💀   (ic_toma_dead) — khi bỏ giữa chừng
 *
 * Dùng 2 ImageView chồng lên nhau để tạo crossfade mượt mà khi đổi stage.
 */
public class TomatoGrowthView extends FrameLayout {

    // ── Stage drawables ────────────────────────────────────────────────────────
    private static final int[] STAGE_DRAWABLES = {
            R.drawable.ic_toma_seed,
            R.drawable.ic_toma_sprout,
            R.drawable.ic_toma_plant,
            R.drawable.ic_toma_flower,
            R.drawable.ic_toma_ripe,
    };

    private static final float[] STAGE_THRESHOLDS = {0f, 0.20f, 0.40f, 0.60f, 0.80f};

    private static final long CROSSFADE_MS = 500L;
    private static final long BOUNCE_MS    = 600L;

    // ── Views ──────────────────────────────────────────────────────────────────
    private ImageView mImgBack;   // Layer sau (đang ẩn)
    private ImageView mImgFront;  // Layer trước (đang hiện)

    private int  mCurrentStage = -2; // -2 = chưa khởi tạo
    private boolean mIsDead    = false;

    public TomatoGrowthView(@NonNull Context context) {
        super(context);
        init();
    }

    public TomatoGrowthView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TomatoGrowthView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Layer sau
        mImgBack = new ImageView(getContext());
        mImgBack.setAlpha(0f);
        addView(mImgBack, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Layer trước
        mImgFront = new ImageView(getContext());
        mImgFront.setAlpha(0f);
        addView(mImgFront, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Cập nhật giai đoạn cây theo progress [0, 1].
     * Chỉ trigger animation khi stage thực sự thay đổi.
     */
    public void setProgress(float progress) {
        if (mIsDead) return;

        int targetStage = stageForProgress(progress);
        if (targetStage == mCurrentStage) return;

        @DrawableRes int drawable = STAGE_DRAWABLES[targetStage];
        crossfadeTo(drawable, targetStage > mCurrentStage);
        mCurrentStage = targetStage;
    }

    /**
     * Hiện cây héo với animation lắc lư rồi fade out nhẹ.
     * Gọi khi người dùng Skip/Reset khi timer đang chạy.
     */
    public void showDead() {
        if (mIsDead) return;
        mIsDead = true;
        mCurrentStage = -1;

        mImgBack.setImageResource(R.drawable.ic_toma_dead);
        mImgBack.setAlpha(0f);

        // Crossfade sang hình chết
        mImgBack.animate()
                .alpha(1f)
                .setDuration(CROSSFADE_MS)
                .start();
        mImgFront.animate()
                .alpha(0f)
                .setDuration(CROSSFADE_MS)
                .withEndAction(this::swapLayers)
                .start();

        // Lắc nhẹ sau khi hiện
        postDelayed(() -> {
            ObjectAnimator shake = ObjectAnimator.ofFloat(
                    mImgFront, "rotation", 0f, -8f, 8f, -5f, 5f, 0f);
            shake.setDuration(600);
            shake.start();
        }, CROSSFADE_MS + 50);
    }

    /**
     * Reset về trạng thái ẩn (khi timer idle hoặc sau khi hoàn thành).
     */
    public void reset() {
        mIsDead = false;
        mCurrentStage = -2;
        mImgFront.clearAnimation();
        mImgBack.clearAnimation();
        mImgFront.setAlpha(0f);
        mImgBack.setAlpha(0f);
    }

    /**
     * Animation "nảy lên" khi hoàn thành 1 Pomodoro (quả chín xuất hiện với bounce).
     */
    public void celebrateComplete() {
        // Bounce scale animation
        AnimatorSet bounce = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mImgFront, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mImgFront, "scaleY", 1f, 1.3f, 1f);
        scaleX.setInterpolator(new OvershootInterpolator(2f));
        scaleY.setInterpolator(new OvershootInterpolator(2f));
        bounce.playTogether(scaleX, scaleY);
        bounce.setDuration(BOUNCE_MS);
        bounce.start();
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private int stageForProgress(float progress) {
        // Tìm stage cao nhất mà progress >= threshold
        int stage = 0;
        for (int i = STAGE_THRESHOLDS.length - 1; i >= 0; i--) {
            if (progress >= STAGE_THRESHOLDS[i]) {
                stage = i;
                break;
            }
        }
        return stage;
    }

    /**
     * Crossfade mImgFront → mImgBack (layer sau hiện lên, layer trước ẩn đi).
     * @param isGrowthUp true nếu đang lớn lên (scale bounce nhẹ), false nếu không.
     */
    private void crossfadeTo(@DrawableRes int drawable, boolean isGrowthUp) {
        mImgBack.setImageResource(drawable);
        mImgBack.setAlpha(0f);

        if (isGrowthUp) {
            // Scale từ nhỏ đến bình thường khi lớn lên
            mImgBack.setScaleX(0.8f);
            mImgBack.setScaleY(0.8f);
            mImgBack.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(CROSSFADE_MS)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
        } else {
            mImgBack.animate()
                    .alpha(1f)
                    .setDuration(CROSSFADE_MS)
                    .start();
        }

        mImgFront.animate()
                .alpha(0f)
                .setDuration(CROSSFADE_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::swapLayers)
                .start();
    }

    /** Sau crossfade: đổi vai trò front/back để sẵn sàng cho lần tiếp theo. */
    private void swapLayers() {
        ImageView tmp = mImgFront;
        mImgFront = mImgBack;
        mImgBack  = tmp;
        // Đảm bảo front luôn vẽ trên back
        mImgFront.bringToFront();
    }
}
