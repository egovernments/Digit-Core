package digit.service;

import digit.repository.TenantConfigRepository;
import digit.service.enrichment.TenantConfigEnricher;
import digit.service.validator.TenantConfigValidator;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TenantConfigService {

    private TenantConfigRepository tenantConfigRepository;

    private TenantConfigEnricher tenantConfigEnricher;

    private TenantConfigValidator tenantConfigValidator;


    public TenantConfigService(TenantConfigRepository tenantConfigRepository, TenantConfigEnricher tenantConfigEnricher, TenantConfigValidator tenantConfigValidator) {
        this.tenantConfigRepository = tenantConfigRepository;
        this.tenantConfigEnricher = tenantConfigEnricher;
        this.tenantConfigValidator = tenantConfigValidator;
    }

    /**
     * @param tenantConfigRequest
     * @return
     */
    public TenantConfigResponse create(TenantConfigRequest tenantConfigRequest) {

        // validate request
        tenantConfigValidator.validateCreateReq(tenantConfigRequest);

        // enrich request
        tenantConfigEnricher.enrichCreateReq(tenantConfigRequest);

        // persist
        tenantConfigRepository.create(tenantConfigRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantConfigRequest.getRequestInfo(), Boolean.TRUE);
        TenantConfigResponse tenantConfigResponse = new TenantConfigResponse().builder()
                .tenantConfigs(Collections.singletonList(tenantConfigRequest.getTenantConfig()))
                .responseInfo(responseInfo)
                .build();

        return tenantConfigResponse;
    }

    /**
     * Searches for a tenant
     * based on search criteria
     *
     * @param searchCriteria
     * @param requestInfo
     * @return
     */
    public TenantConfigResponse search(TenantConfigSearchCriteria searchCriteria, RequestInfo requestInfo) {

        List<TenantConfig> tenantList = tenantConfigRepository.search(searchCriteria);
        if (!tenantList.isEmpty() && searchCriteria.getCode() != null) {
        updateLastLoginTimeAsync(searchCriteria.getCode());
        }

        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE);

        return TenantConfigResponse
                .builder()
                .responseInfo(responseInfo)
                .tenantConfigs(tenantList)
                .build();
    }

    /**
     * @param tenantConfigRequest
     * @return
     */
    public TenantConfigResponse update(TenantConfigRequest tenantConfigRequest) {

        // validate
        // tenantConfigValidator.validateUpdateRequest(tenantRequest);

        // enrich
        tenantConfigEnricher.enrichUpdateReq(tenantConfigRequest);

        // persist
        tenantConfigRepository.update(tenantConfigRequest);

        // convert to TenantResponse
        ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(tenantConfigRequest.getRequestInfo(), Boolean.TRUE);
        return TenantConfigResponse
                .builder()
                .responseInfo(responseInfo)
                .tenantConfigs(Collections.singletonList(tenantConfigRequest.getTenantConfig()))
                .build();

    }

  /**
   * Updates last login time asynchronously to avoid blocking the search response
   * @param code tenant code
   */
  private void updateLastLoginTimeAsync(String code) {
    try {
      long currentTime = System.currentTimeMillis();
      tenantConfigRepository.updateLastLoginTime(code, currentTime);
      log.info("Successfully updated lastLoginTime for tenant: {}", code);
    } catch (Exception e) {
      log.error("Error updating lastLoginTime for tenant: {}", code, e);
      // Don't throw exception to avoid breaking the search operation
    }
  }

  /**
   * Finds least active tenant accounts based on lastLoginTime
   * Accounts that have never logged in are sorted by creation time
   *
   * @param limit Maximum number of accounts to return
   * @param inactiveSinceTimestamp Unix timestamp in milliseconds - find accounts inactive since this time
   * @param inactiveSinceDays Number of days - find accounts inactive for this many days
   * @param requestInfo Request information
   * @return Response containing least active tenant configs
   */
  public TenantConfigResponse findLeastActiveAccounts(Integer limit, Long inactiveSinceTimestamp,
      Integer inactiveSinceDays, RequestInfo requestInfo) {
    // Set default limit if not provided
    if (limit == null || limit <= 0) {
      limit = 10;
    }

    List<TenantConfig> leastActiveTenants = tenantConfigRepository.findLeastActiveAccounts(
        limit, inactiveSinceTimestamp, inactiveSinceDays);

    ResponseInfo responseInfo = ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE);

    return TenantConfigResponse
        .builder()
        .responseInfo(responseInfo)
        .tenantConfigs(leastActiveTenants)
        .build();
  }
}
