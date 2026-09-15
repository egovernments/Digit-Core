package org.egov.user.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.sso.IdpPersistenceException;
import org.egov.user.domain.exception.sso.TokenReplayException;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.persistence.repository.UserIdpDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing SSO user persistence operations with token replay protection.
 * 
 * <p>This service provides atomic operations for persisting user and IDP details during SSO authentication
 * flows. It implements comprehensive token replay protection through multiple layers:
 * <ul>
 *   <li>Application-level validation via {@link #isTokenReplay(String, String)}</li>
 *   <li>Database constraint enforcement (unique constraint on tokenId + tenantId)</li>
 *   <li>Exception handling for constraint violations</li>
 * </ul>
 * 
 * <p>The service handles two main persistence scenarios:
 * <ol>
 *   <li>User creation or role changes requiring both user and IDP details updates</li>
 *   <li>Existing user login requiring only IDP session/MFA details updates</li>
 * </ol>
 * 
 * <p>All operations are transactional and include comprehensive input validation and error handling
 * to maintain data integrity and provide meaningful error messages for SSO integration scenarios.
 * 
 * @see org.egov.user.domain.exception.sso.TokenReplayException
 * @see org.egov.user.persistence.repository.UserIdpDetailsRepository
 */
@Service
public class SsoUserPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(SsoUserPersistenceService.class);
    private static final String CURRENT_TOKEN_CONSTRAINT = "eg_user_idp_details_tokenid_tenantid_key";

    private final UserService userService;
    private final UserIdpDetailsRepository userIdpDetailsRepository;

    public SsoUserPersistenceService(UserService userService,
                                     UserIdpDetailsRepository userIdpDetailsRepository) {
        this.userService = userService;
        this.userIdpDetailsRepository = userIdpDetailsRepository;
    }

    /**
     * Updates user record and upserts IDP details in a single atomic transaction.
     * 
     * <p>This method is used when both user information and IDP details must be persisted together,
     * typically during SSO login with role changes or new user creation scenarios. The operation
     * includes comprehensive token replay protection through database constraint enforcement.
     * 
     * <p>The transaction ensures that either both the user update and IDP details upsert succeed,
     * or neither does, maintaining data consistency. Any constraint violation on the unique
     * tokenId + tenantId constraint is converted to a {@link TokenReplayException}.
     * 
     * @param user the user domain object to update (must contain valid user data)
     * @param idpDetails the IDP details to persist (tokenId, expiration, MFA data)
     * @param tenantId the tenant identifier for schema routing and data isolation
     * @param requestInfo the request context containing user information for audit trails
     * @return the updated user domain object with latest state
     * @throws TokenReplayException if the tokenId has already been used (database constraint violation)
     * @throws IdpPersistenceException if required input parameters are null or invalid
     * @throws DataIntegrityViolationException if other database constraints are violated
     */
    @Transactional
    public User updateUserAndUpsertIdpDetails(User user, UserIdpDetails idpDetails,
                                              String tenantId, RequestInfo requestInfo) {
        validateIdpPersistenceInput(idpDetails, tenantId);
        try {
            // SSO users are built from JWT claims and carry no password: opt into preserving the
            // stored hash. The two-arg overload keeps the deployed mandatory-password contract.
            User updatedUser = userService.updateWithoutOtpValidation(user, requestInfo, true);
            userIdpDetailsRepository.upsert(idpDetails, tenantId);
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
            if (isTokenConstraintViolation(e)) {
                throw new TokenReplayException(idpDetails.getTokenId());
            }
            throw e;
        }
    }

    /**
     * Upserts IDP details only within a transaction, without modifying user record.
     * 
     * <p>This method is used when the user record remains unchanged but IDP session and MFA
     * details must be updated, typically during existing user SSO login scenarios without
     * role changes. The operation includes token replay protection through database constraints.
     * 
     * <p>The method validates input parameters and handles constraint violations by converting
     * them to {@link TokenReplayException} for consistent error handling in SSO flows.
     * 
     * @param idpDetails the IDP details to persist (tokenId, expiration, MFA data)
     * @param tenantId the tenant identifier for schema routing and data isolation
     * @throws TokenReplayException if the tokenId has already been used (database constraint violation)
     * @throws IdpPersistenceException if required input parameters are null or invalid
     * @throws DataIntegrityViolationException if other database constraints are violated
     */
    @Transactional
    public void upsertIdpDetailsOnly(UserIdpDetails idpDetails, String tenantId) {
        validateIdpPersistenceInput(idpDetails, tenantId);
        try {
            userIdpDetailsRepository.upsert(idpDetails, tenantId);
        } catch (DataIntegrityViolationException e) {
            if (isTokenConstraintViolation(e)) {
                throw new TokenReplayException(idpDetails.getTokenId());
            }
            throw e;
        }
    }

    /**
     * Validates input parameters for IDP persistence operations.
     * 
     * <p>This method ensures that all required parameters are present and valid before
     * attempting database operations. It provides detailed error logging for troubleshooting
     * and throws specific exceptions for each validation failure scenario.
     * 
     * @param details the IDP details object to validate (must not be null)
     * @param tenantId the tenant identifier to validate (must not be null)
     * @throws IdpPersistenceException if any required parameter is null or invalid
     */
    private void validateIdpPersistenceInput(UserIdpDetails details, String tenantId) {
        if (details == null) {
            LOG.error("IDP details persistence aborted: details is null for tenantId={}", tenantId);
            throw IdpPersistenceException.invalidInput("details is null");
        }
        if (details.getId() == null) {
            LOG.error("IDP details persistence aborted: details.id is null for tenantId={}", tenantId);
            throw IdpPersistenceException.invalidInput("details.id is null");
        }
        if (tenantId == null) {
            LOG.error("IDP details persistence aborted: tenantId is null for userId={}", details.getId());
            throw IdpPersistenceException.invalidInput("tenantId is null");
        }
    }

    private boolean isTokenConstraintViolation(DataIntegrityViolationException exception) {
        String message = exception.getMessage();
        return message != null && message.contains(CURRENT_TOKEN_CONSTRAINT);
    }

    /**
     * Checks if a JWT token ID has already been used (token replay protection).
     * 
     * <p>This method implements the first layer of token replay protection by querying
     * the database to determine if the given tokenId has been previously used for SSO
     * authentication within the same tenant. This validation occurs before any database
     * write operations to prevent unnecessary processing.
     * 
     * <p>The method follows a fail-secure approach: if the database query fails for any
     * reason (connection issues, query errors, etc.), it assumes the token is a replay
     * attempt and returns true to prevent potential security breaches.
     * 
     * @param tokenId the JWT token ID (jti or uti claim) to check for previous usage
     * @param tenantId the tenant identifier to scope the token replay check
     * @return true if the tokenId has been used before or if database check fails, false otherwise
     * @throws Exception if database query fails (assumes token replay for security)
     */
    public boolean isTokenReplay(String tokenId, String tenantId) {
        try {
            return userIdpDetailsRepository.isTokenReplay(tokenId, tenantId);
        } catch (Exception e) {
            LOG.error("Error checking token replay for tokenId={}, tenantId={}", tokenId, tenantId, e);
            // Fail secure: if we can't check, assume it's a replay to be safe
            return true;
        }
    }
}
