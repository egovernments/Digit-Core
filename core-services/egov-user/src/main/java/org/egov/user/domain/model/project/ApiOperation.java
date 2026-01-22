package org.egov.user.domain.model.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Specify the type of operation being performed i.e. CREATE, UPDATE or DELETE
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum ApiOperation {
  
  CREATE("CREATE"),
  
  UPDATE("UPDATE"),
  
  DELETE("DELETE");

  private String value;

  ApiOperation(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ApiOperation fromValue(String text) {
    for (ApiOperation b : ApiOperation.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

