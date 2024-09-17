package org.egov.web.notification.mail.service;

import javax.mail.internet.MimeMessage;

import org.egov.tracer.model.CustomException;
import org.egov.web.notification.mail.config.ApplicationConfiguration;
import org.egov.web.notification.mail.consumer.contract.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.egov.common.utils.MultiStateInstanceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@ConditionalOnProperty(value = "mail.enabled", havingValue = "true")
@Slf4j
public class ExternalEmailService implements EmailService {

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	public static final String EXCEPTION_MESSAGE = "Exception creating HTML email";
	private JavaMailSenderImpl mailSender;

    public ExternalEmailService(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public void sendEmail(Email email) {
		if(email.isHTML()) {
			sendHTMLEmail(email);
		} else {
			sendTextEmail(email);
		}
    }

	private void sendTextEmail(Email email) {
		final SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(email.getEmailTo().toArray(new String[0]));
		mailMessage.setSubject(email.getSubject());
		mailMessage.setText(email.getBody());
		mailSender.send(mailMessage);
	}

	private void sendHTMLEmail(Email email) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper;
		FileOutputStream fos = null;
		List<String> filePaths = new ArrayList<>();

		if (! CollectionUtils.isEmpty(email.getFileStoreId())){
			if (email.getTenantId() == null){
				throw new CustomException("TENANTID_NULL", "Tenant Id is null when filestore id is not null");
			}
		}


		try {
			helper = new MimeMessageHelper(message, true);
			helper.setTo(email.getEmailTo().toArray(new String[0]));
			helper.setSubject(email.getSubject());
			helper.setText(email.getBody(), true);

			if(email.getFileStoreId() != null) {
				String tenantId = centralInstanceUtil.getStateLevelTenant(email.getTenantId());
				for (Map.Entry<String, String> entry : email.getFileStoreId().entrySet()) {
					String uri = getUri(tenantId, entry.getKey());
					URL url = new URL(uri);
					URLConnection con = url.openConnection();
					UUID uuid = UUID.randomUUID();
					String fieldValue = uuid.toString();
					File download = new File(System.getProperty("java.io.tmpdir"), fieldValue);
					filePaths.add(download.getAbsolutePath());
					ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
					fos = new FileOutputStream(download);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					helper.addAttachment(entry.getValue(), download);
				}
			}

			mailSender.send(message);

		} catch (Exception e) {
			log.error(EXCEPTION_MESSAGE, e);
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					log.error(EXCEPTION_MESSAGE, ex);
				}
			}
			for(int i = 0; i < filePaths.size(); i++) {
				try {
					Files.deleteIfExists(Paths.get(filePaths.get(i)));
				} catch (IOException ex) {
					log.error(EXCEPTION_MESSAGE, ex);
				}
			}
			throw new CustomException(EXCEPTION_MESSAGE, EXCEPTION_MESSAGE + e);
		}
	}

	public String getUri(String tenantId, String entryKey){
		return config.getFilestoreHost() + config.getFilestorePath() + "?tenantId=" + tenantId + "&fileStoreId=" + entryKey;
	}

}
