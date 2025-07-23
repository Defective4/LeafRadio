package io.github.defective4.springfm.client.util;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class FontUtils {
    private FontUtils() {
    }

    public static Font deriveFont(Font font, int size, int style) {
        FontData[] data = font.getFontData();
        for (FontData d : data) {
            d.setHeight(size);
            if (style != -1) d.setStyle(style);
        }
        return new Font(font.getDevice(), data);
    }
}
