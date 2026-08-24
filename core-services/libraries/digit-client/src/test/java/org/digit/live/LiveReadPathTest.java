package org.digit.live;

import org.digit.config.PropagationProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.account.AccountClient;
import org.digit.services.billing.BillingClient;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.employee.EmployeeClient;
import org.digit.services.filestore.FilestoreClient;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.individual.IndividualClient;
import org.digit.services.mdms.MdmsClient;
import org.digit.services.notification.NotificationClient;
import org.digit.services.notification.model.TemplateSearchCriteria;
import org.digit.services.otp.OtpClient;
import org.digit.services.registry.RegistryClient;
import org.digit.services.workflow.WorkflowClient;
import org.digit.util.DigitJson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Calls every client against the running services, one test per client.
 *
 * <p>Read paths only: this suite is safe to run repeatedly against a shared environment and leaves
 * nothing behind. Writes belong in their own suite, where the data they create can be named per run
 * and cleaned up.
 *
 * <p>What a passing test here proves, which no mocked test can: the URL the client builds is one the
 * service actually serves, the headers the interceptor attaches are the ones it requires, and the
 * real response body deserializes into the SDK's model without throwing. That is precisely the class
 * of defect the esMagico gap report was full of.
 *
 * <p>Excluded from {@code mvn test} by the {@code live} tag. Run with {@code e2e/run-live-tests.sh}.
 */
@Tag("live")
class LiveReadPathTest {

    @BeforeEach
    void installContext() {
        // Per-test rather than once per class: the holder is a ThreadLocal, so binding it in
        // @BeforeAll would depend on JUnit keeping every test on the discovery thread.
        LiveEnv.installContext();
    }

    @AfterEach
    void clearContext() {
        LiveEnv.clearContext();
    }

    @Test
    void account_listsTenantsAndConfigs() {
        AccountClient client = new AccountClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("account"), "account unreachable");

        assertNotNull(client.searchTenants(), "tenant search returned no envelope");
        assertNotNull(client.searchTenantConfigs(), "tenant-config search returned no envelope");
    }

    @Test
    void employee_searchesByUserIds() {
        EmployeeClient client = new EmployeeClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("employee"), "employee unreachable");
        assumeTrue(LiveEnv.USER_ID != null, "no DIGIT_USER_ID configured");

        // The userIds filter this branch added. An empty result is a pass: what is under test is that
        // the filter round-trips and the response parses, not that this tenant has that employee.
        assertNotNull(client.searchEmployeesByUserIds(List.of(LiveEnv.USER_ID)));
    }

    @Test
    void individual_readsConfigAndSearches() {
        IndividualClient client = new IndividualClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("individual"), "individual unreachable");

        // Null is the documented answer when the tenant has no config, and the service really does
        // answer 404 there, so this asserts the client maps that to null instead of throwing.
        client.getIndividualConfig();
        assertNotNull(client.searchIndividualsByUserIds(List.of(LiveEnv.USER_ID), 1, 5));
    }

    @Test
    void workflow_listsProcessDefinitions() {
        WorkflowClient client = new WorkflowClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("workflow"), "workflow unreachable");

        assertNotNull(client.listProcessDefinitions());
    }

    @Test
    void registry_listsSchemas() {
        RegistryClient client = new RegistryClient(LiveEnv.restTemplate(), LiveEnv.properties(), null);
        assumeTrue(LiveEnv.reachable("registry"), "registry unreachable");

        assertNotNull(client.listSchemas());
    }

    @Test
    void idgen_searchesTemplates() {
        IdGenClient client = new IdGenClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("idgen"), "idgen unreachable");

        assertNotNull(client.searchTemplates(null, null, null, null, null));
    }

    @Test
    void otp_listsConfigs() {
        OtpClient client = new OtpClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("otp"), "otp unreachable");

        assertNotNull(client.listOtpConfigs());
    }

    @Test
    void notification_searchesTemplates() {
        NotificationClient client = new NotificationClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("notification"), "notification unreachable");

        assertNotNull(client.searchTemplates(
                TemplateSearchCriteria.builder().limit(5).offset(0).build()));
    }

    @Test
    void boundary_searchesByCodes() {
        BoundaryClient client = new BoundaryClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("boundary"), "boundary unreachable");

        // A code that cannot exist: the point is that the request shape is accepted and the empty
        // response parses, not that any particular boundary is present in this tenant.
        assertNotNull(client.searchBoundariesByCodes(List.of("NO_SUCH_BOUNDARY_CODE")));
    }

    @Test
    void filestore_reportsAMissingFileAsUnavailable() {
        FilestoreClient client = new FilestoreClient(
                LiveEnv.restTemplate(), LiveEnv.properties(), new PropagationProperties());
        assumeTrue(LiveEnv.reachable("filestore"), "filestore unreachable");

        // Asserts the negative rather than just non-null: a client that reported every id as present
        // would still pass a not-null check.
        assertFalse(client.isFileAvailable("00000000-0000-0000-0000-000000000000"));
    }

    @Test
    void billing_searchesDemands() {
        BillingClient client = new BillingClient(
                LiveEnv.restTemplate(), LiveEnv.properties(), DigitJson.mapper());
        assumeTrue(LiveEnv.reachable("billing"), "billing unreachable");

        assertNotNull(client.searchDemands("PT", "NO-SUCH-CONSUMER-CODE"));
    }

    @Test
    void headerPropagationIsWhatMakesAuthenticatedCallsWork() {
        // Gateway-only: pod-direct calls bypass Kong, which is where the token is checked, so there
        // the negative case would pass for the wrong reason.
        assumeTrue(LiveEnv.mode() == LiveEnv.Mode.GATEWAY, "gateway mode only");
        assumeTrue(LiveEnv.TOKEN != null, "no DIGIT_TOKEN configured");
        EmployeeClient client = new EmployeeClient(LiveEnv.restTemplate(), LiveEnv.properties());

        // With the context installed by @BeforeEach the interceptor attaches the token and this works.
        assertNotNull(client.searchEmployeesByUserIds(List.of(LiveEnv.USER_ID)));

        // Drop the context and the same call must be rejected. Without this half the suite would pass
        // even if the interceptor attached nothing, since a route that needs no auth cannot tell the
        // difference. /employee answers 401 unauthenticated, so this is what proves propagation.
        LiveEnv.clearContext();
        DigitClientException rejected = assertThrows(DigitClientException.class,
                () -> client.searchEmployeesByUserIds(List.of(LiveEnv.USER_ID)));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), rejected.getHttpStatus().value(),
                "expected 401 once the token was no longer propagated");
    }

    @Test
    void mdms_reportsAnUnknownSchemaAsInvalid() {
        MdmsClient client = new MdmsClient(LiveEnv.restTemplate(), LiveEnv.properties());
        assumeTrue(LiveEnv.reachable("mdms"), "mdms unreachable");

        // A schema that cannot exist must come back invalid, not as an exception. Asserting the
        // answer rather than merely "no crash" is what makes this catch the failure it originally
        // found: without a client id MdmsClient refuses to call at all, and a laxer assertion passed
        // on that client-side rejection while never reaching the service.
        assertFalse(client.isMdmsDataValid("NOSUCH.Schema", Set.of("x")));
    }
}
