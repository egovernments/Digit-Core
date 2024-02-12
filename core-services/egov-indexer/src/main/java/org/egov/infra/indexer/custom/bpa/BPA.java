package org.egov.infra.indexer.custom.bpa;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.egov.infra.indexer.custom.bpa.landInfo.LandInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-06-23T05:52:32.717Z[GMT]")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class BPA   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("applicationNo")
  private String applicationNo = null;

  @JsonProperty("approvalNo")
  private String approvalNo = null;

  @JsonProperty("accountId")
  private String accountId = null;

  @JsonProperty("edcrNumber")
  private String edcrNumber = null;

  @JsonProperty("riskType")
  private String riskType = null;
  
  @JsonProperty("businessService")
  private String businessService = null;

  @JsonProperty("landId")
  private String landId = null;

  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("approvalDate")
  private Long approvalDate = null;
  
  @JsonProperty("applicationDate")
  private Long applicationDate = null;
  
  @JsonProperty("status")
  private String status = null;

  @JsonProperty("documents")
  @Valid
  private List<Document> documents = null;

  @JsonProperty("landInfo")
  private LandInfo landInfo = null;

  @JsonProperty("workflow")
  private Workflow workflow = null;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;

  @JsonProperty("additionalDetails")
  private Object additionalDetails = null;

  public BPA id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique Identifier(UUID) of the bpa application for internal reference.
   * @return id
  **/

  @Size(min=1,max=64)   public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BPA applicationNo(String applicationNo) {
    this.applicationNo = applicationNo;
    return this;
  }

  /**
   * Generate formatted Unique Identifier of the building permit application. Keep the format in mdms
   * @return applicationNo
  **/

  @Size(min=1,max=64)   public String getApplicationNo() {
    return applicationNo;
  }

  public void setApplicationNo(String applicationNo) {
    this.applicationNo = applicationNo;
  }

  public BPA approvalNo(String approvalNo) {
    this.approvalNo = approvalNo;
    return this;
  }

  /**
   * Generate Approval number based on wf status. When to generate approvalNo will be depends on wf state so make it configurable at  application level
   * @return approvalNo
  **/

  @Size(min=1,max=64)   public String getApprovalNo() {
    return approvalNo;
  }

  public void setApprovalNo(String approvalNo) {
    this.approvalNo = approvalNo;
  }

  public BPA accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   * Initiator User UUID
   * @return accountId
  **/

  @Size(min=1,max=64)   public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public BPA edcrNumber(String edcrNumber) {
    this.edcrNumber = edcrNumber;
    return this;
  }

  /**
   * Unique Identifier scrutinized edcr number
   * @return edcrNumber
  **/

  @Size(min=1,max=64)   public String getEdcrNumber() {
    return edcrNumber;
  }

  public void setEdcrNumber(String edcrNumber) {
    this.edcrNumber = edcrNumber;
  }

  public BPA riskType(String riskType) {
    this.riskType = riskType;
    return this;
  }

  /**
   * Risk type will be drived based on mdms configuration
   * @return riskType
  **/

  @Size(min=1,max=64)   public String getRiskType() {
    return riskType;
  }

  public void setRiskType(String riskType) {
    this.riskType = riskType;
  }

  

  public BPA approvalDate(Long approvalDate) {
    this.approvalDate = approvalDate;
    return this;
  }

  /**
   * Risk type will be drived based on mdms configuration
   * @return riskType
  **/

   public Long getApprovalDate() {
    return approvalDate;
  }

  public void setApprovalDate(Long approvalDate) {
    this.approvalDate = approvalDate;
  }
  
  
  public BPA applicationDate(Long applicationDate) {
	    this.applicationDate = applicationDate;
	    return this;
	  }

	  /**
	   * Risk type will be drived based on mdms configuration
	   * @return riskType
	  **/

	   public Long getApplicationDate() {
	    return applicationDate;
	  }

	  public void setApplicationDate(Long applicationDate) {
	    this.applicationDate = applicationDate;
	  }
  
  
  public BPA businessService(String businessService) {
	    this.businessService = businessService;
	    return this;
	  }

	  /**
	   * Risk type will be drived based on mdms configuration
	   * @return riskType
	  **/

	  @Size(min=1,max=64)   public String getBusinessService() {
	    return businessService;
	  }

	  public void setBusinessService(String businessService) {
	    this.businessService = businessService;
	  }
	  
	  
  public BPA landId(String landId) {
    this.landId = landId;
    return this;
  }

  /**
   * Unique Identifier(UUID) of the land for internal reference.
   * @return landId
  **/

  @Size(min=1,max=64)   public String getLandId() {
    return landId;
  }

  public void setLandId(String landId) {
    this.landId = landId;
  }

  public BPA tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Unique ULB identifier.
   * @return tenantId
  **/
      @NotNull

  @Size(min=2,max=256)   public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public BPA status(String status) {
    this.status = status;
    return this;
  }

  /**
   * status of the application.
   * @return status
  **/

    public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public BPA documents(List<Document> documents) {
    this.documents = documents;
    return this;
  }

  public BPA addDocumentsItem(Document documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<Document>();
    }
    this.documents.add(documentsItem);
    return this;
  }

  /**
   * The documents attached by owner for exemption.
   * @return documents
  **/
      @Valid
    public List<Document> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Document> documents) {
    this.documents = documents;
  }

  public BPA landInfo(LandInfo landInfo) {
    this.landInfo = landInfo;
    return this;
  }

  /**
   * Get landInfo
   * @return landInfo
  **/

    @Valid
    public LandInfo getLandInfo() {
    return landInfo;
  }

  public void setLandInfo(LandInfo landInfo) {
    this.landInfo = landInfo;
  }

  public BPA workflow(Workflow workflow) {
    this.workflow = workflow;
    return this;
  }

  /**
   * Get workflow
   * @return workflow
  **/

    @Valid
    public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }

  public BPA auditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
    return this;
  }

  /**
   * Get auditDetails
   * @return auditDetails
  **/

    @Valid
    public AuditDetails getAuditDetails() {
    return auditDetails;
  }

  public void setAuditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
  }

  public BPA additionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
    return this;
  }

  /**
   * The json to capturing the custom fields
   * @return additionalDetails
  **/

    public Object getAdditionalDetails() {
    return additionalDetails;
  }

  public void setAdditionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BPA BPA = (BPA) o;
    return Objects.equals(this.id, BPA.id) &&
        Objects.equals(this.applicationNo, BPA.applicationNo) &&
        Objects.equals(this.approvalNo, BPA.approvalNo) &&
        Objects.equals(this.accountId, BPA.accountId) &&
        Objects.equals(this.edcrNumber, BPA.edcrNumber) &&
        Objects.equals(this.riskType, BPA.riskType) &&
        Objects.equals(this.landId, BPA.landId) &&
        Objects.equals(this.tenantId, BPA.tenantId) &&
        Objects.equals(this.status, BPA.status) &&
        Objects.equals(this.documents, BPA.documents) &&
        Objects.equals(this.landInfo, BPA.landInfo) &&
        Objects.equals(this.workflow, BPA.workflow) &&
        Objects.equals(this.auditDetails, BPA.auditDetails) &&
        Objects.equals(this.additionalDetails, BPA.additionalDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, applicationNo, approvalNo, accountId, edcrNumber, riskType, landId, tenantId, status, documents, landInfo, workflow, auditDetails, additionalDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BPA {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    applicationNo: ").append(toIndentedString(applicationNo)).append("\n");
    sb.append("    approvalNo: ").append(toIndentedString(approvalNo)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    edcrNumber: ").append(toIndentedString(edcrNumber)).append("\n");
    sb.append("    riskType: ").append(toIndentedString(riskType)).append("\n");
    sb.append("    landId: ").append(toIndentedString(landId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
    sb.append("    landInfo: ").append(toIndentedString(landInfo)).append("\n");
    sb.append("    workflow: ").append(toIndentedString(workflow)).append("\n");
    sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
    sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
