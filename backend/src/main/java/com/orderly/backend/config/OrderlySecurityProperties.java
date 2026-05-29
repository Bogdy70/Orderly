package com.orderly.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orderly.security")
public class OrderlySecurityProperties {
  private boolean requireVerifiedEmail = false;

  public boolean isRequireVerifiedEmail() {
    return requireVerifiedEmail;
  }

  public void setRequireVerifiedEmail(boolean requireVerifiedEmail) {
    this.requireVerifiedEmail = requireVerifiedEmail;
  }
}
