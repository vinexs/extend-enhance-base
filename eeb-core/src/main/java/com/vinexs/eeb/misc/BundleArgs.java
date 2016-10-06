package com.vinexs.eeb.misc;

import android.os.Bundle;

@SuppressWarnings("unused")
public class BundleArgs {

    public static final String animationEnter = "animationEnter";

    public static final String animationExit = "animationExit";

    public static final String breadCrumbTitle = "breadCrumbTitle";

    public static final String breadCrumbShortTitle = "breadCrumbShortTitle";

    public static final String fragmentName = "fragmentName";

    public static final String fragmentId = "fragmentId";

    public static final String addToBackStack = "addToBackStack";

    public static Bundle getDefault() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BundleArgs.addToBackStack, false);
        return bundle;
    }

    public static Bundle setTitle(Bundle bundle, String title) {
        bundle.putString(BundleArgs.breadCrumbTitle, title);
        return bundle;
    }

    public static Bundle setShortTitle(Bundle bundle, String shortTitle) {
        bundle.putString(BundleArgs.breadCrumbShortTitle, shortTitle);
        return bundle;
    }

    public static Bundle setName(Bundle bundle, String name) {
        bundle.putString(BundleArgs.fragmentName, name);
        return bundle;
    }

    public static Bundle setName(Bundle bundle, int id) {
        bundle.putInt(BundleArgs.fragmentId, id);
        return bundle;
    }

    public static Bundle setAnimation(Bundle bundle, int enterAnim, int exitAnim) {
        bundle.putInt(BundleArgs.animationEnter, enterAnim);
        bundle.putInt(BundleArgs.animationExit, exitAnim);
        return bundle;
    }

    public static Bundle hasBackStack(Bundle bundle) {
        bundle.putBoolean(BundleArgs.addToBackStack, true);
        return bundle;
    }


}
