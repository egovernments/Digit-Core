package org.egov.keycloak.auth.email;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailSyncConstants {
	public String CODE = "emailCode";
	public String CODE_LENGTH = "length";
	public String CODE_TTL = "ttl";

	// User attribute keys for direct grant flows
	public String CODE_ATTR = "temp_email_code";
	public String CODE_TTL_ATTR = "temp_email_code_ttl";

	// Environment configurable defaults
	public final int DEFAULT_LENGTH = getEnvInt("EMAIL_CODE_LENGTH", 6);
	public final int DEFAULT_TTL = getEnvInt("EMAIL_CODE_TTL", 300);

	public static int getEnvInt(String key, int defaultValue) {
		String value = System.getenv(key);
		return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : defaultValue;
	}

	// Get environment variable or default value
	public static String getEnv(String name, String defaultValue) {
		String value = System.getenv(name);
		return (value != null && !value.isEmpty()) ? value : defaultValue;
	}

	public static boolean getEnvBool(String key, boolean defaultValue) {
		String value = System.getenv(key);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
	}
}
