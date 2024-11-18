package org.egov.user.domain.service.utils;

import org.egov.tracer.model.CustomException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashUtil {
	public static String hashValue(String input) {
		if (input == null) return null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new CustomException("HASH_ERR","Error hashing with SHA-256");
		}
	}
}
