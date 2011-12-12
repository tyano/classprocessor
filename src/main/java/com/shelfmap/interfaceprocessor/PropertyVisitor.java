/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.interfaceprocessor;

import com.shelfmap.interfaceprocessor.impl.DefaultProperty;
import static com.shelfmap.interfaceprocessor.util.Strings.*;

import com.sun.tools.internal.xjc.reader.TypeUtil;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;


import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 *
 * @author Tsutomu YANO
 */
public class PropertyVisitor extends ElementScanner6<Void, Environment> {

    /**
     * {@inheritDoc }
     *
     * we here output the line of class-definition of this interface.
     */
    @Override
    public Void visitType(TypeElement element, Environment env) {
        if(element.getKind() != ElementKind.INTERFACE) return super.visitType(element, env);

        //if the element have some super-interfaces, visit them at first.
        for (TypeMirror superType : element.getInterfaces()) {
            Element superInterface = env.getProcessingEnvironment().getTypeUtils().asElement(superType);
            env.setLevel(env.getLevel() + 1);
            if(!isPropertyEventAware(superType, env)) {
                this.visit(superInterface, env);
            }
            env.setLevel(env.getLevel() - 1);
        }

        InterfaceDefinition definition = env.getInterfaceDefinition();

        if(env.getLevel() == 0) {
            String[] splited = splitPackageName(element.getQualifiedName().toString());
            if(splited == null) {
                throw new IllegalStateException("the qualified name of the element " + element.toString() + " was a null or an empty string.");
            }
            definition.setPackage(splited[0]);
            definition.setInterfaceName(splited[1]);
            definition.addTypeParameters(element.getTypeParameters().toArray(new TypeParameterElement[0]));
        }
        return super.visitType(element, env);
    }

    private boolean isPropertyEventAware(TypeMirror type, Environment env) {
        ProcessingEnvironment p = env.getProcessingEnvironment();
        Types typeUtils = p.getTypeUtils();
        Elements elementUtils = p.getElementUtils();

        TypeMirror propertySupportType = elementUtils.getTypeElement(PropertyChangeEventAware.class.getName()).asType();
        return typeUtils.isSubtype(type, propertySupportType);
    }

    private String[] splitPackageName(String value) {
        if(value == null) return null;
        if(value.isEmpty()) return null;
        int lastIndexOfDot = value.lastIndexOf('.');
        if(lastIndexOfDot < 0) return new String[]{"", value};
        return new String[]{ value.substring(0, lastIndexOfDot), value.substring(lastIndexOfDot + 1, value.length()) };
    }

    @Override
    public Void visitExecutable(ExecutableElement ee, Environment env) {
        //handle only methods.
        if(ee.getKind() != ElementKind.METHOD) return super.visitExecutable(ee, env);

        //this visitor handle methods only in interface.
        Element enclosing = ee.getEnclosingElement();
        if(enclosing == null || enclosing.getKind() != ElementKind.INTERFACE) return super.visitExecutable(ee, env);

        InterfaceDefinition definition = env.getInterfaceDefinition();

        Property property = buildPropertyFromExecutableElement(ee, env.getProcessingEnvironment());
        Types typeUtils = env.getProcessingEnvironment().getTypeUtils();

        //if the building of property object is succeed, the ee is a variation of a property (readable or writable)
        if(property != null) {
            Property prev = definition.findProperty(property.getName(), property.getType(), typeUtils);

            //if a property having same name and same type is already added to InterfaceDifinition,
            //we merge their readable and writable attribute into the previously added object.
            if(prev != null) {
                mergeProperty(prev, property);
            } else {
                //new property. simplly add it into InterfaceDefinition.
                definition.addProperties(property);
            }
        } else {
            //found a method which is not a part of a property.
            definition.addMethods(ee);
        }
        return super.visitExecutable(ee, env);
    }

    private void mergeProperty(Property p1, Property p2) {
        if(p2.isReadable()) {
            p1.setReadable(true);
        }

        if(p2.isWritable()) {
            p1.setWritable(true);
        }
    }

    private Property buildPropertyFromExecutableElement(ExecutableElement ee, ProcessingEnvironment p) {
        String name = ee.getSimpleName().toString();

        Types types = p.getTypeUtils();
        Elements elements = p.getElementUtils();

        Property property = null;
        if(name.startsWith("get") || name.startsWith("set") || name.startsWith("is")) {
            if(name.startsWith("get")) {
                property = new DefaultProperty(uncapitalize(name.substring(3)), ee.getReturnType(), true, false);
            } else if(name.startsWith("set")) {
                if(ee.getParameters().size() == 1) {
                    property = new DefaultProperty(uncapitalize(name.substring(3)), ee.getParameters().get(0).asType(), false, true);
                }
            } else if(name.startsWith("is")) {
                PrimitiveType bool = types.getPrimitiveType(TypeKind.BOOLEAN);
                if(types.isSameType(ee.getReturnType(), bool) ||
                   types.isSameType(ee.getReturnType(), types.boxedClass(bool).asType())) {

                    property =  new DefaultProperty(uncapitalize(name.substring(2)), ee.getReturnType(), true, false);
                }
            }

            if(property != null) {
                //if a method have a @Property annotation,
                //then we record the value of the @Property annotation into a Property object.
                List<? extends AnnotationMirror> annotations = ee.getAnnotationMirrors();
                for (AnnotationMirror annotation : annotations) {

                    //is the annotationMirror same with "com.shelfmap.simplequery.annotation.Property" ?
                    TypeElement propertyAnnotationType = elements.getTypeElement(com.shelfmap.interfaceprocessor.annotation.Property.class.getName());
                    if(types.isSameType(propertyAnnotationType.asType(), annotation.getAnnotationType())) {

                        //check all values through the found annotation
                        //and record the values into a Property instance.
                        //we can not use our loving useful Class<?> object here, because this program run in compile-time (no classloader for loading them!).
                        //so we must record the value as TypeMirror or a simple String object.
                        Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap = elements.getElementValuesWithDefaults(annotation);
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : valueMap.entrySet()) {
                            ExecutableElement key = entry.getKey();
                            AnnotationValue value = entry.getValue();
                            if(key.getSimpleName().toString().equals("retainType")) {
                                //the return value of the method 'retainType' is an enum value RetainType.
                                //but it is manageable as only a VariableElement, and we can get the name of the enum object from the VariableElement.
                                //we can resolve the correct enum-value from the String value through the RetainType.valueOf(String) method.
                                VariableElement type = (VariableElement) value.getValue();
                                property.setRetainType(type.getSimpleName().toString());
                            } else if(key.getSimpleName().toString().equals("realType")) {
                                //the return value of the method 'realType' is a Class<?> object.
                                //but in annotation-processing time, all Class<?> is expressed as TypeMirror.
                                //TypeMirror object contains all information about the Class<?> in source-code level,
                                //so we can retrieve full-class-name from it when it is needed.
                                TypeMirror type = (TypeMirror) value.getValue();
                                property.setRealType(type);
                            } else if(key.getSimpleName().toString().equals("ignore")) {
                                Boolean ignore = (Boolean) value.getValue();
                                property.setIgnored(ignore.booleanValue());
                            }
                        }
                    }
                }
            }
        }
        return property;
    }


}
