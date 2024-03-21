package digit.repository.rowmapper;

import digit.web.models.*;
import org.egov.common.contract.models.Address;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.enums.AddressType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class BirthApplicationRowMapper implements ResultSetExtractor<List<BirthRegistrationApplication>> {
    public List<BirthRegistrationApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String,BirthRegistrationApplication> birthRegistrationApplicationMap = new LinkedHashMap<>();

        while (rs.next()){
            String uuid = rs.getString("bapplicationnumber");
            BirthRegistrationApplication birthRegistrationApplication = birthRegistrationApplicationMap.get(uuid);

            if(birthRegistrationApplication == null) {

                Long lastModifiedTime = rs.getLong("blastModifiedTime");
                if (rs.wasNull()) {
                    lastModifiedTime = null;
                }


                User father = User.builder().uuid(rs.getString("bfatherid")).build();
                User mother = User.builder().uuid(rs.getString("bmotherid")).build();

                AuditDetails auditdetails = AuditDetails.builder()
                        .createdBy(rs.getString("bcreatedBy"))
                        .createdTime(rs.getLong("bcreatedTime"))
                        .lastModifiedBy(rs.getString("blastModifiedBy"))
                        .lastModifiedTime(lastModifiedTime)
                        .build();

                birthRegistrationApplication = BirthRegistrationApplication.builder()
                        .applicationNumber(rs.getString("bapplicationnumber"))
                        .tenantId(rs.getString("btenantid"))
                        .id(rs.getString("bid"))
                        .babyFirstName(rs.getString("bbabyfirstname"))
                        .babyLastName(rs.getString("bbabylastname"))
                        .father(father)
                        .mother(mother)
                        .doctorName(rs.getString("bdoctorname"))
                        .hospitalName(rs.getString("bhospitalname"))
                        .placeOfBirth(rs.getString("bplaceofbirth"))
                        .timeOfBirth(rs.getInt("btimeofbirth"))
                        .auditDetails(auditdetails)
                        .build();
            }
            addChildrenToProperty(rs, birthRegistrationApplication);
            birthRegistrationApplicationMap.put(uuid, birthRegistrationApplication);
        }
        return new ArrayList<>(birthRegistrationApplicationMap.values());
    }

    private void addChildrenToProperty(ResultSet rs, BirthRegistrationApplication birthRegistrationApplication)
            throws SQLException {
        addAddressToApplication(rs, birthRegistrationApplication);
    }

    private void addAddressToApplication(ResultSet rs, BirthRegistrationApplication birthRegistrationApplication) throws SQLException {
        Address address = Address.builder()
                .tenantId(rs.getString("atenantid"))
                .address(rs.getString("aaddress"))
                .city(rs.getString("acity"))
                .pinCode(rs.getString("apincode"))
                .build();

        BirthApplicationAddress birthApplicationAddress= BirthApplicationAddress.builder()
                        .id(rs.getString("aid"))
                        .tenantId(rs.getString("atenantid"))
                        .applicantAddress(address)
                        .build();

        birthRegistrationApplication.setAddress(birthApplicationAddress);

    }

}
