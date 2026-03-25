package org.egov.user.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class FileStoreRepositoryTest {

    @InjectMocks
    private FileStoreRepository fileStoreRepository;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void test_should_geturl_by_fileStoreId() {

        Map<String, String> expectedFileStoreUrls = new HashMap<String, String>();
        expectedFileStoreUrls.put("key", "value");
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(expectedFileStoreUrls);
        Map<String, String> fileStoreUrl = null;
        try {
            List<String> list = new ArrayList<String>();
            list.add("key");
            fileStoreUrl = fileStoreRepository.getUrlByFileStoreId("default", list);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertEquals(fileStoreUrl.get("key"), "value");
    }

    @Test
    public void test_should_return_null_ifurllist_isempty() {
        Map<String, String> expectedFileStoreUrls = new HashMap<String, String>();
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(expectedFileStoreUrls);
        Map<String, String> fileStoreUrl = null;
        try {
            List<String> list = new ArrayList<String>();
            list.add("key");
            fileStoreUrl = fileStoreRepository.getUrlByFileStoreId("default", list);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assertions.assertNull(fileStoreUrl);
    }

    @Test
    public void test_should_return_null_ifurllist_null() {
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenReturn(null);
        Map<String, String> fileStoreUrl = null;
        try {
            List<String> list = new ArrayList<String>();
            list.add("key");
            fileStoreUrl = fileStoreRepository.getUrlByFileStoreId("default", list);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assertions.assertNull(fileStoreUrl);
    }

    @Test
    public void test_should_throwexception_restcallfails() throws Exception {
        when(restTemplate.getForObject(any(String.class), eq(Map.class))).thenThrow(new RuntimeException());
        Assertions.assertThrows(RuntimeException.class, () -> {
            List<String> list = new ArrayList<String>();
            list.add("key");
            fileStoreRepository.getUrlByFileStoreId("default", list);
        });
    }

}
