package org.egov.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MultiStateInstanceUtilTest {

	@Test
	void testIfStateLevelTenantIdIsReturnedForCentralDeployment(){

		MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(2, true, 1);
		String InputTenantId = "in.statea.tenantx";
		String outputTenant = "in.statea";
		String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

		assertEquals(outputTenant, actualOutputTenantId);
	}

	@Test
	void testIfSameTenantIdIsReturnedIfStateLengthIslargerThanActualTenantLength(){

		MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(3, true, 1);
		String InputTenantId = "in.statea.tenantx";
		String outputTenant = "in.statea.tenantx";
		String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

		assertEquals(outputTenant, actualOutputTenantId);
	}

	@Test
	void testIfStateLevelTenantIdIsReturnedForIsolatedDeployment(){

		MultiStateInstanceUtil centralUtil = new MultiStateInstanceUtil(2, false, 1);
		String InputTenantId = "in.statea.tenantx";
		String outputTenant = "in";

		String actualOutputTenantId = centralUtil.getStateLevelTenant(InputTenantId);

		assertEquals(outputTenant, actualOutputTenantId);
	}

}
