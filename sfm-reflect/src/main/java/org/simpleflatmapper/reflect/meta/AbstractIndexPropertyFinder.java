package org.simpleflatmapper.reflect.meta;

import org.simpleflatmapper.reflect.InstantiatorDefinition;
import org.simpleflatmapper.util.Predicate;

import java.lang.reflect.Type;
import java.util.*;

public abstract class AbstractIndexPropertyFinder<T> extends PropertyFinder<T> {
    protected final ClassMeta<T> classMeta;
    protected final Map<Integer, IndexedElement<T, ?>> elements;

    public AbstractIndexPropertyFinder(ClassMeta<T> classMeta, boolean selfScoreFullName) {
        super(selfScoreFullName);
        this.elements = new HashMap<Integer, IndexedElement<T, ?>>();
        this.classMeta = classMeta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void lookForProperties(
            PropertyNameMatcher propertyNameMatcher,
            Object[] properties, FoundProperty<T> matchingProperties,
            PropertyMatchingScore score,
            boolean allowSelfReference,
            PropertyFinderTransformer propertyFinderTransformer, TypeAffinityScorer typeAffinityScorer, PropertyFilter propertyFilter) {


        IndexedColumn indexedColumn = propertyNameMatcher.matchIndex();
        if (indexedColumn != null) {
            lookForAgainstColumn(indexedColumn, properties, matchingProperties, score.arrayIndex(indexedColumn.getIndexValue()), propertyFinderTransformer, typeAffinityScorer, propertyFilter);
        } else {
            extrapolateIndex(propertyNameMatcher, properties, matchingProperties, score.speculative(), propertyFinderTransformer, typeAffinityScorer, propertyFilter);
            speculativeMatching(propertyNameMatcher, properties, matchingProperties, score.speculative(), propertyFinderTransformer, typeAffinityScorer, propertyFilter);
        }
    }

    @SuppressWarnings("unchecked")
    protected void lookForAgainstColumn(IndexedColumn indexedColumn, Object[] properties, final FoundProperty<T> matchingProperties, PropertyMatchingScore score,
                                        PropertyFinderTransformer propertyFinderTransformer, TypeAffinityScorer typeAffinityScorer, PropertyFilter propertyFilter) {

        if (indexedColumn == null || !isValidIndex(indexedColumn)) {
            // no index found
            return;
        }

        final IndexedElement<T, ?> indexedElement = getIndexedElement(indexedColumn);

        if (indexedElement == null) return;

        if (indexedColumn.getSubPropertyNameMatcher() == null) {
            matchingProperties.found(indexedElement.getPropertyMeta(), new Runnable() {
                @Override
                public void run() {
                    if (!indexedElement.hasProperty(SelfPropertyMeta.PROPERTY_PATH)) {
                        indexedElement.addProperty(SelfPropertyMeta.PROPERTY_PATH);
                    }
                }
            }, score.self(indexedElement.getElementClassMeta(), indexedColumn.getIndexProperty()), typeAffinityScorer);
            return ;
        }

        final PropertyFinder<?> eltPropertyFinder = indexedElement.getPropertyFinder();

        if (eltPropertyFinder == null) {
            return;
        }

        propertyFinderTransformer.apply(eltPropertyFinder)
                .lookForProperties(indexedColumn.getSubPropertyNameMatcher(),
                        properties, new FoundProperty() {
                    @Override
                    public void found(final PropertyMeta propertyMeta, final Runnable selectionCallback, final PropertyMatchingScore score, TypeAffinityScorer typeAffinityScorer) {
                        PropertyMeta subProperty = new SubPropertyMeta(classMeta.getReflectionService(), indexedElement.getPropertyMeta(), propertyMeta);
                        matchingProperties.found(subProperty, new Runnable() {
                            @Override
                            public void run() {
                                selectionCallback.run();
                                indexedElement.addProperty(propertyMeta);
                            }
                        }, score, typeAffinityScorer);
                    }
                }, score.matches(indexedColumn.getIndexProperty()), true, propertyFinderTransformer, typeAffinityScorer, propertyFilter);
    }


    private void speculativeMatching(PropertyNameMatcher propertyNameMatcher, Object[] properties, FoundProperty<T> foundProperty, PropertyMatchingScore score, PropertyFinderTransformer propertyFinderTransformer, TypeAffinityScorer typeAffinityScorer, PropertyFilter propertyFilter) {
        // try to match against prefix
        PropertyNameMatch speculativeMatch = propertyNameMatcher.speculativeMatch();

        if (speculativeMatch != null) {
                extrapolateIndex(speculativeMatch.getLeftOverMatcher(), properties, foundProperty, score.speculative(), propertyFinderTransformer, typeAffinityScorer, propertyFilter);
        }
    }

    protected abstract boolean isValidIndex(IndexedColumn indexedColumn);

    protected abstract <E> IndexedElement<T,?> getIndexedElement(IndexedColumn indexedColumn);

    protected abstract void extrapolateIndex(PropertyNameMatcher propertyNameMatcher, Object[] properties, FoundProperty<T> foundProperty, PropertyMatchingScore score, PropertyFinderTransformer propertyFinderTransformer, TypeAffinityScorer typeAffinityScorer, PropertyFilter propertyFilter);

    @Override
    public List<InstantiatorDefinition> getEligibleInstantiatorDefinitions() {
        return classMeta.getInstantiatorDefinitions();
    }
    

    @Override
    public Type getOwnerType() {
        return classMeta.getType();
    }

    @Override
    public PropertyFinder<?> getSubPropertyFinder(PropertyMeta<?, ?> owner) {
        PropertyFinder<?> ie = lookForPropertyFinder(owner);
        if (ie != null) return ie;

        throw new IllegalArgumentException("Unexpected owner " + owner);
    }

    private PropertyFinder<?> lookForPropertyFinder(PropertyMeta<?, ?> owner) {
        ArrayList<Integer> indicies = new ArrayList<Integer>(elements.keySet());
        Collections.sort(indicies);

        for(Integer key : indicies) {
            IndexedElement<T, ?> ie = elements.get(key);
            if (indexMatches(ie.getPropertyMeta(), owner)) {
                return ie.getPropertyFinder();
            }
        }
        return null;
    }

    protected abstract boolean indexMatches(PropertyMeta<T, ?> propertyMeta, PropertyMeta<?, ?> owner);

    @Override
    public PropertyFinder<?> getOrCreateSubPropertyFinder(SubPropertyMeta<?, ?, ?> subPropertyMeta) {
        PropertyMeta<?, ?> ownerProperty = subPropertyMeta.getOwnerProperty();

        PropertyFinder<?> ie = lookForPropertyFinder(ownerProperty);
        
        if (ie != null) return ie;
        
        
        return registerProperty(subPropertyMeta);
        
    }

    protected abstract PropertyFinder<?> registerProperty(SubPropertyMeta<?, ?, ?> subPropertyMeta);

}
