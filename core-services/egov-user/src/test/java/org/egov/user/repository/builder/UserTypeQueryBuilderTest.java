package org.egov.user.repository.builder;

import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.UserType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTypeQueryBuilderTest {

    private UserTypeQueryBuilder builder;

    @Before
    public void setUp() {
        builder = new UserTypeQueryBuilder();
    }

    @Test
    public void test_scalar_userName_generates_equality_predicate() {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .userName("emp_1")
                .type(UserType.EMPLOYEE)
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).contains("userdata.username = ?");
        assertThat(sql).doesNotContain("userdata.username IN");
        assertThat(params).contains("emp_1");
    }

    @Test
    public void test_userNames_list_generates_IN_clause_with_bind_params() {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .userNames(Arrays.asList("emp_1", "emp_2", "emp_3"))
                .type(UserType.EMPLOYEE)
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).contains("userdata.username IN ( ?, ?, ? )");
        assertThat(params).contains("emp_1", "emp_2", "emp_3");
    }

    @Test
    public void test_mobileNumbers_list_generates_IN_clause_with_bind_params() {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .mobileNumbers(Arrays.asList("9111234567", "9222345678"))
                .type(UserType.EMPLOYEE)
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).contains("userdata.mobilenumber IN ( ?, ? )");
        assertThat(params).contains("9111234567", "9222345678");
    }

    @Test
    public void test_scalar_and_list_can_coexist_in_SQL() {
        // (Precedence is enforced in UserService, not here — the builder happily emits both.)
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .userName("scalar_val")
                .userNames(Arrays.asList("list_val_1", "list_val_2"))
                .type(UserType.EMPLOYEE)
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).contains("userdata.username = ?");
        assertThat(sql).contains("userdata.username IN");
    }

    @Test
    public void test_list_only_criteria_still_produces_WHERE_clause() {
        // Guards against the early-return in addWhereClause() short-circuiting
        // when only the new list fields are populated.
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .userNames(Arrays.asList("emp_1"))
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).contains("WHERE");
        assertThat(sql).contains("userdata.username IN");
        assertThat(params).contains("emp_1");
    }

    @Test
    public void test_empty_list_criteria_do_not_emit_IN_clause() {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .tenantId("os.osun")
                .userName("emp_1")
                .userNames(new ArrayList<>())
                .mobileNumbers(new ArrayList<>())
                .type(UserType.EMPLOYEE)
                .build();
        List<Object> params = new ArrayList<>();

        String sql = builder.getQuery(criteria, params);

        assertThat(sql).doesNotContain("userdata.username IN");
        assertThat(sql).doesNotContain("userdata.mobilenumber IN");
    }
}
