/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.processor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Provider;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.function.Supplier;

class GenerateProviderBean {
    private final String packageName;
    private final String interceptorClass;
    private final String pluginClass;
    private final String point;
    private final String qualifier;
    private final String providerClassExtension;
    private final TypeElement interceptor;
    private final BeanUtils utils;

    GenerateProviderBean(TypeElement plugin, TypeElement interceptor,
                         Map<String, String> to,
                         BeanUtils utils) {
        this.interceptor = interceptor;
        this.packageName = utils.packageNameOf(interceptor);
        this.pluginClass = utils.classNameOf(plugin).canonicalName();
        this.point = to.get("value");
        this.qualifier = to.get("qualifier") == null ? "default" : to.get("qualifier");
        this.providerClassExtension = "$Provider" + to.get("seq");
        this.interceptorClass = utils.classNameOf(interceptor).simpleName();
        this.utils = utils;
    }

    public String getProviderClass() {
        return utils.classNameOf(interceptor).canonicalName()  + this.providerClassExtension;
    }

    JavaFile apply() {
        List<? extends Element> executes = utils.asTypeElement(() -> Provider.class).getEnclosedElements();

        Set<MethodSpec> methods = new LinkedHashSet<>();
        for (Element e : executes) {
            ExecutableElement pExecute = (ExecutableElement)e;
            if (pExecute.toString().startsWith("getInterceptorProvider")) {
                // getInterceptorProvider method generate
                ParameterizedTypeName returnType = ParameterizedTypeName.get(Supplier.class, Interceptor.class);
                // final MethodSpec getInterceptorProvider = MethodSpec.methodBuilder("getInterceptorProvider")
                final MethodSpec getInterceptorProvider = MethodSpec.overriding(pExecute)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnType)
                    .addCode("return " + this.interceptorClass + "::new;")
                    .build();
                methods.add(getInterceptorProvider);
            } else if (pExecute.toString().startsWith("getAdviceTo")) {
                // getAdviceTo method generate
                final MethodSpec getAdviceTo = MethodSpec.overriding(pExecute)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return \"$L\" + \"$L\" + \"$L\"", this.point, ":", this.qualifier)
                    .build();
                methods.add(getAdviceTo);
            } else if (pExecute.toString().startsWith("getPluginClassName")) {
                // getPluginClassName method generate
                final MethodSpec getPluginClassName = MethodSpec.overriding(pExecute)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return \"$L\"", this.pluginClass)
                    .build();
                methods.add(getPluginClassName);
            }
        }

        final TypeSpec.Builder specBuild = TypeSpec
            .classBuilder(this.interceptorClass + this.providerClassExtension)
            .addSuperinterface(Provider.class)
            .addModifiers(Modifier.PUBLIC);
        for (MethodSpec method : methods) {
            specBuild.addMethod(method);
        }

        final TypeSpec spec = specBuild.build();

        return JavaFile.builder(packageName, spec).build();
    }
}
