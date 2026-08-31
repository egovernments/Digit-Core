<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp'); section>

    <#if section="header">
        ${msg("otpFormTitle")}

    <#elseif section="form">
        <form id="kc-otp-login-form"
              class="${properties.kcFormClass!}"
              action="${url.loginAction}"
              method="post">

            <p class="kc-otp-instruction">${msg("otpFormLabel")}</p>

            <input type="hidden" id="otp" name="otp"/>

            <div class="kc-otp-boxes" id="kc-otp-boxes"
                 aria-invalid="<#if messagesPerField.existsError('otp')>true</#if>"></div>

            <#if messagesPerField.existsError('otp')>
                <span id="input-error-otp"
                      class="kc-otp-error ${properties.kcInputErrorMessageClass!}"
                      aria-live="polite">
                    ${kcSanitize(messagesPerField.get('otp'))?no_esc}
                </span>
            </#if>

            <div class="kc-otp-actions">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                       name="login" type="submit" value="${msg("doLogIn")}"/>
            </div>

            <div class="kc-otp-links">
                <span>${msg("otpDidntReceive")}</span>
                <button type="submit" name="resend" value="1" class="kc-linklike">${msg("resendCode")}</button>
            </div>
            <div class="kc-otp-links">
                <button type="submit" name="cancel" value="1" class="kc-linklike kc-linklike--muted">${msg("doCancel")}</button>
            </div>

        </form>

        <script>
            (function () {
                var DEFAULT_LEN = 6;
                var hidden = document.getElementById("otp");
                var wrap = document.getElementById("kc-otp-boxes");

                function boxes() { return wrap.querySelectorAll("input"); }

                function sync() {
                    var v = "";
                    boxes().forEach(function (b) { v += b.value; });
                    hidden.value = v;
                }

                function render(n) {
                    wrap.innerHTML = "";
                    for (var i = 0; i < n; i++) {
                        var b = document.createElement("input");
                        b.type = "text";
                        b.inputMode = "numeric";
                        b.maxLength = 1;
                        b.autocomplete = (i === 0) ? "one-time-code" : "off";
                        b.className = "kc-otp-box";
                        b.setAttribute("aria-label", "OTP digit " + (i + 1));
                        wrap.appendChild(b);
                    }
                    var bs = boxes();
                    bs.forEach(function (b, i) {
                        b.addEventListener("input", function () {
                            b.value = b.value.replace(/\D/g, "").slice(-1);
                            if (b.value && i < bs.length - 1) bs[i + 1].focus();
                            sync();
                        });
                        b.addEventListener("keydown", function (e) {
                            if (e.key === "Backspace" && !b.value && i > 0) bs[i - 1].focus();
                        });
                        b.addEventListener("paste", function (e) {
                            var t = (e.clipboardData.getData("text") || "").replace(/\D/g, "");
                            if (!t) return;
                            e.preventDefault();
                            for (var j = 0; j < bs.length; j++) bs[j].value = t[j] || "";
                            bs[Math.min(t.length, bs.length) - 1].focus();
                            sync();
                        });
                    });
                    if (bs[0]) bs[0].focus();
                    sync();
                }

                render(DEFAULT_LEN);

                // the tenant-config script in template.ftl calls this when otpLength is configured
                window.kcApplyOtpLength = function (n) {
                    n = parseInt(n, 10);
                    if (n && n > 0 && n <= 10 && boxes().length !== n) render(n);
                };
            })();
        </script>
    </#if>
</@layout.registrationLayout>
