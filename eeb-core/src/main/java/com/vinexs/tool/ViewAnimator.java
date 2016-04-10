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

package com.vinexs.tool;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressWarnings("unused")
public class ViewAnimator {

    public static void expand(final View view) {
        int duration = (int) (view.getMeasuredHeight() / view.getContext().getResources().getDisplayMetrics().density);
        expand(view, duration);
    }

    public static void expand(final View view, int duration) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public static void collapse(final View view) {
        int duration = (int) (view.getMeasuredHeight() / view.getContext().getResources().getDisplayMetrics().density);
        collapse(view, duration);
    }

    public static void collapse(final View view, int duration) {
        final int initialHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public static void fadeIn(final View view) {
        fadeIn(view, 1000);
    }

    public static void fadeIn(final View view, int duration) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public static void fadeOut(final View view) {
        fadeIn(view, 1000);
    }

    public static void fadeOut(final View view, int duration) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public static void changeImageResource(final ImageView view, final Bitmap newImage) {
        final Animation animFadeOut = new AlphaAnimation(1, 0);
        final Animation animFadeIn = new AlphaAnimation(0, 1);
        animFadeOut.setInterpolator(new DecelerateInterpolator());
        animFadeOut.setDuration(250);
        animFadeIn.setInterpolator(new DecelerateInterpolator());
        animFadeIn.setDuration(300);
        animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setImageBitmap(newImage);
                view.startAnimation(animFadeIn);

            }
        });
        view.startAnimation(animFadeOut);
    }


}
