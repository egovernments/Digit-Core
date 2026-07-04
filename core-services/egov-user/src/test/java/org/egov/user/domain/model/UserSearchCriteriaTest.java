package org.egov.user.domain.model;

import org.egov.user.domain.exception.InvalidUserSearchCriteriaException;
import org.junit.Test;

import java.util.Arrays;

public class UserSearchCriteriaTest {

    @Test
    public void test_should_not_throw_exception_when_search_criteria_is_valid() {
        final UserSearchCriteria searchCriteria = UserSearchCriteria.builder()
                .tenantId("tenantId")
                .userName("greenfish424")
                .build();

        searchCriteria.validate(true);
    }

    @Test(expected = InvalidUserSearchCriteriaException.class)
    public void test_should_throw_exception_when_tenant_id_is_not_present() {
        final UserSearchCriteria searchCriteria = UserSearchCriteria.builder()
                .tenantId(null)
                .build();

        searchCriteria.validate(true);
    }

    // --- Bulk criteria (userNames / mobileNumbers) ---

    @Test
    public void test_userNames_list_alone_passes_validation() {
        final UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .userNames(Arrays.asList("emp_1", "emp_2", "emp_3"))
                .build();

        criteria.validate(true);
    }

    @Test
    public void test_mobileNumbers_list_alone_passes_validation() {
        final UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .mobileNumbers(Arrays.asList("9111234567", "9222345678"))
                .build();

        criteria.validate(true);
    }

    @Test(expected = InvalidUserSearchCriteriaException.class)
    public void test_userNames_list_without_tenant_throws() {
        final UserSearchCriteria criteria = UserSearchCriteria.builder()
                .userNames(Arrays.asList("emp_1", "emp_2"))
                .build();

        criteria.validate(true);
    }

    @Test(expected = InvalidUserSearchCriteriaException.class)
    public void test_mobileNumbers_list_without_tenant_throws() {
        final UserSearchCriteria criteria = UserSearchCriteria.builder()
                .mobileNumbers(Arrays.asList("9111234567"))
                .build();

        criteria.validate(false);
    }

    @Test(expected = InvalidUserSearchCriteriaException.class)
    public void test_empty_criteria_still_throws_for_external_call() {
        final UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .build();

        criteria.validate(false);
    }
}