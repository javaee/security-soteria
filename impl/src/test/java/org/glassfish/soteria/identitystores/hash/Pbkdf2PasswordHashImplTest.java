/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.soteria.identitystores.hash;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

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
       return algo + ":" + iterations + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(encodedHash);
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
