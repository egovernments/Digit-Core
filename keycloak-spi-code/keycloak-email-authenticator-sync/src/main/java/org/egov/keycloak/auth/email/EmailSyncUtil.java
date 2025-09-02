package org.egov.keycloak.auth.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import static org.egov.keycloak.auth.email.EmailSyncConstants.getEnv;

@JBossLog
public class EmailSyncUtil {

	private static final String NOTIFICATION_HOST = getEnv("NOTIFICATION_HOST", "http://localhost:8081");
	private static final String EMAIL_SEND_PATH = getEnv("NOTIFICATION_EMAIL_SEND_PATH", "/notification/email/send");

	public static void sendEmail(EmailRequest emailRequest) {
		if (NOTIFICATION_HOST == null || NOTIFICATION_HOST.isEmpty()) {
			log.error("Notification host is not configured. Set NOTIFICATION_HOST environment variable.");
			throw new IllegalStateException("Notification host not set");
		}

		if (EMAIL_SEND_PATH == null || EMAIL_SEND_PATH.isEmpty()) {
			log.error("Email send path is not configured. Set NOTIFICATION_EMAIL_SEND_PATH environment variable.");
			throw new IllegalStateException("Notification email send path not set");
		}

		// Ensure proper URL concatenation
		String url = NOTIFICATION_HOST.endsWith("/") ?
				NOTIFICATION_HOST.substring(0, NOTIFICATION_HOST.length() - 1) :
				NOTIFICATION_HOST;
		url += EMAIL_SEND_PATH.startsWith("/") ? EMAIL_SEND_PATH : "/" + EMAIL_SEND_PATH;

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			post.setHeader("X-Tenant-ID", emailRequest.getTenantId());

			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(emailRequest);
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

			client.execute(post, response -> {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					String body = response.getEntity() != null ?
							new String(response.getEntity().getContent().readAllBytes()) : "";
					log.errorf("Failed to send email via Notification API. Status: %d, Body: %s", status, body);
					throw new RuntimeException("Notification API call failed, status=" + status);
				}
				return null;
			});

			log.info("Email sent successfully via Notification API to: " + emailRequest.getEmailIds());

		} catch (Exception e) {
			log.error("Exception while sending email via Notification API", e);
			throw new RuntimeException(e);
		}
	}
}
