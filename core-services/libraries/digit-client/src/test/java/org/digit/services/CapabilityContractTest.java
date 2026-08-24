package org.digit.services;

import tools.jackson.databind.ObjectMapper;
import org.digit.services.common.model.CanonicalRequest;
import org.digit.services.common.model.RequestInfo;
import org.digit.services.common.model.UserInfo;
import org.digit.services.filestore.model.DocumentCategory;
import org.digit.services.filestore.model.DownloadUrls;
import org.digit.services.registry.model.RegistryDataResponse;
import org.digit.services.registry.model.RegistrySchema;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Phase 3 shapes: odd response layouts and the envelope whose key order matters. */
class CapabilityContractTest {

    private final ObjectMapper mapper = DigitJson.mapper();

    @Test
    void canonicalRequest_serializesMetadataBeforePayload() throws Exception {
        // Registry resolves tenant and user by scanning the raw body for the first match, so a
        // payload field named tenantId or id must not be able to precede the metadata.
        CanonicalRequest<Map<String, Object>> request = CanonicalRequest.<Map<String, Object>>builder()
                .requestInfo(RequestInfo.builder()
                        .tenantId("TEST3")
                        .userInfo(UserInfo.builder().uuid("u-1").build())
                        .build())
                .data(Map.of("tenantId", "PAYLOAD_TENANT"))
                .build();

        String json = mapper.writeValueAsString(request);

        assertTrue(json.indexOf("\"RequestInfo\"") < json.indexOf("\"data\""), json);
        assertTrue(json.indexOf("\"tenantId\":\"TEST3\"") < json.indexOf("PAYLOAD_TENANT"), json);
    }

    @Test
    void userInfo_doesNotExposeABareIdKey() throws Exception {
        // "id" is one of the keys registry accepts as the acting user, so we must not emit it.
        String json = mapper.writeValueAsString(UserInfo.builder().uuid("u-1").userName("asha").build());
        assertFalse(json.contains("\"id\""), json);
        assertTrue(json.contains("\"uuid\":\"u-1\""), json);
    }

    @Test
    void downloadUrls_parsesArrayAndFlattenedEntries() throws Exception {
        // The service returns the pairs twice: once as an array, once flattened alongside it.
        String json = """
                {"fileStoreIds":[{"id":"f-1","url":"https://store/f-1"},{"id":"f-2","url":"https://store/f-2"}],
                 "f-1":"https://store/f-1",
                 "f-2":"https://store/f-2"}
                """;
        DownloadUrls urls = mapper.readValue(json, DownloadUrls.class);

        assertEquals(2, urls.getFileStoreIds().size());
        assertEquals(Map.of("f-1", "https://store/f-1", "f-2", "https://store/f-2"), urls.asMap());
        assertEquals(2, urls.getUrlsById().size());
    }

    @Test
    void documentCategory_readsItsOddKeyCasing() throws Exception {
        // ID and TenantId are capitalised, the size bounds are strings, and the audit key is singular.
        String json = """
                {"ID":7,"type":"IDENTITY","TenantId":"TEST3","code":"PAN",
                 "allowedFormats":["pdf","jpg"],"minSize":"1024","maxSize":"2097152",
                 "isSensitive":true,"description":"PAN card","isActive":true,"version":2,
                 "auditDetail":{"createdBy":"u-1","createdTime":1787141191641}}
                """;
        DocumentCategory category = mapper.readValue(json, DocumentCategory.class);

        assertEquals(7L, category.getId());
        assertEquals("TEST3", category.getTenantId());
        assertEquals("1024", category.getMinSize());
        assertEquals(List.of("pdf", "jpg"), category.getAllowedFormats());
        assertTrue(category.getIsSensitive());
        assertEquals(2L, category.getVersion());
        assertNotNull(category.getAuditDetails());
        assertEquals("u-1", category.getAuditDetails().getCreatedBy());
    }

    @Test
    void registryResponse_distinguishesQueuedWriteFromApplied() {
        RegistryDataResponse queued = RegistryDataResponse.builder().success(true).httpStatus(202).build();
        RegistryDataResponse applied = RegistryDataResponse.builder().success(true).httpStatus(201).build();

        // Both carry a null data; only the status tells them apart.
        assertTrue(queued.isQueued());
        assertFalse(applied.isQueued());
    }

    @Test
    void registryResponse_typesItsUntypedDataBlock() {
        RegistryDataResponse response = RegistryDataResponse.builder()
                .success(true)
                .data(List.of(Map.of("registryId", "REG-1", "version", 3),
                              Map.of("registryId", "REG-2", "version", 1)))
                .build();

        assertEquals(2, response.getRecords().size());
        assertEquals("REG-1", response.getRecords().get(0).getRegistryId());
        assertEquals(3, response.getRecords().get(0).getVersion());
        // A list still yields a single record for callers that expect one.
        assertEquals("REG-1", response.getRecord().getRegistryId());
    }

    @Test
    void registrySchema_readsCamelCaseExtensionBlocks() throws Exception {
        // Responses use xUnique; only requests use the hyphenated x-unique.
        String json = """
                {"schemaCode":"Trade.License","version":2,"definition":{"type":"object"},
                 "xUnique":[["licenceNumber"]],
                 "xIndexes":[{"name":"idx_licence","fieldPath":"licenceNumber","method":"btree"}],
                 "isLatest":true,"isActive":true}
                """;
        RegistrySchema schema = mapper.readValue(json, RegistrySchema.class);

        assertEquals("Trade.License", schema.getSchemaCode());
        assertEquals(List.of(List.of("licenceNumber")), schema.getXUnique());
        assertEquals("idx_licence", schema.getXIndexes().get(0).getName());
        assertTrue(schema.isLatest());
    }
}
