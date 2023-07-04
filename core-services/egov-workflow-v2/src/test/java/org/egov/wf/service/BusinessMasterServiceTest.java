package org.egov.wf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.BusinessServiceRepository;
import org.egov.wf.validator.BusinessServiceValidator;
import org.egov.wf.web.models.BusinessService;
import org.egov.wf.web.models.BusinessServiceRequest;
import org.egov.wf.web.models.BusinessServiceSearchCriteria;
import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {BusinessMasterService.class})
@ExtendWith(SpringExtension.class)
class BusinessMasterServiceTest {
    @Autowired
    private BusinessMasterService businessMasterService;

    @MockBean
    private BusinessServiceRepository businessServiceRepository;

    @MockBean
    private BusinessServiceValidator businessServiceValidator;

    @MockBean
    private CacheManager cacheManager;

    @MockBean
    private EnrichmentService enrichmentService;

    @MockBean
    private MDMSService mDMSService;

    @MockBean
    private Producer producer;

    @MockBean
    private WorkflowConfig workflowConfig;

    @Test
    void testCreate4() {
        doNothing().when(businessServiceValidator).validateCreateRequest((BusinessServiceRequest) any());
        doNothing().when(producer).push((String) any(), (String) any(), (Object) any());
        when(workflowConfig.getSaveBusinessServiceTopic()).thenReturn("Save Business Service Topic");
        doNothing().when(enrichmentService).enrichCreateBusinessService((BusinessServiceRequest) any());
        when(cacheManager.getCache((String) any())).thenReturn(new ConcurrentMapCache("Name"));

        BusinessServiceRequest businessServiceRequest = new BusinessServiceRequest();
        businessServiceRequest.addBusinessServiceItem(new BusinessService());
        assertEquals(1, businessMasterService.create(businessServiceRequest).size());
        verify(businessServiceValidator).validateCreateRequest((BusinessServiceRequest) any());
        verify(producer).push((String) any(), (String) any(), (Object) any());
        verify(workflowConfig).getSaveBusinessServiceTopic();
        verify(enrichmentService).enrichCreateBusinessService((BusinessServiceRequest) any());
        verify(cacheManager, atLeast(1)).getCache((String) any());
    }
    @Test
    void testSearch() {
        doNothing().when(enrichmentService).enrichTenantIdForStateLevel((String) any(), (List<BusinessService>) any());
        ArrayList<BusinessService> businessServiceList = new ArrayList<>();
        when(businessServiceRepository.getBusinessServices((BusinessServiceSearchCriteria) any()))
                .thenReturn(businessServiceList);
        List<BusinessService> actualSearchResult = businessMasterService.search(new BusinessServiceSearchCriteria());
        assertSame(businessServiceList, actualSearchResult);
        assertTrue(actualSearchResult.isEmpty());
        verify(enrichmentService).enrichTenantIdForStateLevel((String) any(), (List<BusinessService>) any());
        verify(businessServiceRepository).getBusinessServices((BusinessServiceSearchCriteria) any());
    }

    @Test
    void testUpdate4() {
        doNothing().when(producer).push((String) any(), (String) any(), (Object) any());
        when(workflowConfig.getUpdateBusinessServiceTopic()).thenReturn("2020-03-01");
        doNothing().when(enrichmentService).enrichUpdateBusinessService((BusinessServiceRequest) any());
        when(cacheManager.getCache((String) any())).thenReturn(new ConcurrentMapCache("Name"));

        BusinessServiceRequest businessServiceRequest = new BusinessServiceRequest();
        businessServiceRequest.addBusinessServiceItem(new BusinessService());
        assertEquals(1, businessMasterService.update(businessServiceRequest).size());
        verify(producer).push((String) any(), (String) any(), (Object) any());
        verify(workflowConfig).getUpdateBusinessServiceTopic();
        verify(enrichmentService).enrichUpdateBusinessService((BusinessServiceRequest) any());
        verify(cacheManager, atLeast(1)).getCache((String) any());
    }

    @Test
    void testGetMaxBusinessServiceSla2() {
        doNothing().when(enrichmentService).enrichTenantIdForStateLevel((String) any(), (List<BusinessService>) any());

        ArrayList<BusinessService> businessServiceList = new ArrayList<>();
        businessServiceList.add(new BusinessService());
        when(businessServiceRepository.getBusinessServices((BusinessServiceSearchCriteria) any()))
                .thenReturn(businessServiceList);

        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
        processInstanceSearchCriteria.setAssignee("Assignee");
        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
        processInstanceSearchCriteria.setBusinessService("Business Service");
        processInstanceSearchCriteria.setFromDate(1L);
        processInstanceSearchCriteria.setHistory(true);
        processInstanceSearchCriteria.setIds(new ArrayList<>());
        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
        processInstanceSearchCriteria.setIsEscalatedCount(true);
        processInstanceSearchCriteria.setIsNearingSlaCount(true);
        processInstanceSearchCriteria.setLimit(1);
        processInstanceSearchCriteria.setModuleName("Module Name");
        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
        processInstanceSearchCriteria.setOffset(2);
        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
        processInstanceSearchCriteria.setStatus(new ArrayList<>());
        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
        processInstanceSearchCriteria.setTenantId("42");
        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
        processInstanceSearchCriteria.setToDate(1L);
        assertNull(businessMasterService.getMaxBusinessServiceSla(processInstanceSearchCriteria));
        verify(enrichmentService).enrichTenantIdForStateLevel((String) any(), (List<BusinessService>) any());
        verify(businessServiceRepository).getBusinessServices((BusinessServiceSearchCriteria) any());
    }

}

