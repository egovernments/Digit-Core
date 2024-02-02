package org.egov.wf.web.models;


import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ProcessInstanceSearchCriteria {

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    @JsonProperty("status")
    private List<String> status;

    @JsonProperty("businessIds")
    private List<@Size(min=4) String> businessIds;

    @JsonProperty("assignee")
    private String  assignee;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("history")
    private Boolean history = false;

    @JsonProperty("fromDate")
    private Long fromDate = null;

    @JsonProperty("toDate")
    private Long toDate = null;


    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("businessService")
    private String businessService;

    @JsonProperty("moduleName")
    private String moduleName;
    
    @JsonIgnore
    private Boolean isNearingSlaCount;

    @JsonIgnore
    private List<String> tenantSpecifiStatus;

    @JsonIgnore
    private List<String> multipleAssignees;

    @JsonIgnore
    private List<String> statesToIgnore;

    @JsonIgnore
    private Boolean isEscalatedCount;

    @JsonIgnore
    private Boolean isAssignedToMeCount;

    @JsonIgnore
    private List<String> statusesIrrespectiveOfTenant;
    
    @JsonIgnore
    private Long slotPercentageSlaLimit;




    public Boolean isNull(){
        if(this.getBusinessIds()==null && this.getIds()==null && this.getAssignee()==null &&
                this.getStatus()==null)
            return true;
        else return false;
    }



}
