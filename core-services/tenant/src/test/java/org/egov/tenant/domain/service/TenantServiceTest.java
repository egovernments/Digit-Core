package org.egov.tenant.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.egov.tenant.domain.exception.DuplicateTenantCodeException;
import org.egov.tenant.domain.exception.InvalidTenantDetailsException;
import org.egov.tenant.domain.model.Tenant;
import org.egov.tenant.persistence.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

@ExtendWith(MockitoExtension.class)
public class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private List<Tenant> tenants;

    private TenantService tenantService;

	@Value("${egov.services.tenantId}")
	private String tenantId;

	@Value("${egov.services.moduleName}")
	private String moduleName;

	@Value("${egov.services.masterDetailsName}")
	private String masterDetailsName;

	@Value("${egov.services.filterFieldName}")
	private String filterFieldName;


    @BeforeEach
    public void setUp() throws Exception {
        tenantService = new TenantService(tenantRepository);
    }

/*    @Test
    public void test_should_fetch_tenant() {

    	List<String> codeList = new ArrayList<String>();
    	codeList.add("default123");
    	RequestInfo requestInfo = RequestInfo.builder().apiId("emp").action("search").build();
    	when(mdmsRepository.getByCriteria("default", "tenant", "tenants", "code", codeList, requestInfo)).thenReturn(new JSONArray());
        List<Tenant> result = tenantService.getTenants(codeList, requestInfo);
       assertThat(result.size()==0);

    }*/

    @Test
    public void test_should_save_tenant() {
        Tenant tenant = mock(Tenant.class);
        when(tenant.getCode()).thenReturn("code");
        when(tenantRepository.isTenantPresent("code")).thenReturn(0L);
        when(tenantRepository.save(tenant)).thenReturn(tenant);

        Tenant result = tenantService.createTenant(tenant);

        assertThat(result).isEqualTo(tenant);
    }

    @Test
    public void test_should_throw_exception_when_tenant_is_invalid() {
        Tenant tenant = Tenant.builder().build();

        assertThrows(InvalidTenantDetailsException.class, () -> {
            tenantService.createTenant(tenant);
        });

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    public void test_should_throw_exception_when_duplicate_tenant_code_exists() {
        Tenant tenant = mock(Tenant.class);
        when(tenant.getCode()).thenReturn("code");
        when(tenantRepository.isTenantPresent("code")).thenReturn(1L);

        assertThrows(DuplicateTenantCodeException.class, () -> {
            tenantService.createTenant(tenant);
        });

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    public void test_should_update_tenant() {
        Tenant tenant = mock(Tenant.class);
        when(tenant.getCode()).thenReturn("code");
        when(tenantRepository.isTenantPresent("code")).thenReturn(1L);
        when(tenantRepository.update(tenant)).thenReturn(tenant);

        Tenant result = tenantService.updateTenant(tenant);

        assertThat(result).isEqualTo(tenant);
    }

    @Test
    public void test_should_throw_exception_when_tenant_isinvalid_inupdate() {
        Tenant tenant = Tenant.builder().build();

        assertThrows(InvalidTenantDetailsException.class, () -> {
            tenantService.updateTenant(tenant);
        });

        verify(tenantRepository, never()).update(any(Tenant.class));
    }
}
