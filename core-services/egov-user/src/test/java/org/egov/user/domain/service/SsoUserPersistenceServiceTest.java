package org.egov.user.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.sso.IdpPersistenceException;
import org.egov.user.domain.exception.sso.TokenReplayException;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.persistence.repository.UserIdpDetailsRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SsoUserPersistenceServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserIdpDetailsRepository userIdpDetailsRepository;

    private SsoUserPersistenceService ssoUserPersistenceService;

    private User testUser;
    private UserIdpDetails testIdpDetails;
    private RequestInfo testRequestInfo;
    private static final String TENANT_ID = "pb";
    private static final String TOKEN_ID = "test-token-id-123";

    @Before
    public void setup() {
        ssoUserPersistenceService = new SsoUserPersistenceService(userService, userIdpDetailsRepository);

        testUser = User.builder()
                .id(1L)
                .uuid("user-uuid-123")
                .username("testuser")
                .name("Test User")
                .emailId("test@example.com")
                .tenantId(TENANT_ID)
                .type(UserType.EMPLOYEE)
                .active(true)
                .build();

        testIdpDetails = UserIdpDetails.builder()
                .id(1L)
                .uuid("user-uuid-123")
                .tenantId(TENANT_ID)
                .tokenId(TOKEN_ID)
                .idpTokenExp(new Date(System.currentTimeMillis() + 3600000))
                .lastSsoLoginAt(new Date())
                .mfaEnabled(false)
                .createdBy(1L)
                .lastModifiedBy(1L)
                .build();

        testRequestInfo = RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder()
                        .id(1L)
                        .uuid("user-uuid-123")
                        .build())
                .build();
    }

    @Test
    public void testUpdateUserAndUpsertIdpDetails_Success() {
        // Arrange
        User expectedUpdatedUser = User.builder()
                .id(1L)
                .uuid("user-uuid-123")
                .username("updateduser")
                .name("Updated User")
                .build();

        when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean()))
                .thenReturn(expectedUpdatedUser);
        doNothing().when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        User result = ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                testUser, testIdpDetails, TENANT_ID, testRequestInfo);

        // Assert
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("Updated User", result.getName());
        verify(userService).updateWithoutOtpValidation(testUser, testRequestInfo, true);
        verify(userIdpDetailsRepository).upsert(testIdpDetails, TENANT_ID);
    }

    @Test(expected = TokenReplayException.class)
    public void testUpdateUserAndUpsertIdpDetails_TokenReplayViolation() {
        // Arrange
        DataIntegrityViolationException constraintViolation = new DataIntegrityViolationException(
                "Duplicate key value violates unique constraint \"eg_user_idp_details_tokenid_tenantid_key\"");
        
        when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean()))
                .thenReturn(testUser);
        doThrow(constraintViolation).when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                testUser, testIdpDetails, TENANT_ID, testRequestInfo);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testUpdateUserAndUpsertIdpDetails_OtherDataIntegrityViolation() {
        // Arrange
        DataIntegrityViolationException otherViolation = new DataIntegrityViolationException(
                "Some other constraint violation");
        
        when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean()))
                .thenReturn(testUser);
        doThrow(otherViolation).when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                testUser, testIdpDetails, TENANT_ID, testRequestInfo);
    }

    @Test
    public void testUpsertIdpDetailsOnly_Success() {
        // Arrange
        doNothing().when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(testIdpDetails, TENANT_ID);

        // Assert
        verify(userIdpDetailsRepository).upsert(testIdpDetails, TENANT_ID);
    }

    @Test(expected = TokenReplayException.class)
    public void testUpsertIdpDetailsOnly_TokenReplayViolation() {
        // Arrange
        DataIntegrityViolationException constraintViolation = new DataIntegrityViolationException(
                "Duplicate key value violates unique constraint \"eg_user_idp_details_tokenid_tenantid_key\"");
        
        doThrow(constraintViolation).when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(testIdpDetails, TENANT_ID);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testUpsertIdpDetailsOnly_OtherDataIntegrityViolation() {
        // Arrange
        DataIntegrityViolationException otherViolation = new DataIntegrityViolationException(
                "Some other constraint violation");
        
        doThrow(otherViolation).when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(testIdpDetails, TENANT_ID);
    }

    @Test(expected = IdpPersistenceException.class)
    public void testValidateIdpPersistenceInput_NullDetails() {
        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(null, TENANT_ID);
    }

    @Test(expected = IdpPersistenceException.class)
    public void testValidateIdpPersistenceInput_NullTenantId() {
        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(testIdpDetails, null);
    }

    @Test(expected = IdpPersistenceException.class)
    public void testValidateIdpPersistenceInput_NullDetailsId() {
        // Arrange
        UserIdpDetails detailsWithoutId = UserIdpDetails.builder()
                .id(null)
                .uuid("user-uuid-123")
                .tenantId(TENANT_ID)
                .build();

        // Act
        ssoUserPersistenceService.upsertIdpDetailsOnly(detailsWithoutId, TENANT_ID);
    }

    @Test
    public void testIsTokenReplay_ValidToken() {
        // Arrange
        when(userIdpDetailsRepository.isTokenReplay(TOKEN_ID, TENANT_ID)).thenReturn(false);

        // Act
        boolean result = ssoUserPersistenceService.isTokenReplay(TOKEN_ID, TENANT_ID);

        // Assert
        assertFalse(result);
        verify(userIdpDetailsRepository).isTokenReplay(TOKEN_ID, TENANT_ID);
    }

    @Test
    public void testIsTokenReplay_ReplayedToken() {
        // Arrange
        when(userIdpDetailsRepository.isTokenReplay(TOKEN_ID, TENANT_ID)).thenReturn(true);

        // Act
        boolean result = ssoUserPersistenceService.isTokenReplay(TOKEN_ID, TENANT_ID);

        // Assert
        assertTrue(result);
        verify(userIdpDetailsRepository).isTokenReplay(TOKEN_ID, TENANT_ID);
    }

    @Test
    public void testIsTokenReplay_DatabaseError_FailSecure() {
        // Arrange
        when(userIdpDetailsRepository.isTokenReplay(TOKEN_ID, TENANT_ID))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        boolean result = ssoUserPersistenceService.isTokenReplay(TOKEN_ID, TENANT_ID);

        // Assert
        assertTrue(result); // Should fail-secure and return true
    }

    @Test
    public void testIsTokenReplay_NullToken() {
        // Act
        boolean result = ssoUserPersistenceService.isTokenReplay(null, TENANT_ID);

        // Assert
        assertFalse(result);
        verify(userIdpDetailsRepository).isTokenReplay(eq(null), eq(TENANT_ID));
    }

    @Test
    public void testIsTokenReplay_EmptyToken() {
        // Act
        boolean result = ssoUserPersistenceService.isTokenReplay("", TENANT_ID);

        // Assert
        assertFalse(result);
        verify(userIdpDetailsRepository).isTokenReplay(eq(""), eq(TENANT_ID));
    }

    @Test
    public void testUpdateUserAndUpsertIdpDetails_WithNullTokenId_ShouldNotThrow() {
        // Arrange
        UserIdpDetails detailsWithoutTokenId = UserIdpDetails.builder()
                .id(1L)
                .uuid("user-uuid-123")
                .tenantId(TENANT_ID)
                .tokenId(null)
                .build();

        when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean()))
                .thenReturn(testUser);
        doNothing().when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        User result = ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                testUser, detailsWithoutTokenId, TENANT_ID, testRequestInfo);

        // Assert
        assertNotNull(result);
        verify(userService).updateWithoutOtpValidation(testUser, testRequestInfo, true);
        verify(userIdpDetailsRepository).upsert(detailsWithoutTokenId, TENANT_ID);
    }

    @Test
    public void testUpdateUserAndUpsertIdpDetails_WithEmptyTokenId_ShouldNotThrow() {
        // Arrange
        UserIdpDetails detailsWithEmptyTokenId = UserIdpDetails.builder()
                .id(1L)
                .uuid("user-uuid-123")
                .tenantId(TENANT_ID)
                .tokenId("")
                .build();

        when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean()))
                .thenReturn(testUser);
        doNothing().when(userIdpDetailsRepository).upsert(any(UserIdpDetails.class), anyString());

        // Act
        User result = ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                testUser, detailsWithEmptyTokenId, TENANT_ID, testRequestInfo);

        // Assert
        assertNotNull(result);
        verify(userService).updateWithoutOtpValidation(testUser, testRequestInfo, true);
        verify(userIdpDetailsRepository).upsert(detailsWithEmptyTokenId, TENANT_ID);
    }
}
