package digit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.service.BoundaryRelationshipService;
import digit.web.models.BoundaryRelation;
import digit.web.models.BulkBoundaryRelationshipRequest;
import digit.web.models.BulkBoundaryRelationshipResponse;
import digit.web.models.FailedBoundaryRelationship;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Consumes boundary-relationship "batch jobs" from Kafka and creates them synchronously.
 *
 * <p>For the high-cardinality lower levels (e.g. locality and village) the caller (boundary-management)
 * splits each level into fixed-size jobs of up to ~100 sibling records that share a single parent and
 * publishes them keyed by the parent code. Running several boundary-service instances in the same
 * consumer group spreads the partitions across instances, scaling creation horizontally.</p>
 *
 * <p><b>Reliability model.</b> Every record yields an outcome; nothing is silently dropped:</p>
 * <ul>
 *   <li><b>Already created</b> (DUPLICATE_RECORD) — treated as idempotent success, so redelivery of an
 *       already-applied job is safe and never floods the error topic.</li>
 *   <li><b>Transient</b> (parent/entity not yet persisted, transient DB error) — the listener throws so
 *       the container's error handler redelivers the whole job with backoff (unbounded enough to absorb
 *       persistence lag, dead-lettered to the error topic only after the ceiling). Safe because the
 *       insert is idempotent (ON CONFLICT DO NOTHING). No in-listener sleep, so max.poll.interval is
 *       never exceeded.</li>
 *   <li><b>Permanent</b> (genuine bad data) and <b>unparseable/invalid jobs</b> — published to the error
 *       topic for caller reconciliation; the offset commits (no pointless redelivery).</li>
 * </ul>
 *
 * <p>Note: keying jobs by parent code orders SIBLINGS within a partition; it does not guarantee
 * parent-before-child across levels — out-of-order arrival is tolerated by the transient redelivery above.</p>
 */
@Component
@Slf4j
public class BoundaryRelationshipBulkConsumer {

    // Already-exists outcome -> idempotent success (safe under at-least-once redelivery).
    private static final Set<String> ALREADY_DONE = Set.of(ErrorCodes.DUPLICATE_RECORD_CODE);

    // Transient outcomes -> redeliver the whole job (parent/entity persistence lag, transient DB).
    private static final Set<String> RETRYABLE = Set.of(
            "BOUNDARY_ENTITY_DOES_NOT_EXIST", "PARENT_NOT_FOUND", ErrorCodes.BULK_RELATIONSHIP_PERSIST_TRANSIENT_CODE);

    private static final int MAX_BULK_SIZE = 100;

    private final ObjectMapper objectMapper;

    private final BoundaryRelationshipService boundaryRelationshipService;

    private final Producer producer;

    private final ApplicationProperties applicationProperties;

    public BoundaryRelationshipBulkConsumer(ObjectMapper objectMapper, BoundaryRelationshipService boundaryRelationshipService,
                                            Producer producer, ApplicationProperties applicationProperties) {
        this.objectMapper = objectMapper;
        this.boundaryRelationshipService = boundaryRelationshipService;
        this.producer = producer;
        this.applicationProperties = applicationProperties;
    }

    @KafkaListener(topics = {"${kafka.topics.bulk.create.boundary.relationship.job}"})
    public void bulkCreate(HashMap<String, Object> record) {
        BulkBoundaryRelationshipRequest request;
        try {
            request = objectMapper.convertValue(record, BulkBoundaryRelationshipRequest.class);
        } catch (Exception e) {
            // Unparseable/poison message: never retryable. Surface it and let the offset commit.
            log.error("Dropping unparseable bulk boundary relationship job to error topic", e);
            publishRaw(record);
            return;
        }

        // The @Valid bean-validation the controller does does NOT run on the Kafka path, so validate the
        // job structure here. A structurally invalid job is permanent -> error topic, not retried.
        String jobError = validateJob(request);
        if (jobError != null) {
            log.error("Rejecting invalid bulk boundary relationship job ({}); publishing to error topic", jobError);
            publishRaw(record);
            return;
        }

        int requested = request.getBoundaryRelationships().size();
        BulkBoundaryRelationshipResponse response = boundaryRelationshipService.createBulkBoundaryRelationship(request);

        int created = CollectionUtils.isEmpty(response.getSuccessfulBoundaryRelationships()) ? 0 : response.getSuccessfulBoundaryRelationships().size();
        int alreadyDone = 0;
        int retryable = 0;
        List<FailedBoundaryRelationship> permanentFailures = new java.util.ArrayList<>();

        if (!CollectionUtils.isEmpty(response.getFailedBoundaryRelationships())) {
            for (FailedBoundaryRelationship failure : response.getFailedBoundaryRelationships()) {
                if (ALREADY_DONE.contains(failure.getErrorCode())) {
                    alreadyDone++;                 // idempotent success — never an error
                } else if (RETRYABLE.contains(failure.getErrorCode())) {
                    retryable++;                   // redeliver the whole job
                } else {
                    permanentFailures.add(failure); // genuine bad data
                }
            }
        }

        log.info("Bulk boundary relationship job: requested={}, created={}, alreadyExisting={}, retryable={}, permanentFailures={}",
                requested, created, alreadyDone, retryable, permanentFailures.size());

        // Transient failures: throw FIRST so the container redelivers the whole job with non-blocking
        // backoff (idempotent via ON CONFLICT DO NOTHING + DUPLICATE_RECORD-as-success). Publishing
        // permanent failures is deferred to the terminal pass below, so they are NOT re-emitted to the
        // error topic on every redelivery round.
        if (retryable > 0) {
            throw new TransientRelationshipException(
                    retryable + " relationship(s) await parent/entity persistence; redelivering job");
        }

        // Terminal pass (nothing left to retry): surface permanent failures once for reconciliation.
        if (!permanentFailures.isEmpty()) {
            safePublish(BulkBoundaryRelationshipResponse.builder().failedBoundaryRelationships(permanentFailures).build());
        }
    }

    private void publishRaw(HashMap<String, Object> record) {
        safePublish(record);
    }

    /**
     * Best-effort publish to the error topic. A failed publish is logged but never rethrown, so a
     * transport blip on the side-channel cannot masquerade as a job-level retry.
     */
    private void safePublish(Object payload) {
        try {
            producer.push(applicationProperties.getBulkCreateBoundaryRelationshipErrorTopic(), payload);
        } catch (Exception ex) {
            log.error("Failed to publish to bulk boundary relationship error topic", ex);
        }
    }

    /**
     * Validates the structural invariants the controller enforces via bean validation (RequestInfo +
     * userInfo present, 1..MAX_BULK_SIZE records, required fields per record). Returns a reason string
     * if invalid, otherwise null.
     */
    private String validateJob(BulkBoundaryRelationshipRequest request) {
        if (request.getRequestInfo() == null || request.getRequestInfo().getUserInfo() == null
                || request.getRequestInfo().getUserInfo().getUuid() == null) {
            return "missing RequestInfo.userInfo.uuid";
        }
        List<BoundaryRelation> relationships = request.getBoundaryRelationships();
        if (CollectionUtils.isEmpty(relationships)) {
            return "empty BoundaryRelationships";
        }
        if (relationships.size() > MAX_BULK_SIZE) {
            return "job size " + relationships.size() + " exceeds max " + MAX_BULK_SIZE;
        }
        for (BoundaryRelation relationship : relationships) {
            if (relationship == null || relationship.getCode() == null || relationship.getTenantId() == null
                    || relationship.getHierarchyType() == null || relationship.getBoundaryType() == null) {
                return "a relationship is missing required fields (code/tenantId/hierarchyType/boundaryType)";
            }
        }
        return null;
    }
}
