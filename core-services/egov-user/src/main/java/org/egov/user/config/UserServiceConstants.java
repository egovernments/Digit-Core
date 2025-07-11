/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.user.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceConstants {

    public static final String EMAIL_UPDATION_CODE = "EMAIL_UPDATED";

    public static final String DEFAULT_EMAIL_UPDATION_MESSAGE = "Dear Citizen, your e-mail has been updated from {oldEmail} to {newEmail}.\n\nEGOVS";

    public static final String INVALID_USER_REQUEST = "UserRequest is Invalid";

    public static final String ROLECODE_MISSING_CODE = "egs_001";
    public static final String ROLECODE_MISSING_FIELD = "roles";
    public static final String ROLECODE_MISSING_MESSAGE = "Atleast One Role Is Required.";
    public static final String USER_CLIENT_ID = "egov-user-client";
    public static final String IP_HEADER_NAME = "x-real-ip";


    public static final String PATTERN_NAME = "^[^\\\\$\\\"<>?\\\\\\\\~`!@#$%^()+={}\\\\[\\\\]*,:;“”‘’]*$";


    public static final String PATTERN_GENDER = "^[a-zA-Z ]*$";
    public static final String PATTERN_MOBILE = "(^$|[0-9]{10})";
    public static final String PATTERN_CITY = "^[a-zA-Z. ]*$";
    public static final String PATTERN_TENANT = "^[a-zA-Z. ]*$";
    public static final String PATTERN_PINCODE = "^[1-9][0-9]{5}$";
    
    public static final String TENANTID_MDC_STRING = "TENANTID";
    
    public static final String CITIZEN_ROLE_CODE = "CITIZEN";

    public static final String INVALID_TENANT_ID_ERR_CODE = "INVALID_TENANT_ID";
    
}

