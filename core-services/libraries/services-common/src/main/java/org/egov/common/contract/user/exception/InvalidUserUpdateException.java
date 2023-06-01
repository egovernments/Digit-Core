package org.egov.common.contract.user.exception;

import lombok.Getter;
import org.egov.common.contract.request.User;

public class InvalidUserUpdateException extends RuntimeException {

    private static final long serialVersionUID = 580361940613077431L;
    @Getter
    private User user;

    public InvalidUserUpdateException(User user) {
        this.user = user;
    }

}
