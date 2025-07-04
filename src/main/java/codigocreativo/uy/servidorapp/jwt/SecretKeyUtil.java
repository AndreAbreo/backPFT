package codigocreativo.uy.servidorapp.jwt;

import java.util.Base64;
import java.util.logging.Logger;

/**
 * Utility class for retrieving the secret key used to sign JWT tokens.
 */
public final class SecretKeyUtil {

    private static final Logger LOGGER = Logger.getLogger(SecretKeyUtil.class.getName());

    /**
     * Default Base64 encoded secret key used when the environment variable is not present
     * or is invalid. The decoded value must contain at least 32 bytes to satisfy the
     * requirements of HS256.
     */
    public static final String DEFAULT_SECRET_KEY = "ZGVmYXVsdF9zZWNyZXRfa2V5X2Zvcl90ZXN0c19vbmx5XzMyX2J5dGVzX2xvbmc=";

    private SecretKeyUtil() {
        // Utility class
    }

    /**
     * Returns the bytes of the secret key to use for JWT operations. The value is
     * obtained from the <code>SECRET_KEY</code> environment variable. If the variable
     * is not defined, blank or not valid Base64, the {@link #DEFAULT_SECRET_KEY} is
     * used instead and a warning is logged.
     *
     * @return a byte array representing the secret key
     */
    public static byte[] getSecretKeyBytes() {
        String envKey = System.getenv("SECRET_KEY");
        if (envKey == null || envKey.isBlank()) {
            LOGGER.warning("SECRET_KEY env var not set or blank, using default secret key");
            return Base64.getDecoder().decode(DEFAULT_SECRET_KEY);
        }
        try {
            return Base64.getDecoder().decode(envKey);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("SECRET_KEY env var is not valid Base64, using default secret key");
            return Base64.getDecoder().decode(DEFAULT_SECRET_KEY);
        }
    }
}
