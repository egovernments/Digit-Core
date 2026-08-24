package org.digit.live;

import org.digit.services.account.AccountClient;
import org.digit.services.account.model.Tenant;
import org.digit.services.account.model.TenantConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Every read endpoint on AccountClient: tenant search and tenant-config search. */
class LiveAccountReadTest extends LiveReadSupport {

    private final AccountClient client = new AccountClient(LiveEnv.restTemplate(), LiveEnv.properties());

    @Override
    String service() {
        return "account";
    }

    @Test
    void searchTenants() {
        var response = client.searchTenants();
        assertNotNull(response);
        assertKeptEveryField(response);
    }

    @Test
    void searchTenantsPaged() {
        var response = client.searchTenants(null, null, 1, 5);
        assertNotNull(response);
        assumeTrue(response.getTenants() != null, "no tenants to page over");
        // The page the service reports must be the page that was asked for; a client that dropped the
        // parameter would still return a valid-looking first page.
        assertEquals(1, response.getPage());
        assertKeptEveryField(response);
    }

    @Test
    void getTenantByName() {
        List<Tenant> tenants = firstPageOfTenants();
        assumeTrue(!tenants.isEmpty(), "no tenants in this environment");
        String name = tenants.get(0).getName();
        assumeTrue(name != null, "first tenant has no name to look up");

        Tenant found = client.getTenantByName(name);
        assertNotNull(found, "a name taken from the tenant list did not resolve");
        assertEquals(name, found.getName());
        assertKeptEveryField(found);
    }

    @Test
    void searchTenantConfigs() {
        var response = client.searchTenantConfigs();
        assertNotNull(response);
        assertKeptEveryField(response);
    }

    @Test
    void searchTenantConfigsFiltered() {
        List<TenantConfig> configs = firstPageOfConfigs();
        assumeTrue(!configs.isEmpty(), "no tenant configs in this environment");
        String key = configs.get(0).getConfigKey();

        var response = client.searchTenantConfigs(key, 1, 5);
        assertNotNull(response);
        assertNotNull(response.getConfigs());
        // Filtering by a key taken from the list must return that key, not everything.
        assertEquals(List.of(key), response.getConfigs().stream().map(TenantConfig::getConfigKey).distinct().toList());
        assertKeptEveryField(response);
    }

    @Test
    void getTenantConfigByKey() {
        List<TenantConfig> configs = firstPageOfConfigs();
        assumeTrue(!configs.isEmpty(), "no tenant configs in this environment");
        String key = configs.get(0).getConfigKey();

        TenantConfig config = client.getTenantConfig(key);
        assertNotNull(config, "a key taken from the config list did not resolve");
        assertEquals(key, config.getConfigKey());
        assertKeptEveryField(config);
    }

    @Test
    void getTenantConfigValueByKey() {
        List<TenantConfig> configs = firstPageOfConfigs();
        assumeTrue(!configs.isEmpty(), "no tenant configs in this environment");
        TenantConfig first = configs.get(0);
        assumeTrue(first.getConfigValue() != null, "first config has no value");

        // The convenience accessor must agree with the full object it is a shortcut for.
        assertEquals(first.getConfigValue(), client.getTenantConfigValue(first.getConfigKey()));
    }

    private List<Tenant> firstPageOfTenants() {
        var response = client.searchTenants();
        return response == null || response.getTenants() == null ? List.of() : response.getTenants();
    }

    private List<TenantConfig> firstPageOfConfigs() {
        var response = client.searchTenantConfigs();
        return response == null || response.getConfigs() == null ? List.of() : response.getConfigs();
    }
}
