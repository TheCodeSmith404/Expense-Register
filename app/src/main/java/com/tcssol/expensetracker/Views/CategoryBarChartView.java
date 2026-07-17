package com.tcssol.expensetracker.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Horizontal labeled bar chart for "spending by category".
 * Single-series: one hue (colorPrimary) on a colorSurfaceVariant track,
 * values direct-labeled so no legend or axis is required.
 * Colors resolve from the theme, so light/dark mode both work.
 */
public class CategoryBarChartView extends View {

    public static class Item {
        public final String label;
        public final double value;

        public Item(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private String valuePrefix = "";

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float barHeight;
    private float barRadius;
    private float textBarGap;
    private float rowGap;

    public CategoryBarChartView(Context context) {
        this(context, null);
    }

    public CategoryBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        barHeight = 10 * density;
        barRadius = 5 * density;
        textBarGap = 6 * density;
        rowGap = 18 * density;

        float labelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14,
                getResources().getDisplayMetrics());

        barPaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorPrimary));
        trackPaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorSurfaceVariant));
        labelPaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurfaceVariant));
        labelPaint.setTextSize(labelSize);
        valuePaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurface));
        valuePaint.setTextSize(labelSize);
        valuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        valuePaint.setTextAlign(Paint.Align.RIGHT);
    }

    /**
     * @param data        rows to draw, already sorted (largest first)
     * @param valuePrefix currency symbol prepended to each value label
     */
    public void setData(List<Item> data, String valuePrefix) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        this.valuePrefix = valuePrefix == null ? "" : valuePrefix;
        setContentDescription(buildSummary());
        requestLayout();
        invalidate();
    }

    private String buildSummary() {
        if (items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Spending by category. ");
        for (Item item : items) {
            sb.append(item.label).append(": ").append(formatValue(item.value)).append(". ");
        }
        return sb.toString();
    }

    private String formatValue(double value) {
        return String.format(Locale.getDefault(), "%s%,.0f", valuePrefix, value);
    }

    private float rowHeight() {
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        return textHeight + textBarGap + barHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = getPaddingTop() + getPaddingBottom();
        if (!items.isEmpty()) {
            height += (int) Math.ceil(items.size() * rowHeight()
                    + (items.size() - 1) * rowGap);
        }
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) return;

        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float trackWidth = right - left;
        if (trackWidth <= 0) return;

        double max = 0;
        for (Item item : items) {
            if (item.value > max) max = item.value;
        }
        if (max <= 0) return;

        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float y = getPaddingTop();

        for (Item item : items) {
            float textBaseline = y - fm.ascent;

            // Right-aligned value first, so the label can ellipsize around it
            String value = formatValue(item.value);
            float valueWidth = valuePaint.measureText(value);
            canvas.drawText(value, right, textBaseline, valuePaint);

            float labelMax = trackWidth - valueWidth - 12 * getResources().getDisplayMetrics().density;
            CharSequence label = TextUtils.ellipsize(item.label, new android.text.TextPaint(labelPaint),
                    labelMax, TextUtils.TruncateAt.END);
            canvas.drawText(label, 0, label.length(), left, textBaseline, labelPaint);

            // Track + fill, anchored to the start
            float barTop = y + textHeight + textBarGap;
            rect.set(left, barTop, right, barTop + barHeight);
            canvas.drawRoundRect(rect, barRadius, barRadius, trackPaint);

            float fill = (float) (item.value / max) * trackWidth;
            if (fill < barHeight) fill = barHeight;
            rect.set(left, barTop, left + fill, barTop + barHeight);
            canvas.drawRoundRect(rect, barRadius, barRadius, barPaint);

            y += rowHeight() + rowGap;
        }
    }
}
