package org.glassfish.soteria.authorization;

import static org.glassfish.soteria.Utils.getELProcessor;

import javax.ejb.EJBContext;
import javax.naming.InitialContext;
import javax.naming.NamingException; 

public final class EJB {
    
    private EJB() {
        // no instances
    }
    
    public static EJBContext getEJBContext() {
        try {
            return (EJBContext) new InitialContext().lookup("java:comp/EJBContext");
        } catch (NamingException ex) {
            return null;
        }
    }
    
    public static String getCurrentEJBName(EJBContext ejbContext) {
        try {
            switch (ejbContext.getClass().getName()) {
                case "com.sun.ejb.containers.SessionContextImpl":
                case "com.sun.ejb.containers.SingletonContextImpl":
                    String toString = ejbContext.toString();
                    int firstIndex = toString.indexOf(";");
                    if (firstIndex != -1) {
                        return toString.substring(0, firstIndex);
                    }
                    break;
                case "org.jboss.as.ejb3.context.SessionContextImpl":
                    return getELProcessor("ejbContext", ejbContext)
                            .eval("ejbContext.component.componentName")
                            .toString();
            }
        } catch (Exception e) {
            // Ignore
        }
                
        return null;
    }

}
