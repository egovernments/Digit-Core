package org.egov.user.repository.builder;

import org.egov.user.web.contract.LoginAuditSearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Component
public class LoginAuditQueryBuilder {

    @Value("${login.audit.default.limit}")
    private Integer defaultLimit;

    @Value("${login.audit.max.limit}")
    private Integer maxLimit;


    public static final String INSERT_LOGIN_AUDIT_QUERY = "INSERT INTO public.eg_login_audit(\n" +
            "\ttenantid, userid, createdtime, ipaddress, loginstatus, roles, logintype)\n" +
            "\tVALUES (:tenantid, :userid, :createdtime, :ipaddress, :loginstatus, :roles, :logintype);";


    public static final String SEARCH_AUDIT_LOGIN = "SELECT * from eg_login_audit WHERE ";


    public String getQuery(final LoginAuditSearchRequest searchRequest, Map<String, Object> parmaMap) {

         addPaginationClause(searchRequest);
         StringBuilder selectQuery = new StringBuilder(SEARCH_AUDIT_LOGIN);

         selectQuery.append(" userid = :userid ");
        parmaMap.put("userid",searchRequest.getUserId());

         if(!ObjectUtils.isEmpty(searchRequest.getLoginStatus())){
             selectQuery.append(" AND loginstatus = :loginstatus ");
             parmaMap.put("loginstatus",searchRequest.getLoginStatus());
         }

        if(!ObjectUtils.isEmpty(searchRequest.getIpAddress())){
            selectQuery.append(" AND ipaddress = :ipaddress ");
            parmaMap.put("ipaddress", searchRequest.getIpAddress());
        }

        if(!ObjectUtils.isEmpty(searchRequest.getFromDate())){
            selectQuery.append(" AND createdtime >= :fromdate ");
            parmaMap.put("fromdate",searchRequest.getFromDate());
        }

        if(!ObjectUtils.isEmpty(searchRequest.getToDate())){
            selectQuery.append(" AND createdtime <= :todate");
            parmaMap.put("todate",searchRequest.getToDate());
        }

        selectQuery.append(" ORDER BY createdtime DESC ");

        selectQuery.append(" OFFSET :offset ");
        parmaMap.put("offset",searchRequest.getOffset());

        selectQuery.append(" LIMIT :limit ");
        parmaMap.put("limit",searchRequest.getLimit());

        return selectQuery.toString();
    }


    private void addPaginationClause(LoginAuditSearchRequest loginAuditSearchRequest){

        if (ObjectUtils.isEmpty(loginAuditSearchRequest.getOffset()))
            loginAuditSearchRequest.setOffset(0);

        if (ObjectUtils.isEmpty(loginAuditSearchRequest.getLimit()))
            loginAuditSearchRequest.setLimit(defaultLimit);
        else {
            if(loginAuditSearchRequest.getLimit() > maxLimit)
                loginAuditSearchRequest.setLimit(maxLimit);
        }

    }





}
