/*
 * Copyright (c) 2015. Vin @ vinexs.com (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.vinexs.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;

@SuppressWarnings("unused")
public class ScalableLinearLayout extends LinearLayout {

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;

    private float maxScaleFactor = 1.5f;
    private float minScaleFactor = 0.5f;

    public ScalableLinearLayout(Context context) {
        super(context);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public ScalableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.
        scaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public ScalableLinearLayout setMaxScale(float scale) {
        maxScaleFactor = scale;
        return this;
    }

    public ScalableLinearLayout setMinScale(float scale) {
        minScaleFactor = scale;
        return this;
    }

    public ScaleGestureDetector getScaleGestureDetector() {
        return scaleDetector;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));
            invalidate();
            return true;
        }
    }
}
