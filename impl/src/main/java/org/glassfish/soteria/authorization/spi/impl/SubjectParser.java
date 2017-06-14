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
package org.glassfish.soteria.authorization.spi.impl;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
 
public class SubjectParser {
     
    private static Object geronimoPolicyConfigurationFactoryInstance;
    private static ConcurrentMap<String, Map<Principal, Set<String>>> geronimoContextToRoleMapping;
     
    private Map<String, List<String>> groupToRoles = new HashMap<>();
 
    private boolean isJboss;
    private boolean isLiberty;
    private boolean oneToOneMapping;
    private boolean anyAuthenticatedUserRoleMapped = false;
     
    public static void onFactoryCreated() {
        tryInitGeronimo();
    }
     
    private static void tryInitGeronimo() {
        try {
            // Geronimo 3.0.1 contains a protection mechanism to ensure only a Geronimo policy provider is installed.
            // This protection can be beat by creating an instance of GeronimoPolicyConfigurationFactory once. This instance
            // will statically register itself with an internal Geronimo class
            geronimoPolicyConfigurationFactoryInstance = Class.forName("org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory").newInstance();
            geronimoContextToRoleMapping = new ConcurrentHashMap<>();
        } catch (Exception e) {
            // ignore
        }
    }
     
    public static void onPolicyConfigurationCreated(final String contextID) {
         
        // Are we dealing with Geronimo?
        if (geronimoPolicyConfigurationFactoryInstance != null) {
             
            // PrincipalRoleConfiguration
             
            try {
                Class<?> geronimoPolicyConfigurationClass = Class.forName("org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfiguration");
                 
                Object geronimoPolicyConfigurationProxy = Proxy.newProxyInstance(SubjectParser.class.getClassLoader(), new Class[] {geronimoPolicyConfigurationClass}, new InvocationHandler() {
                     
                    @SuppressWarnings("unchecked")
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 
                        // Take special action on the following method:
                         
                        // void setPrincipalRoleMapping(Map<Principal, Set<String>> principalRoleMap) throws PolicyContextException;
                        if (method.getName().equals("setPrincipalRoleMapping")) {
                             
                            geronimoContextToRoleMapping.put(contextID, (Map<Principal, Set<String>>) args[0]);
                             
                        }
                        return null;
                    }
                });
                 
                // Set the proxy on the GeronimoPolicyConfigurationFactory so it will call us back later with the role mapping via the following method:
                 
                // public void setPolicyConfiguration(String contextID, GeronimoPolicyConfiguration configuration) {
                Class.forName("org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory")
                     .getMethod("setPolicyConfiguration", String.class, geronimoPolicyConfigurationClass)
                     .invoke(geronimoPolicyConfigurationFactoryInstance, contextID, geronimoPolicyConfigurationProxy);
                 
                 
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // Ignore
            }
        }
    }
 
    public SubjectParser(String contextID, Collection<String> allDeclaredRoles) {
        // Initialize the groupToRoles map
 
        // Try to get a hold of the proprietary role mapper of each known
        // AS. Sad that this is needed :(
        if (tryGlassFish(contextID, allDeclaredRoles)) {
            return;
        } else if (tryJBoss()) {
            return;
        } else if (tryLiberty()) {
            return;
        } else if (tryWebLogic(contextID, allDeclaredRoles)) {
            return;
        } else if (tryGeronimo(contextID, allDeclaredRoles)) {
            return;
        } else {
            oneToOneMapping = true;
        }
    }
 
    public List<String> getMappedRolesFromPrincipals(Principal[] principals) {
        return getMappedRolesFromPrincipals(asList(principals));
    }
 
    public boolean isAnyAuthenticatedUserRoleMapped() {
        return anyAuthenticatedUserRoleMapped;
    }
    
    public Principal getCallerPrincipalFromPrincipals(Iterable<Principal> principals) {
        
        if (isJboss) {
            try {
                
                // The JACCAuthorizationManager that normally would call us in JBoss only passes
                // either the role principals or the caller principal in, never both, and without any
                // easy way to distinguish between them.
                
                // So we're getting the principals from the Subject here. Do note that we miss the
                // potential extra deployment roles here which may be in the principals collection we get
                // passed in.
                Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
                
                if (subject == null) {
                    return null;
                }
                
                return doGetCallerPrincipalFromPrincipals(subject.getPrincipals());
            } catch (PolicyContextException e1) {
                // Ignore
            }
            
            return null;
        }
       
        return doGetCallerPrincipalFromPrincipals(principals);
    }
 
    @SuppressWarnings("unchecked")
    public List<String> getMappedRolesFromPrincipals(Iterable<Principal> principals) {
        
        List<String> groups = null;
        
        if (isLiberty || isJboss) { 
           
            try {
                Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
                if (subject == null) {
                    return emptyList();
                }
                
                if (isLiberty) {
                    // Liberty is the only known Java EE server that doesn't put the groups in
                    // the principals collection, but puts them in the credentials of a Subject.
                    // This somewhat peculiar decision means a JACC provider never gets to see
                    // groups via the principals that are passed in and must get them from
                    // the current Subject.
                    
                    @SuppressWarnings("rawtypes")
                    Set<Hashtable> tables = subject.getPrivateCredentials(Hashtable.class);
                    if (tables != null && !tables.isEmpty()) {
                        @SuppressWarnings("rawtypes")
                        Hashtable table = tables.iterator().next();
                        groups = (List<String>) table.get("com.ibm.wsspi.security.cred.groups");
                    }
                } else {
                    // The JACCAuthorizationManager that normally would call us in JBoss only passes
                    // either the role principals or the caller principal in, never both, and without any
                    // easy way to distinguish between them.
                    
                    // So we're getting the principals from the Subject here. Do note that we miss the
                    // potential extra deployment roles here which may be in the principals collection we get
                    // passed in.
                    groups = getGroupsFromPrincipals(subject.getPrincipals());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
 
            // Extract the list of groups from the principals. These principals typically contain
            // different kind of principals, some groups, some others. The groups are unfortunately vendor
            // specific.
            groups = getGroupsFromPrincipals(principals);
        }
 
        // Map the groups to roles. E.g. map "admin" to "administrator". Some servers require this.
        return mapGroupsToRoles(groups);
    }
 
    private List<String> mapGroupsToRoles(List<String> groups) {
 
        if (oneToOneMapping) {
            // There is no mapping used, groups directly represent roles.
            return groups;
        }
 
        List<String> roles = new ArrayList<>();
 
        for (String group : groups) {
            if (groupToRoles.containsKey(group)) {
                roles.addAll(groupToRoles.get(group));
            }
        }
 
        return roles;
    }
    
    private boolean tryJBoss() {
        try {
            Class.forName("org.jboss.as.security.service.JaccService", false, Thread.currentThread().getContextClassLoader());
            
            // For not only establish that we're running on JBoss, ignore the
            // role mapper for now
            isJboss = true;
            oneToOneMapping = true;
            
            return true;
        } catch (Exception e) {
            // ignore
        }
        
        return false;
    }
    
    private boolean tryLiberty() {
        isLiberty = (getProperty("wlp.server.name") != null);
     
        // Liberty as only server disables its otherwise mandatory role mapping
        // when portable authentication is used. All other servers have this
        // decoupled - groups from portable authentication modules can be role
        // mapped by the proprietary role mapper. For now we thus assume 1:1 
        // role mapping for Liberty.
        oneToOneMapping = true; 
        return isLiberty;
    }
 
    private boolean tryGlassFish(String contextID, Collection<String> allDeclaredRoles) {
 
        try {
            Class<?> SecurityRoleMapperFactoryClass = Class.forName("org.glassfish.deployment.common.SecurityRoleMapperFactory");
 
            Object factoryInstance = Class.forName("org.glassfish.internal.api.Globals")
                                          .getMethod("get", SecurityRoleMapperFactoryClass.getClass())
                                          .invoke(null, SecurityRoleMapperFactoryClass);
 
            Object securityRoleMapperInstance = SecurityRoleMapperFactoryClass.getMethod("getRoleMapper", String.class)
                                                                              .invoke(factoryInstance, contextID);
 
            @SuppressWarnings("unchecked")
            Map<String, Subject> roleToSubjectMap = (Map<String, Subject>) Class.forName("org.glassfish.deployment.common.SecurityRoleMapper")
                                                                                .getMethod("getRoleToSubjectMapping")
                                                                                .invoke(securityRoleMapperInstance);
 
            for (String role : allDeclaredRoles) {
                if (roleToSubjectMap.containsKey(role)) {
                    Set<Principal> principals = roleToSubjectMap.get(role).getPrincipals();
 
                    List<String> groups = getGroupsFromPrincipals(principals);
                    for (String group : groups) {
                        if (!groupToRoles.containsKey(group)) {
                            groupToRoles.put(group, new ArrayList<>());
                        }
                        groupToRoles.get(group).add(role);
                    }
 
                    if ("**".equals(role) && !groups.isEmpty()) {
                        // JACC spec 3.2 states:
                        //
                        // "For the any "authenticated user role", "**", and unless an application specific mapping has
                        // been established for this role,
                        // the provider must ensure that all permissions added to the role are granted to any
                        // authenticated user."
                        //
                        // Here we check for the "unless" part mentioned above. If we're dealing with the "**" role here
                        // and groups is not
                        // empty, then there's an application specific mapping and "**" maps only to those groups, not
                        // to any authenticated user.
                        anyAuthenticatedUserRoleMapped = true;
                    }
                }
            }
 
            return true;
 
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            return false;
        }
    }
 
    private boolean tryWebLogic(String contextID, Collection<String> allDeclaredRoles) {
 
        try {
 
            // See http://docs.oracle.com/cd/E21764_01/apirefs.1111/e13941/weblogic/security/jacc/RoleMapperFactory.html
            Class<?> roleMapperFactoryClass = Class.forName("weblogic.security.jacc.RoleMapperFactory");
 
            // RoleMapperFactory implementation class always seems to be the value of what is passed on the commandline
            // via the -Dweblogic.security.jacc.RoleMapperFactory.provider option.
            // See http://docs.oracle.com/cd/E57014_01/wls/SCPRG/server_prot.htm
            Object roleMapperFactoryInstance = roleMapperFactoryClass.getMethod("getRoleMapperFactory")
                                                                     .invoke(null);
 
            // See http://docs.oracle.com/cd/E21764_01/apirefs.1111/e13941/weblogic/security/jacc/RoleMapperFactory.html#getRoleMapperForContextID(java.lang.String)
            Object roleMapperInstance = roleMapperFactoryClass.getMethod("getRoleMapperForContextID", String.class)
                                                              .invoke(roleMapperFactoryInstance, contextID);
 
            // This seems really awkward; the Map contains BOTH group names and user names, without ANY way to
            // distinguish between the two.
            // If a user now has a name that happens to be a role as well, we have an issue :X
            @SuppressWarnings("unchecked")
            Map<String, String[]> roleToPrincipalNamesMap = (Map<String, String[]>) Class.forName("weblogic.security.jacc.simpleprovider.RoleMapperImpl")
                                                                                         .getMethod("getRolesToPrincipalNames")
                                                                                         .invoke(roleMapperInstance);
 
            for (String role : allDeclaredRoles) {
                if (roleToPrincipalNamesMap.containsKey(role)) {
 
                    List<String> groupsOrUserNames = asList(roleToPrincipalNamesMap.get(role));
 
                    for (String groupOrUserName : roleToPrincipalNamesMap.get(role)) {
                        // Ignore the fact that the collection also contains user names and hope
                        // that there are no user names in the application with the same name as a group
                        if (!groupToRoles.containsKey(groupOrUserName)) {
                            groupToRoles.put(groupOrUserName, new ArrayList<>());
                        }
                        groupToRoles.get(groupOrUserName).add(role);
                    }
 
                    if ("**".equals(role) && !groupsOrUserNames.isEmpty()) {
                        // JACC spec 3.2 states: [...]
                        anyAuthenticatedUserRoleMapped = true;
                    }
                }
            }
 
            return true;
 
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            return false;
        }
    }
     
    private boolean tryGeronimo(String contextID, Collection<String> allDeclaredRoles) {
        if (geronimoContextToRoleMapping != null) {
             
            if (geronimoContextToRoleMapping.containsKey(contextID)) {
                Map<Principal, Set<String>> principalsToRoles = geronimoContextToRoleMapping.get(contextID);
                 
                for (Map.Entry<Principal, Set<String>> entry : principalsToRoles.entrySet()) {
                     
                    // Convert the principal that's used as the key in the Map to a list of zero or more groups.
                    // (for Geronimo we know that using the default role mapper it's always zero or one group)
                    for (String group : principalToGroups(entry.getKey())) {
                        if (!groupToRoles.containsKey(group)) {
                            groupToRoles.put(group, new ArrayList<>());
                        }
                        groupToRoles.get(group).addAll(entry.getValue());
                         
                        if (entry.getValue().contains("**")) {
                            // JACC spec 3.2 states: [...]
                            anyAuthenticatedUserRoleMapped = true;
                        }
                    }
                }
            }
             
            return true;
        }
         
        return false;
    }
 
    /**
     * Extracts the roles from the vendor specific principals. SAD that this is needed :(
     * 
     * @param principals
     * @return
     */
    public List<String> getGroupsFromPrincipals(Iterable<Principal> principals) {
        List<String> groups = new ArrayList<>();
 
        for (Principal principal : principals) {
            if (principalToGroups(principal, groups)) {
                // return value of true means we're done early. This can be used
                // when we know there's only 1 principal holding all the groups
                return groups;
            }
        }
 
        return groups;
    }
     
    public List<String> principalToGroups(Principal principal) {
        List<String> groups = new ArrayList<>();
        principalToGroups(principal, groups);
        return groups;
    }
    
    private Principal doGetCallerPrincipalFromPrincipals(Iterable<Principal> principals) {
        
        for (Principal principal : principals) {
            switch (principal.getClass().getName()) {
                case "org.glassfish.security.common.PrincipalImpl": // GlassFish/Payara
                // JBoss EAP/WildFly convention 1 - single top level principal of the below type
                case "org.jboss.security.SimplePrincipal":
                case "com.ibm.ws.security.authentication.principals.WSPrincipal": // Liberty
                    return principal;
                // JBoss EAP/WildFly convention 2 - the one and only principal in group called CallerPrincipal
                case "org.jboss.security.SimpleGroup": 
                    if (principal.getName().equals("CallerPrincipal") && principal instanceof Group) {
                        
                        Enumeration<? extends Principal> groupMembers = ((Group) principal).members();
                        
                        if (groupMembers.hasMoreElements()) {
                            return groupMembers.nextElement();
                        }
                    }
                    break;
                case "org.apache.tomee.catalina.TomcatSecurityService$TomcatUser": // TomEE
                    try {
                        return 
                            (Principal) Class.forName("org.apache.catalina.realm.GenericPrincipal")
                                             .getMethod("getUserPrincipal")
                                             .invoke(
                                                 Class.forName("org.apache.tomee.catalina.TomcatSecurityService$TomcatUser")
                                                      .getMethod("getTomcatPrincipal")
                                                      .invoke(principal))
                            
                            ;
                        } catch (Exception e) {
                            
                        }
                        break;
                // TODO: depend on this directly later 
                case "javax.security.enterprise.CallerPrincipal":
                    return principal;
            }
        }
        
        return null;
    }
     
    public boolean principalToGroups(Principal principal, List<String> groups) {
        switch (principal.getClass().getName()) {
 
            case "org.glassfish.security.common.Group": // GlassFish / Payara
            case "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal": // Geronimo
            case "weblogic.security.principal.WLSGroupImpl": // WebLogic
            case "jeus.security.resource.GroupPrincipalImpl": // JEUS
                groups.add(principal.getName());
                break;
     
            case "org.jboss.security.SimpleGroup": // JBoss EAP/WildFly
                if (principal.getName().equals("Roles") && principal instanceof Group) {
                    Group rolesGroup = (Group) principal;
                    for (Principal groupPrincipal : list(rolesGroup.members())) {
                        groups.add(groupPrincipal.getName());
                    }
     
                    // Should only be one group holding the roles, so can exit the loop
                    // early
                    return true;
                }
            case "org.apache.tomee.catalina.TomcatSecurityService$TomcatUser": // TomEE
                try {
                    groups.addAll(
                        asList((String[])
                            Class.forName("org.apache.catalina.realm.GenericPrincipal")
                                 .getMethod("getRoles")
                                 .invoke(
                                     Class.forName("org.apache.tomee.catalina.TomcatSecurityService$TomcatUser")
                                          .getMethod("getTomcatPrincipal")
                                          .invoke(principal))));
                        

                    } catch (Exception e) {
                        
                    }
                    break;
            }
            
        return false;
    }
    
    
 
}