<#import "template.ftl" as layout>
<#-- Blocks direct access to the registration page (/protocol/openid-connect/registrations)
     for any client this theme is assigned to. Hiding the link in login.ftl alone is
     cosmetic; this closes the direct-URL path. -->
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("registerTitle")}
    <#elseif section = "form">
        <div id="kc-registration-blocked" class="${properties.kcAlertClass!} pf-m-warning" style="margin-bottom: 1rem;">
            Self-registration is not available for this application. Please contact your administrator.
        </div>
        <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
            <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
        </div>
    </#if>
</@layout.registrationLayout>
