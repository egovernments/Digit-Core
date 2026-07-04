package org.egov.user.web.contract;

import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.UserType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserSearchRequestTest {

    @Test
    public void test_to_domain() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setId(ids);
        userSearchRequest.setUserName("userName");
        userSearchRequest.setName("name");
        userSearchRequest.setMobileNumber("mobileNumber");
        userSearchRequest.setAadhaarNumber("aadhaarNumber");
        userSearchRequest.setEmailId("emailId");
        userSearchRequest.setPan("pan");
        userSearchRequest.setFuzzyLogic(false);
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType("CITIZEN");

        UserSearchCriteria userSearch = userSearchRequest.toDomain();

        assertThat(userSearch.getId()).isEqualTo(ids);
        assertThat(userSearch.getUserName()).isEqualTo("userName");
        assertThat(userSearch.getName()).isEqualTo("name");
        assertThat(userSearch.getMobileNumber()).isEqualTo("mobileNumber");
        assertThat(userSearch.getEmailId()).isEqualTo("emailId");
        assertThat(userSearch.isFuzzyLogic()).isFalse();
        assertThat(userSearch.getActive()).isTrue();
        assertThat(userSearch.getLimit()).isEqualTo(0);
        assertThat(userSearch.getOffset()).isEqualTo(0);
        assertThat(userSearch.getSort()).isEqualTo(Collections.singletonList("name"));
        assertThat(userSearch.getType()).isEqualTo(UserType.CITIZEN);
    }

    @Test
    public void test_to_domain_carries_bulk_list_fields() {
        List<String> userNames = Arrays.asList("emp_1", "emp_2", "emp_3");
        List<String> mobileNumbers = Arrays.asList("9111234567", "9222345678");

        UserSearchRequest request = new UserSearchRequest();
        request.setTenantId("os.osun");
        request.setUserNames(userNames);
        request.setMobileNumbers(mobileNumbers);
        request.setUserType("EMPLOYEE");

        UserSearchCriteria criteria = request.toDomain();

        assertThat(criteria.getUserNames()).containsExactlyElementsOf(userNames);
        assertThat(criteria.getMobileNumbers()).containsExactlyElementsOf(mobileNumbers);
        assertThat(criteria.getUserName()).isNull();
        assertThat(criteria.getMobileNumber()).isNull();
    }

    @Test
    public void test_to_domain_null_list_fields_stay_null() {
        UserSearchRequest request = new UserSearchRequest();
        request.setTenantId("os.osun");
        request.setUserName("scalar_only");
        request.setUserType("EMPLOYEE");

        UserSearchCriteria criteria = request.toDomain();

        assertThat(criteria.getUserNames()).isNull();
        assertThat(criteria.getMobileNumbers()).isNull();
        assertThat(criteria.getUserName()).isEqualTo("scalar_only");
    }
}