/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.user.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SelfUserRegistrationRequestDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-01T19:30:42.380+05:30")
public class SelfUserRegistrationRequestDTO   {
  @JsonProperty("user")
  private SelfRegistrationUserDTO user = null;

  @JsonProperty("properties")
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  public SelfUserRegistrationRequestDTO user(SelfRegistrationUserDTO user) {
    this.user = user;
    return this;
  }

   /**
   * Get user
   * @return user
  **/
  @ApiModelProperty(value = "")
  public SelfRegistrationUserDTO getUser() {
    return user;
  }

  public void setUser(SelfRegistrationUserDTO user) {
    this.user = user;
  }

  public SelfUserRegistrationRequestDTO properties(List<PropertyDTO> properties) {
    this.properties = properties;
    return this;
  }

  public SelfUserRegistrationRequestDTO addPropertiesItem(PropertyDTO propertiesItem) {
    this.properties.add(propertiesItem);
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public List<PropertyDTO> getProperties() {
    return properties;
  }

  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SelfUserRegistrationRequestDTO selfUserRegistrationRequest = (SelfUserRegistrationRequestDTO) o;
    return Objects.equals(this.user, selfUserRegistrationRequest.user) &&
        Objects.equals(this.properties, selfUserRegistrationRequest.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SelfUserRegistrationRequestDTO {\n");
    
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
