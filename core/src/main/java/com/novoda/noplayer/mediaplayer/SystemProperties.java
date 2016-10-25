package com.novoda.noplayer.mediaplayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class uses reflection to call android.os.SystemProperties.get(String) since the class is hidden.
 */
class SystemProperties {

    private static final String SYSTEM_PROPERTIES_CLASS = "android.os.SystemProperties";
    private static final String SYSTEM_PROPERTIES_METHOD_GET = "get";

    private static final Object STATIC_CLASS_INSTANCE = null;

    String get(String key) throws MissingSystemPropertiesException {
        try {
            Class<?> systemProperties = Class.forName(SYSTEM_PROPERTIES_CLASS);
            Method getMethod = systemProperties.getMethod(SYSTEM_PROPERTIES_METHOD_GET, String.class);
            return (String) getMethod.invoke(STATIC_CLASS_INSTANCE, key);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MissingSystemPropertiesException(e);
        }
    }

    static class MissingSystemPropertiesException extends Exception {
        MissingSystemPropertiesException(Exception e) {
            super(e);
        }
    }
}
