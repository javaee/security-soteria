/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

public class PasswordHashCompare {

    /**
     * Compare two password hashes for equality. Do not fail fast;
     * continue comparing bytes even if a difference has been found,
     * to reduce the possibility that timing attacks can be used
     * to guess passwords.
     * <p>
     * The two hashes can be different lengths if the hash algorithm
     * or parameters used to generate them weren't the same.
     * <p>
     * Use the length of the first parameter (hash of the password being verified)
     * to determine how many bytes are compared, so that the comparison time
     * doesn't reflect the length of the second parameter (hash of the caller's
     * actual password).
     * <p>
     * Use XOR instead of == to compare characters, to avoid branching.
     * Branches can introduce timing differences depending on the branch
     * taken and the CPU's branch prediction state.
     * 
     * @param array1 Hash of the password to verify.
     * @param array2 Hash of the caller's actual password, for comparison.
     * @return True if the password hashes match, false otherwise.
     */
    public static boolean compareBytes(byte[] array1, byte[] array2) {
        int diff = array1.length ^ array2.length;
        for (int i = 0; i < array1.length; i++) {
            diff |= array1[i] ^ array2[i%array2.length];
        }
        return diff == 0;
    }

    /**
     * Compare two passwords, represented as character arrays.
     * <p>
     * Note that passwords should never be stored as plaintext,
     * but this method may be useful for, e.g., verifying a
     * password stored in encrypted form in a database, and
     * decrypted for comparison.
     * <p>
     * Behavior and theory operation are the same as for
     * {@link #compareBytes(byte[], byte[]) compareBytes},
     * except that the parameters are character arrays.
     * 
     * @param array1 The password to verify.
     * @param array2 The caller's actual password, for comparison.
     * @return True if the passwords match, false otherwise.
     */
    public static boolean compareChars(char[] array1, char[] array2) {
        int diff = array1.length ^ array2.length;
        for (int i = 0; i < array1.length; i++) {
            diff |= array1[i] ^ array2[i%array2.length];
        }
        return diff == 0;
    }
}
