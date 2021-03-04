/*
 * Copyright (c) 2017, MegaEase
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

 package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Configurable;
import com.squareup.javapoet.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.ElementKindVisitor6;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@RunWith(Parameterized.class)
public class GenerateConfigurationTest extends GenerateSpecTestBase {

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new NoConfiguration(), "should_be_a_empty_class"}
                , {new DefaultItemMethod(), "should_override_default_method"}
                , {new AbstractItemMethod(), "should_override_abstract_method"}
                , {new ReturnLongMethod(), "should_get_long"}
                , {new ReturnDoubleMethod(), "should_get_double"}
                , {new ReturnStringMethod(), "should_get_string"}
                , {new ReturnListIntMethod(), "should_get_list_int"}
                , {new ReturnListLongMethod(), "should_get_list_long"}
                , {new ReturnListDoubleMethod(), "should_get_list_double"}
                , {new ReturnListBooleanMethod(), "should_get_list_boolean"}
                , {new ReturnListStringMethod(), "should_get_list_string"}
                , {new ReturnArrayMethod(), "should_complain_return_type"}
                , {new ReturnListCharMethod(), "should_complain_return_type"}
        });
    }

    public GenerateConfigurationTest(When when, String generated) {
        super(when, "configuration/" + generated);
    }


    private abstract static class Base implements When {

        @Override
        public String given(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            doGiven(te, utils, thrown);
            return new GenerateConfiguration(TypeSpec.classBuilder("GenFoo")).visitTypeAsClass(te, utils)
                                                                             .build()
                                                                             .toString();
        }

        abstract void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown);
    }

    private static class NoConfiguration extends Base {
        @Override
        void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            when(te.getAnnotation(Configurable.class)).thenReturn(null);
        }
    }

    private static class DefaultItemMethod extends Base {

        final ExecutableElement ee = mock(ExecutableElement.class);
        final TypeMirror returnType = mock(TypeMirror.class);

        @Override
        void doGiven(TypeElement te, final ProcessUtils utils, ExpectedException thrown) {
            final Name name = mock(Name.class);
            when(name.toString()).thenReturn("bar");

            when(returnType.getKind()).thenReturn(returnTypeKind());
            when(returnType.accept(any(TypeVisitor.class), Matchers.any())).thenReturn(returnTypeName());

            when(ee.getAnnotation(Configurable.Item.class)).thenReturn(mock(Configurable.Item.class));
            when(ee.getSimpleName()).thenReturn(name);
            when(ee.getReturnType()).thenReturn(returnType);
            when(ee.accept(any(ElementKindVisitor6.class), Matchers.any())).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final ElementKindVisitor6 visitor = invocationOnMock.getArgumentAt(0, ElementKindVisitor6.class);
                    return visitor.visitExecutableAsMethod(ee, utils);
                }
            });
            when(utils.simpleNameOf(ee)).thenReturn("bar");

            doReturn(singletonList(ee)).when(te).getEnclosedElements();
            when(te.getAnnotation(Configurable.class)).thenReturn(mock(Configurable.class));

            final AnnotationSpec as = AnnotationSpec.builder(Configurable.class).build();
            when(utils.asAnnotationSpecs(anyList())).thenReturn(singletonList(as));
        }

        TypeName returnTypeName() {
            return TypeName.INT;
        }

        TypeKind returnTypeKind() {
            return TypeKind.INT;
        }
    }

    private static class AbstractItemMethod extends DefaultItemMethod {
        @Override
        void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            super.doGiven(te, utils, thrown);
            when(ee.getModifiers()).thenReturn(singleton(Modifier.ABSTRACT));
        }

        @Override
        TypeName returnTypeName() {
            return TypeName.BOOLEAN;
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.BOOLEAN;
        }

    }

    private static class ReturnLongMethod extends AbstractItemMethod {
        @Override
        TypeName returnTypeName() {
            return TypeName.LONG;
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.LONG;
        }
    }

    private static class ReturnDoubleMethod extends AbstractItemMethod {
        @Override
        TypeName returnTypeName() {
            return TypeName.DOUBLE;
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.DOUBLE;
        }
    }

    private static class ReturnStringMethod extends AbstractItemMethod {

        @Override
        void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            super.doGiven(te, utils, thrown);
            when(utils.isSameType(returnType, String.class)).thenReturn(true);
        }

        @Override
        TypeName returnTypeName() {
            return ClassName.get(String.class);
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.DECLARED;
        }
    }

    private static class ReturnListIntMethod extends AbstractItemMethod {

        @Override
        void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            super.doGiven(te, utils, thrown);
            when(utils.isSameType(returnType, List.class, parameterType())).thenReturn(true);
        }

        @Override
        TypeName returnTypeName() {
            return ParameterizedTypeName.get(List.class, parameterType());
        }

        Class<?> parameterType() {
            return Integer.class;
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.DECLARED;
        }

    }

    private static class ReturnListLongMethod extends ReturnListIntMethod {

        @Override
        Class<?> parameterType() {
            return Long.class;
        }
    }

    private static class ReturnListDoubleMethod extends ReturnListIntMethod {

        @Override
        Class<?> parameterType() {
            return Double.class;
        }
    }

    private static class ReturnListBooleanMethod extends ReturnListIntMethod {

        @Override
        Class<?> parameterType() {
            return Boolean.class;
        }
    }

    private static class ReturnListStringMethod extends ReturnListIntMethod {

        @Override
        Class<?> parameterType() {
            return String.class;
        }
    }

    private static class ReturnArrayMethod extends DefaultItemMethod {
        @Override
        void doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            super.doGiven(te, utils, thrown);
            when(utils.asElement(returnType)).thenReturn(mock(Element.class));
            thrown.expect(ElementException.class);
            thrown.expectMessage(endsWith("should be one type or list of those [boolean|int|long|double|String]"));
        }

        @Override
        TypeKind returnTypeKind() {
            return TypeKind.ARRAY;
        }
    }


    private static class ReturnListCharMethod extends ReturnArrayMethod {
        @Override
        TypeKind returnTypeKind() {
            return TypeKind.DECLARED;
        }
    }
}