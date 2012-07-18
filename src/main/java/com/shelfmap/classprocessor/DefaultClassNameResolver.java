package com.shelfmap.classprocessor;

import static com.shelfmap.classprocessor.util.Strings.capitalize;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultClassNameResolver implements ClassNameResolver {

    @Override
    public String classNameFor(String className) {
        return getClassNamePrefix() + capitalize(className) + getClassNameSuffix();
    }

    @Override
    public String abstractClassNameFor(String className) {
        return getAbstractClassNamePrefix() + capitalize(className) + getAbstractClassNameSuffix();
    }

    protected String getClassNamePrefix() { return ""; }
    protected String getClassNameSuffix() { return "Impl"; }
    protected String getAbstractClassNamePrefix() { return "Abstract"; }
    protected String getAbstractClassNameSuffix() { return ""; }
}
