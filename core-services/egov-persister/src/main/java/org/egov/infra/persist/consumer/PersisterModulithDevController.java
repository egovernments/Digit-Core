package org.egov.infra.persist.consumer;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.persist.web.contract.TopicMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * DEVELOPMENT-ONLY trigger to publish a {@link PersistEvent} on demand and watch
 * {@link PersisterModulithListener} react in the logs.
 *
 * <p><b>Not a production entry point.</b> In production, events are fed from a producer
 * module (or a Kafka-to-event bridge), not an HTTP call — an HTTP "persist" surface would
 * regress persister's async/durable/ordered semantics and expose arbitrary mapped writes.
 * This controller exists only as a convenience to observe the in-process flow, and is
 * registered only when {@code persister.modulith.enabled=true}.
 *
 * <p>The publish is wrapped in a {@link TransactionTemplate} because
 * {@code @ApplicationModuleListener} fires on {@code AFTER_COMMIT}: publishing outside a
 * transaction would never trigger the listener. Running the commit inside {@code execute()}
 * also keeps any listener-side error contained here rather than surfacing as an HTTP 500.
 */
@RestController
@Slf4j
@ConditionalOnProperty(value = "persister.modulith.enabled", havingValue = "true", matchIfMissing = false)
public class PersisterModulithDevController {

    @Autowired
    private PersistEventPublisher publisher;

    @Autowired
    private TopicMap topicMap;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * Publish a payload to a chosen topic to drive an actual mapping end-to-end.
     * With no {@code topic}, the first topic loaded from the persister config is used.
     */
    @PostMapping("/_modulith/publish")
    public ResponseEntity<String> publish(@RequestParam(required = false) String topic,
                                          @RequestBody(required = false) Map<String, Object> payload) {
        String resolvedTopic = (topic != null && !topic.isBlank())
                ? topic
                : topicMap.getTopicMap().keySet().stream().findFirst().orElse(null);

        if (resolvedTopic == null) {
            return ResponseEntity.badRequest()
                    .body("No topic provided and no topics are loaded in the persister config.");
        }

        final String targetTopic = resolvedTopic;
        final Map<String, Object> body = (payload != null) ? payload : Map.of();
        log.info("DEV trigger: publishing PersistEvent for topic: {}", targetTopic);

        try {
            new TransactionTemplate(transactionManager)
                    .executeWithoutResult(status -> publisher.publish(targetTopic, body));
        } catch (Exception e) {
            log.warn("Listener ran but processing the payload failed "
                    + "(expected when the demo payload doesn't match the topic mapping)", e);
            return ResponseEntity.ok("Published PersistEvent for topic '" + targetTopic
                    + "'. Listener ran; persist failed for this payload: " + e.getMessage()
                    + " — check the logs.");
        }

        return ResponseEntity.ok("Published PersistEvent for topic '" + targetTopic
                + "'. Listener ran — check the logs for output.");
    }
}
