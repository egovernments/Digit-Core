package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import static digit.config.ServiceConstants.*;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.enums.UserType;
import digit.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class UserUtil {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private Configuration configs;


    @Autowired
    public UserUtil(ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository) {
        this.mapper = mapper;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Returns UserDetailResponse by calling user service with given uri and object
     * @param userRequest Request object for user service
     * @param uri The address of the endpoint
     * @return Response from user service as parsed as userDetailResponse
     */

    public UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
        String dobFormat = null;
        if(uri.toString().contains(configs.getUserSearchEndpoint())  || uri.toString().contains(configs.getUserUpdateEndpoint()))
            dobFormat=DOB_FORMAT_Y_M_D;
        else if(uri.toString().contains(configs.getUserCreateEndpoint()))
            dobFormat = DOB_FORMAT_D_M_Y;
        try{
            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, userRequest);
            parseResponse(responseMap,dobFormat);
            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
            return userDetailResponse;
        }
        catch(IllegalArgumentException  e)
        {
            throw new CustomException(ILLEGAL_ARGUMENT_EXCEPTION_CODE,OBJECTMAPPER_UNABLE_TO_CONVERT);
        }
    }


    /**
     * Parses date formats to long for all users in responseMap
     * @param responseMap LinkedHashMap got from user api response
     */

    public void parseResponse(LinkedHashMap responseMap, String dobFormat){
        List<LinkedHashMap> users = (List<LinkedHashMap>)responseMap.get(USER);
        String format1 = DOB_FORMAT_D_M_Y_H_M_S;
        if(users!=null){
            users.forEach( map -> {
                        map.put(CREATED_DATE,dateTolong((String)map.get(CREATED_DATE),format1));
                        if((String)map.get(LAST_MODIFIED_DATE)!=null)
                            map.put(LAST_MODIFIED_DATE,dateTolong((String)map.get(LAST_MODIFIED_DATE),format1));
                        if((String)map.get(DOB)!=null)
                            map.put(DOB,dateTolong((String)map.get(DOB),dobFormat));
                        if((String)map.get(PWD_EXPIRY_DATE)!=null)
                            map.put(PWD_EXPIRY_DATE,dateTolong((String)map.get(PWD_EXPIRY_DATE),format1));
                    }
            );
        }
    }

    /**
     * Converts date to long
     * @param date date to be parsed
     * @param format Format of the date
     * @return Long value of date
     */
    private Long dateTolong(String date,String format){
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            throw new CustomException(INVALID_DATE_FORMAT_CODE,INVALID_DATE_FORMAT_MESSAGE);
        }
        return  d.getTime();
    }

    /**
     * enriches the userInfo with statelevel tenantId and other fields
     * The function creates user with username as mobile number.
     * @param mobileNumber
     * @param tenantId
     * @param userInfo
     */
    public void addUserDefaultFields(String mobileNumber,String tenantId, User userInfo){
        Role role = getCitizenRole(tenantId);
        userInfo.setMobileNumber(mobileNumber);
        userInfo.setTenantId(getStateLevelTenant(tenantId));
        userInfo.setType("CITIZEN");
    }

    /**
     * Returns role object for citizen
     * @param tenantId
     * @return
     */
    private Role getCitizenRole(String tenantId){
        Role role = Role.builder().build();
        role.setCode(CITIZEN_UPPER);
        role.setName(CITIZEN_LOWER);
        role.setTenantId(getStateLevelTenant(tenantId));
        return role;
    }

    public String getStateLevelTenant(String tenantId){
        return tenantId.split("\\.")[0];
    }

}