package com.soaengry.moment.global.security.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    String getProfileImageUrl();
}
