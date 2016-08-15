/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package javax.security.identitystore;

import javax.security.CallerPrincipal;
import javax.security.identitystore.credential.Credential;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static javax.security.identitystore.CredentialValidationResult.Status.*;

/**
 * <code>CredentialValidationResult</code> is the result from an attempt to
 * validate an instance of {@link Credential}.
 *
 * @see IdentityStore#validate
 * @see IdentityStoreHandler#validate
 *
 * @author Arjan Tijms
 * @author Rudy De Busscher
 */
public class CredentialValidationResult {

    public static final CredentialValidationResult NONE_RESULT = new CredentialValidationResult(null, NONE, null, null);
    public static final CredentialValidationResult INVALID_RESULT = new CredentialValidationResult(NONE_RESULT, INVALID, null, null);
    public static final CredentialValidationResult NOT_VALIDATED_RESULT = new CredentialValidationResult(NONE_RESULT, NOT_VALIDATED, null, null);

    private final CallerPrincipal callerPrincipal;
    private final Status status;
    private final List<String> groups;

    public enum Status {
        /**
         * The default status, indicating that no IdentityStore has been consulted yet.
         */
        NONE,

        /**
         * Indicates that the credential is validated but that no Groups are assigned yet.
         */
        AUTHENTICATED,

        /**
         * Indicates that the credential could not be validated.
         */
        NOT_VALIDATED,

        /**
         * Indicates that the credential is not valid after a validation.
         * attempt.
         */
        INVALID,

        /**
         * Indicates that the credential is valid after a validation attempt and Groups are assigned.
         */
        VALID
    }

    /**
     * Constructor for a VALID result.
     *
     * @param partialValidationResult The current <code>CredentialValidationResult</code>
     * @param callerName              Name of the validated caller
     * @param groups                  Groups associated with the caller from the identity store
     */
    public CredentialValidationResult(CredentialValidationResult partialValidationResult, String callerName, List<String> groups) {
        this(partialValidationResult, new CallerPrincipal(callerName), groups);
    }

    /**
     * Constructor for a AUTHENTICATED result, groups are set to an empty List.
     *
     * @param partialValidationResult The current <code>CredentialValidationResult</code>
     * @param callerName              Name of the validated caller
     */
    public CredentialValidationResult(CredentialValidationResult partialValidationResult, String callerName) {
        this(partialValidationResult, AUTHENTICATED, new CallerPrincipal(callerName), new ArrayList<>());
    }

    /**
     * Constructor for a VALID result, the partialValidationResult parameter should already have the Caller information.
     *
     * @param partialValidationResult The already available CredentialValidationResult.
     * @param groups                  Groups associated with the caller from the identity store
     */
    public CredentialValidationResult(CredentialValidationResult partialValidationResult, List<String> groups) {
        this(partialValidationResult, partialValidationResult.getCallerPrincipal(), groups);
        if (partialValidationResult.getCallerPrincipal() == null) {
            throw new IllegalArgumentException("CallerPrincipal of partialValidationResult can't be null");
        }
    }

    /**
     * Constructor for a VALID result
     *
     * @param partialValidationResult The already available CredentialValidationResult.
     * @param callerPrincipal         Validated caller
     * @param groups                  Groups associated with the caller from the identity store
     */
    public CredentialValidationResult(CredentialValidationResult partialValidationResult, CallerPrincipal callerPrincipal, List<String> groups) {
        this(partialValidationResult, VALID, callerPrincipal, groups);
    }

    private List<String> determineGroups(Status status, List<String> originalGroups, List<String> additionalGroups) {
        List<String> temp = new ArrayList<>();
        if (VALID == status) {
            if (null != originalGroups) {
                temp.addAll(originalGroups);
            }
            if (null != additionalGroups) {
                temp.addAll(additionalGroups);
            }
        } else {
            temp = additionalGroups;
        }
        return unmodifiableList(temp);

    }

    /**
     * Constructor using a partial Validation result to add for example authorization info.
     *
     * @param partialValidationResult The already available CredentialValidationResult.
     * @param status                  Validation status
     * @param groups                  Groups associated with the caller from the identity store
     */
    public CredentialValidationResult(CredentialValidationResult partialValidationResult, Status status, CallerPrincipal callerPrincipal, List<String> groups) {
        if (status == Status.AUTHENTICATED || status == Status.VALID) {
            this.groups = determineGroups(status, partialValidationResult.getCallerGroups(), groups);
            this.callerPrincipal = callerPrincipal;
        } else {
            if (partialValidationResult != null) {
                // Keep the partial data
                this.groups = partialValidationResult.getCallerGroups();
                this.callerPrincipal = partialValidationResult.getCallerPrincipal(); // Keep the callerPrincipal
            } else {
                if (Status.NONE != status) {
                    throw new IllegalArgumentException("partialValidationResult required when Status is not NONE");
                } else {
                    this.callerPrincipal = null;
                    this.groups = null;
                }
            }
        }
        if (partialValidationResult != null && Status.VALID == partialValidationResult.getStatus() && Status.AUTHENTICATED == status) {
            this.status = partialValidationResult.getStatus();
        } else {
            this.status = status;  // Keep the new status

        }
    }

    /**
     * Determines the validation status.
     *
     * @return The validation status
     */
    public Status getStatus() {
        return status;
    }

    public CallerPrincipal getCallerPrincipal() {
        return callerPrincipal;
    }

    /**
     * Determines the list of groups that the specified Caller is in, based on
     * the associated persistence store..
     *
     * @return The list of groups that the specified Caller is in, empty if
     * none. <code>null</code> if {@link #getStatus} does not return
     * {@link Status#VALID VALID} or if the identity store does not
     * support groups.
     */
    public List<String> getCallerGroups() {
        return groups;
    }

}
