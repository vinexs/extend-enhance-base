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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.vinexs.R;
import com.vinexs.eeb.BaseActivity;
import com.vinexs.eeb.fragment.BasePrefFragment;
import com.vinexs.tool.Utility;

@SuppressWarnings("unused")
public abstract class BasePrefActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setToolbar(getToolbarResId());

        if (savedInstanceState == null) {

            BasePrefFragment basePrefFrag = new BasePrefFragment();
            Bundle prefArgs = new Bundle();
            prefArgs.putInt("res_id", getPreferencesFromResource());
            basePrefFrag.setArguments(prefArgs);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.frame_pref_content, basePrefFrag).commit();
        }
    }

    public void setPreferenceFrame() {
        FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);

        // Prepare linear layout for holder parent.
        LinearLayout parentView = new LinearLayout(this);
        LinearLayout.LayoutParams parentParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        parentView.setOrientation(LinearLayout.VERTICAL);
        parentView.setLayoutParams(parentParams);

        // Add default toolbar to holder.
        LayoutInflater inflater = LayoutInflater.from(this);
        AppBarLayout toolbarView = (AppBarLayout) inflater.inflate(R.layout.toolbar, null);
        parentView.addView(toolbarView);

        // Add frame layout to holder below toolbar.
        FrameLayout containerFrame = new FrameLayout(this);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerFrame.setLayoutParams(params);
        containerFrame.setId(R.id.frame_pref_content);
        parentView.addView(containerFrame);

        rootView.addView(parentView);
    }
    
    @Override
    public int getContentFrameResId() {
        setPreferenceFrame();
        return 0;
    }

    @Override
    public void BackStackSyncStatus() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Utility.hideKeyBroad(this);
        setResult(BaseActivity.RESULT_OK);
        finish();
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onBaseBackStackChanged(boolean noTurnBack, int entryCount) {
    }

    public abstract int getToolbarResId();

    public abstract int getPreferencesFromResource();

}
