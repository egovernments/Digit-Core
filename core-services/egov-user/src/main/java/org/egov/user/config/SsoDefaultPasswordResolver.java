package org.egov.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a cryptographically secure random password for new SSO-created users.
 * Each call returns a unique 15-character password with guaranteed character diversity.
 * SSO users authenticate via IdP JWT; the password is write-once and not used for login.
 */
@Slf4j
@Component
public class SsoDefaultPasswordResolver {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%";
    private static final int LENGTH = 15;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure random password for a new SSO user.
     * 15 chars with at least one from [A-Z], [a-z], [0-9], and [@#$%], then shuffled.
     *
     * @return a new random password (never null)
     */
    public String generatePassword() {
        List<Character> chars = new ArrayList<>(LENGTH);

        pickOne(chars, UPPERCASE);
        pickOne(chars, LOWERCASE);
        pickOne(chars, DIGITS);
        pickOne(chars, SPECIAL);

        String all = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
        for (int i = chars.size(); i < LENGTH; i++) {
            chars.add(all.charAt(secureRandom.nextInt(all.length())));
        }

        Collections.shuffle(chars, secureRandom);
        StringBuilder sb = new StringBuilder(LENGTH);
        for (Character c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    private void pickOne(List<Character> out, String bucket) {
        out.add(bucket.charAt(secureRandom.nextInt(bucket.length())));
    }
}
