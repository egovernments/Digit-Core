package org.egov.wf.web.models.individual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.egov.common.contract.user.enums.UserType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
	@JsonProperty("username")
	private @Size(max = 180) String username;
	@JsonProperty("password")
	private @Size(max = 64) String password;
	@JsonProperty("tenantId")
	private @Size(min = 2, max = 1000) String tenantId;
	@JsonProperty("roles")
	private @Valid List<Role> roles;
	@JsonProperty("type")
	private @Size(max = 50) UserType userType;

	public UserDetails(String username, String password, String tenantId, List<Role> roles, UserType userType) {
		this.username = username;
		this.password = password;
		this.tenantId = tenantId;
		this.roles = roles;
		this.userType = userType;
	}

	public UserDetails() {
	}

	public static UserDetailsBuilder builder() {
		return new UserDetailsBuilder();
	}

	public String getUsername() {
		return this.username;
	}

	@JsonProperty("username")
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	@JsonProperty("password")
	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantId() {
		return this.tenantId;
	}

	@JsonProperty("tenantId")
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<Role> getRoles() {
		return this.roles;
	}

	@JsonProperty("roles")
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public UserType getUserType() {
		return this.userType;
	}

	@JsonProperty("type")
	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof UserDetails)) {
			return false;
		} else {
			UserDetails other = (UserDetails) o;
			if (!other.canEqual(this)) {
				return false;
			} else {
				Object this$username = this.getUsername();
				Object other$username = other.getUsername();
				if (this$username == null) {
					if (other$username != null) {
						return false;
					}
				} else if (!this$username.equals(other$username)) {
					return false;
				}

				Object this$password = this.getPassword();
				Object other$password = other.getPassword();
				if (this$password == null) {
					if (other$password != null) {
						return false;
					}
				} else if (!this$password.equals(other$password)) {
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

				Object this$roles = this.getRoles();
				Object other$roles = other.getRoles();
				if (this$roles == null) {
					if (other$roles != null) {
						return false;
					}
				} else if (!this$roles.equals(other$roles)) {
					return false;
				}

				Object this$userType = this.getUserType();
				Object other$userType = other.getUserType();
				if (this$userType == null) {
					if (other$userType != null) {
						return false;
					}
				} else if (!this$userType.equals(other$userType)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof UserDetails;
	}

	public int hashCode() {
		int PRIME = 59;
		int result = 1;
		Object $username = this.getUsername();
		result = result * 59 + ($username == null ? 43 : $username.hashCode());
		Object $password = this.getPassword();
		result = result * 59 + ($password == null ? 43 : $password.hashCode());
		Object $tenantId = this.getTenantId();
		result = result * 59 + ($tenantId == null ? 43 : $tenantId.hashCode());
		Object $roles = this.getRoles();
		result = result * 59 + ($roles == null ? 43 : $roles.hashCode());
		Object $userType = this.getUserType();
		result = result * 59 + ($userType == null ? 43 : $userType.hashCode());
		return result;
	}

	public String toString() {
		String var10000 = this.getUsername();
		return "UserDetails(username=" + var10000 + ", password=" + this.getPassword() + ", tenantId=" + this.getTenantId() + ", roles=" + this.getRoles() + ", userType=" + this.getUserType() + ")";
	}

	public static class UserDetailsBuilder {
		private String username;
		private String password;
		private String tenantId;
		private List<Role> roles;
		private UserType userType;

		UserDetailsBuilder() {
		}

		@JsonProperty("username")
		public UserDetailsBuilder username(String username) {
			this.username = username;
			return this;
		}

		@JsonProperty("password")
		public UserDetailsBuilder password(String password) {
			this.password = password;
			return this;
		}

		@JsonProperty("tenantId")
		public UserDetailsBuilder tenantId(String tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		@JsonProperty("roles")
		public UserDetailsBuilder roles(List<Role> roles) {
			this.roles = roles;
			return this;
		}

		@JsonProperty("type")
		public UserDetailsBuilder userType(UserType userType) {
			this.userType = userType;
			return this;
		}

		public UserDetails build() {
			return new UserDetails(this.username, this.password, this.tenantId, this.roles, this.userType);
		}

		public String toString() {
			return "UserDetails.UserDetailsBuilder(username=" + this.username + ", password=" + this.password + ", tenantId=" + this.tenantId + ", roles=" + this.roles + ", userType=" + this.userType + ")";
		}
	}
}


