package org.example.keycloak.abac;

import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;

public class ABACPolicyRepresentation extends AbstractPolicyRepresentation {
	private String requiredAttribute;
	private String requiredValue;
	private String code;

	public String getRequiredAttribute() {
		return requiredAttribute;
	}

	public String getRequiredValue() {
		return requiredValue;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setRequiredAttribute(String requiredAttribute) {
		this.requiredAttribute = requiredAttribute;
	}

	public void setRequiredValue(String requiredValue) {
		this.requiredValue = requiredValue;
	}
}
