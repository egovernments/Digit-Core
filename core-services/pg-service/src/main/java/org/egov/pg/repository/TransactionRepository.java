package org.egov.pg.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.TransactionDump;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TransactionRepository {

	private static final TransactionRowMapper rowMapper = new TransactionRowMapper();
	private static final String INSERT_TXN = "INSERT INTO eg_pg_transactions " +
			"(txn_id, txn_amount, txn_status, txn_status_msg, gateway, consumer_code, bill_id, product_info, " +
			"user_uuid, user_name, mobile_number, email_id, name, user_tenant_id, tenant_id, " +
			"gateway_txn_id, gateway_payment_mode, gateway_status_code, gateway_status_msg, receipt, " +
			"additional_details, created_by, created_time, last_modified_by, last_modified_time, request_id) " +
			"VALUES (?, ?::numeric, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)";
	private static final String UPDATE_TXN = "UPDATE eg_pg_transactions SET " +
			"txn_status=?, txn_status_msg=?, gateway_txn_id=?, gateway_payment_mode=?, " +
			"gateway_status_code=?, gateway_status_msg=?, receipt=?, " +
			"last_modified_by=?, last_modified_time=?, request_id=? " +
			"WHERE txn_id=? AND tenant_id=?";
	private static final String INSERT_TXN_DUMP = "INSERT INTO eg_pg_transactions_dump " +
			"(txn_id, txn_request, txn_response, created_by, created_time, last_modified_by, last_modified_time, request_id) " +
			"VALUES (?, ?, ?::jsonb, ?, ?, ?, ?, ?)";
	private static final String UPDATE_TXN_DUMP = "UPDATE eg_pg_transactions_dump SET " +
			"txn_response=?::jsonb, last_modified_by=?, last_modified_time=?, request_id=? " +
			"WHERE txn_id=?";

	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public TransactionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	public void saveTransaction(Transaction txn, String requestId) {
		try {
			jdbcTemplate.update(INSERT_TXN,
					txn.getTxnId(),
					txn.getTxnAmount(),
					txn.getTxnStatus().toString(),
					txn.getTxnStatusMsg(),
					txn.getGateway(),
					txn.getConsumerCode(),
					txn.getBillId(),
					txn.getProductInfo(),
					txn.getUser().getUuid(),
					txn.getUser().getUserName(),
					txn.getUser().getMobileNumber(),
					txn.getUser().getEmailId(),
					txn.getUser().getName(),
					txn.getUser().getTenantId(),
					txn.getTenantId(),
					txn.getGatewayTxnId(),
					txn.getGatewayPaymentMode(),
					txn.getGatewayStatusCode(),
					txn.getGatewayStatusMsg(),
					txn.getReceipt(),
					txn.getAdditionalDetails() != null ? txn.getAdditionalDetails().toString() : null,
					txn.getAuditDetails().getCreatedBy(),
					txn.getAuditDetails().getCreatedTime(),
					txn.getAuditDetails().getLastModifiedBy(),
					txn.getAuditDetails().getLastModifiedTime(),
					requestId
			);
		} catch (DataAccessException e) {
			log.error("Failed to save transaction: {}", txn.getTxnId(), e);
			throw new CustomException("TXN_SAVE_ERROR", "Failed to save transaction: " + e.getMessage());
		}
	}

	public void updateTransaction(Transaction txn, String requestId) {
		try {
			jdbcTemplate.update(UPDATE_TXN,
					txn.getTxnStatus().toString(),
					txn.getTxnStatusMsg(),
					txn.getGatewayTxnId(),
					txn.getGatewayPaymentMode(),
					txn.getGatewayStatusCode(),
					txn.getGatewayStatusMsg(),
					txn.getReceipt(),
					txn.getAuditDetails().getLastModifiedBy(),
					txn.getAuditDetails().getLastModifiedTime(),
					requestId,
					txn.getTxnId(),
					txn.getTenantId()
			);
		} catch (DataAccessException e) {
			log.error("Failed to update transaction: {}", txn.getTxnId(), e);
			throw new CustomException("TXN_UPDATE_ERROR", "Failed to update transaction: " + e.getMessage());
		}
	}

	public void saveTransactionDump(TransactionDump dump, String requestId) {
		try {
			jdbcTemplate.update(INSERT_TXN_DUMP,
					dump.getTxnId(),
					dump.getTxnRequest(),
					toJson(dump.getTxnResponse()),
					dump.getAuditDetails().getCreatedBy(),
					dump.getAuditDetails().getCreatedTime(),
					dump.getAuditDetails().getLastModifiedBy(),
					dump.getAuditDetails().getLastModifiedTime(),
					requestId
			);
		} catch (DataAccessException e) {
			log.error("Failed to save transaction dump: {}", dump.getTxnId(), e);
			throw new CustomException("TXN_DUMP_SAVE_ERROR", "Failed to save transaction dump: " + e.getMessage());
		}
	}

	public void updateTransactionDump(TransactionDump dump, String requestId) {
		try {
			jdbcTemplate.update(UPDATE_TXN_DUMP,
					toJson(dump.getTxnResponse()),
					dump.getAuditDetails().getLastModifiedBy(),
					dump.getAuditDetails().getLastModifiedTime(),
					requestId,
					dump.getTxnId()
			);
		} catch (DataAccessException e) {
			log.error("Failed to update transaction dump: {}", dump.getTxnId(), e);
			throw new CustomException("TXN_DUMP_UPDATE_ERROR", "Failed to update transaction dump: " + e.getMessage());
		}
	}

	public List<Transaction> fetchTransactions(TransactionCriteria transactionCriteria) {
		List<Object> params = new ArrayList<>();
		String query = TransactionQueryBuilder.getPaymentSearchQueryByCreatedTimeRange(transactionCriteria, params);

		log.debug(query);
		return jdbcTemplate.query(query, params.toArray(), rowMapper);
	}

	public List<Transaction> fetchTransactionsByTimeRange(TransactionCriteria transactionCriteria, Long startTime, Long endTime) {
		List<Object> params = new ArrayList<>();
		String query = TransactionQueryBuilder.getPaymentSearchQueryByCreatedTimeRange(transactionCriteria, startTime, endTime, params);
		log.debug(query);
		return jdbcTemplate.query(query, params.toArray(), rowMapper);
	}

	private String toJson(Object obj) {
		try {
			return obj != null ? objectMapper.writeValueAsString(obj) : null;
		} catch (JsonProcessingException e) {
			throw new CustomException("JSON_SERIALIZATION_ERROR",
					"Failed to serialize object: " + e.getMessage());
		}
	}
}
