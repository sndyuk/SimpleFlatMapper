package org.simpleflatmapper.reflect.meta;


import org.junit.Test;
import org.simpleflatmapper.reflect.instantiator.ExecutableInstantiatorDefinition;
import org.simpleflatmapper.reflect.InstantiatorDefinition;
import org.simpleflatmapper.reflect.ReflectionService;
import org.simpleflatmapper.util.TypeReference;

import java.util.*;

import static org.junit.Assert.*;

public class ListClassMetaTest {



    @Test
    public void testUseArrayListOnList() throws NoSuchMethodException {
        typeMetaHasTheSpecifiedClassEmptyConstructor(
                new TypeReference<List<String>>() {},
                ArrayList.class);

    }

    @Test
    public void testUseSpecifiedImplType() throws NoSuchMethodException {
        typeMetaHasTheSpecifiedClassEmptyConstructor(
                new TypeReference<MyList>() {},
                MyList.class);
    }

    static class MyList extends LinkedList<String> {

    }



    private void typeMetaHasTheSpecifiedClassEmptyConstructor(TypeReference<?> typeReference, Class<?> impl) throws NoSuchMethodException {
        final ClassMeta<List<String>> classMeta =
                ReflectionService.newInstance().getClassMeta(typeReference.getType());

        hasOneInstantiatorDefinitionWithEmptyConstructorOnImpl(classMeta, impl);
    }



    private void hasOneInstantiatorDefinitionWithEmptyConstructorOnImpl(ClassMeta<?> classMeta, Class<?> impl) throws NoSuchMethodException {
        assertTrue(classMeta instanceof ArrayClassMeta);
        final List<InstantiatorDefinition> instantiatorDefinitions = classMeta.getInstantiatorDefinitions();

        assertEquals(1, instantiatorDefinitions.size());
        final ExecutableInstantiatorDefinition instantiatorDefinition = (ExecutableInstantiatorDefinition) instantiatorDefinitions.get(0);

        assertEquals(0, instantiatorDefinition.getParameters().length);
        assertEquals(impl.getDeclaredConstructor(), instantiatorDefinition.getExecutable());
    }
}
