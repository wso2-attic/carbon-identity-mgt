package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * PairOfClaimsDTO
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class PairOfClaimsDTO {
    @JsonProperty("old-claim")
    private PairOfClaimsDTO oldClaim = null;

    @JsonProperty("new-claim")
    private PairOfClaimsDTO newClaim = null;

    public PairOfClaimsDTO oldClaim(PairOfClaimsDTO oldClaim) {
        this.oldClaim = oldClaim;
        return this;
    }

    /**
     * Get oldClaim
     *
     * @return oldClaim
     **/
    @ApiModelProperty(value = "")
    public PairOfClaimsDTO getOldClaim() {
        return oldClaim;
    }

    public void setOldClaim(PairOfClaimsDTO oldClaim) {
        this.oldClaim = oldClaim;
    }

    public PairOfClaimsDTO newClaim(PairOfClaimsDTO newClaim) {
        this.newClaim = newClaim;
        return this;
    }

    /**
     * Get newClaim
     *
     * @return newClaim
     **/
    @ApiModelProperty(value = "")
    public PairOfClaimsDTO getNewClaim() {
        return newClaim;
    }

    public void setNewClaim(PairOfClaimsDTO newClaim) {
        this.newClaim = newClaim;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PairOfClaimsDTO pairOfClaims = (PairOfClaimsDTO) o;
        return Objects.equals(this.oldClaim, pairOfClaims.oldClaim) &&
                Objects.equals(this.newClaim, pairOfClaims.newClaim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldClaim, newClaim);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PairOfClaimsDTO {\n");

        sb.append("    oldClaim: ").append(toIndentedString(oldClaim)).append("\n");
        sb.append("    newClaim: ").append(toIndentedString(newClaim)).append("\n");
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

