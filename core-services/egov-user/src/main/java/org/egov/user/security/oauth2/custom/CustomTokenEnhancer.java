package org.egov.user.security.oauth2.custom;

import org.egov.user.domain.model.SecureUser;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CustomTokenEnhancer extends TokenEnhancerChain {

    /**
     * Enhances the OAuth2 access token with additional user information.
     * Adds ResponseInfo metadata and UserRequest details to the token's additional information.
     * This allows clients to receive user information along with the access token.
     *
     * @param accessToken the base OAuth2 access token to enhance
     * @param authentication the OAuth2 authentication containing user details
     * @return the enhanced OAuth2 access token with user information in additionalInfo
     */
    @Override
    public OAuth2AccessToken enhance(final OAuth2AccessToken accessToken, final OAuth2Authentication authentication) {
        final DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;

        SecureUser su = (SecureUser) authentication.getUserAuthentication().getPrincipal();
        final Map<String, Object> info = new LinkedHashMap<String, Object>();
        final Map<String, Object> responseInfo = new LinkedHashMap<String, Object>();

        responseInfo.put("api_id", "");
        responseInfo.put("ver", "");
        responseInfo.put("ts", "");
        responseInfo.put("res_msg_id", "");
        responseInfo.put("msg_id", "");
        responseInfo.put("status", "Access Token generated successfully");
        info.put("ResponseInfo", responseInfo);
        info.put("UserRequest", su.getUser());

        token.setAdditionalInformation(info);

        return super.enhance(token, authentication);
    }


}