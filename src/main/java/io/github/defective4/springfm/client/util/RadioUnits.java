package io.github.defective4.springfm.client.util;

public class RadioUnits {
    private RadioUnits() {
    }

    public static String toHzUnits(float freq) {
        float val = freq;
        char unit = 0;
        if (freq >= 1e9f) {
            unit = 'G';
            val /= 1e9f;
        } else if (freq >= 1e6f) {
            unit = 'M';
            val /= 1e6f;
        } else if (freq >= 1e3f) {
            unit = 'K';
            val /= 1e3f;
        }
        return val + " " + (unit == 0 ? "Hz" : unit + "Hz");
    }
}
