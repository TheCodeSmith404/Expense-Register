package com.tcssol.expensetracker.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.tcssol.expensetracker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Donut chart for "spending by category" with the total in the center and a
 * legend (color dot, label, value + share) below. Slice colors come from the
 * validated categorical palette; slices are separated by small gaps and the
 * legend restates every slice, so color is never the only encoding.
 */
public class CategoryPieChartView extends View {

    private static final float GAP_DEGREES = 2f;

    private final List<CategoryBarChartView.Item> items = new ArrayList<>();
    private String valuePrefix = "";
    private String valueSuffix = "";
    private double total = 0;

    private final int[] sliceColors;
    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private final float density;
    private final float ringWidth;
    private final float donutDiameter;
    private final float legendGap;
    private final float rowGap;
    private final float dotRadius;

    public CategoryPieChartView(Context context) {
        this(context, null);
    }

    public CategoryPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        ringWidth = 26 * density;
        donutDiameter = 180 * density;
        legendGap = 20 * density;
        rowGap = 14 * density;
        dotRadius = 5 * density;

        sliceColors = new int[]{
                ContextCompat.getColor(context, R.color.chart_cat_1),
                ContextCompat.getColor(context, R.color.chart_cat_2),
                ContextCompat.getColor(context, R.color.chart_cat_3),
                ContextCompat.getColor(context, R.color.chart_cat_4),
                ContextCompat.getColor(context, R.color.chart_cat_5),
                ContextCompat.getColor(context, R.color.chart_cat_6),
        };

        float labelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14,
                getResources().getDisplayMetrics());

        slicePaint.setStyle(Paint.Style.STROKE);
        slicePaint.setStrokeWidth(ringWidth);

        labelPaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurfaceVariant));
        labelPaint.setTextSize(labelSize);

        valuePaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurface));
        valuePaint.setTextSize(labelSize);
        valuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        valuePaint.setTextAlign(Paint.Align.RIGHT);

        centerValuePaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurface));
        centerValuePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
                getResources().getDisplayMetrics()));
        centerValuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        centerValuePaint.setTextAlign(Paint.Align.CENTER);

        centerLabelPaint.setColor(MaterialColors.getColor(this,
                com.google.android.material.R.attr.colorOnSurfaceVariant));
        centerLabelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                getResources().getDisplayMetrics()));
        centerLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** Accepts the same items as the bar chart so both stay consistent. */
    public void setData(List<CategoryBarChartView.Item> data, String valuePrefix) {
        setData(data, valuePrefix, "");
    }

    /** Same as {@link #setData(List, String)} with a suffix appended to value labels (e.g. "%"). */
    public void setData(List<CategoryBarChartView.Item> data, String valuePrefix, String valueSuffix) {
        items.clear();
        if (data != null) items.addAll(data);
        this.valuePrefix = valuePrefix == null ? "" : valuePrefix;
        this.valueSuffix = valueSuffix == null ? "" : valueSuffix;
        total = 0;
        for (CategoryBarChartView.Item item : items) total += item.value;
        setContentDescription(buildSummary());
        requestLayout();
        invalidate();
    }

    private String buildSummary() {
        if (items.isEmpty() || total <= 0) return "";
        StringBuilder sb = new StringBuilder("Spending by category, total ")
                .append(formatValue(total)).append(". ");
        for (CategoryBarChartView.Item item : items) {
            sb.append(item.label).append(": ").append(formatValue(item.value))
                    .append(", ").append(Math.round(item.value * 100 / total)).append(" percent. ");
        }
        return sb.toString();
    }

    private String formatValue(double value) {
        return String.format(Locale.getDefault(), "%s%,.0f%s", valuePrefix, value, valueSuffix);
    }

    private float legendRowHeight() {
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = getPaddingTop() + getPaddingBottom();
        if (!items.isEmpty()) {
            height += (int) Math.ceil(donutDiameter + legendGap
                    + items.size() * legendRowHeight()
                    + (items.size() - 1) * rowGap);
        }
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty() || total <= 0) return;

        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float contentWidth = right - left;
        if (contentWidth <= 0) return;

        // Donut, centered horizontally
        float centerX = left + contentWidth / 2f;
        float centerY = getPaddingTop() + donutDiameter / 2f;
        float radius = (donutDiameter - ringWidth) / 2f;
        arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        int gaps = items.size() > 1 ? items.size() : 0;
        float available = 360f - gaps * GAP_DEGREES;
        float start = -90f;
        for (int i = 0; i < items.size(); i++) {
            float sweep = (float) (items.get(i).value / total) * available;
            slicePaint.setColor(sliceColors[i % sliceColors.length]);
            canvas.drawArc(arcBounds, start, Math.max(sweep, 0.5f), false, slicePaint);
            start += sweep + (gaps > 0 ? GAP_DEGREES : 0);
        }

        // Center total
        Paint.FontMetrics cv = centerValuePaint.getFontMetrics();
        canvas.drawText(formatValue(total), centerX,
                centerY - (cv.ascent + cv.descent) / 2f - 6 * density, centerValuePaint);
        canvas.drawText(getContext().getString(R.string.chart_total), centerX,
                centerY - (cv.ascent + cv.descent) / 2f + 14 * density, centerLabelPaint);

        // Legend
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float rowHeight = legendRowHeight();
        float y = getPaddingTop() + donutDiameter + legendGap;
        for (int i = 0; i < items.size(); i++) {
            CategoryBarChartView.Item item = items.get(i);
            float baseline = y - fm.ascent;
            float rowCenterY = y + rowHeight / 2f;

            dotPaint.setColor(sliceColors[i % sliceColors.length]);
            canvas.drawCircle(left + dotRadius, rowCenterY, dotRadius, dotPaint);

            String value = String.format(Locale.getDefault(), "%s (%d%%)",
                    formatValue(item.value), Math.round(item.value * 100 / total));
            float valueWidth = valuePaint.measureText(value);
            canvas.drawText(value, right, baseline, valuePaint);

            float labelStart = left + dotRadius * 2 + 10 * density;
            float labelMax = right - labelStart - valueWidth - 12 * density;
            CharSequence label = TextUtils.ellipsize(item.label, labelPaint,
                    Math.max(labelMax, 0), TextUtils.TruncateAt.END);
            canvas.drawText(label, 0, label.length(), labelStart, baseline, labelPaint);

            y += rowHeight + rowGap;
        }
    }
}
