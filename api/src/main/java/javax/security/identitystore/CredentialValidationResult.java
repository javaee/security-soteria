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
package javax.security.identitystore;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static javax.security.identitystore.CredentialValidationResult.Status.INVALID;
import static javax.security.identitystore.CredentialValidationResult.Status.NOT_VALIDATED;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

import java.util.ArrayList;
import java.util.List;

import javax.security.CallerPrincipal;
import javax.security.identitystore.credential.Credential;

/**
 * <code>CredentialValidationResult</code> is the result from an attempt to
 * validate an instance of {@link Credential}.
 *
 * @see IdentityStore#validate
 */
public class CredentialValidationResult {

	public static final CredentialValidationResult INVALID_RESULT = new CredentialValidationResult(INVALID, null, null);
    public static final CredentialValidationResult NOT_VALIDATED_RESULT = new CredentialValidationResult(NOT_VALIDATED, null, null);

    private final CallerPrincipal callerPrincipal;
	private final Status status;
	private final List<String> groups;

	public enum Status {
		/**
		 * Indicates that the credential could not be validated
		 */
		NOT_VALIDATED,

		/**
		 * Indicates that the credential is not valid after a validation
		 * attempt.
		 */
		INVALID,

		/**
		 * Indicates that the credential is valid after a validation attempt.
		 */
		VALID
	};
	
	/**
     * Constructor for a VALID result
     * 
     * @param callerName Name of the validated caller
     */
    public CredentialValidationResult(String callerName) {
        this(new CallerPrincipal(callerName), null);
    }
    
    /**
     * Constructor for a VALID result
     * 
     * @param callerPrincipal Validated caller
     */
    public CredentialValidationResult(CallerPrincipal callerPrincipal) {
        this(callerPrincipal, null);
    }
	
    /**
     * Constructor for a VALID result
     * 
     * @param callerName
     *            Name of the validated caller
     * @param groups
     *            Groups associated with the caller from the identity store
     */
    public CredentialValidationResult(String callerName, List<String> groups) {
        this(new CallerPrincipal(callerName), groups);
    }
	
	/**
     * Constructor for a VALID result
     * 
     * @param callerPrincipal
     *            Validated caller
     * @param groups
     *            Groups associated with the caller from the identity store
     */
	public CredentialValidationResult(CallerPrincipal callerPrincipal, List<String> groups) {
	    this(VALID, callerPrincipal, groups);
	}
	
	/**
	 * Constructor
	 *
	 * @param status
	 *            Validation status
	 * @param callerPrincipal
	 *            Validated caller
	 * @param groups
	 *            Groups associated with the caller from the identity store
	 */
	public CredentialValidationResult(Status status, CallerPrincipal callerPrincipal, List<String> groups) {

		if (status == null) {
			throw new NullPointerException("status");
		}

		this.status = status;
		this.callerPrincipal = callerPrincipal;

		if (status == VALID) {
		    // TODO: to be discussed, should we use null or empty list?
			this.groups = groups != null? unmodifiableList(new ArrayList<>(groups)) : emptyList();
		} else {
			this.groups = null;
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
	 * the associated identity store.
	 *
	 * @return The list of groups that the specified Caller is in, empty if
	 *         none. <code>null</code> if {@link #getStatus} does not return
	 *         {@link Status#VALID VALID} 
	 */
	public List<String> getCallerGroups() {
		return groups;
	}

}
