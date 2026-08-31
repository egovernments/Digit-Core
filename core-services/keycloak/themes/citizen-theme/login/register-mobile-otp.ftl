<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp'); section>

    <#if section="header">
        ${msg("registerOtpTitle")}

    <#elseif section="form">
        <form id="kc-register-mobile-otp-form"
              class="${properties.kcFormClass!}"
              action="${url.loginAction}"
              method="post">

            <p class="kc-otp-instruction">
                ${msg("registerOtpSentTo")} <span id="kc-otp-number">${maskedMobile!''}</span>
                <button type="submit" name="cancel" value="1" class="kc-otp-edit"
                        aria-label="${msg("registerOtpChangeNumber")}" title="${msg("registerOtpChangeNumber")}">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
                    </svg>
                </button>
            </p>

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
                       name="login" type="submit" value="${msg("registerOtpSubmit")}"/>
            </div>

            <div class="kc-otp-links">
                <span>${msg("otpDidntReceive")}</span>
                <button type="submit" name="resend" value="1" class="kc-linklike">${msg("resendCode")}</button>
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

                // theme-only: show the full number saved by the previous screen
                try {
                    var full = sessionStorage.getItem("kcRegMobile");
                    var span = document.getElementById("kc-otp-number");
                    if (full && span) span.textContent = full;
                } catch (e) {}

                // the tenant-config script in template.ftl calls this when otpLength is configured
                window.kcApplyOtpLength = function (n) {
                    n = parseInt(n, 10);
                    if (n && n > 0 && n <= 10 && boxes().length !== n) render(n);
                };
            })();
        </script>
    </#if>
</@layout.registrationLayout>
