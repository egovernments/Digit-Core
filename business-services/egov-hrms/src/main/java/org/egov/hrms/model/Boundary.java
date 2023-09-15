package org.egov.hrms.model;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Boundary {
	
	 private int id;
	 private int boundaryNum;
	 private String name;
	 private String localname;
	 private Double longitude;
	 private Double latitude;
	 private String label;
	 private String code;
	 private String area;
	 private List<BigInteger> pincode;
	 private List<Boundary> children;

}
