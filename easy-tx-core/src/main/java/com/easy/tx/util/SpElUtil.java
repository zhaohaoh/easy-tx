package com.easy.tx.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;


public class SpElUtil {

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    public static Object parse(String spel, String[] paramNames, Object[] args) {
        if (!StringUtils.isEmpty(spel)) {
            Expression expression = PARSER.parseExpression(spel);
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return expression.getValue(context);
        }
        return null;
    }
    //@redisTestController.bbb
    public static Object parse(String spel, String[] paramNames, Object[] args, BeanFactory beanFactory) {
        if (!StringUtils.isEmpty(spel)) {
            Expression expression = PARSER.parseExpression(spel);
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            if (beanFactory != null) {
                context.setBeanResolver(new BeanFactoryResolver(beanFactory));
            }
            return expression.getValue(context);
        }
        return null;
    }
}
