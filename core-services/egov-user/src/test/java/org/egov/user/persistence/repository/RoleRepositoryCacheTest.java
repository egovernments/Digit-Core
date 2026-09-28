package org.egov.user.persistence.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleRepositoryCacheTest {

    private static final String URL = "http://mdms/egov-mdms-service/v1/_search";

    private RestTemplate restTemplate;
    private RoleRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        repository = new RoleRepository(null, restTemplate, mapper, null);
        ReflectionTestUtils.setField(repository, "roleMasterName", "roles");
        ReflectionTestUtils.setField(repository, "roleModuleName", "ACCESSCONTROL-ROLES");
        ReflectionTestUtils.setField(repository, "host", "http://mdms");
        ReflectionTestUtils.setField(repository, "path", "/egov-mdms-service/v1/_search");
        ReflectionTestUtils.setField(repository, "rolesCacheTtlMs", 300_000L);
        ReflectionTestUtils.setField(repository, "rolesRetryAfterFailureMs", 30_000L);
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class))).thenReturn(rolesResponse());
    }

    private JsonNode rolesResponse() throws Exception {
        return mapper.readTree("{\"MdmsRes\":{\"ACCESSCONTROL-ROLES\":{\"roles\":["
                + "{\"code\":\"SUPERUSER\",\"name\":\"Super User\",\"description\":\"all\"},"
                + "{\"code\":\"CITIZEN\",\"name\":\"Citizen\",\"description\":\"public\"},"
                + "{\"code\":\"GRO\",\"name\":\"Grievance Routing Officer\"}]}}}");
    }

    private Set<String> codes(Set<Role> roles) {
        return roles.stream().map(Role::getCode).collect(Collectors.toSet());
    }

    @Test
    public void repeated_lookups_for_same_tenant_call_mdms_once() {
        Set<Role> first = repository.findRolesByCode(new HashSet<>(Arrays.asList("SUPERUSER", "UNKNOWN")), "bo");
        Set<Role> second = repository.findRolesByCode(Collections.singleton("GRO"), "bo");

        assertEquals(Collections.singleton("SUPERUSER"), codes(first));
        assertEquals("Super User", first.iterator().next().getName());
        assertEquals(Collections.singleton("GRO"), codes(second));
        verify(restTemplate, times(1)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    public void each_tenant_has_its_own_cache_entry() {
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "oy");
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");

        verify(restTemplate, times(2)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    public void citizen_lookup_keeps_tenant_header_and_separate_entry() {
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
        repository.findRolesByCode(Collections.singleton("CITIZEN"), "bo");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).postForObject(eq(URL), captor.capture(), eq(JsonNode.class));
        assertNull(captor.getAllValues().get(0).getHeaders().getFirst("tenantId"));
        assertEquals("bo", captor.getAllValues().get(1).getHeaders().getFirst("tenantId"));
    }

    @Test
    public void refetches_after_ttl() throws Exception {
        ReflectionTestUtils.setField(repository, "rolesCacheTtlMs", 1L);
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
        Thread.sleep(5);
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");

        verify(restTemplate, times(2)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    public void serves_cached_roles_when_refresh_fails() throws Exception {
        ReflectionTestUtils.setField(repository, "rolesCacheTtlMs", 1L);
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
        Thread.sleep(5);
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("mdms down"));

        assertEquals(Collections.singleton("SUPERUSER"),
                codes(repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo")));
    }

    @Test(expected = ResourceAccessException.class)
    public void first_fetch_failure_propagates() {
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("mdms down"));
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
    }

    @Test
    public void returned_roles_are_copies_of_cache() {
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo").iterator().next().setName("changed");

        assertEquals("Super User",
                repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo").iterator().next().getName());
    }

    private void expireCache() throws Exception {
        ReflectionTestUtils.setField(repository, "rolesCacheTtlMs", 1L);
        repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
        Thread.sleep(5);
    }

    @Test
    public void failed_refresh_backs_off_instead_of_calling_mdms_every_request() throws Exception {
        expireCache();
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("mdms down"));
        for (int i = 0; i < 20; i++) {
            assertEquals(Collections.singleton("SUPERUSER"),
                    codes(repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo")));
        }
        verify(restTemplate, times(2)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    public void empty_mdms_response_does_not_replace_cached_roles() throws Exception {
        expireCache();
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(mapper.readTree("{\"MdmsRes\":{}}"));
        assertEquals(Collections.singleton("SUPERUSER"),
                codes(repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo")));
    }

    @Test
    public void concurrent_cold_start_calls_mdms_once() throws Exception {
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class))).thenAnswer(inv -> {
            Thread.sleep(200);
            return rolesResponse();
        });
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(50);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<Set<Role>>> results = new java.util.ArrayList<>();
        for (int i = 0; i < 200; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo");
            }));
        }
        start.countDown();
        for (java.util.concurrent.Future<Set<Role>> f : results) {
            assertEquals(Collections.singleton("SUPERUSER"), codes(f.get(10, java.util.concurrent.TimeUnit.SECONDS)));
        }
        pool.shutdown();
        verify(restTemplate, times(1)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }

    @Test
    public void slow_refresh_does_not_block_other_requests() throws Exception {
        expireCache();
        java.util.concurrent.CountDownLatch inFetch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        when(restTemplate.postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class))).thenAnswer(inv -> {
            inFetch.countDown();
            release.await();
            return rolesResponse();
        });
        Thread refresher = new Thread(() -> repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo"));
        refresher.start();
        inFetch.await();

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(20);
        java.util.List<java.util.concurrent.Future<Set<Role>>> results = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            results.add(pool.submit(() -> repository.findRolesByCode(Collections.singleton("SUPERUSER"), "bo")));
        }
        for (java.util.concurrent.Future<Set<Role>> f : results) {
            assertEquals(Collections.singleton("SUPERUSER"), codes(f.get(2, java.util.concurrent.TimeUnit.SECONDS)));
        }
        release.countDown();
        refresher.join();
        pool.shutdown();
        verify(restTemplate, times(2)).postForObject(eq(URL), any(HttpEntity.class), eq(JsonNode.class));
    }
}
