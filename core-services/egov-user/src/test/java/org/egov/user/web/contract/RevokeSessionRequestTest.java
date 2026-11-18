package org.egov.user.web.contract;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Parses real JSON to check "userUuid" accepts both a string and a list. */
public class RevokeSessionRequestTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void userUuid_as_string() throws Exception {
        RevokeSessionRequest r = parse("{\"tenantId\":\"ko\",\"userUuid\":\"u1\"}");

        assertEquals("u1", r.getUserUuid());
        assertEquals(Collections.singletonList("u1"), r.getUserUuidList());
        assertTrue(isValid(r));
    }

    @Test
    public void userUuid_as_list_with_duplicates_removed() throws Exception {
        RevokeSessionRequest r = parse("{\"tenantId\":\"ko\",\"userUuid\":[\"u1\",\"u2\",\"u1\"]}");

        assertNull(r.getUserUuid());
        assertEquals(Arrays.asList("u1", "u2"), r.getUserUuidList());
        assertTrue(isValid(r));
    }

    @Test(expected = JsonMappingException.class)
    public void userUuid_as_number_is_rejected() throws Exception {
        parse("{\"tenantId\":\"ko\",\"userUuid\":123}");
    }

    @Test(expected = JsonMappingException.class)
    public void userUuid_list_with_non_string_entry_is_rejected() throws Exception {
        parse("{\"tenantId\":\"ko\",\"userUuid\":[\"u1\",5]}");
    }

    @Test
    public void missing_empty_or_blank_userUuid_is_invalid() throws Exception {
        assertFalse(isValid(parse("{\"tenantId\":\"ko\"}")));
        assertFalse(isValid(parse("{\"tenantId\":\"ko\",\"userUuid\":\" \"}")));
        assertFalse(isValid(parse("{\"tenantId\":\"ko\",\"userUuid\":[]}")));
        assertFalse(isValid(parse("{\"tenantId\":\"ko\",\"userUuid\":[\"u1\",\" \"]}")));
        assertFalse(isValid(parse("{\"tenantId\":\"ko\",\"userUuid\":[\"u1\",null]}")));
    }

    private static RevokeSessionRequest parse(String json) throws Exception {
        return MAPPER.readValue(json, RevokeSessionRequest.class);
    }

    private static boolean isValid(RevokeSessionRequest r) {
        return VALIDATOR.validate(r).isEmpty();
    }
}
