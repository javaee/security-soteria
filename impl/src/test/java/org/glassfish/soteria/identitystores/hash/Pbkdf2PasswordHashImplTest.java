package org.glassfish.soteria.identitystores.hash;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 *
 */

public class Pbkdf2PasswordHashImplTest {

    private Pbkdf2PasswordHashImpl hashImpl = new Pbkdf2PasswordHashImpl();

    private byte[] salt1;
    private byte[] salt2;

    private byte[] encodedHash1;
    private byte[] encodedHash2;
    private byte[] encodedHash3;
    private byte[] encodedHash4;

    @Before
    public void setup() {
        SecureRandom secureRandom = new SecureRandom();

        salt1 = new byte[32];
        secureRandom.nextBytes(salt1);

        salt2 = new byte[64];
        secureRandom.nextBytes(salt2);

        encodedHash1 = createHash("Soteria".toCharArray(), salt1, "PBKDF2WithHmacSHA256", 16, 32);
        encodedHash2 = createHash("Soteria".toCharArray(), salt2, "PBKDF2WithHmacSHA512", 1024, 64);
        encodedHash3 = createHash("SomethingElse".toCharArray(), salt1, "PBKDF2WithHmacSHA512", 1024, 64);
        encodedHash4 = createHash("SomethingElse".toCharArray(), salt2, "PBKDF2WithHmacSHA256", 32, 32);
    }

    @Test
    public void verify() {
        // Some happy cases
        Assert.assertTrue(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", 16)));
        Assert.assertTrue(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash2, salt2, "PBKDF2WithHmacSHA512", 1024)));
        Assert.assertTrue(hashImpl.verify("SomethingElse".toCharArray(), encodedPassword(encodedHash3, salt1, "PBKDF2WithHmacSHA512", 1024)));
        Assert.assertTrue(hashImpl.verify("SomethingElse".toCharArray(), encodedPassword(encodedHash4, salt2, "PBKDF2WithHmacSHA256", 32)));
    }

    @Test
    public void verify_failure() {
        // Some wrong  cases
        Assert.assertFalse(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", 64)));
        Assert.assertFalse(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA512", 16)));
        Assert.assertFalse(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt2, "PBKDF2WithHmacSHA256", 16)));
        Assert.assertFalse(hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash2, salt1, "PBKDF2WithHmacSHA256", 16)));

        Assert.assertFalse(hashImpl.verify("SomethingElse".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", 16)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verify_wrong1() {
        // Not supported algo
        hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA1", 64));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verify_wrong2() {
        // iterations not a number
        String encodedPassword = encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", 12);
        String newValue = encodedPassword.replaceAll(":12:", ":hihihi:");
        hashImpl.verify("Soteria".toCharArray(), newValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verify_wrong3() {
        // since -1 (or illegal value) is not captured within Soteria ....
        hashImpl.verify("Soteria".toCharArray(), encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", -1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verify_wrong4() {
        // Illegal Base64
        String encodedPassword = encodedPassword(encodedHash1, salt1, "PBKDF2WithHmacSHA256", 64);
        String newValue = encodedPassword.substring(0, 30) + "$" + encodedPassword.substring(31); // add an illegal value
        hashImpl.verify("Soteria".toCharArray(), newValue);
    }

    private String encodedPassword(byte[] encodedHash, byte[] salt, String algo, int iterations) {
        String result = algo + ":" + iterations + ":" + Base64.encode(salt) + ":" + Base64.encode(encodedHash);
        // base64 adds line breaks every 76 characters. But this breaks the verification.
        // generation can be omitted true XmlUtils but this is JVM specific.
        return result.replaceAll("\n", "");
    }

    private byte[] createHash(char[] password, byte[] salt, String algorithm, int iterations, int keySizeBytes) {
        // We don't use the Pbkdf2PasswordHashImpl code itself as we absolutely want to be sure that we are testing independently.

        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(
                    new PBEKeySpec(password, salt, iterations, keySizeBytes * 8)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

}