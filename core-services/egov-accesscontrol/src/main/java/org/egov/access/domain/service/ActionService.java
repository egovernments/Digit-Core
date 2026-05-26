package org.egov.access.domain.service;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.access.domain.criteria.ActionSearchCriteria;
import org.egov.access.domain.criteria.ValidateActionCriteria;
import org.egov.access.domain.model.Action;
import org.egov.access.domain.model.ActionContainer;
import org.egov.access.domain.model.ActionValidation;
import org.egov.access.domain.model.authorize.AuthorizationRequest;
import org.egov.access.domain.model.authorize.Role;
import org.egov.access.persistence.repository.ActionRepository;
import org.egov.access.persistence.repository.BaseRepository;
import org.egov.access.persistence.repository.MdmsRepository;
import org.egov.access.persistence.repository.querybuilder.ActionFinderQueryBuilder;
import org.egov.access.persistence.repository.querybuilder.ValidateActionQueryBuilder;
import org.egov.access.persistence.repository.rowmapper.ActionRowMapper;
import org.egov.access.persistence.repository.rowmapper.ActionValidationRowMapper;
import org.egov.access.util.AccessControlConstants;
import org.egov.access.web.contract.action.ActionRequest;
import org.egov.access.web.contract.action.Module;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ActionService {

	private BaseRepository repository;

	private ActionRepository actionRepository;

	private MdmsRepository mdmsRepository;
	
	@Autowired
	private MultiStateInstanceUtil multiStateInstanceUtil;

	@Autowired
	public ActionService(BaseRepository repository, ActionRepository actionRepository, MdmsRepository mdmsRepository) {
		this.repository = repository;
		this.actionRepository = actionRepository;
		this.mdmsRepository = mdmsRepository;
	}

	public List<Action> getActions(ActionSearchCriteria actionSearchCriteria) {
		ActionFinderQueryBuilder queryBuilder = new ActionFinderQueryBuilder(actionSearchCriteria);
		return (List<Action>) (List<?>) repository.run(queryBuilder, new ActionRowMapper());
	}

	public ActionValidation validate(ValidateActionCriteria criteria) {
		ValidateActionQueryBuilder queryBuilder = new ValidateActionQueryBuilder(criteria);
		return (ActionValidation) repository.run(queryBuilder, new ActionValidationRowMapper()).get(0);
	}

	public List<Action> createAction(ActionRequest actionRequest) {

		return actionRepository.createAction(actionRequest);
	}

	public List<Action> updateAction(ActionRequest actionRequest) {

		return actionRepository.updateAction(actionRequest);
	}

	public boolean checkActionNameExit(String name) {

		return actionRepository.checkActionNameExit(name);
	}

	public boolean checkCombinationOfUrlAndqueryparamsExist(String url, String queryParams) {

		return actionRepository.checkCombinationOfUrlAndqueryparamsExist(url, queryParams);
	}

	public List<Module> getAllActionsBasedOnRoles(final ActionRequest actionRequest) {

		return actionRepository.getAllActionsBasedOnRoles(actionRequest).getModules();

	}

	public List<Action> getAllActions(final ActionRequest actionRequest) {

		return actionRepository.getAllActions(actionRequest);

	}
	public List<Action> getAllMDMSActions(final ActionRequest actionRequest) throws JSONException, UnsupportedEncodingException{

		return actionRepository.getAllMDMSActions(actionRequest);
	}
	
	/**
	 * Authorize the request
	 *
	 * @param authorizeRequest URI and role to be authorized
	 * @return true when authorized, false when unauthorized
	 */
	public boolean isAuthorized(AuthorizationRequest authorizeRequest){

		String inputTenantId = authorizeRequest.getTenantIds().iterator().next();
		Set<String> applicableRoles = getApplicableRoles(authorizeRequest);
		String[] tenantsToCheck = buildTenantCheckOrder(inputTenantId, applicableRoles);

		for (String tenantId : tenantsToCheck) {
			if (isAuthorizedOnGivenTenantLevel(authorizeRequest.getUri(), applicableRoles, tenantId))
				return true;
		}

		return false;
	}

	private String[] buildTenantCheckOrder(String tenantId, Set<String> roleCodes) {
		String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
		boolean addNational = multiStateInstanceUtil.getIsEnvironmentCentralInstance()
				&& roleCodes.contains(AccessControlConstants.CITIZNE_ROLE_CODE);

		String[] tenants = addNational
				? new String[]{ tenantId, stateLevelTenantId, tenantId.split("\\.")[0] }
				: new String[]{ tenantId, stateLevelTenantId };

		log.debug("Tenants for auth check: {}", (Object) tenants);
		return tenants;
	}

	private boolean isAuthorizedOnGivenTenantLevel(String uriToBeAuthorized, Set<String> applicableRoles, String tenantId){

		for (String roleCode : applicableRoles) {
			ActionContainer container = mdmsRepository.fetchRoleActionData(tenantId, roleCode);
			if (container == null) continue;
			if (container.getUris().contains(uriToBeAuthorized)) {
				log.debug("Role {} authorized for URI {}", roleCode, uriToBeAuthorized);
				return true;
			}
			if (container.matchesRegexUri(uriToBeAuthorized)) {
				log.debug("Role {} authorized for URI {} via regex", roleCode, uriToBeAuthorized);
				return true;
			}
		}

		log.debug("No role in {} authorized for URI {}", applicableRoles, uriToBeAuthorized);
		return false;
	}

	private Set<String> getApplicableRoles(AuthorizationRequest authorizationRequest){

		Set<String> requestTenantIds = authorizationRequest.getTenantIds();
		String tenantId = requestTenantIds.iterator().next();
		String centralInstanceLevelTenantId = getCentralInstanceLevelTenant(tenantId);
		String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);

		Set<Role> roles = authorizationRequest.getRoles();
		Set<String> applicableRoleCodes = new HashSet<>(roles.size() * 2);

		for (Role role : roles) {
			if (requestTenantIds.contains(role.getTenantId()) || role.getTenantId().equalsIgnoreCase(stateLevelTenantId))
				applicableRoleCodes.add(role.getCode());
			if (!ObjectUtils.isEmpty(stateLevelTenantId) && role.getTenantId().equalsIgnoreCase(centralInstanceLevelTenantId))
				applicableRoleCodes.add(role.getCode());
		}

		return applicableRoleCodes;
	}

	private String getCentralInstanceLevelTenant(String tenantId){
		return tenantId.split("\\.")[0];
	}
}
