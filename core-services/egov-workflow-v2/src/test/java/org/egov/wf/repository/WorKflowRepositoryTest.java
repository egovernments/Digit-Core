//package org.egov.wf.repository;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.anyBoolean;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.egov.common.contract.request.RequestInfo;
//import org.egov.wf.repository.querybuilder.WorkflowQueryBuilder;
//import org.egov.wf.repository.rowmapper.WorkflowRowMapper;
//import org.egov.wf.util.WorkflowUtil;
//import org.egov.wf.web.models.ProcessInstance;
//import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.dao.DataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.ResultSetExtractor;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//@ContextConfiguration(classes = {WorKflowRepository.class})
//@ExtendWith(SpringExtension.class)
//class WorKflowRepositoryTest {
//    @MockBean
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private WorKflowRepository worKflowRepository;
//
//    @MockBean
//    private WorkflowQueryBuilder workflowQueryBuilder;
//
//    @MockBean
//    private WorkflowRowMapper workflowRowMapper;
//
//    @MockBean
//    private WorkflowUtil workflowUtil;
//
//    @Test
//    void testGetProcessInstances() throws DataAccessException {
//        when(workflowQueryBuilder.getProcessInstanceIds((ProcessInstanceSearchCriteria) any(), (List<Object>) any()))
//                .thenReturn("Process Instance Ids");
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (RowMapper<Object>) any()))
//                .thenReturn(new ArrayList<>());
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertTrue(worKflowRepository.getProcessInstances(processInstanceSearchCriteria).isEmpty());
//        verify(workflowQueryBuilder).getProcessInstanceIds((ProcessInstanceSearchCriteria) any(), (List<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (RowMapper<Object>) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetProcessInstances2() throws DataAccessException {
//        when(workflowQueryBuilder.getProcessInstanceSearchQueryById((List<String>) any(), (List<Object>) any()))
//                .thenReturn("42");
//        when(workflowQueryBuilder.getProcessInstanceIds((ProcessInstanceSearchCriteria) any(), (List<Object>) any()))
//                .thenReturn("Process Instance Ids");
//
//        ArrayList<Object> objectList = new ArrayList<>();
//        objectList.add("42");
//        ArrayList<Object> objectList1 = new ArrayList<>();
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (ResultSetExtractor<Object>) any()))
//                .thenReturn(objectList1);
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (RowMapper<Object>) any())).thenReturn(objectList);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        List<ProcessInstance> actualProcessInstances = worKflowRepository
//                .getProcessInstances(processInstanceSearchCriteria);
//        assertSame(objectList1, actualProcessInstances);
//        assertTrue(actualProcessInstances.isEmpty());
//        verify(workflowQueryBuilder).getProcessInstanceIds((ProcessInstanceSearchCriteria) any(), (List<Object>) any());
//        verify(workflowQueryBuilder).getProcessInstanceSearchQueryById((List<String>) any(), (List<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (ResultSetExtractor<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (RowMapper<Object>) any());
//        verify(workflowUtil, atLeast(1)).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetProcessInstancesForUserInbox() {
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertTrue(worKflowRepository.getProcessInstancesForUserInbox(processInstanceSearchCriteria).isEmpty());
//    }
//
//    @Test
//    void testGetProcessInstancesForUserInbox2() {
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = mock(ProcessInstanceSearchCriteria.class);
//        when(processInstanceSearchCriteria.getStatus()).thenReturn(new ArrayList<>());
//        when(processInstanceSearchCriteria.getTenantSpecifiStatus()).thenReturn(new ArrayList<>());
//        doNothing().when(processInstanceSearchCriteria).setAssignee((String) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessService((String) any());
//        doNothing().when(processInstanceSearchCriteria).setFromDate((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setHistory((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setIsAssignedToMeCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setLimit((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setModuleName((String) any());
//        doNothing().when(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setOffset((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantId((String) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setToDate((Long) any());
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertTrue(worKflowRepository.getProcessInstancesForUserInbox(processInstanceSearchCriteria).isEmpty());
//        verify(processInstanceSearchCriteria).getStatus();
//        verify(processInstanceSearchCriteria).getTenantSpecifiStatus();
//        verify(processInstanceSearchCriteria).setAssignee((String) any());
//        verify(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        verify(processInstanceSearchCriteria).setBusinessService((String) any());
//        verify(processInstanceSearchCriteria).setFromDate((Long) any());
//        verify(processInstanceSearchCriteria).setHistory((Boolean) any());
//        verify(processInstanceSearchCriteria).setIds((List<String>) any());
//        verify(processInstanceSearchCriteria).setIsAssignedToMeCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setLimit((Integer) any());
//        verify(processInstanceSearchCriteria).setModuleName((String) any());
//        verify(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        verify(processInstanceSearchCriteria).setOffset((Integer) any());
//        verify(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        verify(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        verify(processInstanceSearchCriteria).setTenantId((String) any());
//        verify(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setToDate((Long) any());
//    }
//
//    @Test
//    void testGetProcessInstancesForUserInbox3() throws DataAccessException {
//        when(workflowQueryBuilder.getInboxIdQuery((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any())).thenReturn("Inbox Id Query");
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (RowMapper<Object>) any()))
//                .thenReturn(new ArrayList<>());
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ArrayList<String> stringList = new ArrayList<>();
//        stringList.add("foo");
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = mock(ProcessInstanceSearchCriteria.class);
//        when(processInstanceSearchCriteria.getTenantId()).thenReturn("42");
//        when(processInstanceSearchCriteria.getStatus()).thenReturn(stringList);
//        when(processInstanceSearchCriteria.getTenantSpecifiStatus()).thenReturn(new ArrayList<>());
//        doNothing().when(processInstanceSearchCriteria).setAssignee((String) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessService((String) any());
//        doNothing().when(processInstanceSearchCriteria).setFromDate((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setHistory((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setIsAssignedToMeCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setLimit((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setModuleName((String) any());
//        doNothing().when(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setOffset((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantId((String) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setToDate((Long) any());
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertTrue(worKflowRepository.getProcessInstancesForUserInbox(processInstanceSearchCriteria).isEmpty());
//        verify(workflowQueryBuilder).getInboxIdQuery((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (RowMapper<Object>) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//        verify(processInstanceSearchCriteria).getTenantId();
//        verify(processInstanceSearchCriteria).getStatus();
//        verify(processInstanceSearchCriteria).setAssignee((String) any());
//        verify(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        verify(processInstanceSearchCriteria).setBusinessService((String) any());
//        verify(processInstanceSearchCriteria).setFromDate((Long) any());
//        verify(processInstanceSearchCriteria).setHistory((Boolean) any());
//        verify(processInstanceSearchCriteria).setIds((List<String>) any());
//        verify(processInstanceSearchCriteria, atLeast(1)).setIsAssignedToMeCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setLimit((Integer) any());
//        verify(processInstanceSearchCriteria).setModuleName((String) any());
//        verify(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        verify(processInstanceSearchCriteria).setOffset((Integer) any());
//        verify(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        verify(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        verify(processInstanceSearchCriteria).setTenantId((String) any());
//        verify(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setToDate((Long) any());
//    }
//
//    @Test
//    void testGetProcessInstancesForUserInbox4() throws DataAccessException {
//        when(workflowQueryBuilder.getProcessInstanceSearchQueryById((List<String>) any(), (List<Object>) any()))
//                .thenReturn("42");
//        when(workflowQueryBuilder.getInboxIdQuery((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any())).thenReturn("Inbox Id Query");
//
//        ArrayList<Object> objectList = new ArrayList<>();
//        objectList.add("42");
//        ArrayList<Object> objectList1 = new ArrayList<>();
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (ResultSetExtractor<Object>) any()))
//                .thenReturn(objectList1);
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (RowMapper<Object>) any())).thenReturn(objectList);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ArrayList<String> stringList = new ArrayList<>();
//        stringList.add("foo");
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = mock(ProcessInstanceSearchCriteria.class);
//        when(processInstanceSearchCriteria.getTenantId()).thenReturn("42");
//        when(processInstanceSearchCriteria.getStatus()).thenReturn(stringList);
//        when(processInstanceSearchCriteria.getTenantSpecifiStatus()).thenReturn(new ArrayList<>());
//        doNothing().when(processInstanceSearchCriteria).setAssignee((String) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setBusinessService((String) any());
//        doNothing().when(processInstanceSearchCriteria).setFromDate((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setHistory((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIds((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setIsAssignedToMeCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        doNothing().when(processInstanceSearchCriteria).setLimit((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setModuleName((String) any());
//        doNothing().when(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setOffset((Integer) any());
//        doNothing().when(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        doNothing().when(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantId((String) any());
//        doNothing().when(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        doNothing().when(processInstanceSearchCriteria).setToDate((Long) any());
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        List<ProcessInstance> actualProcessInstancesForUserInbox = worKflowRepository
//                .getProcessInstancesForUserInbox(processInstanceSearchCriteria);
//        assertSame(objectList1, actualProcessInstancesForUserInbox);
//        assertTrue(actualProcessInstancesForUserInbox.isEmpty());
//        verify(workflowQueryBuilder).getInboxIdQuery((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any());
//        verify(workflowQueryBuilder).getProcessInstanceSearchQueryById((List<String>) any(), (List<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (ResultSetExtractor<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (RowMapper<Object>) any());
//        verify(workflowUtil, atLeast(1)).replaceSchemaPlaceholder((String) any(), (String) any());
//        verify(processInstanceSearchCriteria, atLeast(1)).getTenantId();
//        verify(processInstanceSearchCriteria).getStatus();
//        verify(processInstanceSearchCriteria).setAssignee((String) any());
//        verify(processInstanceSearchCriteria).setBusinessIds((List<String>) any());
//        verify(processInstanceSearchCriteria).setBusinessService((String) any());
//        verify(processInstanceSearchCriteria).setFromDate((Long) any());
//        verify(processInstanceSearchCriteria).setHistory((Boolean) any());
//        verify(processInstanceSearchCriteria).setIds((List<String>) any());
//        verify(processInstanceSearchCriteria, atLeast(1)).setIsAssignedToMeCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsEscalatedCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setIsNearingSlaCount((Boolean) any());
//        verify(processInstanceSearchCriteria).setLimit((Integer) any());
//        verify(processInstanceSearchCriteria).setModuleName((String) any());
//        verify(processInstanceSearchCriteria).setMultipleAssignees((List<String>) any());
//        verify(processInstanceSearchCriteria).setOffset((Integer) any());
//        verify(processInstanceSearchCriteria).setSlotPercentageSlaLimit((Long) any());
//        verify(processInstanceSearchCriteria).setStatesToIgnore((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setStatusesIrrespectiveOfTenant((List<String>) any());
//        verify(processInstanceSearchCriteria).setTenantId((String) any());
//        verify(processInstanceSearchCriteria).setTenantSpecifiStatus((List<String>) any());
//        verify(processInstanceSearchCriteria).setToDate((Long) any());
//    }
//
//    @Test
//    void testGetProcessInstancesForUserInboxCount() throws DataAccessException {
//        when(workflowQueryBuilder.getInboxIdCount((ProcessInstanceSearchCriteria) any(), (ArrayList<Object>) any()))
//                .thenReturn("3");
//        when(jdbcTemplate.queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any())).thenReturn(1);
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertEquals(1,
//                worKflowRepository.getProcessInstancesForUserInboxCount(processInstanceSearchCriteria).intValue());
//        verify(workflowQueryBuilder).getInboxIdCount((ProcessInstanceSearchCriteria) any(), (ArrayList<Object>) any());
//        verify(jdbcTemplate).queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any());
//        assertTrue(processInstanceSearchCriteria.getIsAssignedToMeCount());
//    }
//
//    @Test
//    void testGetInboxCount() throws DataAccessException {
//        when(workflowQueryBuilder.getInboxCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any())).thenReturn("3");
//        when(jdbcTemplate.queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any())).thenReturn(1);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertEquals(1, worKflowRepository.getInboxCount(processInstanceSearchCriteria).intValue());
//        verify(workflowQueryBuilder).getInboxCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any());
//        verify(jdbcTemplate).queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetProcessInstancesCount() throws DataAccessException {
//        when(workflowQueryBuilder.getProcessInstanceCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                anyBoolean())).thenReturn("3");
//        when(jdbcTemplate.queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any())).thenReturn(1);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertEquals(1, worKflowRepository.getProcessInstancesCount(processInstanceSearchCriteria).intValue());
//        verify(workflowQueryBuilder).getProcessInstanceCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                anyBoolean());
//        verify(jdbcTemplate).queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetInboxStatusCount() throws DataAccessException {
//        when(workflowQueryBuilder.getInboxCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any())).thenReturn("3");
//        ArrayList<Map<String, Object>> mapList = new ArrayList<>();
//        when(jdbcTemplate.queryForList((String) any(), (Object[]) any())).thenReturn(mapList);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        List actualInboxStatusCount = worKflowRepository.getInboxStatusCount(processInstanceSearchCriteria);
//        assertSame(mapList, actualInboxStatusCount);
//        assertTrue(actualInboxStatusCount.isEmpty());
//        verify(workflowQueryBuilder).getInboxCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                (Boolean) any());
//        verify(jdbcTemplate).queryForList((String) any(), (Object[]) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetProcessInstancesStatusCount() throws DataAccessException {
//        when(workflowQueryBuilder.getProcessInstanceCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                anyBoolean())).thenReturn("3");
//        ArrayList<Map<String, Object>> mapList = new ArrayList<>();
//        when(jdbcTemplate.queryForList((String) any(), (Object[]) any())).thenReturn(mapList);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        List actualProcessInstancesStatusCount = worKflowRepository
//                .getProcessInstancesStatusCount(processInstanceSearchCriteria);
//        assertSame(mapList, actualProcessInstancesStatusCount);
//        assertTrue(actualProcessInstancesStatusCount.isEmpty());
//        verify(workflowQueryBuilder).getProcessInstanceCount((ProcessInstanceSearchCriteria) any(), (List<Object>) any(),
//                anyBoolean());
//        verify(jdbcTemplate).queryForList((String) any(), (Object[]) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testFetchEscalatedApplicationsBusinessIdsFromDb() throws DataAccessException {
//        when(workflowQueryBuilder.getAutoEscalatedApplicationsBusinessIdsQuery((ProcessInstanceSearchCriteria) any(),
//                (ArrayList<Object>) any())).thenReturn("Auto Escalated Applications Business Ids Query");
//        ArrayList<Object> objectList = new ArrayList<>();
//        when(jdbcTemplate.query((String) any(), (Object[]) any(), (RowMapper<Object>) any())).thenReturn(objectList);
//        when(workflowUtil.replaceSchemaPlaceholder((String) any(), (String) any()))
//                .thenReturn("Replace Schema Placeholder");
//        RequestInfo requestInfo = new RequestInfo();
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        List<String> actualFetchEscalatedApplicationsBusinessIdsFromDbResult = worKflowRepository
//                .fetchEscalatedApplicationsBusinessIdsFromDb(requestInfo, processInstanceSearchCriteria);
//        assertSame(objectList, actualFetchEscalatedApplicationsBusinessIdsFromDbResult);
//        assertTrue(actualFetchEscalatedApplicationsBusinessIdsFromDbResult.isEmpty());
//        verify(workflowQueryBuilder).getAutoEscalatedApplicationsBusinessIdsQuery((ProcessInstanceSearchCriteria) any(),
//                (ArrayList<Object>) any());
//        verify(jdbcTemplate).query((String) any(), (Object[]) any(), (RowMapper<Object>) any());
//        verify(workflowUtil).replaceSchemaPlaceholder((String) any(), (String) any());
//    }
//
//    @Test
//    void testGetEscalatedApplicationsCount() throws DataAccessException {
//        when(workflowQueryBuilder.getEscalatedApplicationsCount((RequestInfo) any(),
//                (ProcessInstanceSearchCriteria) any(), (ArrayList<Object>) any())).thenReturn("3");
//        when(jdbcTemplate.queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any())).thenReturn(1);
//        RequestInfo requestInfo = new RequestInfo();
//
//        ProcessInstanceSearchCriteria processInstanceSearchCriteria = new ProcessInstanceSearchCriteria();
//        processInstanceSearchCriteria.setAssignee("Assignee");
//        processInstanceSearchCriteria.setBusinessIds(new ArrayList<>());
//        processInstanceSearchCriteria.setBusinessService("Business Service");
//        processInstanceSearchCriteria.setFromDate(1L);
//        processInstanceSearchCriteria.setHistory(true);
//        processInstanceSearchCriteria.setIds(new ArrayList<>());
//        processInstanceSearchCriteria.setIsAssignedToMeCount(true);
//        processInstanceSearchCriteria.setIsEscalatedCount(true);
//        processInstanceSearchCriteria.setIsNearingSlaCount(true);
//        processInstanceSearchCriteria.setLimit(1);
//        processInstanceSearchCriteria.setModuleName("Module Name");
//        processInstanceSearchCriteria.setMultipleAssignees(new ArrayList<>());
//        processInstanceSearchCriteria.setOffset(2);
//        processInstanceSearchCriteria.setSlotPercentageSlaLimit(1L);
//        processInstanceSearchCriteria.setStatesToIgnore(new ArrayList<>());
//        processInstanceSearchCriteria.setStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setStatusesIrrespectiveOfTenant(new ArrayList<>());
//        processInstanceSearchCriteria.setTenantId("42");
//        processInstanceSearchCriteria.setTenantSpecifiStatus(new ArrayList<>());
//        processInstanceSearchCriteria.setToDate(1L);
//        assertEquals(1,
//                worKflowRepository.getEscalatedApplicationsCount(requestInfo, processInstanceSearchCriteria).intValue());
//        verify(workflowQueryBuilder).getEscalatedApplicationsCount((RequestInfo) any(),
//                (ProcessInstanceSearchCriteria) any(), (ArrayList<Object>) any());
//        verify(jdbcTemplate).queryForObject((String) any(), (Object[]) any(), (Class<Integer>) any());
//    }
//}
//
