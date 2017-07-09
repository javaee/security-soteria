package org.glassfish.soteria.cdi;

import static java.util.Arrays.stream;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.cdi.CdiUtils.getELProcessor;

import javax.el.ELProcessor;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

public class AnnotationELPProcessor {
    
    public static LoginToContinue process(LoginToContinue in) {
        if (!hasAnyELExpression(in)) {
            return in;
        }
        
        return 
            new LoginToContinueAnnotationLiteral(
                    evalImmediate(in.loginPage()), 
                    in.useForwardToLogin(), 
                    evalImmediate(in.errorPage()));
    }
    
    public static boolean hasAnyELExpression(LoginToContinue in) {
        return hasAnyELExpression(in.loginPage(), in.errorPage());
    }
    
    public static String evalImmediate(String expression) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor().eval(toRawExpression(expression));
    }
    
    public static String evalELExpression(String expression) {
        if (!isELExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor().eval(toRawExpression(expression));
    }
    
    public static String evalELExpression(ELProcessor elProcessor, String expression) {
        if (elProcessor == null || !isELExpression(expression)) {
            return expression;
        }
        
        return (String) elProcessor.eval(toRawExpression(expression));
    }
    
    @SafeVarargs
    public static boolean hasAnyELExpression(String... expressions) {
        return stream(expressions).anyMatch(expr -> isELExpression(expr));
    }
    
    private static boolean isELExpression(String expression) {
        return !isEmpty(expression) && (isDeferredExpression(expression) || isImmediateExpression(expression)) && expression.endsWith("}");
    }
    
    private static boolean isDeferredExpression(String expression) {
        return expression.startsWith("#{");
    }
    
    private static boolean isImmediateExpression(String expression) {
        return expression.startsWith("${");
    }
    
    private static String toRawExpression(String expression) {
        return expression.substring(2, expression.length() -1);
    }
}
