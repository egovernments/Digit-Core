package org.digit.util;

import org.digit.services.billing.model.Demand;
import org.digit.services.billing.model.DemandCreate;
import org.digit.services.billing.model.DemandStatus;
import org.digit.services.common.model.BulkFailure;
import org.digit.services.employee.model.CreateEmployeeRequest;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryRecord;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mapper contract, one test per setting, each stating the reason the setting exists.
 *
 * <p>Because unknown response fields are tolerated, these value assertions are the only thing that
 * catches a field quietly ceasing to populate — so they assert parsed values, never just non-null.
 */
class DigitJsonTest {

    private final JsonMapper mapper = DigitJson.mapper();

    // ── settings we set deliberately ─────────────────────────────────────────

    @Test
    void ignoresUnknownResponseFields() {
        // A service adding a field must not break a consumer built against an older SDK.
        Demand demand = mapper.readValue(
                "{\"consumerCode\":\"C-1\",\"somethingAddedLaterByTheService\":\"whatever\"}", Demand.class);
        assertEquals("C-1", demand.getConsumerCode());
    }

    @Test
    void readsUnknownEnumValueAsNull() {
        // Unknown enum *values* throw even when unknown properties are ignored, so this needs its
        // own setting — which moved to EnumFeature in Jackson 3.
        assertNull(mapper.readValue("{\"status\":\"SOME_NEW_STATUS\"}", Demand.class).getStatus());
    }

    @Test
    void enumsStillGoOutAsTheirConstantNames() {
        // Jackson 3 enables WRITE_ENUMS_USING_TO_STRING; harmless only because no enum here
        // overrides toString(), so name() and toString() coincide. Pinned in case one ever does.
        assertTrue(mapper.writeValueAsString(DemandCreate.builder().status(DemandStatus.PARTIALLY_PAID).build())
                .contains("\"status\":\"PARTIALLY_PAID\""));
    }

    @Test
    void omitsNullsFromRequestBodies() {
        // Several services parse writes with a strict mapper that rejects unknown keys.
        String json = mapper.writeValueAsString(DemandCreate.builder().consumerCode("C-1").build());
        assertEquals("{\"consumerCode\":\"C-1\"}", json);
    }

    @Test
    void explicitNullForAPrimitiveIsNotFatal() {
        // R1: Jackson 3 turns FAIL_ON_NULL_FOR_PRIMITIVES on, which would make an explicit null
        // throw where 2.x left 0/false — contradicting the contract that a service-side change
        // must never fail the call. 93 primitive fields across 43 models depend on this.
        Demand demand = mapper.readValue(
                "{\"consumerCode\":\"C-1\",\"periodFrom\":null,\"isDemandPaid\":null,\"version\":null}", Demand.class);
        assertEquals(0L, demand.getPeriodFrom());
        assertFalse(demand.isDemandPaid());
        assertEquals(0, demand.getVersion());
    }

    @Test
    void explicitNullInARecordComponentIsNotFatal() {
        // Records are always creator-based, so this takes the canonical-constructor path rather
        // than setters. A null index in a 207 body would otherwise abort the whole bulk result.
        assertEquals(0, mapper.readValue("{\"index\":null,\"errors\":[]}", BulkFailure.class).index());
    }

    @Test
    void trailingTokensDoNotBreakParsing() {
        // R2: Jackson 3 turns FAIL_ON_TRAILING_TOKENS on. JwtTokenUtil parses a base64url-decoded
        // segment whose padding slack can leave trailing bytes, so a token that works today would
        // start failing.
        assertDoesNotThrow(() -> mapper.readValue("{\"consumerCode\":\"C-1\"} {}", Demand.class));
    }

    // ── contracts that must survive the flips we accept ──────────────────────

    @Test
    void quotedMoneyKeepsItsScaleThroughTheTreeModel() {
        // R6: billing serializes money as quoted strings, and BillingClient reads every response as
        // JsonNode before convertValue. BigDecimal.equals compares scale, so scale is the assertion.
        JsonNode tree = mapper.readTree("{\"totalAmount\":\"400.00\",\"consumerCode\":\"C-1\"}");
        Demand demand = mapper.convertValue(tree, Demand.class);
        assertEquals(new BigDecimal("400.00"), demand.getTotalAmount());
    }

    @Test
    void unquotedDecimalsLoseScaleThroughTheDoublePath() {
        // Floats are not read as BigDecimal by default (in either Jackson version), so an unquoted
        // 400.00 becomes a double and its scale is gone before BigDecimal ever sees it. The value
        // survives, the scale does not — which is exactly why billing quotes every money field, and
        // why the assertion above is the one that matters.
        //
        // If a service ever starts sending unquoted money, the fix is
        // USE_BIG_DECIMAL_FOR_FLOATS on the mapper, not a change here.
        BigDecimal parsed = mapper.convertValue(mapper.readTree("{\"totalAmount\":400.00}"), Demand.class)
                .getTotalAmount();
        assertEquals(0, new BigDecimal("400.00").compareTo(parsed), "value must survive");
        assertNotEquals(new BigDecimal("400.00"), parsed, "but scale does not");
    }

    @Test
    void lombokAllArgsIsNotPromotedToACreator() {
        // Jackson 3 turns DETECT_PARAMETER_NAMES on and the parent compiles with -parameters. Every
        // @Builder class here also has @NoArgsConstructor, which must keep winning — otherwise
        // deserialization leaves the setter path and field-level @JsonProperty stops applying.
        Demand demand = mapper.readValue("{\"isDemandPaid\":true,\"consumerCode\":\"C-1\"}", Demand.class);
        assertTrue(demand.isDemandPaid());
        assertEquals("C-1", demand.getConsumerCode());
    }

    @Test
    void instantsAreReadFromIso() {
        // Registry is the one service using java.time.Instant rather than epoch millis.
        assertEquals(Instant.parse("2026-08-19T10:00:00Z"),
                mapper.readValue("{\"createdTime\":\"2026-08-19T10:00:00Z\"}", RegistryRecord.class)
                        .getCreatedTime());
    }

    // ── behaviour adopted from Jackson 3 ─────────────────────────────────────

    @Test
    void offsetDateTimeGoesOutAsIso() {
        // R4: under 2.x this went out as a decimal epoch, which the employee service tolerated only
        // because Jackson accepts a number token — and which discarded the offset. ISO is what that
        // service both emits and expects.
        String json = mapper.writeValueAsString(CreateEmployeeRequest.builder()
                .dateOfAppointment(OffsetDateTime.parse("2026-08-19T10:00:00Z")).build());
        assertTrue(json.contains("\"dateOfAppointment\":\"2026-08-19T10:00:00Z\""), json);
    }

    @Test
    void outboundKeyOrderIsUnchangedByJackson3() {
        // Jackson 3 enables SORT_PROPERTIES_ALPHABETICALLY, which looked like a risk to any service
        // that scans a raw body for its first match. It is not, for a subtler reason than explicit
        // @JsonPropertyOrder: SORT_CREATOR_PROPERTIES_FIRST is also on, and Lombok's
        // @AllArgsConstructor is detected as a creator now that the build passes -parameters. Every
        // field is therefore a creator property and keeps its declared position, leaving alphabetical
        // sorting nothing to reorder.
        //
        // Verified by isolation: disabling SORT_CREATOR_PROPERTIES_FIRST alone flips this class to
        // {"data":…,"version":…}, whereas disabling SORT_PROPERTIES_ALPHABETICALLY changes nothing.
        //
        // RegistryData declares version before data, so declaration and alphabetical order disagree —
        // which is what makes it a usable probe.
        String json = mapper.writeValueAsString(
                RegistryData.builder().version(2).data(mapper.createObjectNode()).build());
        assertTrue(json.indexOf("\"version\"") < json.indexOf("\"data\""), json);
    }

}
