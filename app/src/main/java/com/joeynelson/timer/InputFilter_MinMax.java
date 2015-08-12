package com.joeynelson.timer;

/**
 * Created by joey on 8/13/2015.
 *
 * reference: http://stackoverflow.com/questions/14212518/is-there-any-way-to-define-a-min-and-max-value-for-edittext-in-android
 *
 *
 * This code doesn't acutally work properly, needs to be edited still if it's going to be used in ControlsFragment
 */

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilter_MinMax implements InputFilter {

    private int min, max;

    public InputFilter_MinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilter_MinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}