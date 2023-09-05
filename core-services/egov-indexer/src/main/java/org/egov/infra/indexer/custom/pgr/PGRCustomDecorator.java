package org.egov.infra.indexer.custom.pgr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.indexer.service.ServiceRequestRepository;
import org.egov.infra.indexer.util.IndexerConstants;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PGRCustomDecorator {
	
	@Autowired
	private IndexerUtils indexerUtils;
	
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;
	
	@Autowired
	private RestTemplate restTemplate;

	@Value("${egov.statelevel.tenantId}")
	private  String stateLevelTenantId ;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	/**
	 * Builds a custom object for PGR that is common for core index and legacy index,
	 * 
	 * @param serviceResponse
	 * @return
	 */
	public PGRIndexObject dataTransformationForPGR(ServiceResponse serviceResponse) {
		PGRIndexObject pgrIndexObject = new PGRIndexObject();
		ObjectMapper mapper = indexerUtils.getObjectMapper();
		List<ServiceIndexObject> serviceIndexObjects = new ArrayList<>();
		for(int i = 0; i < serviceResponse.getServices().size(); i++) {
			ServiceIndexObject object = new ServiceIndexObject();
			object = mapper.convertValue(serviceResponse.getServices().get(i), ServiceIndexObject.class);
			object.setActionHistory(serviceResponse.getActionHistory().get(i));
			for(ActionInfo action: serviceResponse.getActionHistory().get(i).getActions()) {
				if(!StringUtils.isEmpty(action.getBy())) {
					if(action.getBy().contains("Grievance Routing Officer") || action.getBy().contains("Department Grievance Routing Officer")) {
						object.setGro(action.getBy().split(":")[0]);
						if(!StringUtils.isEmpty(action.getAssignee())) {
							object.setAssignee(action.getAssignee());
						}
						break;
					}else if(action.getBy().contains("Employee")) {
						object.setAssignee(action.getBy().split(":")[0]);
					}
				}
			}
			object.setDepartment(getDepartment(serviceResponse.getServices().get(i)));
			object.setComplaintCategory(indexerUtils.splitCamelCase(serviceResponse.getServices().get(i).getServiceCode()));
			serviceIndexObjects.add(object);
		}
		pgrIndexObject.setServiceRequests(serviceIndexObjects);
		return pgrIndexObject;
	}
	
	/**
	 * Department is fetched from MDMS
	 * 
	 * @param service
	 * @return
	 */
	public String getDepartment(Service service) {
		StringBuilder uri = new StringBuilder();
		MdmsCriteriaReq request = prepareMdMsRequestForDept(uri, stateLevelTenantId, service.getServiceCode(), new RequestInfo());
		try {
//			Object responseObject =  serviceRequestRepository.fetchResult(uri, request, centralInstanceUtil.getStateLevelTenant(service.getTenantId()));
//			Object response =  mapper.convertValue(responseObject,  Map.class);

			Object response = restTemplate.postForObject(uri.toString(), request, Map.class);
			List<String> depts = JsonPath.read(response, "$.MdmsRes.RAINMAKER-PGR.ServiceDefs");
			if(!CollectionUtils.isEmpty(depts)) {
				return depts.get(0);
			}else
				return null;
		}catch(Exception e) {
			log.error("Exception while fetching dept: ",e);
			return null;
		}
	}
	
	/**
	 * Prepares MDMS request for the service category search
	 * 
	 * @param uri
	 * @param tenantId
	 * @param category
	 * @param requestInfo
	 * @return
	 */
	public MdmsCriteriaReq prepareMdMsRequestForDept(StringBuilder uri, String tenantId, String category, RequestInfo requestInfo) {
		uri.append(mdmsHost).append(mdmsEndpoint);
		MasterDetail masterDetail = org.egov.mdms.model.MasterDetail.builder()
				.name("ServiceDefs").filter("[?((@.serviceCode IN ["+category+"]) && (@.active == true))].department").build();
		List<MasterDetail> masterDetails = new ArrayList<>();
		masterDetails.add(masterDetail);
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName("RAINMAKER-PGR")
				.masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	public String enrichDepartmentPlaceholderInPgrRequest(String value) {
		StringBuilder builder = new StringBuilder(value);
		builder.deleteCharAt(builder.length()-1);
		builder.append(",").append(IndexerConstants.DEPARTMENT_PLACEHOLDER).append(":").append(IndexerConstants.DEPT_CODE_PLACEHOLDER).append("}");
		return builder.toString();
	}

	public String getDepartmentCodeForPgrRequest(String kafkaJson) {
		StringBuilder uri = new StringBuilder();
		String serviceCode = JsonPath.read(kafkaJson, "$.service.serviceCode");
		MdmsCriteriaReq request = prepareMdMsRequestForDept(uri, stateLevelTenantId, serviceCode, new RequestInfo());
		try {
//			Object responseObject =  serviceRequestRepository.fetchResult(uri, request, stateLevelTenantId);
//			Object response =  mapper.convertValue(responseObject,  Map.class);

			Object response = restTemplate.postForObject(uri.toString(), request, Map.class);
			List<String> depts = JsonPath.read(response, "$.MdmsRes.RAINMAKER-PGR.ServiceDefs");
			if(!CollectionUtils.isEmpty(depts)) {
				return depts.get(0);
			}else
				return null;
		}catch(Exception e) {
			log.error("Exception while fetching dept: ",e);
			return null;
		}
	}
}