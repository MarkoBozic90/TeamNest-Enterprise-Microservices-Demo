package com.teamnest.userservice.model;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SignupProperties {
    @Value("${signup.base-url:https://app.example.com/signup}")
    private String signupBaseUrl;

    @Value("${signup.deep-link}")
    private String signupDeepLink;
}
