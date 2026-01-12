package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Apportion Request.
 */
@Schema(description = "Apportion Request.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2019-02-25T15:07:36.183+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApportionRequest   {
        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("Bills")
        @Valid
        private List<Bill> bills = null;

        public ApportionRequest addBillsItem(Bill billsItem) {
            if (this.bills == null) {
            this.bills = new ArrayList<>();
            }
        this.bills.add(billsItem);
        return this;
        }

}

