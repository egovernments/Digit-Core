<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}"<#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}"</#if>>

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="importmap">
        {
            "imports": {
                "rfc4648": "${url.resourcesCommonPath}/node_modules/rfc4648/lib/rfc4648.js"
            }
        }
    </script>
    <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="module">
        import { checkCookiesAndSetTimer } from "${url.resourcesPath}/js/authChecker.js";

        checkCookiesAndSetTimer(
          "${url.ssoLoginInOtherTabsUrl?no_esc}"
        );
    </script>
</head>

<body class="${properties.kcBodyClass!}">
<div class="kc-shell">

    <#-- Brand bar -->
    <header class="kc-brandbar">
        <div class="kc-brandbar__inner">
            <div class="kc-brand">
                <img id="kc-dynamic-logo" class="kc-brand__logo" src="${url.resourcesPath}/img/logo.png" alt="${realm.displayName!realm.name} logo"/>
                <span class="kc-brand__name">${realm.displayName!realm.name}</span>
            </div>
        </div>
    </header>

    <main class="kc-main">

        <#-- Form side -->
        <div class="kc-form-side">
            <div class="${properties.kcLoginClass!}">
                <div id="kc-header" class="${properties.kcHeaderClass!}">
                    <div id="kc-header-wrapper"
                         class="${properties.kcHeaderWrapperClass!}">${kcSanitize(msg("loginTitleHtml",(realm.displayNameHtml!'')))?no_esc}</div>
                </div>
                <div class="${properties.kcFormCardClass!}">
                    <span class="kc-card-accent"></span>
                    <header class="${properties.kcFormHeaderClass!}">
                        <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                            <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
                                <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                                    <div id="kc-locale-dropdown" class="menu-button-links ${properties.kcLocaleDropDownClass!}">
                                        <button tabindex="1" id="kc-current-locale-link" aria-label="${msg("languages")}" aria-haspopup="true" aria-expanded="false" aria-controls="language-switch1">${locale.current}</button>
                                        <ul role="menu" tabindex="-1" aria-labelledby="kc-current-locale-link" aria-activedescendant="" id="language-switch1" class="${properties.kcLocaleListClass!}">
                                            <#assign i = 1>
                                            <#list locale.supported as l>
                                                <li class="${properties.kcLocaleListItemClass!}" role="none">
                                                    <a role="menuitem" id="language-${i}" class="${properties.kcLocaleItemClass!}" href="${l.url}">${l.label}</a>
                                                </li>
                                                <#assign i++>
                                            </#list>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </#if>
                    <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
                        <#if displayRequiredFields>
                            <div class="${properties.kcContentWrapperClass!}">
                                <div class="${properties.kcLabelWrapperClass!} subtitle">
                                    <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                </div>
                                <div class="col-md-10">
                                    <h1 id="kc-page-title"><#nested "header"></h1>
                                </div>
                            </div>
                        <#else>
                            <h1 id="kc-page-title"><#nested "header"></h1>
                        </#if>
                    <#else>
                        <#if displayRequiredFields>
                            <div class="${properties.kcContentWrapperClass!}">
                                <div class="${properties.kcLabelWrapperClass!} subtitle">
                                    <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                </div>
                                <div class="col-md-10">
                                    <#nested "show-username">
                                    <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                        <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                        <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                                            <div class="kc-login-tooltip">
                                                <i class="${properties.kcResetFlowIcon!}"></i>
                                                <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                            </div>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        <#else>
                            <#nested "show-username">
                            <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                                    <div class="kc-login-tooltip">
                                        <i class="${properties.kcResetFlowIcon!}"></i>
                                        <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                    </div>
                                </a>
                            </div>
                        </#if>
                    </#if>
                  </header>
                  <div id="kc-content">
                    <div id="kc-content-wrapper">

                      <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                          <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                              <div class="pf-c-alert__icon">
                                  <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                  <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                  <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                  <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                              </div>
                                  <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                          </div>
                      </#if>

                      <#nested "form">

                      <#if auth?has_content && auth.showTryAnotherWayLink()>
                          <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                              <div class="${properties.kcFormGroupClass!}">
                                  <input type="hidden" name="tryAnotherWay" value="on"/>
                                  <a href="#" id="try-another-way"
                                     onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                              </div>
                          </form>
                      </#if>

                      <#nested "socialProviders">

                      <#if displayInfo>
                          <div id="kc-info" class="${properties.kcSignUpClass!}">
                              <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                  <#nested "info">
                              </div>
                          </div>
                      </#if>
                    </div>
                  </div>

                </div>
            </div>
        </div>

        <#-- Showcase side: single illustration image (drop your image at resources/img/showcase.png) -->
        <aside class="kc-showcase" aria-hidden="true">
            <img class="kc-showcase__img" id="kc-showcase-img" src="${url.resourcesPath}/img/showcase1.png" alt=""/>
            <div class="kc-showcase-nav">
                <button type="button" class="kc-showcase-arrow" id="kc-showcase-prev" aria-label="Previous image">&#10094;</button>
                <button type="button" class="kc-showcase-arrow" id="kc-showcase-next" aria-label="Next image">&#10095;</button>
            </div>
        </aside>

    </main>

</div>

<#-- Showcase image carousel: left/right arrows cycle through the images -->
<script>
    (function () {
        var img = document.getElementById("kc-showcase-img");
        if (!img) return;

        var images = [
            "${url.resourcesPath}/img/showcase1.png",
            "${url.resourcesPath}/img/showcase2.png",
            "${url.resourcesPath}/img/showcase3.png",
            "${url.resourcesPath}/img/showcase4.png"
        ];
        var current = 0;

        // preload so switching is instant
        images.forEach(function (src) { new Image().src = src; });

        function show(i) {
            current = (i + images.length) % images.length;
            img.style.opacity = "0";
            var next = new Image();
            next.onload = function () {
                img.src = next.src;
                img.style.opacity = "1";
            };
            next.src = images[current];
        }

        // auto-advance every 3s; a manual click restarts the timer so the
        // image the user chose isn't immediately replaced
        var timer = setInterval(function () { show(current + 1); }, 3000);

        function manual(step) {
            clearInterval(timer);
            show(current + step);
            timer = setInterval(function () { show(current + 1); }, 3000);
        }

        document.getElementById("kc-showcase-prev")
            .addEventListener("click", function () { manual(-1); });
        document.getElementById("kc-showcase-next")
            .addEventListener("click", function () { manual(1); });
    })();
</script>

<#-- Dynamic tenant config (single call):
     GET /accounts/v3/config (x-tenant-id: realm name)
       - configKey "loginLogoUrl" -> configValue IS the logo URL (S3) -> swap brand logo
       - configKey "mobilePrefix" -> configValue is the country prefix (e.g. "+91")
         -> shown as a badge on the mobile-number field; prepended to the value on submit
     On any failure the default logo stays and the plain input keeps working. -->
<script>
    (function () {
        var tenant = "${realm.name}";
        if (!tenant) return;

        function findConfig(data, key) {
            var c = (data.configs || []).find(function (c) {
                return c.configKey === key && c.isActive !== false;
            });
            return c && c.configValue ? c.configValue : null;
        }

        function applyLogo(url) {
            var img = document.getElementById("kc-dynamic-logo");
            if (!img || !url) return;
            var probe = new Image();
            probe.onload = function () { img.src = url; };
            probe.src = url;
        }

        function applyPrefix(prefix) {
            var input = document.getElementById("mobileNumber");
            if (!input || !prefix) return;

            // badge before the input
            var wrap = document.createElement("div");
            wrap.className = "kc-mobile-prefix-group";
            input.parentNode.insertBefore(wrap, input);
            var badge = document.createElement("span");
            badge.className = "kc-mobile-prefix";
            badge.textContent = prefix;
            wrap.appendChild(badge);
            wrap.appendChild(input);

            // server re-fills the full number on validation errors — show only the local part
            if (input.value && input.value.indexOf(prefix) === 0) {
                input.value = input.value.slice(prefix.length);
            }

            // the visible input keeps only the local number; a hidden field carries
            // prefix + local number to the server, so the input never changes on submit
            var form = input.form;
            if (form) {
                var hidden = document.createElement("input");
                hidden.type = "hidden";
                hidden.name = "mobileNumber";
                form.appendChild(hidden);
                input.removeAttribute("name");

                form.addEventListener("submit", function () {
                    var v = input.value.replace(/[\s-]/g, "");
                    hidden.value = (v && v.indexOf(prefix) !== 0) ? prefix + v : v;
                });
            }
        }

        function applyOtpLength(n) {
            // OTP templates expose this hook; default is 6 boxes if unset
            if (n && typeof window.kcApplyOtpLength === "function") window.kcApplyOtpLength(n);
        }

        fetch("/accounts/v3/config", { headers: { "x-tenant-id": tenant } })
            .then(function (r) { if (!r.ok) throw new Error("config " + r.status); return r.json(); })
            .then(function (data) {
                applyLogo(findConfig(data, "loginLogoUrl"));
                applyPrefix(findConfig(data, "mobilePrefix"));
                applyOtpLength(findConfig(data, "otpLength"));
            })
            .catch(function (e) {
                console.debug("Tenant config unavailable, using defaults:", e && e.message);
            });
    })();
</script>
</body>
</html>
</#macro>
