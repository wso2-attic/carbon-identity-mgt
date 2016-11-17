package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LocalClaimDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class LocalClaimDTO   {
  @JsonProperty("mapped-attributes")
  private List<AttributeMappingDTO> mappedAttributes = new ArrayList<AttributeMappingDTO>();

  @JsonProperty("claim-properties")
  private Object claimProperties = null;

  public LocalClaimDTO mappedAttributes(List<AttributeMappingDTO> mappedAttributes) {
    this.mappedAttributes = mappedAttributes;
    return this;
  }

  public LocalClaimDTO addMappedAttributesItem(AttributeMappingDTO mappedAttributesItem) {
    this.mappedAttributes.add(mappedAttributesItem);
    return this;
  }

   /**
   * Get mappedAttributes
   * @return mappedAttributes
  **/
  @ApiModelProperty(value = "")
  public List<AttributeMappingDTO> getMappedAttributes() {
    return mappedAttributes;
  }

  public void setMappedAttributes(List<AttributeMappingDTO> mappedAttributes) {
    this.mappedAttributes = mappedAttributes;
  }

  public LocalClaimDTO claimProperties(Object claimProperties) {
    this.claimProperties = claimProperties;
    return this;
  }

   /**
   * Get claimProperties
   * @return claimProperties
  **/
  @ApiModelProperty(value = "")
  public Object getClaimProperties() {
    return claimProperties;
  }

  public void setClaimProperties(Object claimProperties) {
    this.claimProperties = claimProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalClaimDTO localClaim = (LocalClaimDTO) o;
    return Objects.equals(this.mappedAttributes, localClaim.mappedAttributes) &&
        Objects.equals(this.claimProperties, localClaim.claimProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mappedAttributes, claimProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LocalClaimDTO {\n");
    
    sb.append("    mappedAttributes: ").append(toIndentedString(mappedAttributes)).append("\n");
    sb.append("    claimProperties: ").append(toIndentedString(claimProperties)).append("\n");
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

