package org.egov.user.domain.service.utils;

import org.egov.tracer.model.CustomException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashUtil {

	private static final String HASH_ALGORITHM = "SHA-512";

	// Private constructor to prevent instantiation
	private HashUtil() {
		throw new UnsupportedOperationException("HashUtil class cannot be instantiated");
	}

	public static String hashValue(String input) {
		if (input == null || input.isEmpty()) return null;
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new CustomException("HASH_ERR", "Error while hashing the value");
		}
	}
}