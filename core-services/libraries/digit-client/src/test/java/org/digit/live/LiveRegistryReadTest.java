package org.digit.live;

import org.digit.services.registry.RegistryClient;
import org.digit.services.registry.model.RegistrySchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Read endpoints on RegistryClient that need no pre-existing records.
 *
 * <p>The data reads — {@code searchRegistryData} and {@code getRegistryDataById} — are deliberately
 * absent: every one of them needs a record that this suite would have to create first, and a read
 * test that creates data is a write test wearing the wrong name. They belong with the write suite,
 * where the record's lifecycle is owned end to end.
 *
 * <p>Built with a null RedisTemplate so the version cache is off. That is the important
 * configuration to test: the cache exists to skip the search before an update, so leaving it on
 * would let a cached id stand in for the live read under test.
 */
class LiveRegistryReadTest extends LiveReadSupport {

    private final RegistryClient client =
            new RegistryClient(LiveEnv.restTemplate(), LiveEnv.properties(), null);

    @Override
    String service() {
        return "registry";
    }

    @Test
    void listSchemas() {
        List<RegistrySchema> schemas = client.listSchemas();
        assertNotNull(schemas);
        assertKeptEveryField(schemas);
    }

    @Test
    void getSchemaByCode() {
        RegistrySchema first = firstSchema();
        RegistrySchema found = client.getSchema(first.getSchemaCode(), null);
        assertNotNull(found, "a schema code taken from the list did not resolve");
        assertEquals(first.getSchemaCode(), found.getSchemaCode());
        assertKeptEveryField(found);
    }

    @Test
    void getSchemaAtAPinnedVersion() {
        RegistrySchema first = firstSchema();
        RegistrySchema pinned = client.getSchema(first.getSchemaCode(), first.getVersion());
        assertNotNull(pinned);
        // Asking for a specific version must return that version, not merely the latest.
        assertEquals(first.getVersion(), pinned.getVersion());
        assertKeptEveryField(pinned);
    }

    private RegistrySchema firstSchema() {
        List<RegistrySchema> schemas = client.listSchemas();
        assumeTrue(schemas != null && !schemas.isEmpty(), "no registry schemas in this environment");
        assumeTrue(schemas.get(0).getSchemaCode() != null, "first schema has no code");
        return schemas.get(0);
    }
}
