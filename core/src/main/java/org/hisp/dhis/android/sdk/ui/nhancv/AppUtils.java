package org.hisp.dhis.android.sdk.ui.nhancv;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.text.Normalizer;

/**
 * Created by nhancao on 4/5/17.
 */

public class AppUtils {
    private static final String TAG = AppUtils.class.getSimpleName();

    /**
     * Check is contain text
     *
     * @return true if "originalText" contain "search"
     */
    public static boolean isContainText(String search, String originalText) {
        return isContainText(search, originalText, false);
    }

    /**
     * Check is contain text
     */
    public static boolean isContainText(String search, String originalText, boolean caseSensitive) {
        if (search != null && !search.equalsIgnoreCase("")) {
            String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD)
                                              .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            if (!caseSensitive) normalizedText = normalizedText.toLowerCase();
            int start = normalizedText.indexOf((!caseSensitive) ? search.toLowerCase() : search);
            if (start < 0) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Highlight text
     *
     * @return CharSequence had been high lighted
     */
    public static CharSequence highlightText(String search, String originalText) {
        return highlightText(search, originalText, false, Color.WHITE);
    }

    public static CharSequence highlightText(String search, String originalText, int color) {
        return highlightText(search, originalText, false, color);
    }

    public static CharSequence highlightText(String search, String originalText, boolean caseSensitive, int color) {
        if (search != null && !search.equalsIgnoreCase("")) {
            String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD)
                                              .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            if (!caseSensitive) normalizedText = normalizedText.toLowerCase();
            int start = normalizedText.indexOf((!caseSensitive) ? search.toLowerCase() : search);
            if (start < 0) {
                return originalText;
            } else {
                Spannable highlighted = new SpannableString(originalText);
                while (start >= 0) {
                    int spanStart = Math.min(start, originalText.length());
                    int spanEnd = Math.min(start + search.length(), originalText.length());
                    highlighted.setSpan(new ForegroundColorSpan(color), spanStart, spanEnd,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = normalizedText.indexOf(search, spanEnd);
                }
                return highlighted;
            }
        }
        return originalText;
    }

}
