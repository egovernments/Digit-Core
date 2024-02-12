package org.egov.infra.indexer.custom.bpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * This object holds list of documents attached during the transaciton for a property
 */
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-06-23T05:54:07.373Z[GMT]")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("documentType")
  private String documentType = null;

  @JsonProperty("fileStoreId")
  private String fileStoreId = null;

  @JsonProperty("documentUid")
  private String documentUid = null;

  @JsonProperty("additionalDetails")
  private Object additionalDetails = null;
  
  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;

  public Document id(String id) {
    this.id = id;
    return this;
  }

  /**
   * system id of the Document.
   * @return id
  **/

  @Size(max=64)   public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Document documentType(String documentType) {
    this.documentType = documentType;
    return this;
  }

  /**
   * unique document type code, should be validated with document type master
   * @return documentType
  **/

    public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  public Document fileStoreId(String fileStoreId) {
    this.fileStoreId = fileStoreId;
    return this;
  }

  /**
   * File store reference key.
   * @return fileStoreId
  **/

    public String getFileStoreId() {
    return fileStoreId;
  }

  public void setFileStoreId(String fileStoreId) {
    this.fileStoreId = fileStoreId;
  }

  public Document documentUid(String documentUid) {
    this.documentUid = documentUid;
    return this;
  }

  /**
   * The unique id(Pancard Number,Adhar etc.) of the given Document.
   * @return documentUid
  **/

  @Size(max=64)   public String getDocumentUid() {
    return documentUid;
  }

  public void setDocumentUid(String documentUid) {
    this.documentUid = documentUid;
  }

  public Document additionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
    return this;
  }

  /**
   * Json object to capture any extra information which is not accommodated by model
   * @return additionalDetails
  **/

    public Object getAdditionalDetails() {
    return additionalDetails;
  }

  public void setAdditionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
  }

  public Document auditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
    return this;
  }

  /**
   * Get geoLocation
   * @return geoLocation
  **/

    @Valid
    public AuditDetails getAuditDetails() {
    return auditDetails;
  }

  public void setAuditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Document document = (Document) o;
    return Objects.equals(this.id, document.id) &&
        Objects.equals(this.documentType, document.documentType) &&
        Objects.equals(this.fileStoreId, document.fileStoreId) &&
        Objects.equals(this.documentUid, document.documentUid) &&
        Objects.equals(this.additionalDetails, document.additionalDetails) &&
        Objects.equals(this.auditDetails, document.auditDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, documentType, fileStoreId, documentUid, additionalDetails, auditDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Document {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    documentType: ").append(toIndentedString(documentType)).append("\n");
    sb.append("    fileStoreId: ").append(toIndentedString(fileStoreId)).append("\n");
    sb.append("    documentUid: ").append(toIndentedString(documentUid)).append("\n");
    sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
    sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
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
