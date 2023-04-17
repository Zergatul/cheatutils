package com.zergatul.cheatutils.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Object getDeclaredField(Object object, String name) {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object callMethod(Object object, String name, Class<?>[] parameterTypes, Object[] arguments) {
        try {
            Method method = object.getClass().getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(object, arguments);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}