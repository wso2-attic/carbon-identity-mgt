package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ClaimDialectDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class ClaimDialectDTO {
    @JsonProperty("claim-dialect-uri")
    private String claimDialectUri = null;

    public ClaimDialectDTO claimDialectUri(String claimDialectUri) {
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


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimDialectDTO claimDialect = (ClaimDialectDTO) o;
        return Objects.equals(this.claimDialectUri, claimDialect.claimDialectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimDialectUri);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClaimDialectDTO {\n");

        sb.append("    claimDialectUri: ").append(toIndentedString(claimDialectUri)).append("\n");
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

