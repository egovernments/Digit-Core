package digit.enrichment;

import digit.service.UserService;
import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Component
@Slf4j
public class BirthApplicationEnrichment {

    @Autowired
    private IdgenUtil idgenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtil userUtils;

    public void enrichBirthApplication(BirthRegistrationRequest birthRegistrationRequest) {
       List<String> birthRegistrationIdList = idgenUtil.getIdList(birthRegistrationRequest.getRequestInfo(), birthRegistrationRequest.getBirthRegistrationApplications().get(0).getTenantId(), "btr.registrationid", "", birthRegistrationRequest.getBirthRegistrationApplications().size());
        Integer index = 0;
        for(BirthRegistrationApplication application : birthRegistrationRequest.getBirthRegistrationApplications()){
            // Enrich audit details
            AuditDetails auditDetails = AuditDetails.builder().createdBy(birthRegistrationRequest.getRequestInfo().getUserInfo().getUuid()).createdTime(System.currentTimeMillis()).lastModifiedBy(birthRegistrationRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();
            application.setAuditDetails(auditDetails);

            // Enrich UUID
            application.setId(UUID.randomUUID().toString());

//            Enrich application number from IDgen
            application.setApplicationNumber(birthRegistrationIdList.get(index++));
//             Enrich registration Id
            application.getAddress().setApplicationNumber(application.getId());

            // Enrich address UUID
            application.getAddress().setId(UUID.randomUUID().toString());

        }
    }

    public void enrichBirthApplicationUponUpdate(BirthRegistrationRequest birthRegistrationRequest) {
        // Enrich lastModifiedTime and lastModifiedBy in case of update
        birthRegistrationRequest.getBirthRegistrationApplications().get(0).getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
        birthRegistrationRequest.getBirthRegistrationApplications().get(0).getAuditDetails().setLastModifiedBy(birthRegistrationRequest.getRequestInfo().getUserInfo().getUuid());
    }

    public void enrichFatherApplicantOnSearch(BirthRegistrationApplication application) {
        UserDetailResponse fatherUserResponse = userService.searchUser(userUtils.getStateLevelTenant(application.getTenantId()),application.getFather().getUuid(),null);
        User fatherUser = fatherUserResponse.getUser().get(0);
        log.info(fatherUser.toString());
        User fatherApplicant = User.builder()
                .mobileNumber(fatherUser.getMobileNumber())
                .id(fatherUser.getId())
                .name(fatherUser.getName())
                .userName((fatherUser.getUserName()))
                .type(fatherUser.getType())
                .roles(fatherUser.getRoles())
                .uuid(fatherUser.getUuid()).build();
        application.setFather(fatherApplicant);
    }

    public void enrichMotherApplicantOnSearch(BirthRegistrationApplication application) {
        UserDetailResponse motherUserResponse = userService.searchUser(userUtils.getStateLevelTenant(application.getTenantId()),application.getMother().getUuid(),null);
        User motherUser = motherUserResponse.getUser().get(0);
        log.info(motherUser.toString());
        User motherApplicant = User.builder()
                .mobileNumber(motherUser.getMobileNumber())
                .id(motherUser.getId())
                .name(motherUser.getName())
                .userName((motherUser.getUserName()))
                .type(motherUser.getType())
                .roles(motherUser.getRoles())
                .uuid(motherUser.getUuid()).build();
        application.setMother(motherApplicant);
    }
}