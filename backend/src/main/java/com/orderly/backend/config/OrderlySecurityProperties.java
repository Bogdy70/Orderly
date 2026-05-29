package com.orderly.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orderly.security")
public class OrderlySecurityProperties {
  private boolean requireVerifiedEmail = true;

  public boolean isRequireVerifiedEmail() {
    return requireVerifiedEmail;
  }

  public void setRequireVerifiedEmail(boolean requireVerifiedEmail) {
    this.requireVerifiedEmail = requireVerifiedEmail;
  }
}
