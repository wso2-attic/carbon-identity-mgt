package org.wso2.carbon.identity.policy.password.history.util;

import org.wso2.carbon.identity.policy.password.history.constants.PasswordHistoryConstants;
import org.wso2.carbon.identity.policy.password.history.exeption.IdentityPasswordHistoryException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password salting and Hashing utilities
 */
public class PasswordHistoryUtil {

    /**
     * Generate salt value
     * @return : generate salt value
     */
    public static String generateSaltValue() {

        try {
            SecureRandom secureRandom = SecureRandom.getInstance(PasswordHistoryConstants.SHA_1_PRNG);
            byte[] bytes = new byte[16];
            //secureRandom is automatically seeded by calling nextBytes
            secureRandom.nextBytes(bytes);
            return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1PRNG algorithm could not be found.");
        }
    }

    /**
     * Convert credentials into salted hash
     * @param credential : password
     * @param saltValue : generated salt
     * @param hashingAlgorithm : hashing algorithm to generate salted hash
     * @return : salted hash
     * @throws IdentityPasswordHistoryException : When provided algorithm is invalid
     */
    public static String generateSaltedHash(char[] credential, String saltValue, String hashingAlgorithm) throws
            IdentityPasswordHistoryException {

        try {
            String digestInput = new String(credential);
            if (saltValue != null) {
                digestInput = digestInput + saltValue;
            }
            MessageDigest dgst = MessageDigest.getInstance(hashingAlgorithm);
            byte[] byteValue = dgst.digest(digestInput.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(byteValue), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            throw new IdentityPasswordHistoryException("Error occurred while generating salted hash for given password"
                    , e);
        }
    }

    /**
     * Covert days to milliseconds
     * @param days : days to convert
     * @return : millisecond value
     */
    public static long convertDaysToMilliseconds(int days) {

        return 1000L * 3600 * 24 * days;
    }

}
