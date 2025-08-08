package io.github.defective4.springfm.client.utils;

public class RadioUtils {
    private RadioUtils() {
    }

    public static String createFrequencyString(float freq) {
        float div;
        String unit;
        if (freq >= 1e9f) {
            div = 1e8f;
            unit = "GHz";
        } else if (freq >= 1e6f) {
            div = 1e5f;
            unit = "MHz";
        } else if (freq >= 1e3f) {
            div = 1e2f;
            unit = "KHz";
        } else {
            return (int) freq + " Hz";
        }
        int val = (int) (freq / div);
        float fval = val / 10f;
        if (fval % 1 == 0) return (int) fval + " " + unit;
        return fval + " " + unit;
    }
}
