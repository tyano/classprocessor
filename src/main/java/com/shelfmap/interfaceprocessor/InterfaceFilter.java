package com.shelfmap.interfaceprocessor;

import javax.lang.model.type.TypeMirror;

/**
 * A interface which InterfaceProcessor and PropertyVisitor uses for checking 
 * a type is able to handle by them.
 * <p>
 * If the {@code canHandle} method return false, InterfaceProcessor ignore the type
 * then it do not generate any method for the type.
 * 
 * @author t_yano
 */
public interface InterfaceFilter {
    boolean canHandle(TypeMirror type);
}
