package org.wso2.carbon.meta.claim.mgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Objects;

/**
 * a (key, MapItem) map. &#x60;default&#x60;is an example key
 */
@ApiModel(description = "a (key, MapItem) map. `default`is an example key")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-17T23:15:30.678+05:30")
public class ClaimPropertiesDTO extends HashMap<String, KeyValueDTO> {
    @JsonProperty("default")
    private KeyValueDTO _default = null;

    public ClaimPropertiesDTO _default(KeyValueDTO _default) {
        this._default = _default;
        return this;
    }

    /**
     * Get _default
     *
     * @return _default
     **/
    @ApiModelProperty(value = "")
    public KeyValueDTO getDefault() {
        return _default;
    }

    public void setDefault(KeyValueDTO _default) {
        this._default = _default;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimPropertiesDTO claimProperties = (ClaimPropertiesDTO) o;
        return Objects.equals(this._default, claimProperties._default) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_default, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClaimPropertiesDTO {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
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

