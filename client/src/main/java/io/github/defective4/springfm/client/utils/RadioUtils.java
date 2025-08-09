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

    public static int parseFrequencyString(String str) {
        if (str.isEmpty()) throw new IllegalArgumentException("Frequency is empty");
        char modifier = str.charAt(str.length() - 1);
        if (Character.isDigit(modifier)) return Integer.parseInt(str);
        str = str.substring(0, str.length() - 1);
        int multiplier = switch (Character.toLowerCase(modifier)) {
            case 'k' -> 1000;
            case 'm' -> 1000000;
            case 'g' -> 1000000000;
            default -> throw new IllegalArgumentException("Invalid modifier '" + modifier + "'");
        };
        return (int) (Double.parseDouble(str) * multiplier);
    }
}
