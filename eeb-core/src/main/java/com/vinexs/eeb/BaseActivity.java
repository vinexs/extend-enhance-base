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

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.vinexs.eeb.misc.BaseExceptionHandler;
import com.vinexs.eeb.misc.BundleArgs;
import com.vinexs.tool.Utility;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("unused")
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG = getClass().getSimpleName();
    protected static long appInitTime = Calendar.getInstance().getTimeInMillis();
    protected static BaseExceptionHandler exceptionHandler = null;

    public static final int CLOSE_ACTION_NONE = 0;
    public static final int CLOSE_DIALOG = 1;
    public static final int CLOSE_TOAST = 2;

    protected int closeAction = BaseActivity.CLOSE_DIALOG;

    protected boolean closeByDialog = true;
    protected boolean closeByToast = false;
    protected boolean allowBack = true;
    protected boolean pressBackToClose = false;

    // Navigation Drawer
    protected DrawerLayout drawerLayout = null;
    protected ActionBarDrawerToggle drawerToggle = null;
    protected Boolean hasLeftDrawer = false;
    protected Boolean hasRightDrawer = false;

    // Theme Res
    protected int currentTheme = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOverflowMenuAvailable();
        setCustomSetting();
        setContentFrame();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
        setDefaultBackStackListener();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (hasLeftDrawer || hasRightDrawer) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BackStackSyncStatus();
        if (!BuildConfig.DEBUG && exceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentTheme != PreferenceManager.getDefaultSharedPreferences(this).getInt("theme", getBaseContext().getApplicationInfo().theme)) {
            restart();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
        if (hasLeftDrawer || hasRightDrawer) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public abstract void onNewIntent(Intent intent);

    public void restart() {
        final Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        final Intent intent = new Intent(this, getClass());
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    // ================  Default layout frame  =========================

    public void setContentFrame() {
        int layoutResId = getContentFrame();
        if (layoutResId > 0) {
            setContentView(layoutResId);
        }
    }

    public abstract int getContentFrame();

    protected void setToolbar(int resId) {
        Toolbar toolbar = (Toolbar) findViewById(resId);
        if (toolbar != null) {
            setToolbar(toolbar);
        }
    }

    protected void setToolbar(Toolbar toolbar) {
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

    // ================  Handle back and fragment  =========================

    /**
     * To add system default back stack, put this method to onCreate().
     */
    public void setDefaultBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                BackStackSyncStatus();
            }
        });
    }

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

    public void lockBackPress() {
        allowBack = false;
    }

    public void unlockBackPress() {
        allowBack = true;
    }

    public void setCloseAction(int action) {
        closeAction = action;
    }

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

    /**
     * Require setDefaultBackStackListener() ran in onCreate
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
                if (drawerLayout != null) {
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
                    if (drawerToggle != null) {
                        drawerToggle.setDrawerIndicatorEnabled(true);
                    }
                }
                if (hasRightDrawer) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
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

    public void setBackStackTitle(String title) {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        actionbar.setTitle(title);
    }

    public void setBackStackTitle(SpannableString title) {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        actionbar.setTitle(title);
    }

    public abstract void onBaseBackStackChanged(boolean noTurnBack, int entryCount);

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

    // ================  Fragments Control =========================

    public void addLeftDrawer(Fragment frag) {
        addLeftDrawer(frag, "LEFT_DRAWER");
    }

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

    public void addRightDrawer(Fragment frag) {
        if (hasRightDrawer) {
            return;
        }
        hasRightDrawer = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_drawer_right, frag, "RIGHT_DRAWER").commitAllowingStateLoss();
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
        setDrawerToggleEnable();
    }

    public void closeLeftDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void closeRightDrawer() {
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    private void setDrawerToggleEnable() {
        if (drawerToggle != null) {
            return;
        }
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * <p>The user should be pointed(through an intent) to the system settings to change it manually.
     * The application should handle its localization on its own just like described.</p>
     * <p>It should run in onCreate() method and before setContentView().</p>
     * <p><font color="red">Don't forget to add android:configChanges="layoutDirection|locale" to
     * every activity at AndroidManifest.</font></p>
     */
    public void setCustomSetting() {
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(this);

        // Override application original locale.
        String defaultLocale = Locale.getDefault().toString();
        String appLocale = sharePref.getString("locale", defaultLocale);
        if (!appLocale.isEmpty() && !defaultLocale.equals(appLocale)) {
            Locale locale;
            if (appLocale.contains("_")) {
                String[] localePart = appLocale.split("_");
                locale = new Locale(localePart[0], localePart[1]);
            } else {
                locale = new Locale(appLocale);
            }
            Locale.setDefault(locale);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        // Override application original Theme.
        try {
            Integer appTheme = sharePref.getInt("theme", getBaseContext().getApplicationInfo().theme);
            currentTheme = appTheme;
            setTheme(appTheme);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================  Fragments Control =========================

    public Fragment getExistFragment(String fragName) {
        return getSupportFragmentManager().findFragmentByTag(fragName);
    }

    public Fragment getTopFragment() {
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentManager.BackStackEntry backEntry = fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1);
        String backEntryName = backEntry.getName();
        return fragMgr.findFragmentByTag(backEntryName);
    }

    public void addFragment(Fragment fragment) {
        addFragment(R.id.frame_content, fragment);
    }

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
        if (breadCrumbTitle != null && !breadCrumbTitle.isEmpty()) {
            transaction.setBreadCrumbTitle(breadCrumbTitle);
        }
        if (breadCrumbShortTitle != null && !breadCrumbShortTitle.isEmpty()) {
            transaction.setBreadCrumbShortTitle(breadCrumbShortTitle);
        }
        if (transaction.isAddToBackStackAllowed() && addToBackStack) {
            transaction.addToBackStack(fragName);
        }
        // Add fragment
        transaction.add(viewId, fragment, fragName).commitAllowingStateLoss();
    }

    public void replaceFragment(Fragment fragment) {
        replaceFragment(R.id.frame_content, fragment);
    }

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
        if (breadCrumbTitle != null && !breadCrumbTitle.isEmpty()) {
            transaction.setBreadCrumbTitle(breadCrumbTitle);
        }
        if (breadCrumbShortTitle != null && !breadCrumbShortTitle.isEmpty()) {
            transaction.setBreadCrumbShortTitle(breadCrumbShortTitle);
        }
        if (transaction.isAddToBackStackAllowed() && addToBackStack) {
            transaction.addToBackStack(fragName);
        }
        // Replace fragment
        transaction.replace(viewId, fragment, fragName).commitAllowingStateLoss();
    }

    public void showFragment(Fragment fragment) {
        if (!fragment.isHidden()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.show(fragment).commitAllowingStateLoss();
    }

    public void hideFragment(Fragment fragment) {
        if (!fragment.isAdded()) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragment).commitAllowingStateLoss();
    }

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

    // ================  Hack =========================

    /**
     * <p>This hack used to add overflow menu button if device has physical menu button.
     * Phones with a physical menu button don't have an overflow menu in the ActionBar. </p>
     * <p>This avoids ambiguity for the user, essentially having two buttons available to open the exact same menu.</p>
     * <p>It should run in onCreate() method and before setContentView().</p>
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

}