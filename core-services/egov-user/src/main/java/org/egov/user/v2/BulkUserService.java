package org.egov.user.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.domain.model.User;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.domain.service.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Bulk create orchestrator.
 * <p>
 * One flow, one User model end-to-end. Steps:
 * <ol>
 *   <li>sanitize + enrich (null out server-owned fields, apply defaults)</li>
 *   <li>bulk PII encrypt (one call to enc-service)</li>
 *   <li>bulk uniqueness check (one SQL for the whole batch)</li>
 *   <li>parallel BCrypt on survivors (fixed thread pool)</li>
 *   <li>batch INSERT eg_user + batch INSERT eg_userrole_v1</li>
 *   <li>bulk PII decrypt (one call to enc-service)</li>
 * </ol>
 * Failed users are returned with id=null (successful users have id populated).
 * v1 {@code UserService} is untouched.
 */
@Service
@Slf4j
public class BulkUserService {

    private final BulkUserRepository bulkUserRepository;
    private final PasswordEncoder passwordEncoder;                      // v1 bean, reused
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;   // v1 bean, reused
    private final MultiStateInstanceUtil multiStateInstanceUtil;       // v1 bean, reused
    private final UserUtils userUtils;                                 // v1 bean, reused
    private final ExecutorService bcryptPool;

    @Value("${default.password.expiry.in.days:90}")
    private int defaultPasswordExpiryInDays;

    @Value("${egov.user.bulk.max:100}")
    private int maxBulkSize;

    @Autowired
    public BulkUserService(BulkUserRepository bulkUserRepository,
                           PasswordEncoder passwordEncoder,
                           EncryptionDecryptionUtil encryptionDecryptionUtil,
                           MultiStateInstanceUtil multiStateInstanceUtil,
                           UserUtils userUtils,
                           @Qualifier("bcryptPool") ExecutorService bcryptPool) {
        this.bulkUserRepository = bulkUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.userUtils = userUtils;
        this.bcryptPool = bcryptPool;
    }

    @SuppressWarnings("unchecked")
    public List<User> createUsersBulk(List<User> incoming, RequestInfo requestInfo) {

        if (incoming == null || incoming.isEmpty()) return Collections.emptyList();
        if (incoming.size() > maxBulkSize) {
            throw new IllegalArgumentException(
                    "Bulk size " + incoming.size() + " exceeds max " + maxBulkSize);
        }

        long t0 = System.currentTimeMillis();

        // 1. sanitize + enrichCreate: server owns id/uuid/audit; caller cannot set them.
        String stateTenant = multiStateInstanceUtil.getStateLevelTenant(incoming.get(0).getTenantId());
        Long loggedInUserId = requestInfo != null && requestInfo.getUserInfo() != null
                ? requestInfo.getUserInfo().getId() : null;
        String loggedInUserUuid = requestInfo != null && requestInfo.getUserInfo() != null
                ? requestInfo.getUserInfo().getUuid() : null;
        Date now = new Date();

        for (User u : incoming) {
            u.setId(null);
            u.setUuid(null);
            u.setCreatedBy(loggedInUserId);
            u.setCreatedDate(now);
            u.setLastModifiedBy(loggedInUserId);
            u.setLastModifiedDate(now);
            if (u.getActive() == null) u.setActive(true);
            if (u.getTenantId() != null) {
                u.setTenantId(userUtils.getStateLevelTenantForCitizen(u.getTenantId(), u.getType()));
            }
        }

        // 2. bulk PII encrypt. encryptionDecryptionUtil.encryptObject already handles Collections<T>.
        List<User> encrypted = (List<User>) encryptionDecryptionUtil
                .encryptObject(incoming, "User", User.class);
        long tEnc = System.currentTimeMillis();

        // 3. bulk uniqueness check: one SQL for the whole batch, then also
        //    dedup within the batch itself. Keep the FIRST occurrence of each key.
        Set<String> existingUsernames = bulkUserRepository.findExistingUsernames(encrypted, stateTenant);
        long tUniq = System.currentTimeMillis();

        Set<String> takenKeys = new HashSet<>();
        List<User> survivors = new ArrayList<>();
        for (User u : encrypted) {
            if (existingUsernames.contains(u.getUsername())) {
                log.debug("Skipping duplicate (already exists in DB): {}", u.getUsername());
                continue;
            }
            String key = keyOf(u);
            if (!takenKeys.add(key)) {
                log.debug("Skipping duplicate within batch: {}", u.getUsername());
                continue;
            }
            survivors.add(u);
        }

        // 4. Password preparation: generate random if missing, then hash IN PARALLEL.
        for (User u : survivors) {
            if (!StringUtils.hasText(u.getPassword())) {
                u.setPassword(UUID.randomUUID().toString());
            }
        }
        hashPasswordsInParallel(survivors);
        long tHash = System.currentTimeMillis();

        // 5. Batch INSERT eg_user + batch INSERT eg_userrole_v1. Server-assigned id/uuid populated in-place.
        for (User u : survivors) {
            u.setUuid(UUID.randomUUID().toString());
            u.setDefaultPasswordExpiry(defaultPasswordExpiryInDays);
        }
        bulkUserRepository.createBulk(survivors);
        long tIns = System.currentTimeMillis();

        // 6. bulk PII decrypt for response.
        List<User> decryptedSurvivors = (List<User>) encryptionDecryptionUtil
                .decryptObject(survivors, "UserSelf", User.class, requestInfo);
        long tDec = System.currentTimeMillis();

        // Build response: preserve original order and count. Failed users have id=null.
        Set<String> savedUsernames = decryptedSurvivors.stream()
                .map(User::getUsername).collect(Collectors.toSet());
        List<User> response = new ArrayList<>(incoming.size());
        // Map by (username, type, tenantId) — but incoming was mutated during encrypt.
        // Simpler: return the surviving (populated) users first, then the ones we skipped as-is.
        // Callers correlate by username. For strict order-preservation, extend below.
        response.addAll(decryptedSurvivors);
        for (User u : incoming) {
            if (!savedUsernames.contains(u.getUsername())) {
                response.add(u);   // id remains null → caller sees this row failed
            }
        }

        log.info("v2 bulk timings (ms) — encrypt: {}, uniqueness: {}, bcrypt: {}, insert: {}, decrypt: {}, total: {}",
                tEnc - t0, tUniq - tEnc, tHash - tUniq, tIns - tHash, tDec - tIns, tDec - t0);

        return response;
    }

    /**
     * Hash all survivors' passwords in parallel via the fixed-size bcryptPool.
     * Each hash is independent; passwords are set in place on the User objects.
     */
    private void hashPasswordsInParallel(List<User> survivors) {
        if (survivors.isEmpty()) return;

        List<CompletableFuture<Void>> tasks = survivors.stream()
                .filter(u -> StringUtils.hasText(u.getPassword()))
                .map(u -> CompletableFuture.runAsync(
                        () -> u.setPassword(passwordEncoder.encode(u.getPassword())),
                        bcryptPool))
                .collect(Collectors.toList());

        CompletableFuture
                .allOf(tasks.toArray(new CompletableFuture[0]))
                .join();
    }

    private String keyOf(User u) {
        return String.format("%s|%s|%s",
                u.getTenantId(),
                u.getType() == null ? "" : u.getType().toString(),
                u.getUsername());
    }
}
