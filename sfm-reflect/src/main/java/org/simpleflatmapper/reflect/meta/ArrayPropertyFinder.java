package org.simpleflatmapper.reflect.meta;

import org.simpleflatmapper.reflect.property.SpeculativeArrayIndexResolutionProperty;
import org.simpleflatmapper.util.BooleanSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.simpleflatmapper.util.Asserts.requireNonNull;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArrayPropertyFinder<T, E> extends AbstractIndexPropertyFinder<T> {


    public ArrayPropertyFinder(ArrayClassMeta<T, E> arrayClassMeta, boolean selfScoreFullName) {
        super(arrayClassMeta, selfScoreFullName);
    }

    @Override
    protected IndexedElement<T, E> getIndexedElement(IndexedColumn indexedColumn) {
        int indexValue = indexedColumn.getIndexValue();
        return getIndexedElement(indexValue);
    }

    protected IndexedElement<T, E> getIndexedElement(int indexValue) {
        IndexedElement<T, E> indexedElement = (IndexedElement<T, E>) elements.get(indexValue);

        if (indexedElement == null) {
            indexedElement = new IndexedElement<T, E>(
                    newElementPropertyMeta(indexValue, "element" + indexValue), ((ArrayClassMeta<T, E>)classMeta).getElementClassMeta());
            elements.put(indexValue, indexedElement);
        }

        return indexedElement;
    }

    private PropertyMeta<T, E> newElementPropertyMeta(int index, String name) {
        ArrayClassMeta<T, E> arrayClassMeta = (ArrayClassMeta<T, E>) classMeta;
        BooleanSupplier appendSetter = new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                for(int i = 1; i < elements.size(); i++) {
                    if (elements.get(i).hasAnyProperty()) {
                        return false;
                    }
                }
                return true;
            }
        };
        return new ArrayElementPropertyMeta<T, E>(name,
                classMeta.getType(), arrayClassMeta.getReflectionService(), index, arrayClassMeta,
                arrayClassMeta.<T, E>newSetterFactory(appendSetter), arrayClassMeta.<T, E>newGetterFactory());
    }

    @Override
    protected void extrapolateIndex(PropertyNameMatcher propertyNameMatcher, Object[] properties, FoundProperty<T> foundProperty, PropertyMatchingScore score, PropertyFinderTransformer propertyFinderTransformer, TypeAffinityScorer typeAffinityScorer, PropertyFilter propertyFilter) {
        final ClassMeta<E> elementClassMeta = ((ArrayClassMeta)classMeta).getElementClassMeta();

        // all element has same type so check if can find any property matching
        PropertyMeta<E, ?> property =
                elementClassMeta.newPropertyFinder().findProperty(propertyNameMatcher, properties, typeAffinityScorer, propertyFilter);

        if (property != null) {
            if (ObjectPropertyFinder.containsProperty(properties, SpeculativeArrayIndexResolutionProperty.class)) {
                List<Integer> keys = getKeys();
                appendEmptySlot(keys);
                for (Integer k : keys) {
                    IndexedElement element = getIndexedElement(k);
                    ExtrapolateFoundProperty<T> matchingProperties = new ExtrapolateFoundProperty<T>(element, foundProperty);
                    lookForAgainstColumn(new IndexedColumn(k, propertyNameMatcher), properties, matchingProperties, score.speculativeArrayIndex(k), propertyFinderTransformer, typeAffinityScorer, propertyFilter);
                    if (matchingProperties.hasFound()) {
                        return;
                    }
                }
            } else {
                // only look for element 0
                FoundProperty<T> fp = foundProperty;
                if (!elements.isEmpty()) {
                    IndexedElement element = getIndexedElement(0);
                    fp = new ExtrapolateFoundProperty<T>(element, foundProperty);
                }
                lookForAgainstColumn(new IndexedColumn(0, propertyNameMatcher), properties, fp, score, propertyFinderTransformer, typeAffinityScorer, propertyFilter);
            }
        }
	}

    private void appendEmptySlot(List<Integer> keys) {
        if (keys.isEmpty()) {
            keys.add(0);
            return;
        }

        for(int i = 0; i + 1 < keys.size(); i++) {
            int k1 = keys.get(i);
            int k2 = keys.get(i + 1);
            if (k2 - k1 > 1) {
                keys.add(i + 1, k1 + 1);
                return;
            }
        }
        keys.add(keys.get(keys.size() -1) + 1);
    }

    private List<Integer> getKeys() {
        ArrayList<Integer> keys = new ArrayList<Integer>(elements.keySet());
        Collections.sort(keys);
        return keys;
    }

    @Override
    protected boolean indexMatches(PropertyMeta<T, ?> propertyMeta, PropertyMeta<?, ?> owner) {
        if (owner == propertyMeta) return true;
        if (propertyMeta instanceof  ArrayElementPropertyMeta && owner instanceof ArrayElementPropertyMeta) {
            return ((ArrayElementPropertyMeta) propertyMeta).getIndex() == ((ArrayElementPropertyMeta) owner).getIndex();
        }
        return false;
    }

    @Override
    protected PropertyFinder<?> registerProperty(SubPropertyMeta<?, ?, ?> subPropertyMeta) {
        PropertyMeta<?, ?> ownerProperty = subPropertyMeta.getOwnerProperty();
        
        if (ownerProperty instanceof  ArrayElementPropertyMeta) {
            ArrayElementPropertyMeta arrayElementPropertyMeta = (ArrayElementPropertyMeta) ownerProperty;
            IndexedElement<T, E> indexedElement = getIndexedElement(new IndexedColumn(arrayElementPropertyMeta.getIndex(), null));
            return indexedElement.getPropertyFinder();
        }
        
        throw new IllegalArgumentException("Illegal owner expected ArrayElementPropertyMeta got " + subPropertyMeta);
    }

    @Override
    protected boolean isValidIndex(IndexedColumn indexedColumn) {
        return indexedColumn.getIndexValue() >= 0;
    }

    private static class ExtrapolateFoundProperty<T> implements FoundProperty<T> {
        private final IndexedElement element;
        private final FoundProperty foundProperty;
        private boolean found;

        public ExtrapolateFoundProperty(IndexedElement element, FoundProperty foundProperty) {
            this.element = requireNonNull("element", element);
            this.foundProperty = requireNonNull("foundProperty", foundProperty);
        }

        @Override
        public <P extends PropertyMeta<T, ?>> void found(P propertyMeta, Runnable selectionCallback, PropertyMatchingScore score, TypeAffinityScorer typeAffinityScorer) {
            String pathCheck;

            if (propertyMeta instanceof ArrayElementPropertyMeta) {
                pathCheck = SelfPropertyMeta.PROPERTY_PATH;
            } else if (propertyMeta.isSubProperty()) {
                pathCheck = ((SubPropertyMeta)propertyMeta).getSubProperty().getPath();
            } else {
                throw new IllegalArgumentException("Excepted match " + propertyMeta);
            }

            if (!element.hasProperty(pathCheck)) {
                foundProperty.found(propertyMeta, selectionCallback, score, typeAffinityScorer);
                this.found = true;
            }
        }

        public boolean hasFound() {
            return found;
        }
    }
}
