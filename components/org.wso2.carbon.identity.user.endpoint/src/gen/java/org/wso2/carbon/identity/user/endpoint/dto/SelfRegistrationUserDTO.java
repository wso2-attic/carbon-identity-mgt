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
 * SelfRegistrationUserDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-01T19:30:42.380+05:30")
public class SelfRegistrationUserDTO   {
  @JsonProperty("username")
  private String username = null;

  @JsonProperty("domain")
  private String domain = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("claims")
  private List<ClaimDTO> claims = new ArrayList<ClaimDTO>();

  public SelfRegistrationUserDTO username(String username) {
    this.username = username;
    return this;
  }

   /**
   * Get username
   * @return username
  **/
  @ApiModelProperty(value = "")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public SelfRegistrationUserDTO domain(String domain) {
    this.domain = domain;
    return this;
  }

   /**
   * Get domain
   * @return domain
  **/
  @ApiModelProperty(value = "")
  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public SelfRegistrationUserDTO password(String password) {
    this.password = password;
    return this;
  }

   /**
   * Get password
   * @return password
  **/
  @ApiModelProperty(value = "")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public SelfRegistrationUserDTO claims(List<ClaimDTO> claims) {
    this.claims = claims;
    return this;
  }

  public SelfRegistrationUserDTO addClaimsItem(ClaimDTO claimsItem) {
    this.claims.add(claimsItem);
    return this;
  }

   /**
   * Get claims
   * @return claims
  **/
  @ApiModelProperty(value = "")
  public List<ClaimDTO> getClaims() {
    return claims;
  }

  public void setClaims(List<ClaimDTO> claims) {
    this.claims = claims;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SelfRegistrationUserDTO selfRegistrationUser = (SelfRegistrationUserDTO) o;
    return Objects.equals(this.username, selfRegistrationUser.username) &&
        Objects.equals(this.domain, selfRegistrationUser.domain) &&
        Objects.equals(this.password, selfRegistrationUser.password) &&
        Objects.equals(this.claims, selfRegistrationUser.claims);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, domain, password, claims);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SelfRegistrationUserDTO {\n");
    
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    claims: ").append(toIndentedString(claims)).append("\n");
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
