<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('mobileNumber'); section>

    <#if section="header">
        ${msg("registerMobileTitle")}

    <#elseif section="form">
        <form id="kc-register-mobile-form"
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
                               type="submit" value="${msg("registerMobileSubmit")}"/>
                    </div>
                </div>
                <div class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span>${msg("registerAlreadyUser")} <a href="${url.loginUrl}">${msg("registerLoginLink")}</a></span>
                    </div>
                </div>
            </div>

        </form>

        <script>
            // theme-only: remember the full number (prefix badge + typed local part)
            // so the OTP screen can display it unmasked
            (function () {
                var form = document.getElementById("kc-register-mobile-form");
                var input = document.getElementById("mobileNumber");
                if (!form || !input) return;
                form.addEventListener("submit", function () {
                    var badge = form.querySelector(".kc-mobile-prefix");
                    var prefix = badge ? badge.textContent.trim() : "";
                    var v = input.value.replace(/[\s-]/g, "");
                    if (v && prefix && v.indexOf(prefix) === 0) prefix = "";
                    try { sessionStorage.setItem("kcRegMobile", prefix + v); } catch (e) {}
                });
            })();
        </script>
    </#if>
</@layout.registrationLayout>
