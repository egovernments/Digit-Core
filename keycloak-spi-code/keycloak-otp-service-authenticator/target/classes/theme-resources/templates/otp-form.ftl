<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp'); section>

    <#if section="header">
        ${msg("otpFormTitle")}

    <#elseif section="form">
        <form id="kc-otp-login-form"
              class="${properties.kcFormClass!}"
              action="${url.loginAction}"
              method="post">

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="otp" class="${properties.kcLabelClass!}">
                        ${msg("otpFormLabel")}
                    </label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="otp"
                           name="otp"
                           type="text"
                           inputmode="numeric"
                           autocomplete="one-time-code"
                           class="${properties.kcInputClass!}"
                           autofocus
                           aria-invalid="<#if messagesPerField.existsError('otp')>true</#if>"/>

                    <#if messagesPerField.existsError('otp')>
                        <span id="input-error-otp"
                              class="${properties.kcInputErrorMessageClass!}"
                              aria-live="polite">
                            ${kcSanitize(messagesPerField.get('otp'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                               name="login" type="submit" value="${msg("doLogIn")}"/>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                               name="resend" type="submit" value="${msg("resendCode")}"/>
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                               name="cancel" type="submit" value="${msg("doCancel")}"/>
                    </div>
                </div>
            </div>

        </form>
    </#if>
</@layout.registrationLayout>