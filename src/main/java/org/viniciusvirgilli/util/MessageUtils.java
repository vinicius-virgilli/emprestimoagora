package org.viniciusvirgilli.util;

import java.util.ResourceBundle;

public class MessageUtils {

    private MessageUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String BUNDLE_NAME = "ValidationMessages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getString(String key) {
        try {
            return new String(RESOURCE_BUNDLE.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            return '!' + key + '!';
        }
    }
}
