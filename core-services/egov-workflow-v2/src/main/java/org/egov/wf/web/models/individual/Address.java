package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.user.enums.AddressType;
import org.springframework.validation.annotation.Validated;

/**
 * @deprecated
 */
@ApiModel(description = "Representation of a address. Individual APIs may choose to extend from this using allOf if more details needed to be added in their case. ")
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class Address {
	@JsonProperty("id")
	private @Size(min = 2, max = 64) String id = null;
	@JsonProperty("clientReferenceId")
	private @Size(min = 2, max = 64) String clientReferenceId = null;
	@JsonProperty("individualId")
	private @Size(min = 2, max = 64) String individualId = null;
	@JsonProperty("tenantId")
	private String tenantId = null;
	@JsonProperty("doorNo")
	private @Size(min = 2, max = 64) String doorNo = null;
	@JsonProperty("latitude")
	private @DecimalMin("-90")
	@DecimalMax("90") Double latitude = null;
	@JsonProperty("longitude")
	private @DecimalMin("-180")
	@DecimalMax("180") Double longitude = null;
	@JsonProperty("locationAccuracy")
	private @DecimalMin("0") Double locationAccuracy = null;
	@JsonProperty("type")
	private @NotNull AddressType type = null;
	@JsonProperty("addressLine1")
	private @Size(min = 2, max = 256) String addressLine1 = null;
	@JsonProperty("addressLine2")
	private @Size(min = 2, max = 256) String addressLine2 = null;
	@JsonProperty("landmark")
	private @Size(min = 2, max = 256) String landmark = null;
	@JsonProperty("city")
	private @Size(min = 2, max = 256) String city = null;
	@JsonProperty("pincode")
	private @Size(min = 2, max = 64) String pincode = null;
	@JsonProperty("buildingName")
	private @Size(min = 2, max = 256) String buildingName = null;
	@JsonProperty("street")
	private @Size(min = 2, max = 256) String street = null;
	@JsonProperty("locality")
	private @Valid Boundary locality = null;
	@JsonProperty("ward")
	private @Valid Boundary ward = null;
	@JsonProperty("isDeleted")
	private Boolean isDeleted;
	@JsonProperty("auditDetails")
	private @Valid AuditDetails auditDetails;

	public Address() {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
	}

	public Address(String id, String clientReferenceId, String individualId, String tenantId, String doorNo, Double latitude, Double longitude, Double locationAccuracy, AddressType type, String addressLine1, String addressLine2, String landmark, String city, String pincode, String buildingName, String street, Boundary locality, Boundary ward, Boolean isDeleted, AuditDetails auditDetails) {
		this.isDeleted = Boolean.FALSE;
		this.auditDetails = null;
		this.id = id;
		this.clientReferenceId = clientReferenceId;
		this.individualId = individualId;
		this.tenantId = tenantId;
		this.doorNo = doorNo;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationAccuracy = locationAccuracy;
		this.type = type;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.landmark = landmark;
		this.city = city;
		this.pincode = pincode;
		this.buildingName = buildingName;
		this.street = street;
		this.locality = locality;
		this.ward = ward;
		this.isDeleted = isDeleted;
		this.auditDetails = auditDetails;
	}

	public static AddressBuilder builder() {
		return new AddressBuilder();
	}

	public String getId() {
		return this.id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	public String getClientReferenceId() {
		return this.clientReferenceId;
	}

	@JsonProperty("clientReferenceId")
	public void setClientReferenceId(String clientReferenceId) {
		this.clientReferenceId = clientReferenceId;
	}

	public String getIndividualId() {
		return this.individualId;
	}

	@JsonProperty("individualId")
	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	public String getTenantId() {
		return this.tenantId;
	}

	@JsonProperty("tenantId")
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getDoorNo() {
		return this.doorNo;
	}

	@JsonProperty("doorNo")
	public void setDoorNo(String doorNo) {
		this.doorNo = doorNo;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	@JsonProperty("latitude")
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	@JsonProperty("longitude")
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLocationAccuracy() {
		return this.locationAccuracy;
	}

	@JsonProperty("locationAccuracy")
	public void setLocationAccuracy(Double locationAccuracy) {
		this.locationAccuracy = locationAccuracy;
	}

	public AddressType getType() {
		return this.type;
	}

	@JsonProperty("type")
	public void setType(AddressType type) {
		this.type = type;
	}

	public String getAddressLine1() {
		return this.addressLine1;
	}

	@JsonProperty("addressLine1")
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return this.addressLine2;
	}

	@JsonProperty("addressLine2")
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getLandmark() {
		return this.landmark;
	}

	@JsonProperty("landmark")
	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getCity() {
		return this.city;
	}

	@JsonProperty("city")
	public void setCity(String city) {
		this.city = city;
	}

	public String getPincode() {
		return this.pincode;
	}

	@JsonProperty("pincode")
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getBuildingName() {
		return this.buildingName;
	}

	@JsonProperty("buildingName")
	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public String getStreet() {
		return this.street;
	}

	@JsonProperty("street")
	public void setStreet(String street) {
		this.street = street;
	}

	public Boundary getLocality() {
		return this.locality;
	}

	@JsonProperty("locality")
	public void setLocality(Boundary locality) {
		this.locality = locality;
	}

	public Boundary getWard() {
		return this.ward;
	}

	@JsonProperty("ward")
	public void setWard(Boundary ward) {
		this.ward = ward;
	}

	public Boolean getIsDeleted() {
		return this.isDeleted;
	}

	@JsonProperty("isDeleted")
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public AuditDetails getAuditDetails() {
		return this.auditDetails;
	}

	@JsonProperty("auditDetails")
	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Address)) {
			return false;
		} else {
			Address other = (Address) o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$latitude = this.getLatitude();
				Object other$latitude = other.getLatitude();
				if (this$latitude == null) {
					if (other$latitude != null) {
						return false;
					}
				} else if (!this$latitude.equals(other$latitude)) {
					return false;
				}

				Object this$longitude = this.getLongitude();
				Object other$longitude = other.getLongitude();
				if (this$longitude == null) {
					if (other$longitude != null) {
						return false;
					}
				} else if (!this$longitude.equals(other$longitude)) {
					return false;
				}

				Object this$locationAccuracy = this.getLocationAccuracy();
				Object other$locationAccuracy = other.getLocationAccuracy();
				if (this$locationAccuracy == null) {
					if (other$locationAccuracy != null) {
						return false;
					}
				} else if (!this$locationAccuracy.equals(other$locationAccuracy)) {
					return false;
				}

				Object this$isDeleted = this.getIsDeleted();
				Object other$isDeleted = other.getIsDeleted();
				if (this$isDeleted == null) {
					if (other$isDeleted != null) {
						return false;
					}
				} else if (!this$isDeleted.equals(other$isDeleted)) {
					return false;
				}

				Object this$id = this.getId();
				Object other$id = other.getId();
				if (this$id == null) {
					if (other$id != null) {
						return false;
					}
				} else if (!this$id.equals(other$id)) {
					return false;
				}

				Object this$clientReferenceId = this.getClientReferenceId();
				Object other$clientReferenceId = other.getClientReferenceId();
				if (this$clientReferenceId == null) {
					if (other$clientReferenceId != null) {
						return false;
					}
				} else if (!this$clientReferenceId.equals(other$clientReferenceId)) {
					return false;
				}

				Object this$individualId = this.getIndividualId();
				Object other$individualId = other.getIndividualId();
				if (this$individualId == null) {
					if (other$individualId != null) {
						return false;
					}
				} else if (!this$individualId.equals(other$individualId)) {
					return false;
				}

				Object this$tenantId = this.getTenantId();
				Object other$tenantId = other.getTenantId();
				if (this$tenantId == null) {
					if (other$tenantId != null) {
						return false;
					}
				} else if (!this$tenantId.equals(other$tenantId)) {
					return false;
				}

				Object this$doorNo = this.getDoorNo();
				Object other$doorNo = other.getDoorNo();
				if (this$doorNo == null) {
					if (other$doorNo != null) {
						return false;
					}
				} else if (!this$doorNo.equals(other$doorNo)) {
					return false;
				}

				Object this$type = this.getType();
				Object other$type = other.getType();
				if (this$type == null) {
					if (other$type != null) {
						return false;
					}
				} else if (!this$type.equals(other$type)) {
					return false;
				}

				Object this$addressLine1 = this.getAddressLine1();
				Object other$addressLine1 = other.getAddressLine1();
				if (this$addressLine1 == null) {
					if (other$addressLine1 != null) {
						return false;
					}
				} else if (!this$addressLine1.equals(other$addressLine1)) {
					return false;
				}

				Object this$addressLine2 = this.getAddressLine2();
				Object other$addressLine2 = other.getAddressLine2();
				if (this$addressLine2 == null) {
					if (other$addressLine2 != null) {
						return false;
					}
				} else if (!this$addressLine2.equals(other$addressLine2)) {
					return false;
				}

				Object this$landmark = this.getLandmark();
				Object other$landmark = other.getLandmark();
				if (this$landmark == null) {
					if (other$landmark != null) {
						return false;
					}
				} else if (!this$landmark.equals(other$landmark)) {
					return false;
				}

				Object this$city = this.getCity();
				Object other$city = other.getCity();
				if (this$city == null) {
					if (other$city != null) {
						return false;
					}
				} else if (!this$city.equals(other$city)) {
					return false;
				}

				Object this$pincode = this.getPincode();
				Object other$pincode = other.getPincode();
				if (this$pincode == null) {
					if (other$pincode != null) {
						return false;
					}
				} else if (!this$pincode.equals(other$pincode)) {
					return false;
				}

				Object this$buildingName = this.getBuildingName();
				Object other$buildingName = other.getBuildingName();
				if (this$buildingName == null) {
					if (other$buildingName != null) {
						return false;
					}
				} else if (!this$buildingName.equals(other$buildingName)) {
					return false;
				}

				Object this$street = this.getStreet();
				Object other$street = other.getStreet();
				if (this$street == null) {
					if (other$street != null) {
						return false;
					}
				} else if (!this$street.equals(other$street)) {
					return false;
				}

				Object this$locality = this.getLocality();
				Object other$locality = other.getLocality();
				if (this$locality == null) {
					if (other$locality != null) {
						return false;
					}
				} else if (!this$locality.equals(other$locality)) {
					return false;
				}

				Object this$ward = this.getWard();
				Object other$ward = other.getWard();
				if (this$ward == null) {
					if (other$ward != null) {
						return false;
					}
				} else if (!this$ward.equals(other$ward)) {
					return false;
				}

				Object this$auditDetails = this.getAuditDetails();
				Object other$auditDetails = other.getAuditDetails();
				if (this$auditDetails == null) {
					if (other$auditDetails != null) {
						return false;
					}
				} else if (!this$auditDetails.equals(other$auditDetails)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof Address;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $latitude = this.getLatitude();
		result = result * 59 + ($latitude == null ? 43 : $latitude.hashCode());
		Object $longitude = this.getLongitude();
		result = result * 59 + ($longitude == null ? 43 : $longitude.hashCode());
		Object $locationAccuracy = this.getLocationAccuracy();
		result = result * 59 + ($locationAccuracy == null ? 43 : $locationAccuracy.hashCode());
		Object $isDeleted = this.getIsDeleted();
		result = result * 59 + ($isDeleted == null ? 43 : $isDeleted.hashCode());
		Object $id = this.getId();
		result = result * 59 + ($id == null ? 43 : $id.hashCode());
		Object $clientReferenceId = this.getClientReferenceId();
		result = result * 59 + ($clientReferenceId == null ? 43 : $clientReferenceId.hashCode());
		Object $individualId = this.getIndividualId();
		result = result * 59 + ($individualId == null ? 43 : $individualId.hashCode());
		Object $tenantId = this.getTenantId();
		result = result * 59 + ($tenantId == null ? 43 : $tenantId.hashCode());
		Object $doorNo = this.getDoorNo();
		result = result * 59 + ($doorNo == null ? 43 : $doorNo.hashCode());
		Object $type = this.getType();
		result = result * 59 + ($type == null ? 43 : $type.hashCode());
		Object $addressLine1 = this.getAddressLine1();
		result = result * 59 + ($addressLine1 == null ? 43 : $addressLine1.hashCode());
		Object $addressLine2 = this.getAddressLine2();
		result = result * 59 + ($addressLine2 == null ? 43 : $addressLine2.hashCode());
		Object $landmark = this.getLandmark();
		result = result * 59 + ($landmark == null ? 43 : $landmark.hashCode());
		Object $city = this.getCity();
		result = result * 59 + ($city == null ? 43 : $city.hashCode());
		Object $pincode = this.getPincode();
		result = result * 59 + ($pincode == null ? 43 : $pincode.hashCode());
		Object $buildingName = this.getBuildingName();
		result = result * 59 + ($buildingName == null ? 43 : $buildingName.hashCode());
		Object $street = this.getStreet();
		result = result * 59 + ($street == null ? 43 : $street.hashCode());
		Object $locality = this.getLocality();
		result = result * 59 + ($locality == null ? 43 : $locality.hashCode());
		Object $ward = this.getWard();
		result = result * 59 + ($ward == null ? 43 : $ward.hashCode());
		Object $auditDetails = this.getAuditDetails();
		result = result * 59 + ($auditDetails == null ? 43 : $auditDetails.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getId();
		return "Address(id=" + var10000 + ", clientReferenceId=" + this.getClientReferenceId() + ", individualId=" + this.getIndividualId() + ", tenantId=" + this.getTenantId() + ", doorNo=" + this.getDoorNo() + ", latitude=" + this.getLatitude() + ", longitude=" + this.getLongitude() + ", locationAccuracy=" + this.getLocationAccuracy() + ", type=" + this.getType() + ", addressLine1=" + this.getAddressLine1() + ", addressLine2=" + this.getAddressLine2() + ", landmark=" + this.getLandmark() + ", city=" + this.getCity() + ", pincode=" + this.getPincode() + ", buildingName=" + this.getBuildingName() + ", street=" + this.getStreet() + ", locality=" + this.getLocality() + ", ward=" + this.getWard() + ", isDeleted=" + this.getIsDeleted() + ", auditDetails=" + this.getAuditDetails() + ")";
	}

	public static class AddressBuilder {
		private String id;
		private String clientReferenceId;
		private String individualId;
		private String tenantId;
		private String doorNo;
		private Double latitude;
		private Double longitude;
		private Double locationAccuracy;
		private AddressType type;
		private String addressLine1;
		private String addressLine2;
		private String landmark;
		private String city;
		private String pincode;
		private String buildingName;
		private String street;
		private Boundary locality;
		private Boundary ward;
		private Boolean isDeleted;
		private AuditDetails auditDetails;

		AddressBuilder() {
		}

		@JsonProperty("id")
		public AddressBuilder id(String id) {
			this.id = id;
			return this;
		}

		@JsonProperty("clientReferenceId")
		public AddressBuilder clientReferenceId(String clientReferenceId) {
			this.clientReferenceId = clientReferenceId;
			return this;
		}

		@JsonProperty("individualId")
		public AddressBuilder individualId(String individualId) {
			this.individualId = individualId;
			return this;
		}

		@JsonProperty("tenantId")
		public AddressBuilder tenantId(String tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		@JsonProperty("doorNo")
		public AddressBuilder doorNo(String doorNo) {
			this.doorNo = doorNo;
			return this;
		}

		@JsonProperty("latitude")
		public AddressBuilder latitude(Double latitude) {
			this.latitude = latitude;
			return this;
		}

		@JsonProperty("longitude")
		public AddressBuilder longitude(Double longitude) {
			this.longitude = longitude;
			return this;
		}

		@JsonProperty("locationAccuracy")
		public AddressBuilder locationAccuracy(Double locationAccuracy) {
			this.locationAccuracy = locationAccuracy;
			return this;
		}

		@JsonProperty("type")
		public AddressBuilder type(AddressType type) {
			this.type = type;
			return this;
		}

		@JsonProperty("addressLine1")
		public AddressBuilder addressLine1(String addressLine1) {
			this.addressLine1 = addressLine1;
			return this;
		}

		@JsonProperty("addressLine2")
		public AddressBuilder addressLine2(String addressLine2) {
			this.addressLine2 = addressLine2;
			return this;
		}

		@JsonProperty("landmark")
		public AddressBuilder landmark(String landmark) {
			this.landmark = landmark;
			return this;
		}

		@JsonProperty("city")
		public AddressBuilder city(String city) {
			this.city = city;
			return this;
		}

		@JsonProperty("pincode")
		public AddressBuilder pincode(String pincode) {
			this.pincode = pincode;
			return this;
		}

		@JsonProperty("buildingName")
		public AddressBuilder buildingName(String buildingName) {
			this.buildingName = buildingName;
			return this;
		}

		@JsonProperty("street")
		public AddressBuilder street(String street) {
			this.street = street;
			return this;
		}

		@JsonProperty("locality")
		public AddressBuilder locality(Boundary locality) {
			this.locality = locality;
			return this;
		}

		@JsonProperty("ward")
		public AddressBuilder ward(Boundary ward) {
			this.ward = ward;
			return this;
		}

		@JsonProperty("isDeleted")
		public AddressBuilder isDeleted(Boolean isDeleted) {
			this.isDeleted = isDeleted;
			return this;
		}

		@JsonProperty("auditDetails")
		public AddressBuilder auditDetails(AuditDetails auditDetails) {
			this.auditDetails = auditDetails;
			return this;
		}

		public Address build() {
			return new Address(this.id, this.clientReferenceId, this.individualId, this.tenantId, this.doorNo, this.latitude, this.longitude, this.locationAccuracy, this.type, this.addressLine1, this.addressLine2, this.landmark, this.city, this.pincode, this.buildingName, this.street, this.locality, this.ward, this.isDeleted, this.auditDetails);
		}

		public String toString() {
			return "Address.AddressBuilder(id=" + this.id + ", clientReferenceId=" + this.clientReferenceId + ", individualId=" + this.individualId + ", tenantId=" + this.tenantId + ", doorNo=" + this.doorNo + ", latitude=" + this.latitude + ", longitude=" + this.longitude + ", locationAccuracy=" + this.locationAccuracy + ", type=" + this.type + ", addressLine1=" + this.addressLine1 + ", addressLine2=" + this.addressLine2 + ", landmark=" + this.landmark + ", city=" + this.city + ", pincode=" + this.pincode + ", buildingName=" + this.buildingName + ", street=" + this.street + ", locality=" + this.locality + ", ward=" + this.ward + ", isDeleted=" + this.isDeleted + ", auditDetails=" + this.auditDetails + ")";
		}
	}
}


