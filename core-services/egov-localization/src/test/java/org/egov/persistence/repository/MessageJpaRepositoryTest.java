package org.egov.persistence.repository;


import org.egov.TestConfiguration;
import org.egov.persistence.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(TestConfiguration.class)
public class MessageJpaRepositoryTest {

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    @Test
    @Sql(scripts = {"/sql/clearMessages.sql", "/sql/createMessages.sql"})
    public void shouldFetchMessagesForGivenTenantAndLocale() {
        final List<Message> actualMessages = messageJpaRepository
            .find("tenant1", "en_US").collect(Collectors.toList());

        assertEquals(2, actualMessages.size());
    }

    @Test
    @Sql(scripts = {"/sql/clearMessages.sql", "/sql/createMessages.sql"})
    public void shouldSaveMessages() {
        final String locale = "newLocale";
        final String tenant = "newTenant";
        final Message message1 = Message.builder().tenantId(tenant)
            .code("code1")
            .locale(locale)
            .message("New message1")
            .module("module")
            .createdBy(1L)
            .createdDate(new Date())
            .build();
        final Message message2 = Message.builder()
            .tenantId(tenant)
            .code("code2")
            .locale(locale)
            .message("New message2")
            .module("module")
            .createdBy(1L)
            .createdDate(new Date())
            .build();

        messageJpaRepository.saveAll(Arrays.asList(message1, message2));

        assertEquals(2, messageJpaRepository.find(tenant, locale).collect(Collectors.toList()).size());
    }
}
