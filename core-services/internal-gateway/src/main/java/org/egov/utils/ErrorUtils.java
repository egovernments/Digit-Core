package org.egov.utils;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ErrorUtils {

	private static final ThreadLocal<ObjectMapper> om = new ThreadLocal<ObjectMapper>() {
		@Override
		protected ObjectMapper initialValue() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return objectMapper;
		}
	};

	public static ObjectMapper getObjectMapper() {
		return om.get();
	}

	public static HashMap<String, Object> getErrorInfoObject(String code, String message, String description) {
		HashMap<String, Object> error = new HashMap<String, Object>();
		error.put("code", "INTERNAL_GATEWAY_ERROR");
		error.put("message", code + " : " + message);
		error.put("description", description);
		return error;
	}

	public static String getObjectJSONString(Object obj) throws JsonProcessingException {
		return om.get().writeValueAsString(obj);
	}

	public static void setCustomException(HttpStatus status, String message) {
		throw new CustomException(message, status, message);
	}
}
