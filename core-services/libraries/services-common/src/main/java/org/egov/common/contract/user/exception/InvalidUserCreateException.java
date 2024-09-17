package org.egov.common.contract.user.exception;

import lombok.Getter;
import org.egov.common.contract.request.User;

@Getter
public class InvalidUserCreateException extends RuntimeException {

    private static final long serialVersionUID = -761312648494992125L;
    private User user;

    public InvalidUserCreateException(User user) {
        this.user = user;
    }

}
