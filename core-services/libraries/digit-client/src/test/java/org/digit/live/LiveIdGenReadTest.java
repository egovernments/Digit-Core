package org.digit.live;

import org.digit.services.idgen.IdGenClient;
import org.digit.services.idgen.model.IdGenTemplate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Read endpoints on IdGenClient.
 *
 * <p>Generation is a write — it advances a sequence counter that nothing can put back — so it stays
 * out of this suite. That matters more here than elsewhere: a burnt id in a shared tenant is exactly
 * how the {@code individualid} collisions during the employee work started.
 */
class LiveIdGenReadTest extends LiveReadSupport {

    private final IdGenClient client = new IdGenClient(LiveEnv.restTemplate(), LiveEnv.properties());

    @Override
    String service() {
        return "idgen";
    }

    @Test
    void searchTemplates() {
        List<IdGenTemplate> templates = client.searchTemplates(null, null, null, null, null);
        assertNotNull(templates);
        assertKeptEveryField(templates);
    }

    @Test
    void searchTemplatesPaged() {
        List<IdGenTemplate> page = client.searchTemplates(null, null, null, 1, 0);
        assertNotNull(page);
        assertTrue(page.size() <= 1, "limit=1 returned " + page.size() + " templates");
        assertKeptEveryField(page);
    }

    @Test
    void searchTemplatesByCode() {
        String code = firstTemplateCode();
        List<IdGenTemplate> found = client.searchTemplates(code, null, null, null, null);
        assertNotNull(found);
        assertFalse(found.isEmpty(), "filtering by a code from the list returned nothing");
        // Filtering must actually filter, rather than returning the unfiltered list.
        assertEquals(List.of(code),
                found.stream().map(IdGenTemplate::getTemplateCode).distinct().toList());
        assertKeptEveryField(found);
    }

    @Test
    void getTemplateByCode() {
        String code = firstTemplateCode();
        IdGenTemplate template = client.getTemplate(code);
        assertNotNull(template, "a code taken from the template list did not resolve");
        assertEquals(code, template.getTemplateCode());
        assertKeptEveryField(template);
    }

    @Test
    void templateExists() {
        assertTrue(client.templateExists(firstTemplateCode()));
        // The negative matters as much: an implementation that answered true for everything would
        // pass the positive case alone.
        assertFalse(client.templateExists("NO-SUCH-TEMPLATE-" + System.nanoTime()));
    }

    private String firstTemplateCode() {
        List<IdGenTemplate> templates = client.searchTemplates(null, null, null, null, null);
        assumeTrue(templates != null && !templates.isEmpty(), "no idgen templates in this environment");
        String code = templates.get(0).getTemplateCode();
        assumeTrue(code != null, "first template has no code");
        return code;
    }
}
