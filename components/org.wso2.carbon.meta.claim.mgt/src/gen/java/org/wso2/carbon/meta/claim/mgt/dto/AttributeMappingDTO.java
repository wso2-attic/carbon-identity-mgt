package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * AttributeMappingDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class AttributeMappingDTO   {
  @JsonProperty("user-store.domain")
  private String userStoreDomain = null;

  @JsonProperty("attribute-name")
  private String attributeName = null;

  public AttributeMappingDTO userStoreDomain(String userStoreDomain) {
    this.userStoreDomain = userStoreDomain;
    return this;
  }

   /**
   * Get userStoreDomain
   * @return userStoreDomain
  **/
  @ApiModelProperty(value = "")
  public String getUserStoreDomain() {
    return userStoreDomain;
  }

  public void setUserStoreDomain(String userStoreDomain) {
    this.userStoreDomain = userStoreDomain;
  }

  public AttributeMappingDTO attributeName(String attributeName) {
    this.attributeName = attributeName;
    return this;
  }

   /**
   * Get attributeName
   * @return attributeName
  **/
  @ApiModelProperty(value = "")
  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributeMappingDTO attributeMapping = (AttributeMappingDTO) o;
    return Objects.equals(this.userStoreDomain, attributeMapping.userStoreDomain) &&
        Objects.equals(this.attributeName, attributeMapping.attributeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userStoreDomain, attributeName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AttributeMappingDTO {\n");
    
    sb.append("    userStoreDomain: ").append(toIndentedString(userStoreDomain)).append("\n");
    sb.append("    attributeName: ").append(toIndentedString(attributeName)).append("\n");
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

