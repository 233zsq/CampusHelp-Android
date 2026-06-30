package com.campus.help.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.campus.help.R;

/**
 * 信用分仪表盘（亮点：信用分可视化）。
 * 纯 Canvas 绘制，无第三方依赖。分数 0~1000，按区间着色（低/中/高）。
 * 用法：creditGaugeView.setScore(820);
 */
public class CreditGaugeView extends View {

    private static final int MAX_SCORE = 1000;
    private static final float START_ANGLE = 135f;
    private static final float SWEEP_ANGLE = 270f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int score;
    private int highColor;
    private int midColor;
    private int lowColor;
    private int trackColor;

    public CreditGaugeView(Context context) {
        this(context, null);
    }

    public CreditGaugeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreditGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        highColor = getResources().getColor(R.color.credit_high, null);
        midColor = getResources().getColor(R.color.credit_mid, null);
        lowColor = getResources().getColor(R.color.credit_low, null);
        trackColor = getResources().getColor(R.color.divider, null);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(MAX_SCORE, score));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (size == 0) {
            size = dp(220);
        }
        setMeasuredDimension(size, (int) (size * 0.75f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float stroke = dp(16);
        trackPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);
        float inset = stroke / 2f;
        arcRect.set(inset, inset, w - inset, w - inset);
        textPaint.setTextSize(dp(36));
        labelPaint.setTextSize(dp(13));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 底弧
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, trackPaint);
        // 进度弧
        float ratio = score / (float) MAX_SCORE;
        progressPaint.setColor(score >= 700 ? highColor : score >= 400 ? midColor : lowColor);
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE * ratio, false, progressPaint);
        // 中心文字
        float cx = getWidth() / 2f;
        float cy = getWidth() / 2f;
        canvas.drawText(String.valueOf(score), cx, cy, textPaint);
        canvas.drawText("信用分", cx, cy + dp(22), labelPaint);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
