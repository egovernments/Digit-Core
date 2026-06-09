package org.egov.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.egov.common.exception.InvalidTenantIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultiStateInstanceUtilTest {

    // -------------------------------------------------------------------------
    // Existing: getStateLevelTenant
    // -------------------------------------------------------------------------

    @Test
    public void testIfStateLevelTenantIdIsReturnedForCentralDeployment() {

        MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(2, true, 1);
        String InputTenantId = "in.statea.tenantx";
        String outputTenant = "in.statea";
        String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

        assertEquals(actualOutputTenantId, outputTenant);
    }

    @Test
    public void testIfSameTenantIdIsReturnedIfStateLengthIslargerThanActualTenantLength() {

        MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(3, true, 1);
        String InputTenantId = "in.statea.tenantx";
        String outputTenant = "in.statea.tenantx";
        String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

        assertEquals(actualOutputTenantId, outputTenant);
    }

    @Test
    public void testIfStateLevelTenantIdIsReturnedForIsolatedDeployment() {

        MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(2, false, 1);
        String InputTenantId = "in.statea.tenantx";
        String outputTenant = "in";

        String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

        assertEquals(actualOutputTenantId, outputTenant);
    }

    // -------------------------------------------------------------------------
    // getStateLevelTenant — truth table coverage (India/Punjab examples)
    // -------------------------------------------------------------------------

    @Test
    public void testGetStateLevelTenant_central_length1_singlePart() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("pb", util.getStateLevelTenant("pb"));
    }

    @Test
    public void testGetStateLevelTenant_central_length1_twoParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("pb", util.getStateLevelTenant("pb.amritsar"));
    }

    @Test
    public void testGetStateLevelTenant_central_length1_threeParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("pb", util.getStateLevelTenant("pb.amritsar.local1"));
    }

    @Test
    public void testGetStateLevelTenant_central_length2_singlePart() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertEquals("pb", util.getStateLevelTenant("pb"));
    }

    @Test
    public void testGetStateLevelTenant_central_length2_twoParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertEquals("pb.amritsar", util.getStateLevelTenant("pb.amritsar"));
    }

    @Test
    public void testGetStateLevelTenant_central_length2_threeParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertEquals("pb.amritsar", util.getStateLevelTenant("pb.amritsar.local1"));
    }

    @Test
    public void testGetStateLevelTenant_central_length2_fourParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertEquals("pb.amritsar", util.getStateLevelTenant("pb.amritsar.local1.mohali"));
    }

    @Test
    public void testGetStateLevelTenant_central_length3_threeParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(3, true, 1);
        assertEquals("pb.amritsar.local1", util.getStateLevelTenant("pb.amritsar.local1"));
    }

    @Test
    public void testGetStateLevelTenant_central_length3_fourParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(3, true, 1);
        assertEquals("pb.amritsar.local1", util.getStateLevelTenant("pb.amritsar.local1.mohali"));
    }

    @Test
    public void testGetStateLevelTenant_nonCentral_returnsFirstPart() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, false, 1);
        assertEquals("pb", util.getStateLevelTenant("pb.amritsar.local1"));
    }

    @Test
    public void testGetStateLevelTenant_nonCentral_singlePart() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        assertEquals("pb", util.getStateLevelTenant("pb"));
    }

    // -------------------------------------------------------------------------
    // isTenantIdStateLevel
    // -------------------------------------------------------------------------

    @Test
    public void testIsTenantIdStateLevel_central_length1_singlePart_isState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertTrue(util.isTenantIdStateLevel("pb"));
    }

    @Test
    public void testIsTenantIdStateLevel_central_length1_twoParts_isNotState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertFalse(util.isTenantIdStateLevel("pb.amritsar"));
    }

    @Test
    public void testIsTenantIdStateLevel_central_length1_threeParts_isNotState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertFalse(util.isTenantIdStateLevel("pb.amritsar.local1"));
    }

    @Test
    public void testIsTenantIdStateLevel_central_length2_singlePart_isState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertTrue(util.isTenantIdStateLevel("pb"));
    }

    @Test
    public void testIsTenantIdStateLevel_central_length2_twoParts_isState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertTrue(util.isTenantIdStateLevel("pb.amritsar"));
    }

    @Test
    public void testIsTenantIdStateLevel_central_length2_threeParts_isNotState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(2, true, 1);
        assertFalse(util.isTenantIdStateLevel("pb.amritsar.local1"));
    }

    @Test
    public void testIsTenantIdStateLevel_nonCentral_noDot_isState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        assertTrue(util.isTenantIdStateLevel("pb"));
    }

    @Test
    public void testIsTenantIdStateLevel_nonCentral_hasDot_isNotState() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        assertFalse(util.isTenantIdStateLevel("pb.amritsar"));
    }

    // -------------------------------------------------------------------------
    // replaceSchemaPlaceholder — central=true
    // -------------------------------------------------------------------------

    @Test
    public void testReplaceSchemaPlaceholder_central_position1_singlePartTenant() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb");
        assertEquals("select * from pb.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position1_twoParts() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar");
        assertEquals("select * from amritsar.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position1_threeParts() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar.local1");
        assertEquals("select * from amritsar.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position0_twoParts() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 0);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar");
        assertEquals("select * from pb.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position0_fourParts() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 0);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "in.pb.amritsar.local1");
        assertEquals("select * from in.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position2_twoPartsCapped() throws InvalidTenantIdException {
        // position=2 but only 2 parts -> capped to index 1
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar");
        assertEquals("select * from amritsar.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position2_threeParts() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar.local1");
        assertEquals("select * from local1.employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_central_position2_fourPartsCapped() throws InvalidTenantIdException {
        // position=2, 4 parts -> capped to index 2 (not 3)
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar.local1.mohali");
        assertEquals("select * from local1.employee", result);
    }

    // -------------------------------------------------------------------------
    // replaceSchemaPlaceholder — central=false
    // -------------------------------------------------------------------------

    @Test
    public void testReplaceSchemaPlaceholder_nonCentral_stripsSchemaPrefix() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb");
        assertEquals("select * from employee", result);
    }

    @Test
    public void testReplaceSchemaPlaceholder_nonCentral_multiPartTenant_stripsSchemaPrefix() throws InvalidTenantIdException {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        String result = util.replaceSchemaPlaceholder("select * from {schema}.employee", "pb.amritsar");
        assertEquals("select * from employee", result);
    }

    // -------------------------------------------------------------------------
    // getStateSpecificTopicName — central=true
    // -------------------------------------------------------------------------

    @Test
    public void testGetStateSpecificTopicName_central_position1_singlePart() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("pb-save-employee", util.getStateSpecificTopicName("pb", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position1_twoParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("amritsar-save-employee", util.getStateSpecificTopicName("pb.amritsar", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position1_threeParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 1);
        assertEquals("amritsar-save-employee", util.getStateSpecificTopicName("pb.amritsar.local1", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position0_twoParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 0);
        assertEquals("pb-save-employee", util.getStateSpecificTopicName("pb.amritsar", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position0_fourParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 0);
        assertEquals("in-save-employee", util.getStateSpecificTopicName("in.pb.amritsar", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position2_twoPartsCapped() {
        // position=2, only 2 parts -> capped to index 1
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        assertEquals("amritsar-save-employee", util.getStateSpecificTopicName("pb.amritsar", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position2_threeParts() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        assertEquals("local1-save-employee", util.getStateSpecificTopicName("pb.amritsar.local1", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_central_position2_fourPartsCapped() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, true, 2);
        assertEquals("local1-save-employee", util.getStateSpecificTopicName("pb.amritsar.local1.mohali", "save-employee"));
    }

    // -------------------------------------------------------------------------
    // getStateSpecificTopicName — central=false
    // -------------------------------------------------------------------------

    @Test
    public void testGetStateSpecificTopicName_nonCentral_topicUnchanged() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        assertEquals("save-employee", util.getStateSpecificTopicName("pb", "save-employee"));
    }

    @Test
    public void testGetStateSpecificTopicName_nonCentral_multiPartTenant_topicUnchanged() {
        MultiStateInstanceUtil util = new MultiStateInstanceUtil(1, false, 1);
        assertEquals("save-employee", util.getStateSpecificTopicName("pb.amritsar", "save-employee"));
    }
}
