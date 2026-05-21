package com.financeiramente.android.ui.metas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.financeiramente.android.R;

/**
 * Custom view that draws a circular progress ring using Canvas.
 * Shows the percentage in the center with a dynamic color:
 *  - Verde  (≥ 75%)
 *  - Azul   (≥ 25% and < 75%)
 *  - Vermelho (< 25%)
 */
public class CircularProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private float progress = 0f; // 0..100
    private float strokeWidth;

    public CircularProgressView(Context context) {
        super(context);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        strokeWidth = dp(context, 8f);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.cinza_divider));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(context, 13f));
        textPaint.setFakeBoldText(true);

        applyProgressColor(context);
    }

    /** Set progress value (0–100) and redraw. */
    public void setProgress(float value) {
        this.progress = Math.max(0f, Math.min(100f, value));
        applyProgressColor(getContext());
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    private void applyProgressColor(Context context) {
        int colorRes;
        if (progress >= 75f) {
            colorRes = R.color.verde_success;
        } else if (progress >= 25f) {
            colorRes = R.color.azul_primary;
        } else {
            colorRes = R.color.vermelho_error;
        }
        int color = ContextCompat.getColor(context, colorRes);
        progressPaint.setColor(color);
        textPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = (Math.min(getWidth(), getHeight()) - strokeWidth) / 2f;

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Background track (full circle)
        canvas.drawArc(oval, 0f, 360f, false, trackPaint);

        // Progress arc starting from top (-90°)
        float sweep = (progress / 100f) * 360f;
        if (sweep > 0f) {
            canvas.drawArc(oval, -90f, sweep, false, progressPaint);
        }

        // Percentage label in center
        String label = (int) progress + "%";
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(label, cx, textY, textPaint);
    }

    // --- helpers ---

    private static float dp(Context ctx, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                ctx.getResources().getDisplayMetrics());
    }

    private static float sp(Context ctx, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                ctx.getResources().getDisplayMetrics());
    }
}
