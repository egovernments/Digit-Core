package org.egov.user.domain.model;

import lombok.*;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SMSRequest {

    private String mobileNumber;

    private String message;

}