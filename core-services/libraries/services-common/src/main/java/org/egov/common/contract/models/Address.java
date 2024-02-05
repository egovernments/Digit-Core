package org.egov.common.contract.models;

import lombok.*;
import org.egov.common.contract.user.enums.AddressType;
import org.springframework.util.StringUtils;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Address {
    private String pinCode;
    private String city;
    private String address;
    private AddressType type;
    private Long id;
    private String tenantId;
    private Long userId;
    private String addressType;
    private Long LastModifiedBy;
    private Date LastModifiedDate;

    boolean isInvalid() {
        return isPinCodeInvalid()
                || isCityInvalid()
                || isAddressInvalid();
    }


    boolean isNotEmpty() {
        return !StringUtils.isEmpty(pinCode)
                || !StringUtils.isEmpty(city)
                || !StringUtils.isEmpty(address);
    }

    boolean isPinCodeInvalid() {
        return pinCode != null && pinCode.length() > 10;
    }

    boolean isCityInvalid() {
        return city != null && city.length() > 300;
    }

    boolean isAddressInvalid() {
        return address != null && address.length() > 300;
    }
}
