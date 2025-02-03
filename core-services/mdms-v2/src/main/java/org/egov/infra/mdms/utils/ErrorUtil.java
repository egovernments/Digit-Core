package org.egov.infra.mdms.utils;

import org.egov.tracer.model.CustomException;

import java.util.Map;

public class ErrorUtil {

    private ErrorUtil(){}

    /**
     * This method throws custom exception for a map of exceptions
     * @param exceptions
     */
    public static void throwCustomExceptions(Map<String, String> exceptions) {
        if (!exceptions.isEmpty()) {
            throw new CustomException(exceptions);
        }
    }

}
