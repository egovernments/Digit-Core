package org.egov.common.utils;

import org.egov.common.exception.MethodNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.egov.common.constants.ServiceCommonConstants.SET_KEYWORD;

public class UUIDEnrichmentUtil {

    private UUIDEnrichmentUtil(){}

    /**
     * This method accepts an object of generic type and the field to be enriched.
     * If the parent object passed is an instance of List, each of the child objects
     * are enriched with random UUIDs. However, if the passed parent object is non-collection
     * type, the target field passed is enriched with UUID.
     *
     * @param obj
     * @param field
     * @param <T>
     */
    public static <T> void enrichRandomUuid(T obj, String field) {
        Class<?> objClass = getObjClass(obj);
        Method setIdMethod = getMethod(getSetterMethodName(field), objClass);

        if (obj instanceof List) {
            IntStream.range(0, ((List<?>) obj).size())
                    .forEach(i -> {
                        ReflectionUtils.invokeMethod(setIdMethod, ((List<?>) obj).get(i), UUID.randomUUID().toString());
                    });
        } else {
            ReflectionUtils.invokeMethod(setIdMethod, obj, UUID.randomUUID().toString());
        }
    }

    /**
     * This method accepts an object and returns the
     * class the parameter object belongs to
     * @param obj
     * @param <T>
     * @return
     */
    private static <T> Class<?> getObjClass(T obj) {
        if(obj instanceof List){
            return ((List<?>) obj).stream().findAny().get().getClass();
        }else{
            return obj.getClass();
        }
    }

    /**
     * This method returns the invokable method from the class
     * based on the methodName passed to it
     * @param methodName
     * @param clazz
     * @return
     */
    private static Method getMethod(String methodName, Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst().orElseThrow(() -> new MethodNotFoundException("Method " + methodName + " not found in class " + clazz.getName())); // Change this
    }

    /**
     * This method generates and returns the setter method name for the
     * field being passed to it
     * @param field
     * @return
     */
    private static String getSetterMethodName(String field) {
        return new StringBuilder(SET_KEYWORD)
                .append(field.substring(0, 1).toUpperCase())
                .append(field.substring(1))
                .toString();
    }
}
