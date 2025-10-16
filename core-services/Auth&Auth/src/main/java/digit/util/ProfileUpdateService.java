package digit.util;

import lombok.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

public class ProfileUpdateService {

    public String updateProfile() {
        String url = "https://digit-lts.digit.org/user/profile/_update?tenantId=pg&_=1710244148558";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authority", "digit-lts.digit.org");
        headers.set("accept", "application/json, text/plain, */*");
        headers.set("accept-language", "en-US,en;q=0.9");
        headers.set("cookie", "_ga=GA1.1.24958639.1705385219; _ga_V8656WJB3Z=GS1.1.1705387149.1.0.1705387163.0.0.0; __cuid=fbea9e1d7aa64a6081daea421f2810c8; amp_fef1e8=a8820a05-e48e-4cad-a2b8-fd9fc70558cfR...1hniiufbl.1hniiufbm.5e.3.5h; _ga_H9YC8FEN6F=GS1.1.1708947094.10.1.1708947115.39.0.0");
        headers.set("origin", "https://digit-lts.digit.org");
        headers.set("referer", "https://digit-lts.digit.org/digit-ui/employee/user/profile");
        headers.set("sec-ch-ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"");
        headers.set("sec-ch-ua-mobile", "?0");
        headers.set("sec-ch-ua-platform", "\"Windows\"");
        headers.set("sec-fetch-dest", "empty");
        headers.set("sec-fetch-mode", "cors");
        headers.set("sec-fetch-site", "same-origin");
        headers.set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        ProfileUpdateRequest requestBody = new ProfileUpdateRequest();
        requestBody.setUser(new User(338, "afc7eaf1-a25f-46c9-b16f-3f7de29009ff", "EGOvM134NmNmd", "gudduu1",
                "9353822214", "xyz123@egovernments.org", "string", "EMPLOYEE", true, "pg", "Kaikoo", "MALE", null));
        requestBody.setRequestInfo(new RequestInfo("Rainmaker", "11926af5-050a-4fd6-a258-350ce179ff7f", "1710244148558|en_IN"));

        HttpEntity<ProfileUpdateRequest> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            return "Failed to update profile. Status code: " + responseEntity.getStatusCodeValue();
        }
    }

    @Getter
    @Setter
    public static class ProfileUpdateRequest {
        private User user;
        private RequestInfo RequestInfo;

        // getters and setters
    }

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class User {
        private int id;
        private String uuid;
        private String userName;
        private String name;
        private String mobileNumber;
        private String emailId;
        private String locale;
        private String type;
        private boolean active;
        private String tenantId;
        private String permanentCity;
        private String gender;
        private Object photo;

        // constructor, getters, and setters
    }

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RequestInfo {
        private String apiId;
        private String authToken;
        private String msgId;

        // constructor, getters, and setters
    }
}

