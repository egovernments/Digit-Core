package org.egov.pg.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.clients.idgen.IdGenClient;
import org.egov.pg.clients.registry.RegistryClient;
import org.egov.pg.clients.registry.models.DataSearchRequest;
import org.egov.pg.clients.registry.models.RegistryData;
import org.egov.pg.config.AppProperties;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.constants.TransactionAdditionalFields;
import org.egov.pg.models.BankAccount;
import org.egov.pg.models.Transaction;
import org.egov.tracer.model.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonMap;

@Slf4j
@Service
public class EnrichmentService {

	private final IdGenClient idGenClient;
	private final RegistryClient registryClient;
	private final ObjectMapper objectMapper;
	private final UserService userService;
	private final AppProperties appProperties;

	@Autowired
	public EnrichmentService(IdGenClient idGenClient, RegistryClient registryClient,
							 ObjectMapper objectMapper, UserService userService, AppProperties appProperties) {
		this.idGenClient = idGenClient;
		this.registryClient = registryClient;
		this.objectMapper = objectMapper;
		this.userService = userService;
		this.appProperties = appProperties;
	}

	void enrichCreateTransaction(Transaction txn, String tenantId, String userId) {
		txn.setTenantId(tenantId);

		BankAccount bankAccount = getBankAccount(tenantId, userId);
		txn.setAdditionalFields(singletonMap(TransactionAdditionalFields.BANK_ACCOUNT_NUMBER, bankAccount.getAccountNumber()));

		// Generate ID from ID Gen service and assign to txn object
		String txnId = idGenClient.generateId(tenantId, appProperties.getIdGenTxnIdTemplateCode(), Map.of("TENANTID", tenantId));
		txn.setTxnId(txnId);
		txn.setUser(userService.createOrSearchUser(txn, tenantId, userId));
		txn.setTxnStatus(Transaction.TxnStatusEnum.PENDING);
		txn.setTxnStatusMsg(PgConstants.TXN_INITIATED);

		if (Objects.isNull(txn.getAdditionalDetails())) {
			txn.setAdditionalDetails(objectMapper.createObjectNode());
			((ObjectNode) txn.getAdditionalDetails()).set("taxAndPayments", objectMapper.valueToTree(txn.getTaxAndPayments()));
		} else {
			Map<String, Object> additionDetailsMap = objectMapper.convertValue(txn.getAdditionalDetails(), Map.class);
			additionDetailsMap.put("taxAndPayments", (Object) txn.getTaxAndPayments());
			txn.setAdditionalDetails(objectMapper.convertValue(additionDetailsMap, Object.class));
		}

		String uri = UriComponentsBuilder
				.fromHttpUrl(txn.getCallbackUrl())
				.queryParams(new LinkedMultiValueMap<>(singletonMap(PgConstants.PG_TXN_IN_LABEL,
						Collections.singletonList(txnId))))
				.build()
				.toUriString();
		txn.setCallbackUrl(uri);

		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(userId)
				.createdTime(System.currentTimeMillis())
				.build();
		txn.setAuditDetails(auditDetails);
	}

	void enrichUpdateTransaction(Transaction currentTxnStatus, Transaction newTxn, String userId) {
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(currentTxnStatus.getAuditDetails().getCreatedBy())
				.createdTime(currentTxnStatus.getAuditDetails().getCreatedTime())
				.lastModifiedBy(userId)
				.lastModifiedTime(System.currentTimeMillis()).build();
		newTxn.setAuditDetails(auditDetails);

		newTxn.setTxnId(currentTxnStatus.getTxnId());
		newTxn.setGateway(currentTxnStatus.getGateway());
		newTxn.setBillId(currentTxnStatus.getBillId());
		newTxn.setProductInfo(currentTxnStatus.getProductInfo());
		newTxn.setTenantId(currentTxnStatus.getTenantId());
		newTxn.setUser(currentTxnStatus.getUser());
		newTxn.setAdditionalDetails(currentTxnStatus.getAdditionalDetails());
		newTxn.setTaxAndPayments(currentTxnStatus.getTaxAndPayments());
		newTxn.setConsumerCode(currentTxnStatus.getConsumerCode());
		newTxn.setTxnStatusMsg(currentTxnStatus.getTxnStatusMsg());
		newTxn.setReceipt(currentTxnStatus.getReceipt());

	}

	private BankAccount getBankAccount(String tenantId, String userId) {
		DataSearchRequest request = DataSearchRequest.builder()
				.filters(Map.of("active", true))
				.build();

		List<RegistryData> records = registryClient.search(tenantId, userId, "bankAccount", request);

		if (records.size() != 1) {
			log.error("Expected to find one bank account for tenant {}, instead found {}", tenantId, records.size());
			throw new CustomException("BANK_ACCOUNT_FETCH_ERROR", "Unable to fetch related bank account");
		}

		return objectMapper.convertValue(records.get(0).getData(), BankAccount.class);
	}
}
