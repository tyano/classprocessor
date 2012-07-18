package com.shelfmap.classprocessor;

/**
 * An implementation of ClassNameResolver, which remove a prefix 'I' from the name of a interface and the
 * uses the remaining name as the name of a Class.
 * @author Tsutomu YANO
 */
public class RemovePrefixClassNameResolver extends DefaultClassNameResolver implements ClassNameResolver {

    @Override
    public String classNameFor(String className) {
        parameterCheck(className);
        String remains = className.substring(1);
        return super.classNameFor(remains);
    }

    @Override
    public String abstractClassNameFor(String className) {
        parameterCheck(className);
        String remains = className.substring(1);
        return super.abstractClassNameFor(remains);
    }

    private void parameterCheck(String className) {
        if(className == null) throw new IllegalArgumentException("'className' should not be null.");
        if(className.isEmpty()) throw new IllegalArgumentException("'className' should not be an empty string.");
        if(className.length() < 2) throw new IllegalArgumentException("the number of chars of 'className' should be longer than 2, and it should begin with 'I'.");
        if(!className.startsWith("I") && !className.startsWith("T")) {
            throw new IllegalArgumentException("The first charactor of 'className' should be 'I' or 'T'.");
        }    
    }

    @Override
    protected String getClassNamePrefix() { return ""; }

    @Override
    protected String getClassNameSuffix() { return ""; }

    @Override
    protected String getAbstractClassNamePrefix() { return "Abstract"; }

    @Override
    protected String getAbstractClassNameSuffix() { return ""; }
}
