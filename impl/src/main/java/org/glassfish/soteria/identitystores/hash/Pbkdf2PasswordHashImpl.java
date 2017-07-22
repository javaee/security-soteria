/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
public class Pbkdf2PasswordHashImpl implements Pbkdf2PasswordHash {

    private static final Set<String> SUPPORTED_ALGORITHMS = getUnmodifiableSetFromStringArray(new String[] {
            "PBKDF2WithHmacSHA1",
            "PBKDF2WithHmacSHA224",
            "PBKDF2WithHmacSHA256",
            "PBKDF2WithHmacSHA384",
            "PBKDF2WithHmacSHA512"
    });
    
    private static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 2048;
    private static final int DEFAULT_SALT_SIZE = 32;       // 32-byte/256-bit salt
    private static final int DEFAULT_KEY_SIZE = 32;        // 32-byte/256-bit key/hash

    private String defaultAlgorithm = DEFAULT_ALGORITHM;   // PBKDF2 algorithm to use
    private int defaultIterations = DEFAULT_ITERATIONS;    // number of iterations
    private int defaultSaltSizeBytes = DEFAULT_SALT_SIZE;  // salt size in bytes
    private int defaultKeySizeBytes = DEFAULT_KEY_SIZE;    // derived key (i.e., password hash) size in bytes
    
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateHash(char[] password) {
        byte[] salt = getRandomSalt(new byte[defaultSaltSizeBytes]);
        byte[] hash = pbkdf2(password, salt, defaultAlgorithm, defaultIterations, defaultKeySizeBytes);
        return new EncodedPasswordHash(hash, salt, defaultAlgorithm, defaultIterations).getEncoded();
    }

    @Override
    public boolean verifyHash(char[] password, String hashedPassword) {
        EncodedPasswordHash encodedPasswordHash = new EncodedPasswordHash(hashedPassword);
        byte[] hashToVerify = pbkdf2(
                password,
                encodedPasswordHash.getSalt(),
                encodedPasswordHash.getAlgorithm(),
                encodedPasswordHash.getIterations(),
                encodedPasswordHash.getHash().length);
        return PasswordHashCompare.compareBytes(hashToVerify, encodedPasswordHash.getHash());
    }

    private byte[] pbkdf2(char[] password, byte[] salt, String algorithm, int iterations, int keySizeBytes) {
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(
                    new PBEKeySpec(password, salt, iterations, keySizeBytes * 8)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    private synchronized byte[] getRandomSalt(byte[] salt) {
        random.nextBytes(salt);
        return salt;
    }

    private static Set<String> getUnmodifiableSetFromStringArray(String[] strings) {
        HashSet<String> set = new HashSet<String>();
        for (String s : strings) {
            set.add(s);
        }
        return Collections.unmodifiableSet(set);
    }

    private static class EncodedPasswordHash {

        private String algorithm;
        private int iterations;
        private byte[] salt;
        private byte[] hash;
        private String encoded;

        private EncodedPasswordHash() {};

        EncodedPasswordHash(byte[] hash, byte[] salt, String algorithm, int iterations) {
            this.algorithm = algorithm;
            this.iterations = iterations;
            this.salt = salt;
            this.hash = hash;
            encode();
        }

        EncodedPasswordHash(String encoded) {
            this.encoded = encoded;
            decode();
        }

        String getAlgorithm() { return algorithm; }
        int getIterations() { return iterations; }
        byte[] getSalt() { return salt; }
        byte[] getHash() { return hash; }
        String getEncoded() { return encoded; }

        private void encode() {
            StringBuilder builder = new StringBuilder();
            builder.append(algorithm + ":" + iterations + ":");
            builder.append(Base64.getEncoder().encodeToString(salt));
            builder.append(":");
            builder.append(Base64.getEncoder().encodeToString(hash));
            encoded = builder.toString();
        }

        private void decode() {
            String[] tokens = encoded.split(":");
            if (tokens.length != 4) {
                throw new IllegalArgumentException("Bad hash encoding");
            }
            if (!SUPPORTED_ALGORITHMS.contains(tokens[0])) {
                throw new IllegalArgumentException("Bad hash encoding");
            }
            algorithm = tokens[0];
            iterations = Integer.parseInt(tokens[1]);
            salt = Base64.getDecoder().decode(tokens[2]);
            hash = Base64.getDecoder().decode(tokens[3]);
        }
    }

}
