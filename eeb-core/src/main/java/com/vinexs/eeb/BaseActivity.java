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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.vinexs.BuildConfig;
import com.vinexs.R;
import com.vinexs.eeb.misc.BaseContextWrapper;
import com.vinexs.eeb.misc.BaseExceptionHandler;
import com.vinexs.eeb.misc.BundleArgs;
import com.vinexs.tool.Utility;

import java.lang.reflect.Field;
import java.util.Calendar;

@SuppressWarnings("unused")
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG = getClass().getSimpleName();
    protected static long appInitTime = Calendar.getInstance().getTimeInMillis();
    protected static BaseExceptionHandler exceptionHandler = null;

    public static final int CLOSE_ACTION_NONE = 0;
    public static final int CLOSE_DIALOG = 1;
    public static final int CLOSE_TOAST = 2;

    public static final int DRAWER_MAIN_ONLY = 0;
    public static final int DRAWER_EVERYWHERE = 1;

    public static final String LEFT_DRAWER_NAME = "LEFT_DRAWER";
    public static final String RIGHT_DRAWER_NAME = "RIGHT_DRAWER";

    protected int closeAction = BaseActivity.CLOSE_DIALOG;
    protected int drawerBehavior = BaseActivity.DRAWER_MAIN_ONLY;

    protected boolean allowBack = true;
    protected boolean pressBackToClose = false;

    // Navigation Drawer
    protected DrawerLayout drawerLayout = null;
    protected ActionBarDrawerToggle drawerToggle = null;
    protected Boolean hasLeftDrawer = false;
    protected Boolean hasRightDrawer = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOverflowMenuAvailable();
        setContentFrame();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }

        if (savedInstanceState != null) {
            hasLeftDrawer = savedInstanceState.getBoolean("hasLeftDrawer");
            hasRightDrawer = savedInstanceState.getBoolean("hasRightDrawer");
            allowBack = savedInstanceState.getBoolean("allowBack");
            closeAction = savedInstanceState.getInt("closeAction");
        }

        setDefaultBackStackListener();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (hasLeftDrawer || hasRightDrawer) {
            getDrawerToggle().syncState();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasLeftDrawer", hasLeftDrawer);
        outState.putBoolean("hasRightDrawer", hasRightDrawer);
        outState.putBoolean("allowBack", allowBack);
        outState.putInt("closeAction", closeAction);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
        if (hasLeftDrawer || hasRightDrawer) {
            getDrawerToggle().onConfigurationChanged(newConfig);
            getDrawerToggle().syncState();
        }
    }

    @Override
    public abstract void onNewIntent(Intent intent);


    @Override
    public void onStart() {
        super.onStart();
        BackStackSyncStatus();
        if (!BuildConfig.DEBUG && exceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        }
    }

    /**
     * Restart application programmatically.
     */
    public void restart() {
        final Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        final Intent intent = new Intent(this, getClass());
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    // region Default layout frame =================================================================

    /**
     * * It called by {@link #onCreate(Bundle)}) from {@link BaseActivity} automatically. <br/>
     * Set a layout as the base of this application, all fragment will attache on it.
     */
    public void setContentFrame() {
        int layoutResId = getContentFrameResId();
        if (layoutResId > 0) {
            setContentView(layoutResId);
        }
    }

    /**
     * Assign layout resource id to be content frame.
     *
     * @return Layout resource id
     */
    public abstract int getContentFrameResId();

    /**
     * Set a new Toolbar as action bar.
     * @param resId Toolbar resource id
     */
    public void setToolbar(int resId) {
        Toolbar toolbar = (Toolbar) findViewById(resId);
        if (toolbar != null) {
            setToolbar(toolbar);
        }
    }

    /**
     * Set a new Toolbar as action bar.
     * @param toolbar Toolbar found from view
     */
    public void setToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        if ((hasLeftDrawer || hasRightDrawer) && getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (drawerToggle != null) {
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
            drawerToggle.syncState();
            drawerLayout.addDrawerListener(drawerToggle);
        }
    }

    // endregion Default layout frame ==============================================================

    // region Back Handling ========================================================================

    /**
     * * It called by {@link #onCreate(Bundle)}) from {@link BaseActivity} automatically. <br/>
     * It used to add system default back stack.<br/>
     */
    public void setDefaultBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                BackStackSyncStatus();
            }
        });
    }

    /**
     * Event to trigger when back button / (android.R.id.home) was hit.
     */
    @Override
    public void onBackPressed() {
        Utility.hideKeyBroad(this);
        try {
            if (allowBack) {
                FragmentManager fragmentMgr = getSupportFragmentManager();
                if (fragmentMgr.getBackStackEntryCount() > 0) {
                    fragmentMgr.popBackStack();
                } else if (drawerLayout != null &&
                        (drawerLayout.isDrawerOpen(GravityCompat.START) ||
                                drawerLayout.isDrawerOpen(GravityCompat.END))) {
                    closeLeftDrawer();
                    closeRightDrawer();
                } else {
                    closeAppsConfirmation();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When {@link #setDefaultBackStackListener()} ran in {@link #onCreate(Bundle)},<br/>
     * this method will execute on every back stack change.
     */
    protected void BackStackSyncStatus() {
        try {
            FragmentManager fragMgr = getSupportFragmentManager();
            int count = fragMgr.getBackStackEntryCount();
            if (count - 1 >= 0) {
                BackStackEntry entry = fragMgr.getBackStackEntryAt(count - 1);
                String title = (String) entry.getBreadCrumbTitle();
                if (getSupportActionBar() != null) {
                    setBackStackTitle(title);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    if (!hasLeftDrawer && !hasRightDrawer) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                }
                if (drawerBehavior == DRAWER_MAIN_ONLY && drawerLayout != null) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
                }
                if (drawerToggle != null) {
                    drawerToggle.setDrawerIndicatorEnabled(false);
                }
                onBaseBackStackChanged(false, count);
            } else {
                if (getSupportActionBar() != null) {
                    setBackStackTitle(Utility.getAppName(this));
                    getSupportActionBar().setHomeButtonEnabled(false);
                    if (!hasLeftDrawer && !hasRightDrawer) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                }
                if (hasLeftDrawer) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
                    if (getDrawerToggle() != null) {
                        getDrawerToggle().setDrawerIndicatorEnabled(true);
                    }
                }
                if (hasRightDrawer) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                }
                onBaseBackStackChanged(true, count);
            }
            if (drawerToggle != null) {
                drawerToggle.syncState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set BreadCrumbTitle.
     * */
    public void setBackStackTitle(String title) {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        actionbar.setTitle(title);
    }

    /**
     * Set BreadCrumbTitle with custom spannable string.
     * */
    public void setBackStackTitle(SpannableString title) {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        actionbar.setTitle(title);
    }

    /**
     * When {@link #BackStackSyncStatus()} executed, it will fire this method.
     * @param noTurnBack Is there are back stack still can go back.
     * @param entryCount Fragment back stack entry count in {@link FragmentManager}
     */
    public abstract void onBaseBackStackChanged(boolean noTurnBack, int entryCount);

    /**
     * Handling (android.R.id.home) hit event. Determine it is a back or just draw the drawer.
     * @param item Menu item hit.
     * @return Is event already handle.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
        if (drawerToggle == null) {
            onBackPressed();
            return true;
        }
        return drawerToggle.onOptionsItemSelected(item);
    }

    /**
     * Lock back press button event.
     * !! User can still exit by pressing home button.
     */
    public void lockBackPress() {
        allowBack = false;
    }

    /**
     * Unlock locked back button.
     */
    public void unlockBackPress() {
        allowBack = true;
    }

    /**
     * Set close action when user close the application
     * @param action Action to take [CLOSE_DIALOG, CLOSE_TOAST, CLOSE_NONE].
     */
    public void setCloseAction(int action) {
        closeAction = action;
    }

    /**
     * Handle user closing application event. To change behaviour, use method {@link #setCloseAction(int)}.
     */
    public void closeAppsConfirmation() {
        switch (closeAction) {
            case CLOSE_DIALOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.app_close, Utility.getAppName(this)));
                builder.setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(AppCompatActivity.RESULT_CANCELED);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AppCompatDialog dialog = builder.create();
                dialog.show();
                break;
            case CLOSE_TOAST:
                if (!pressBackToClose) {
                    pressBackToClose = true;
                    Toast.makeText(this, getResources().getString(R.string.app_close_back,
                            Utility.getAppName(this)), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pressBackToClose = false;
                        }
                    }, 2000);
                    return;
                }
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                setResult(RESULT_CANCELED);
                finish();
        }
    }

    // endregion Back Handling =====================================================================

    // region Drawer Control =======================================================================

    public boolean hasLeftDrawer() {
        return hasLeftDrawer;
    }

    public boolean hasRightDrawer() {
        return hasRightDrawer;
    }

    /**
     * Add a fragment to left drawer(R.id.frame_drawer_left)
     * @param frag Fragment
     */
    public void addLeftDrawer(Fragment frag) {
        addLeftDrawer(frag, LEFT_DRAWER_NAME);
    }

    /**
     * Add a fragment to left drawer(R.id.frame_drawer_left) with custom TAG.
     * @param frag Fragment
     * @param tag Tag name in String
     */
    public void addLeftDrawer(Fragment frag, String tag) {
        if (hasLeftDrawer) {
            return;
        }
        hasLeftDrawer = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_drawer_left, frag, tag).commitAllowingStateLoss();
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
        setDrawerToggleEnable();
    }

    /**
     * Add a fragment to right drawer(R.id.frame_drawer_right)
     * @param frag Fragment
     */
    public void addRightDrawer(Fragment frag) {
        addRightDrawer(frag, RIGHT_DRAWER_NAME);
    }

    /**
     * Add a fragment to right drawer(R.id.frame_drawer_right) with custom TAG.
     * @param frag Fragment
     * @param tag Tag name in String
     */
    public void addRightDrawer(Fragment frag, String tag) {
        if (hasRightDrawer) {
            return;
        }
        hasRightDrawer = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_drawer_right, frag, tag).commitAllowingStateLoss();
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
        setDrawerToggleEnable();
    }

    /**
     * To control lock or unlock left drawer.<br/>
     * @param mode DrawerLayout.LOCK_MODE_UNLOCKED or DrawerLayout.LOCK_MODE_LOCKED_CLOSED.
     */
    public void setLeftDrawerLockMode(final int mode) {
        if (drawerLayout == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawerLayout.setDrawerLockMode(mode, GravityCompat.START);
            }
        });
    }

    /**
     * To control lock or unlock right drawer.<br/>
     *
     * @param mode DrawerLayout.LOCK_MODE_UNLOCKED or DrawerLayout.LOCK_MODE_LOCKED_CLOSED.
     */
    public void setRightDrawerLockMode(final int mode) {
        if (drawerLayout == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawerLayout.setDrawerLockMode(mode, GravityCompat.END);
            }
        });
    }

    /**
     * Close left drawer programmatically if it is opened.
     */
    public void closeLeftDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Close right drawer programmatically if it is opened.
     */
    public void closeRightDrawer() {
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    /**
     * Get drawer toggle controller
     * @return DrawerToggle
     */
    public ActionBarDrawerToggle getDrawerToggle() {
        if (drawerToggle == null) {
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        }
        return drawerToggle;
    }

    /**
     * Set application can have an drawer.<br/>
     * It called from {@link #addLeftDrawer(Fragment)} or {@link #addRightDrawer(Fragment)}
     */
    private void setDrawerToggleEnable() {
        ActionBarDrawerToggle drawerToggle = getDrawerToggle();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setDrawerBehavior(int behavior) {
        drawerBehavior = behavior;
    }

    // endregion Drawer Control ====================================================================

    // region Fragments Control ====================================================================

    /**
     * Get attached fragment by tag from {@link FragmentManager}
     * @param fragName Fragment TAG in String.
     * @return Fragment matched with TAG.
     */
    public Fragment getExistFragment(String fragName) {
        return getSupportFragmentManager().findFragmentByTag(fragName);
    }

    /**
     * Get latest attached fragment from {@link FragmentManager}
     * @return Latest attached Fragment
     */
    public Fragment getTopFragment() {
        FragmentManager fragMgr = getSupportFragmentManager();
        if (fragMgr.getBackStackEntryCount() == 0) {
            return null;
        }
        FragmentManager.BackStackEntry backEntry = fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        String backEntryName = backEntry.getName();
        return fragMgr.findFragmentByTag(backEntryName);
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
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();
        BackStackEntry lastEntry = fragMgr.getBackStackEntryCount() == 0 ?
                null : fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        // Transaction options
        String fragName = fragment.getClass().getSimpleName();
        String breadCrumbTitle;
        String breadCrumbShortTitle;
        Boolean addToBackStack = false;
        int animationEnter = 0;
        int animationExit = 0;
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
                        lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(this);
            }
            if (args.containsKey(BundleArgs.breadCrumbShortTitle)) {
                breadCrumbShortTitle = args.getString(BundleArgs.breadCrumbShortTitle);
            } else {
                breadCrumbShortTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(this);
            }
            if (args.containsKey(BundleArgs.fragmentName)) {
                fragName = args.getString(BundleArgs.fragmentName);
            }
            if (args.containsKey(BundleArgs.addToBackStack)) {
                addToBackStack = args.getBoolean(BundleArgs.addToBackStack);
            }
        } else {
            breadCrumbTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(this);
            breadCrumbShortTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(this);
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
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();
        BackStackEntry lastEntry = fragMgr.getBackStackEntryCount() == 0 ?
                null : fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        // Transaction options
        String fragName = fragment.getClass().getSimpleName();
        String breadCrumbTitle;
        String breadCrumbShortTitle;
        Boolean addToBackStack = true;
        int animationEnter = 0;
        int animationExit = 0;
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
                        lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(this);
            }
            if (args.containsKey(BundleArgs.breadCrumbShortTitle)) {
                breadCrumbShortTitle = args.getString(BundleArgs.breadCrumbShortTitle);
            } else {
                breadCrumbShortTitle = (lastEntry != null) ?
                        lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(this);
            }
            if (args.containsKey(BundleArgs.fragmentName)) {
                fragName = args.getString(BundleArgs.fragmentName);
            }
            if (args.containsKey(BundleArgs.addToBackStack)) {
                addToBackStack = args.getBoolean(BundleArgs.addToBackStack);
            }
        } else {
            breadCrumbTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbTitle().toString() : Utility.getAppName(this);
            breadCrumbShortTitle = (lastEntry != null) ?
                    lastEntry.getBreadCrumbShortTitle().toString() : Utility.getAppName(this);
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
        if (!fragment.isHidden()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(fragment).commitAllowingStateLoss();
        Bundle args = fragment.getArguments();
        if (args != null && args.containsKey(BundleArgs.breadCrumbTitle)) {
            manager.popBackStack();
        }
    }

    // endregion Fragments Control =================================================================

    // region Override Setting =====================================================================

    /**
     * <p>* It called from activity itself.</p>
     * It used to override original context setting with SharedPreferences variables.<br/>
     * For using your own ContextWrapper, developer should override {@link #getBaseContext(Context)} method.
     * @param newBase Context from activity.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(getBaseContext(newBase));
    }

    /**
     * Provide ContextWrapper to {@link #attachBaseContext(Context)} method.
     *
     * @param newBase Context from activity.
     * @return ContextWrapper
     */
    public ContextWrapper getBaseContext(Context newBase) {
        return new BaseContextWrapper(newBase);
    }

    // endregion Override Setting ==================================================================

    // region Hacking feature ======================================================================

    /**
     * * It called by {@link #onCreate(Bundle)}) from {@link BaseActivity} automatically. <br/>
     * <p>This hack used to add overflow menu button if device has physical menu button.
     * Phones with a physical menu button don't have an overflow menu in the ActionBar. </p>
     * <p>This avoids ambiguity for the user, essentially having two buttons available to open the exact same menu.</p>
     */
    public void setOverflowMenuAvailable() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // endregion Hacking feature ===================================================================
}