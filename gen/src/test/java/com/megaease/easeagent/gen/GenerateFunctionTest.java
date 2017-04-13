package com.megaease.easeagent.gen;

import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class GenerateFunctionTest extends GenerateSpecTestBase {

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new NonAbstract(), "should_complain_non_abstract"}
                , {new NestClass(), "should_complain_non_abstract"}
                , {new OneFactory(), "should_generate_java_file"}
        });
    }

    public GenerateFunctionTest(When when, String generated) {
        super(when, "function/" + generated);
    }

    private abstract static class Base implements When {
        @Override
        public String given(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            Iterable<? extends AssemblyProcessor.GenerateSpecFactory> factories = doGiven(te, utils, thrown);
            return new GenerateFunction(utils, factories).apply(te).toString();
        }

        abstract Iterable<? extends AssemblyProcessor.GenerateSpecFactory> doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown);
    }

    private static abstract class Complain extends Base {

        @Override
        Iterable<? extends AssemblyProcessor.GenerateSpecFactory> doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            doGiven(thrown);
            return Collections.emptyList();
        }

        private void doGiven(ExpectedException thrown) {
            thrown.expect(ElementException.class);
            thrown.expectMessage(endsWith(expectMessage()));
        }

        abstract String expectMessage();
    }

    private static class NonAbstract extends Complain {

        @Override
        String expectMessage() {
            return "should be abstract";
        }
    }

    private static class NestClass extends Complain {
        @Override
        Iterable<? extends AssemblyProcessor.GenerateSpecFactory> doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            when(te.getModifiers()).thenReturn(Sets.newHashSet(Modifier.PUBLIC, Modifier.ABSTRACT));
            when(te.getNestingKind()).thenReturn(NestingKind.MEMBER);
            return super.doGiven(te, utils, thrown);
        }

        @Override
        String expectMessage() {
            return "should not be nest";
        }
    }

    private static class OneFactory extends Base {

        @Override
        Iterable<? extends AssemblyProcessor.GenerateSpecFactory> doGiven(TypeElement te, ProcessUtils utils, ExpectedException thrown) {
            when(te.getModifiers()).thenReturn(Sets.newHashSet(Modifier.PUBLIC, Modifier.ABSTRACT));
            when(te.getNestingKind()).thenReturn(NestingKind.TOP_LEVEL);

            when(utils.simpleNameOf(te)).thenReturn("Bar");
            when(utils.typeNameOf(any(TypeMirror.class))).thenReturn(ClassName.get(Bar.class));
            when(utils.packageNameOf(te)).thenReturn("com.megaease.easeagent.gen");
            doReturn(Collections.emptyList()).when(utils).asAnnotationSpecs(anyList());
            return Collections.singletonList(mock(AssemblyProcessor.GenerateSpecFactory.class));
        }
    }
}