package com.vinexs.eeb.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.NumberPickerPreference;
import android.support.v7.preference.NumberPickerPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

@SuppressWarnings("unused")
public class BasePrefFragment extends PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle args = getArguments();
        if (args != null && args.containsKey("res_id")) {
            setPreferencesFromResource(args.getInt("res_id", 0), rootKey);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (handled) {
            return;
        }
        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }
        final DialogFragment f;
        if (preference instanceof NumberPickerPreference) {
            f = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}
