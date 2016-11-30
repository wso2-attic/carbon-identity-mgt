package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ExternalClaimDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class ExternalClaimDTO {
    @JsonProperty("claim-dialect-uri")
    private String claimDialectUri = null;

    @JsonProperty("claim-uri")
    private String claimUri = null;

    @JsonProperty("local-claim")
    private String localClaim = null;

    public ExternalClaimDTO claimDialectUri(String claimDialectUri) {
        this.claimDialectUri = claimDialectUri;
        return this;
    }

    /**
     * Get claimDialectUri
     *
     * @return claimDialectUri
     **/
    @ApiModelProperty(value = "")
    public String getClaimDialectUri() {
        return claimDialectUri;
    }

    public void setClaimDialectUri(String claimDialectUri) {
        this.claimDialectUri = claimDialectUri;
    }

    public ExternalClaimDTO claimUri(String claimUri) {
        this.claimUri = claimUri;
        return this;
    }

    /**
     * Get claimUri
     *
     * @return claimUri
     **/
    @ApiModelProperty(value = "")
    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public ExternalClaimDTO localClaim(String localClaim) {
        this.localClaim = localClaim;
        return this;
    }

    /**
     * Get localClaim
     *
     * @return localClaim
     **/
    @ApiModelProperty(value = "")
    public String getLocalClaim() {
        return localClaim;
    }

    public void setLocalClaim(String localClaim) {
        this.localClaim = localClaim;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalClaimDTO externalClaim = (ExternalClaimDTO) o;
        return Objects.equals(this.claimDialectUri, externalClaim.claimDialectUri) &&
                Objects.equals(this.claimUri, externalClaim.claimUri) &&
                Objects.equals(this.localClaim, externalClaim.localClaim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimDialectUri, claimUri, localClaim);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalClaimDTO {\n");

        sb.append("    claimDialectUri: ").append(toIndentedString(claimDialectUri)).append("\n");
        sb.append("    claimUri: ").append(toIndentedString(claimUri)).append("\n");
        sb.append("    localClaim: ").append(toIndentedString(localClaim)).append("\n");
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

