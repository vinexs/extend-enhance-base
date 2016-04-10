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

package android.support.v7.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.vinexs.R;

@SuppressWarnings("unused")
public class NumberPickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private static final String STATE_PICKER_VALUE = "number_picker_value";

    private NumberPicker numberPicker;

    private boolean restoredState;
    private int restoredValue;

    public static NumberPickerPreferenceDialogFragmentCompat newInstance(String key) {
        NumberPickerPreferenceDialogFragmentCompat fragment = new NumberPickerPreferenceDialogFragmentCompat();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoredState = true;
            restoredValue = savedInstanceState.getInt(STATE_PICKER_VALUE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PICKER_VALUE, numberPicker.getValue());
    }

    @Override
    protected void onBindDialogView(View view) {
        NumberPickerPreference preference = getNumberPickerPreference();

        numberPicker = (NumberPicker) view.findViewById(R.id.numberpicker);
        numberPicker.setMinValue(preference.getMinValue());
        numberPicker.setMaxValue(preference.getMaxValue());
        numberPicker.setValue(restoredState ? restoredValue : preference.getValue());
        if (preference.getDescendantFocusability() > 0) {
            //noinspection ResourceType
            numberPicker.setDescendantFocusability(preference.getDescendantFocusability());
        }
        numberPicker.setWrapSelectorWheel(preference.isWrapSelectorWheel());

        TextView textView = (TextView) view.findViewById(R.id.subtitle);
        textView.setText(preference.getSubtitleText());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = numberPicker.getValue();
            if (getNumberPickerPreference().callChangeListener(value)) {
                getNumberPickerPreference().setValue(value);
            }
        }
    }

    private NumberPickerPreference getNumberPickerPreference() {
        return (NumberPickerPreference) getPreference();
    }

}