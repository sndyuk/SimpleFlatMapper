package org.simpleflatmapper.reflect.instantiator;

import org.junit.Test;
import org.simpleflatmapper.reflect.getter.ConstantGetter;

import static org.junit.Assert.*;

public class GetterToInstantiatorTest {

    @Test
    public void test() throws Exception {
        Object o = new Object();
        GetterToInstantiator<Object, Object> getterToInstantiator = new GetterToInstantiator<Object, Object>(
                new ConstantGetter<Object, Object>(o)
        );

        assertEquals(o, getterToInstantiator.newInstance(null));
    }
}