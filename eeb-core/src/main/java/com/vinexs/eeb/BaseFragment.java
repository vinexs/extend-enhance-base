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

package com.vinexs.eeb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.vinexs.R;
import com.vinexs.eeb.misc.BundleArgs;
import com.vinexs.tool.Utility;

@SuppressWarnings("unused")
public abstract class BaseFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();

    /**
     * Should be overwrite in onCreate method from extended class.
     */
    protected Boolean noClickThroughView = true;

    /**
     * Will be fill in onCreate method.
     */
    protected Bundle args = null;
    protected Context context = null;

    /**
     * Get bread crumb title from argument bundle.
     *
     * @return Bread crumb title
     */
    public String getBreadCrumbTitle() {
        if (args == null || !args.containsKey(BundleArgs.breadCrumbTitle)) {
            return null;
        }
        return args.getString(BundleArgs.breadCrumbTitle);
    }

    /**
     * Get bread crumb short title from argument bundle.
     * @return Bread crumb short title
     */
    public String getBreadCrumbShortTitle() {
        if (args == null || !args.containsKey(BundleArgs.breadCrumbShortTitle)) {
            return null;
        }
        return args.getString(BundleArgs.breadCrumbShortTitle);
    }

    /**
     * Get fragment tag name from argument bundle.
     * @return Fragment tag name
     */
    public String getFragmentName() {
        if (args == null || !args.containsKey(BundleArgs.fragmentName)) {
            return getClass().getSimpleName();
        }
        return args.getString(BundleArgs.fragmentName);
    }

    /**
     * Get fragment id from argument bundle.
     * @return Fragment id
     */
    public int getFragmentId() {
        if (args == null || !args.containsKey(BundleArgs.fragmentId)) {
            return 0;
        }
        return args.getInt(BundleArgs.fragmentId);
    }

    /**
     * Get parent BaseActivity.
     * @return BaseActivity
     */
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
        context = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Fix API > 11  IllegalStateException -> Can not perform this action after onSaveInstanceState.
        outState.putBoolean("onSaveInstanceState", true); // WORKAROUND_FOR_BUG_19917
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutResId = getLayoutResId();
        if (layoutResId == 0) {
            return null;
        }
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (noClickThroughView) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View view, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int toolbarResId = getToolbarResId();
        if (toolbarResId != 0 && getView() != null) {
            Toolbar toolbar = (Toolbar) getView().findViewById(toolbarResId);
            if (toolbar != null) {
                getBaseActivity().setToolbar(toolbar);
            } else {
                Log.d(TAG, "Assigned Toolbar cannot be found.");
            }
        }
    }

    /**
     * Assign fragment layout recourse id. If fragment do not have layout, leave it 0.
     * @return Fragment layout id.
     */
    public abstract int getLayoutResId();

    /**
     * Assign toolbar resource id. If layout do not have toolbar, leave it 0.
     * @return Toolbar resource id.
     */
    public abstract int getToolbarResId();

    // region Fragments Control ====================================================================

    /**
     * Get attached fragment by tag from {@link FragmentManager}
     * @param fragName Fragment TAG in String.
     * @return Fragment matched with TAG.
     */
    public Fragment getExistFragment(String fragName) {
        return getActivity().getSupportFragmentManager().findFragmentByTag(fragName);
    }

    /**
     * Determine current is the latest attached fragment.
     * @return Is latest fragment
     */
    public Boolean isOnTop() {
        FragmentManager fragMgr = getActivity().getSupportFragmentManager();
        FragmentManager.BackStackEntry backEntry = fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        String backEntryName = backEntry.getName();
        return backEntryName != null && backEntryName.equals(getFragmentName());
    }

    /**
     * Add a fragment to default FrameLayout(R.id.frame_content).
     * @param fragment Fragment
     */
    public void addFragment(Fragment fragment) {
        addFragment(R.id.frame_content, fragment);
    }

    /**
     * Add a fragment to targeted FrameLayout.
     * @param viewId Targeted FrameLayout resource id
     * @param fragment Fragment
     */
    public void addFragment(int viewId, Fragment fragment) {
        if (fragment.isAdded()) {
            return;
        }
        FragmentManager fragMgr = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();
        FragmentManager.BackStackEntry lastEntry = fragMgr.getBackStackEntryCount() == 0 ?
                null : fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        // Transaction options
        String fragName = fragment.getClass().getSimpleName();
        String breadCrumbTitle;
        String breadCrumbShortTitle;
        int animationEnter = 0;
        int animationExit = 0;
        Boolean addToBackStack = false;
        // Fill options
        Bundle args = fragment.getArguments();
        if (args != null) {
            if (args.containsKey(BundleArgs.animationEnter) && args.containsKey(BundleArgs.animationExit)) {
                animationEnter = args.getInt(BundleArgs.animationEnter);
                animationExit = args.getInt(BundleArgs.animationExit);
            }
            if (args.containsKey(BundleArgs.breadCrumbTitle)) {
                breadCrumbTitle = args.getString(BundleArgs.breadCrumbTitle);
            } else {
                breadCrumbTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(getActivity());
            }
            if (args.containsKey(BundleArgs.breadCrumbShortTitle)) {
                breadCrumbShortTitle = args.getString(BundleArgs.breadCrumbShortTitle);
            } else {
                breadCrumbShortTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(getActivity());
            }
            if (args.containsKey(BundleArgs.fragmentName)) {
                fragName = args.getString(BundleArgs.fragmentName);
            }
            if (args.containsKey(BundleArgs.addToBackStack)) {
                addToBackStack = args.getBoolean(BundleArgs.addToBackStack);
            }
        } else {
            breadCrumbTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(getActivity());
            breadCrumbShortTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(getActivity());
        }
        // Set option to fragment manager
        if (animationEnter != 0 && animationExit != 0) {
            transaction.setCustomAnimations(animationEnter, animationExit);
        }
        if (breadCrumbTitle != null) {
            transaction.setBreadCrumbTitle(breadCrumbTitle);
        }
        if (breadCrumbShortTitle != null) {
            transaction.setBreadCrumbShortTitle(breadCrumbShortTitle);
        }
        if (transaction.isAddToBackStackAllowed() && addToBackStack) {
            transaction.addToBackStack(fragName);
        }
        // Add fragment
        transaction.add(viewId, fragment, fragName).commitAllowingStateLoss();
    }

    /**
     * Replace a fragment from default FrameLayout(R.id.frame_content) with another fragment.
     * @param fragment Fragment
     */
    public void replaceFragment(Fragment fragment) {
        replaceFragment(R.id.frame_content, fragment);
    }

    /**
     * Replace a fragment from targeted FrameLayout with another fragment.
     * @param viewId Targeted FrameLayout resource id
     * @param fragment Fragment
     */
    public void replaceFragment(int viewId, Fragment fragment) {
        if (fragment.isAdded()) {
            return;
        }
        FragmentManager fragMgr = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();
        FragmentManager.BackStackEntry lastEntry = fragMgr.getBackStackEntryCount() == 0 ?
                null : fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        // Transaction options
        String fragName = fragment.getClass().getSimpleName();
        String breadCrumbTitle;
        String breadCrumbShortTitle;
        int animationEnter = 0;
        int animationExit = 0;
        Boolean addToBackStack = true;
        // Fill options
        Bundle args = fragment.getArguments();
        if (args != null) {
            if (args.containsKey(BundleArgs.animationEnter) && args.containsKey(BundleArgs.animationExit)) {
                animationEnter = args.getInt(BundleArgs.animationEnter);
                animationExit = args.getInt(BundleArgs.animationExit);
            }
            if (args.containsKey(BundleArgs.breadCrumbTitle)) {
                breadCrumbTitle = args.getString(BundleArgs.breadCrumbTitle);
            } else {
                breadCrumbTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(getActivity());
            }
            if (args.containsKey(BundleArgs.breadCrumbShortTitle)) {
                breadCrumbShortTitle = args.getString(BundleArgs.breadCrumbShortTitle);
            } else {
                breadCrumbShortTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(getActivity());
            }
            if (args.containsKey(BundleArgs.fragmentName)) {
                fragName = args.getString(BundleArgs.fragmentName);
            }
            if (args.containsKey(BundleArgs.addToBackStack)) {
                addToBackStack = args.getBoolean(BundleArgs.addToBackStack);
            }
        } else {
            breadCrumbTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(getActivity());
            breadCrumbShortTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(getActivity());
        }
        // Set option to fragment manager
        if (animationEnter != 0 && animationExit != 0) {
            transaction.setCustomAnimations(animationEnter, animationExit);
        }
        if (breadCrumbTitle != null) {
            transaction.setBreadCrumbTitle(breadCrumbTitle);
        }
        if (breadCrumbShortTitle != null) {
            transaction.setBreadCrumbShortTitle(breadCrumbShortTitle);
        }
        if (transaction.isAddToBackStackAllowed() && addToBackStack) {
            transaction.addToBackStack(fragName);
        }
        // Replace fragment
        transaction.replace(viewId, fragment, fragName).commitAllowingStateLoss();
    }

    /**
     * Show a hidden fragment.
     * @param fragment Fragment
     */
    public void showFragment(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.show(fragment).commitAllowingStateLoss();
    }

    /**
     * Hide a visible fragment.
     * @param fragment Fragment
     */
    public void hideFragment(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(fragment).commitAllowingStateLoss();
    }

    /**
     * Remove a fragment from {@link FragmentManager}.
     * @param fragment Fragment
     */
    public void removeFragment(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(fragment).commitAllowingStateLoss();
        Bundle args = fragment.getArguments();
        if (args != null && args.containsKey(BundleArgs.breadCrumbTitle)) {
            manager.popBackStack();
        }
    }

    /**
     * Remove current fragment from {@link FragmentManager}.
     */
    public void removeSelf() {
        removeFragment(this);
    }

    // endregion Fragments Control =================================================================
}
