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

package com.vinexs.eeb.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vinexs.R;
import com.vinexs.eeb.BaseActivity;
import com.vinexs.eeb.BaseFragment;

import java.util.ArrayList;

public abstract class BaseCarouselActivity extends BaseActivity {

    private ViewPager viewPager;
    private CarouselAdapter carouselAdapter;

    @Override
    public int getContentFrameResId() {
        return R.layout.carousel_base_activity;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCloseAction(CLOSE_ACTION_NONE);

        Bundle args = getIntent().getExtras();
        if (args == null || !args.containsKey("images")) {
            Log.e("Carousel", "Bundle argument must contain non-empty [images:ArrayList]");
            setResult(RESULT_CANCELED);
            finish();
        }
        Integer startIndex = args.containsKey("startIndex") ? args.getInt("startIndex") : 0;
        ArrayList<String> images = args.getStringArrayList("images");
        ArrayList<String> captions = args.containsKey("captions") ? args.getStringArrayList("captions") : new ArrayList<String>();
        if (images.size() == 0) {
            Log.e("Carousel", "Bundle argument must contain [images:ArrayList]");
            setResult(RESULT_CANCELED);
            finish();
        }

        Log.i("Carousel", "Carousel initial with " + images.size() + " picture(s) and start in position [" + startIndex + "].");

        Toolbar toolbar = (Toolbar) findViewById(R.id.lib_carousel_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        viewPager = (ViewPager) findViewById(R.id.lib_carousel_viewpager);
        carouselAdapter = new CarouselAdapter(getSupportFragmentManager(), images, captions);
        viewPager.setAdapter(carouselAdapter);
        viewPager.setOffscreenPageLimit(5);

        if (startIndex < images.size()) {
            viewPager.setCurrentItem(startIndex);
        }
    }

    /**
     * Get custom Fragment to viewpager, return null if use default view.
     *
     * @return Fragment Fragment contain view.
     */
    public abstract Fragment getCarouselFragment();

    /**
     * Load image by your own ways.
     *
     * @param imgView View to display image. It could be any view extended from ImageView.
     * @param path    String content given by activity. Used to identify which image to display.
     */
    public abstract void loadDrawable(ImageView imgView, String path);

    public class CarouselAdapter extends FragmentPagerAdapter {

        private ArrayList<String> images;
        private ArrayList<String> captions;

        CarouselAdapter(FragmentManager fm, ArrayList<String> images, ArrayList<String> captions) {
            super(fm);
            this.images = images;
            this.captions = captions;
        }

        @Override
        public Fragment getItem(int position) {
            if (images.size() < position) {
                return null;
            }
            Fragment carouselFrag = getCarouselFragment();
            if (carouselFrag == null) {
                carouselFrag = new DefaultCarouselFragment();
            }
            Bundle args = new Bundle();
            args.putString("image", images.get(position));
            if (captions.size() > position) {
                args.putString("caption", captions.get(position));
            }
            carouselFrag.setArguments(args);
            return carouselFrag;
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    public static class DefaultCarouselFragment extends BaseFragment {

        protected ImageView imgView;
        protected TextView txtCaption;

        @Override
        public int getLayoutResId() {
            return R.layout.carousel_base_fragment;
        }

        @Override
        public int getToolbarResId() {
            return 0;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            imgView = (ImageView) view.findViewById(R.id.lib_carousel_img);
            txtCaption = (TextView) view.findViewById(R.id.lib_carousel_txt_caption);
        }

        @Override
        public void onStart() {
            super.onStart();
            if (imgView != null && imgView.getDrawable() == null && args.containsKey("image")) {
                String image = args.getString("image");
                ((BaseCarouselActivity) getActivity()).loadDrawable(imgView, image);
            }
            if (txtCaption != null && args.containsKey("caption")) {
                txtCaption.setText(args.getString("caption"));
            }
        }
    }
}
