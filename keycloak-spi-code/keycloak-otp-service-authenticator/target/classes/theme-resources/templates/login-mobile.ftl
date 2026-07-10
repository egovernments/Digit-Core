<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('mobileNumber'); section>

    <#if section="header">
        ${msg("mobileLoginTitle")}

    <#elseif section="form">
        <form id="kc-mobile-login-form"
              class="${properties.kcFormClass!}"
              action="${url.loginAction}"
              method="post">

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="mobileNumber" class="${properties.kcLabelClass!}">
                        ${msg("registerMobileLabel")}
                    </label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="mobileNumber"
                           name="mobileNumber"
                           type="tel"
                           inputmode="numeric"
                           autocomplete="tel"
                           class="${properties.kcInputClass!}"
                           autofocus
                           value="${(mobileNumber!'')}"
                           aria-invalid="<#if messagesPerField.existsError('mobileNumber')>true</#if>"/>

                    <#if messagesPerField.existsError('mobileNumber')>
                        <span id="input-error-mobileNumber"
                              class="${properties.kcInputErrorMessageClass!}"
                              aria-live="polite">
                            ${kcSanitize(messagesPerField.get('mobileNumber'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                               type="submit" value="${msg("doLogIn")}"/>
                    </div>
                </div>
            </div>

        </form>

        <#if realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
