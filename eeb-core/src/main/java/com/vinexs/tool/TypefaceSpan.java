package com.vinexs.tool;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import java.io.File;

public class TypefaceSpan extends MetricAffectingSpan {

    private Context context;
    private Typeface typeface;

    public TypefaceSpan(Context context) {
        this.context = context;
    }

    public TypefaceSpan(Context context, File typefaceFile) {
        typeface = Typeface.createFromFile(typefaceFile);
    }

    public void fromAsset(String typefacepath) {
        typeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), typefacepath);
    }

    public void fromPath(String typefacepath) {
        typeface = Typeface.createFromFile(typefacepath);
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {
        textPaint.setTypeface(typeface);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        textPaint.setTypeface(typeface);
    }
}
