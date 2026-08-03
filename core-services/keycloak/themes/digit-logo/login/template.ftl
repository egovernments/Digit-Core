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
            <span class="kc-brandbar__step">Workspace Access</span>
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

        <#-- Showcase side (matches the onboarding AuthShell feature panel) -->
        <aside class="kc-showcase" aria-hidden="true">
            <div class="kc-showcase__grid"></div>
            <div class="kc-showcase__inner">
                <div class="kc-showcase__badge">
                    <span class="kc-showcase__badge-dot"></span>
                    Secure Access
                </div>

                <div class="kc-features">

                    <#-- Templates -->
                    <div class="kc-feature is-active" data-accent="amber">
                        <div class="kc-mockup kc-mockup--templates">
                            <div class="kc-tmpl">
                                <span class="kc-tmpl__ico">&#128196;</span>
                                <div class="kc-tmpl__body">
                                    <p class="kc-tmpl__name">Business License</p>
                                    <div class="kc-tags"><span>Trade License</span><span>Business Reg.</span></div>
                                </div>
                            </div>
                            <div class="kc-tmpl">
                                <span class="kc-tmpl__ico">&#127970;</span>
                                <div class="kc-tmpl__body">
                                    <p class="kc-tmpl__name">Building Permits</p>
                                    <div class="kc-tags"><span>Construction</span><span>Works Approval</span></div>
                                </div>
                            </div>
                            <div class="kc-tmpl">
                                <span class="kc-tmpl__ico">&#128293;</span>
                                <div class="kc-tmpl__body">
                                    <p class="kc-tmpl__name">Fire NOC</p>
                                    <div class="kc-tags"><span>Fire Permit</span></div>
                                </div>
                            </div>
                        </div>
                        <p class="kc-feature__title">Ready-to-use templates</p>
                        <p class="kc-feature__desc">10+ service types, pre-configured</p>
                    </div>

                    <#-- Roles -->
                    <div class="kc-feature" data-accent="violet">
                        <div class="kc-mockup kc-mockup--roles">
                            <div class="kc-role">
                                <span class="kc-role__avatar">&#128100;</span>
                                <p class="kc-role__name">Citizen</p>
                                <span class="kc-role__badge">Default</span>
                            </div>
                            <div class="kc-role">
                                <span class="kc-role__avatar">&#128100;</span>
                                <p class="kc-role__name">Doc Verifier</p>
                                <span class="kc-role__steps">2 steps</span>
                            </div>
                            <div class="kc-role">
                                <span class="kc-role__avatar">&#128100;</span>
                                <p class="kc-role__name">Approver</p>
                                <span class="kc-role__badge">Approver</span>
                            </div>
                        </div>
                        <p class="kc-feature__title">Role-based access</p>
                        <p class="kc-feature__desc">Citizen, verifier &amp; approver workflows</p>
                    </div>

                    <#-- No-code builder -->
                    <div class="kc-feature" data-accent="emerald">
                        <div class="kc-mockup kc-mockup--nocode">
                            <div class="kc-palette">
                                <p class="kc-mockup__label">Fields</p>
                                <span class="kc-chip">&#9632; Name</span>
                                <span class="kc-chip">&#9632; Address</span>
                                <span class="kc-chip">&#9632; File</span>
                                <span class="kc-chip">&#9632; Slider</span>
                            </div>
                            <div class="kc-canvas">
                                <p class="kc-mockup__label">Canvas</p>
                                <div class="kc-canvas__field is-on">Full Name &#10022;</div>
                                <div class="kc-canvas__field">Phone Number</div>
                                <div class="kc-canvas__drop">+ drop field</div>
                            </div>
                        </div>
                        <p class="kc-feature__title">Configure without code</p>
                        <p class="kc-feature__desc">Visual form builder &amp; workflow designer</p>
                    </div>

                    <#-- Analytics -->
                    <div class="kc-feature" data-accent="sky">
                        <div class="kc-mockup kc-mockup--analytics">
                            <div class="kc-kpis">
                                <div class="kc-kpi"><span class="kc-kpi__v">18.4k</span><span class="kc-kpi__l">Active</span></div>
                                <div class="kc-kpi"><span class="kc-kpi__v">697</span><span class="kc-kpi__l">Pending</span></div>
                                <div class="kc-kpi"><span class="kc-kpi__v">92%</span><span class="kc-kpi__l">SLA</span></div>
                            </div>
                            <div class="kc-bars">
                                <span style="height:28%"></span><span style="height:42%"></span><span style="height:35%"></span>
                                <span style="height:58%"></span><span style="height:48%"></span><span style="height:67%"></span>
                                <span style="height:55%"></span><span style="height:78%"></span><span style="height:68%"></span>
                                <span style="height:88%"></span><span style="height:75%"></span><span style="height:92%"></span>
                            </div>
                            <p class="kc-trend">&#9650; Application trend &middot; last 12 months</p>
                        </div>
                        <p class="kc-feature__title">Monitor &amp; manage</p>
                        <p class="kc-feature__desc">Real-time KPIs, SLA tracking, audit trails</p>
                    </div>

                </div>

                <div class="kc-showcase__foot">
                    <div class="kc-showcase__pills">
                        <span class="kc-pill is-active"></span>
                        <span class="kc-pill"></span>
                        <span class="kc-pill"></span>
                        <span class="kc-pill"></span>
                    </div>
                    <div class="kc-showcase__trust">
                        <span class="kc-showcase__trust-line"></span>
                        Trusted by public institutions
                    </div>
                </div>
            </div>
        </aside>

    </main>

    <#-- Footer bar -->
    <footer class="kc-footer">
        <div class="kc-footer__inner">
            <span>Secure government workspace</span>
            <span>v1.0</span>
        </div>
    </footer>

</div>

<#-- Auto-rotate the active feature card to mirror the onboarding showcase -->
<script>
    (function () {
        var cards = document.querySelectorAll(".kc-feature");
        var pills = document.querySelectorAll(".kc-pill");
        if (!cards.length) return;
        var active = 0;
        setInterval(function () {
            cards[active].classList.remove("is-active");
            if (pills[active]) pills[active].classList.remove("is-active");
            active = (active + 1) % cards.length;
            cards[active].classList.add("is-active");
            if (pills[active]) pills[active].classList.add("is-active");
        }, 3800);
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

            // on submit, send prefix + local number as one value
            var form = input.form;
            if (form) {
                form.addEventListener("submit", function () {
                    var v = input.value.replace(/[\s-]/g, "");
                    if (v && v.indexOf(prefix) !== 0) {
                        input.value = prefix + v;
                    }
                });
            }
        }

        fetch("/accounts/v3/config", { headers: { "x-tenant-id": tenant } })
            .then(function (r) { if (!r.ok) throw new Error("config " + r.status); return r.json(); })
            .then(function (data) {
                applyLogo(findConfig(data, "loginLogoUrl"));
                applyPrefix(findConfig(data, "mobilePrefix"));
            })
            .catch(function (e) {
                console.debug("Tenant config unavailable, using defaults:", e && e.message);
            });
    })();
</script>
</body>
</html>
</#macro>
