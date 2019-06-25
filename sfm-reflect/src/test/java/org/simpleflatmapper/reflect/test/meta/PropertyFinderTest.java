package org.simpleflatmapper.reflect.test.meta;

import org.junit.Assert;
import org.junit.Test;
import org.simpleflatmapper.reflect.TypeAffinity;
import org.simpleflatmapper.reflect.getter.NullGetter;
import org.simpleflatmapper.reflect.meta.*;
import org.simpleflatmapper.reflect.property.EligibleAsNonMappedProperty;
import org.simpleflatmapper.reflect.property.OptionalProperty;
import org.simpleflatmapper.test.beans.DbObject;
import org.simpleflatmapper.reflect.ReflectionService;
import org.simpleflatmapper.tuple.Tuples;
import org.simpleflatmapper.util.Predicate;

import static org.junit.Assert.*;

public class PropertyFinderTest {


    private PropertyFinder.PropertyFilter isValidPropertyMeta = PropertyFinder.PropertyFilter.trueFilter();

    @Test
    public void testFindElementOnArray() {

        ClassMeta<DbObject[]> classMeta = ReflectionService.newInstance().getClassMeta(DbObject[].class);

        PropertyFinder<DbObject[]> propertyFinder = classMeta.newPropertyFinder();

        PropertyMeta<DbObject[], ?> propEltId = propertyFinder.findProperty(matcher("elt0_id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("2_id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);
        Assert.assertEquals(1, ((ArrayElementPropertyMeta<?, ?>) ((SubPropertyMeta<?, ?, ?>) propEltId).getOwnerProperty()).getIndex());


        propEltId = propertyFinder.findProperty(matcher("id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);
        assertEquals(3, ((ArrayElementPropertyMeta<?, ?>) ((SubPropertyMeta<?, ?, ?>) propEltId).getOwnerProperty()).getIndex());


        propEltId = propertyFinder.findProperty(matcher("name"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);
        assertEquals(0, ((ArrayElementPropertyMeta<?, ?>) ((SubPropertyMeta<?, ?, ?>) propEltId).getOwnerProperty()).getIndex());

        propEltId = propertyFinder.findProperty(matcher("2_notid"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNull(propEltId);


        propEltId = propertyFinder.findProperty(matcher("notid"), new Object[0], (TypeAffinity)null, isValidPropertyMeta); // will safe match
        assertTrue(propEltId instanceof  ArrayElementPropertyMeta);
        assertEquals(4, ((ArrayElementPropertyMeta)propEltId).getIndex());


    }

    private PropertyNameMatcher matcher(String col) {
        return new DefaultPropertyNameMatcher(col, 0, false, false);
    }

    @Test
    public void testFindElementOnTuple() {
        ClassMeta<DbObject[]> classMeta = ReflectionService.newInstance().getClassMeta(Tuples.typeDef(String.class, DbObject.class, DbObject.class));

        PropertyFinder<DbObject[]> propertyFinder = classMeta.newPropertyFinder();

        PropertyMeta<DbObject[], ?> propEltId = propertyFinder.findProperty(matcher("element2_id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("element1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("elt1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);


        propEltId = propertyFinder.findProperty(matcher("4_id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("2_notid"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNull(propEltId);


        propEltId = propertyFinder.findProperty(matcher("elt0"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(propEltId);

        propEltId = propertyFinder.findProperty(matcher("notid"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNull(propEltId);

    }


    // https://github.com/arnaudroger/SimpleFlatMapper/issues/56
    @Test
    public void testArrayElementConstructorInjectionWithIncompatibleConstructorUseCompatibleOutlay() {
        ClassMeta<ObjectWithIncompatibleConstructor[]> classMeta = ReflectionService.newInstance().getClassMeta(ObjectWithIncompatibleConstructor[].class);

        PropertyFinder<ObjectWithIncompatibleConstructor[]> propertyFinder = classMeta.newPropertyFinder();

        assertNotNull(propertyFinder.findProperty(matcher("1_arg1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
        assertNotNull(propertyFinder.findProperty(matcher("1_arg3"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
        assertNotNull(propertyFinder.findProperty(matcher("2_arg1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
        assertNotNull(propertyFinder.findProperty(matcher("2_arg2"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
    }

    @Test
    public void testArrayElementConstructorInjectionWithIncompatibleConstructorUseIncompatibleOutlay() {
        ClassMeta<ObjectWithIncompatibleConstructor[]> classMeta = ReflectionService.newInstance().getClassMeta(ObjectWithIncompatibleConstructor[].class);

        PropertyFinder<ObjectWithIncompatibleConstructor[]> propertyFinder = classMeta.newPropertyFinder();

        assertNotNull(propertyFinder.findProperty(matcher("1_arg1"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
        assertNotNull(propertyFinder.findProperty(matcher("1_arg3"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
        assertNull(propertyFinder.findProperty(matcher("1_arg2"), new Object[0], (TypeAffinity)null, isValidPropertyMeta));
    }

    static class ObjectWithIncompatibleConstructor {
        private final String arg1;
        private final Long arg2;
        private final Integer arg3;

        public ObjectWithIncompatibleConstructor(String arg1, Long arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = null;
        }
        public ObjectWithIncompatibleConstructor(String arg1, Integer arg3) {
            this.arg1 = arg1;
            this.arg2 = null;
            this.arg3 = arg3;
        }
    }
    
    @Test
    public void testIsEnabled531() {
        ClassMeta<O531> classMeta = ReflectionService.newInstance().getClassMeta(O531.class);

        Predicate<PropertyMeta<?, ?>> p = new Predicate<PropertyMeta<?, ?>>() {
            @Override
            public boolean test(PropertyMeta<?, ?> propertyMeta) {
                return !NullGetter.isNull(propertyMeta.getGetter());
            }
        };

        PropertyFinder.PropertyFilter predicate = new PropertyFinder.PropertyFilter(p);

        PropertyFinder<O531> propertyFinder = classMeta.newPropertyFinder();
        propertyFinder.findProperty(DefaultPropertyNameMatcher.of("id"), new Object[0], (TypeAffinity) null, predicate);
        PropertyMeta<O531, Object> property = propertyFinder.findProperty(DefaultPropertyNameMatcher.of("is_enabled"), new Object[0], (TypeAffinity) null, predicate);

        assertNotNull(property);

    }


    @Test
    public void testOptionalPropReturNotMappedProperty664() {
        ClassMeta<DbObject> classMeta = ReflectionService.newInstance().getClassMeta(DbObject.class);

        PropertyFinder<DbObject> finder = classMeta.newPropertyFinder();

        PropertyMeta<DbObject, Object> id = finder.findProperty(DefaultPropertyNameMatcher.of("id"), new Object[0], (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(id);

        PropertyMeta<DbObject, Object> type = finder.findProperty(DefaultPropertyNameMatcher.of("type"), new Object[] { new OptionalProperty() {}, new EligibleAsNonMappedProperty() {}}, (TypeAffinity)null, isValidPropertyMeta);
        assertNotNull(type);
        assertTrue(type instanceof  NonMappedPropertyMeta);
        assertEquals("type", type.getName());

    }
    
    public static class O531 {
        private final boolean isEnabled;

        public O531(boolean isEnabled, int id) {
            this.isEnabled = isEnabled;
            this.id = id;
        }

        private final int id;

        public boolean isEnabled() {
            return isEnabled;
        }

        public int getId() {
            return id;
        }
    }
}
