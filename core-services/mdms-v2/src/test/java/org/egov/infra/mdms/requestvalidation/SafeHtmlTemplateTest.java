package org.egov.infra.mdms.requestvalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeHtmlTemplateTest {
    /** Templates found in deployed MDMS and localization data. */
    @ParameterizedTest
    @ValueSource(strings = {
        "<!DOCTYPE html><html><body><p style='font-family:Arial,sans-serif;color:#333;font-size:14px'>Dear {userName},</p>"
            + "<p style='font-family:Arial,sans-serif;color:#333;font-size:14px'>You have <strong>{billCount}</strong> expense"
            + " bill(s) for campaign <strong>{campaignName}</strong> waiting for your approval.</p><p style='font-family:Arial,"
            + "sans-serif;color:#333;font-size:14px'>Please log in to https://example.digit.org/payments-ui/employee to take"
            + " action.</p><br/><p style='font-family:Arial,sans-serif;color:#333;font-size:14px'>- Team</p></body></html>",
        "<h2>Console login credentials</h2><p>Hi {User's name},<br>Your profile has been created to access the console."
            + " Please find your login credentials below -<br>URL: {website URL}<br>Username: {Username}<br>Password:"
            + " {Password}<br>Thank you,<br>{Implementation partner}</p>",
        "<strong>Campaign Details:</strong><br/><strong>Campaign:</strong> {campaignName}<br/><strong>User Credential Access"
            + " Link:</strong> <a href=\"{accessLink}\">Access Link</a><br/><strong>App Download:</strong>"
            + " <a href=\"{appLink}\">App Link</a>",
        "Hi {campaignManagerName},<br/><br/>Your health campaign setup is now complete!",
        "<strong>Need Support?</strong><br/>Please contact our support team at"
            + " <a href=mailto:support@egov.org.in'>support@egov.org.in</a>.",
        "<table border=\"1\" cellpadding=\"4\" width=\"100%\"><tr><th align=\"left\">A</th><td colspan=\"2\""
            + " style=\"padding:4px;border:1px solid #ccc\">B</td></tr></table>",
        "<p>Visit <a href=\"https://example.org/x?y=1&amp;z=2\" target=\"_blank\">site</a> for children <5 years & a < b</p>",
        "Plain text with {{PLACEHOLDER}} and no markup"
    })
    void acceptsPlainFormattingTemplates(String html) {
        assertTrue(SafeHtmlTemplate.isSafe(html), html);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert(1)</script>",
        "<p>Hi</p><script src=https://evil.example/x.js></script>",
        "<img src=x onerror=alert(1)>",
        "<p onclick=\"alert(1)\">x</p>",
        "<p ONMOUSEOVER=alert(1)>x</p>",
        "<a href=\"javascript:alert(1)\">x</a>",
        "<a href=\"jav&#x61;script:alert(1)\">x</a>",
        "<a href=\" JaVaScRiPt:alert(1)\">x</a>",
        "<a href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\">x</a>",
        "<a href=\"vbscript:msgbox(1)\">x</a>",
        "<a href=\"//evil.example\">x</a>",
        "<svg onload=alert(1)>",
        "<svg><a href=\"https://x\">y</a></svg>",
        "<iframe src=\"https://evil.example\"></iframe>",
        "<object data=\"x\"></object>",
        "<embed src=\"x\">",
        "<form action=\"https://evil.example\"><input name=p></form>",
        "<style>body{background:url(javascript:alert(1))}</style>",
        "<p style=\"background:url(https://evil.example/t.png)\">x</p>",
        "<p style=\"width:expression(alert(1))\">x</p>",
        "<p style=\"color:red;behavior:url(x.htc)\">x</p>",
        "<p style=\"color:\\72 ed\">x</p>",
        "<p style=\"color:red/**/\">x</p>",
        "<!--[if gte mso 9]><script>alert(1)</script><![endif]--><p>x</p>",
        "<!-- comment --><p>x</p>",
        "<p title=\"x\">y</p>",
        "<p class=\"x\">y</p>",
        "<meta http-equiv=\"refresh\" content=\"0;url=https://evil.example\">",
        "<base href=\"https://evil.example/\">",
        "<link rel=stylesheet href=https://evil.example/x.css>",
        "<math><mtext><table><mglyph><style><img src=x onerror=alert(1)>",
        "<textarea><img src=x onerror=alert(1)></textarea>",
        "<noscript><p title=\"</noscript><img src=x onerror=alert(1)>\"></noscript>",
        "<details open ontoggle=alert(1)>",
        "<p>x</p>\u0000",
        "<a href=\"{accessLink\" onclick=\"x\">y</a>",
        "<table background=\"javascript:alert(1)\"><tr><td>x</td></tr></table>",
        "<table><tr><td bgcolor=\"red;x:expression(1)\">x</td></tr></table>",
        // Tags a parser drops here but that can take effect once the template is joined with other content.
        "<p>Hi</p><script src=\"data:&comma;alert(1)//",
        "<x onxxx=1",
        "<p>x</p><a target=\"x\" href=\"https://x?<script>alert(1)</script>",
        "<h2>T</h2><FRAMESET><FRAME SRC=\"javascript:alert(1)\"></FRAMESET>",
        "<p>x</p></style></script>",
        "<p>x</p><head>",
        "<p>x</p></>",
        "<b>Hi</b><",
        "<p>Welcome to the console.</p><",
        "<!DOCTYPE doc [<p>x</p>",
        "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><p>x</p>",
        "<p>x</p><!DOCTYPE html>",
        // Markup carried as text, encoded or not, and script URLs as text.
        "<p>&lt;script&gt;alert(1)&lt;/script&gt;</p>",
        "<p>%3Cscript%3Ealert(1)%3C/script%3E</p>",
        "<h2>T</h2>javascript:alert(1)",
        "<h2>T</h2>data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
        "<a href=\"https://x/?q=%3Cscript%3E\">x</a>"
    })
    void rejectsActiveContent(String html) {
        assertFalse(SafeHtmlTemplate.isSafe(html), html);
    }

    @Test
    void deepNestingFailsWithoutExhaustingARequestThreadStack() throws Exception {
        String shallow = "<b>".repeat(MAX_TEMPLATE_DEPTH) + "x";
        String deep = "<b>".repeat(20_000) + "x";
        AtomicReference<Object> results = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                results.set(new boolean[] {SafeHtmlTemplate.isSafe(shallow), SafeHtmlTemplate.isSafe(deep)});
            } catch (Throwable failure) {
                results.set(failure);
            }
        }, "request", 1 << 20);
        thread.start();
        thread.join();
        assertTrue(results.get() instanceof boolean[], String.valueOf(results.get()));
        assertTrue(((boolean[]) results.get())[0]);
        assertFalse(((boolean[]) results.get())[1]);
    }

    // html and body are implied, so this many nested tags stays within the 64-level limit.
    private static final int MAX_TEMPLATE_DEPTH = 60;
}
