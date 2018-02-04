/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import com.obtuse.util.gowing.*;
import com.obtuse.util.gowing.p2a.GowingEntityReference;
import com.obtuse.util.gowing.p2a.GowingUtil;
import com.obtuse.util.gowing.p2a.holders.GowingBooleanHolder;
import com.obtuse.util.gowing.p2a.holders.GowingPackableEntityHolder;
import com.obtuse.util.gowing.p2a.holders.GowingPackableMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class TwoDimensionalTreeMap<T1,T2,V> extends GowingAbstractPackableEntity implements Serializable, TwoDimensionalSortedMap<T1,T2,V> {

    private static final EntityTypeName ENTITY_TYPE_NAME = new EntityTypeName( TwoDimensionalTreeMap.class );
    private static final int VERSION = 1;

    private static final EntityName OUTER_MAP = new EntityName( "_om" );
    private static final EntityName READONLY = new EntityName( "_um" );

    public static final GowingEntityFactory FACTORY = new GowingEntityFactory( ENTITY_TYPE_NAME ) {

        @Override
        public int getOldestSupportedVersion() {

            return VERSION;

        }

        @Override
        public int getNewestSupportedVersion() {

            return VERSION;

        }

        @Override
        @NotNull
        public GowingPackable createEntity(
                @NotNull final GowingUnPacker unPacker,
                @NotNull final GowingPackedEntityBundle bundle,
                @NotNull final GowingEntityReference er
        ) {

            return new TwoDimensionalTreeMap( unPacker, bundle );

        }

    };

    @SuppressWarnings("unused") public static final TwoDimensionalSortedMap<?,?,?> EMPTY_MAP2D =
            new TwoDimensionalTreeMap<>( new TwoDimensionalTreeMap<>(), true );

    private GowingEntityReference _outerMapReference;

    private final boolean _readonly;

    private SortedMap<T1,SortedMap<T2,V>> _map = new TreeMap<>();

    public TwoDimensionalTreeMap() {
        super( new GowingNameMarkerThing() );

        _readonly = false;

    }

    public TwoDimensionalTreeMap( @NotNull final TwoDimensionalSortedMap<T1,T2,V> map, final boolean makeReadonly ) {
        super( new GowingNameMarkerThing() );

        for ( T1 t1 : map.outerKeys() ) {

            SortedMap<T2,V> innerMap = map.getInnerMap( t1, false );
            if ( innerMap != null ) {

                for ( T2 t2 : innerMap.keySet() ) {

                    put( t1, t2, innerMap.get( t2 ) );

                }

            }

        }

        if ( makeReadonly ) {

            for ( T1 t1 : _map.keySet() ) {

                _map.put( t1, Collections.unmodifiableSortedMap( _map.get( t1 ) ) );

            }

            _map = Collections.unmodifiableSortedMap( _map );

        }

        _readonly = makeReadonly;

    }

    public TwoDimensionalTreeMap( final @NotNull TwoDimensionalSortedMap<T1,T2,V> map ) {
        this( map, false );

    }

    private TwoDimensionalTreeMap( @NotNull final GowingUnPacker unPacker, @NotNull final GowingPackedEntityBundle bundle ) {

        super( unPacker, bundle.getSuperBundle() );

        _outerMapReference = bundle.getMandatoryEntityReference( OUTER_MAP );

        _readonly = bundle.booleanValue( READONLY );

    }

    @Override
    public @NotNull GowingPackedEntityBundle bundleThyself(
            final boolean isPackingSuper, @NotNull final GowingPacker packer
    ) {

        GowingPackedEntityBundle bundle = new GowingPackedEntityBundle(
                ENTITY_TYPE_NAME,
                VERSION,
                super.bundleRoot( packer ),
                packer.getPackingContext()
        );

        TreeMap<T1,GowingPackableMapping<T2,V>> fullMapping = new TreeMap<>();
        for ( T1 key1 : _map.keySet() ) {

            SortedMap<T2, V> inner = _map.get( key1 );
            if ( !inner.isEmpty() ) {

                SortedMap<T2,V> condensedInner = new TreeMap<>();
                for ( T2 key2 : inner.keySet() ) {

                    V value = inner.get( key2 );
                    if ( value != null ) {

                        condensedInner.put( key2, value );

                    }

                }

                if ( !condensedInner.isEmpty() ) {

                    GowingPackableMapping<T2, V> packedInner = new GowingPackableMapping<>( condensedInner );
                    fullMapping.put( key1, packedInner );

                }

            }

        }

        GowingPackableMapping<T1,GowingPackableMapping<T2,V>> packedMapping = new GowingPackableMapping<>( fullMapping );

        bundle.addHolder( new GowingPackableEntityHolder( OUTER_MAP, packedMapping, packer, true ) );
        bundle.addHolder( new GowingBooleanHolder( READONLY, _readonly, true ) );

        return bundle;

    }

    @Override
    public boolean finishUnpacking( @NotNull final GowingUnPacker unPacker ) {

        // Our parent class has no finishUnpacking method so we skip the step of letting it finish unpacking.

        // Check if our outer map's instance is ready for use.

        if ( !unPacker.isEntityFinished( _outerMapReference ) ) {

            return false;

        }

        GowingPackable packable = unPacker.resolveReference( _outerMapReference );
        if ( ( packable instanceof GowingPackableMapping ) ) {

            // The temporary variable is required in order to make this assignment a declaration which allows
            // the @SuppressWarnings("unchecked") annotation (the annotation is not allowed on a simple assignment statement).
            @SuppressWarnings("unchecked")
            TreeMap<T1, SortedMap<T2, V>> tmap = ( (GowingPackableMapping<T1, SortedMap<T2, V>>)packable ).rebuildMap( new TreeMap<>() );
            _map = tmap;

//            @SuppressWarnings("unchecked")
//            GowingPackableMapping<T1,GowingPackableMapping<T2,V>> packedMapping =
//                    (GowingPackableMapping<T1, GowingPackableMapping<T2,V>>)packable;
//
//            _map =
//            for ( GowingPackableKeyValuePair<T1, GowingPackableMapping<T2,V>> outerPair : packedMapping.getMappings() ) {
//
//                GowingPackableMapping<T2,V> outerMap = outerPair.getValue();
//
//                for ( GowingPackableKeyValuePair<T2,V> innerMap : outerMap.getMappings() ) {
//
//                    put( outerPair.getKey(), innerMap.getKey(), innerMap.getValue() );
//
//                }
//
//            }

            if ( _readonly ) {

                for ( T1 t1 : _map.keySet() ) {

                    _map.put( t1, Collections.unmodifiableSortedMap( _map.get( t1 ) ) );

                }

                _map = Collections.unmodifiableSortedMap( _map );

            }

        } else {

            GowingUtil.getGrumpy( "TwoDimensionalTreeMap", "outer map", GowingPackableMapping.class, packable );

        }

        return true;

    }

    @Override
    public boolean isReadonly() {

        return _readonly;

    }

//    @Override
//    public @NotNull GowingPackedEntityBundle bundleThyself(
//            final boolean isPackingSuper, @NotNull final GowingPacker packer
//    ) {
//
//        GowingPackedEntityBundle bundle = new GowingPackedEntityBundle(
//                ENTITY_TYPE_NAME,
//                VERSION,
//                super.bundleRoot( packer ),
//                packer.getPackingContext()
//        );
//
//        GowingPackableCollection<GowingPackableKeyValuePair<T1, GowingPackableCollection<GowingPackableKeyValuePair<T2,V>>>> packedMapping =
//                new GowingPackableCollection<>();
//        for ( T1 key1 : _map.keySet() ) {
//
//            SortedMap<T2, V> outerMapping = _map.get( key1 );
//            if ( outerMapping != null ) {
//
//                GowingPackableCollection<GowingPackableKeyValuePair<T2,V>> innerMap = new GowingPackableCollection<>();
//                for ( T2 key2 : outerMapping.keySet() ) {
//
//                    V value = outerMapping.get( key2 );
//                    if ( value != null ) {
//
//                        GowingPackableKeyValuePair<T2,V> innerPair = new GowingPackableKeyValuePair<>( key2, value );
//                        innerMap.add( innerPair );
//
//                    }
//
//                }
//
//                if ( !innerMap.isEmpty() ) {
//
//                    GowingPackableKeyValuePair<
//                            T1,
//                            GowingPackableCollection<GowingPackableKeyValuePair<T2, V>>
//                    > outerPair = new GowingPackableKeyValuePair<>(
//                            key1,
//                            innerMap
//                    );
//
//                    packedMapping.add( outerPair );
//
//                }
//
//            }
//
//        }
//
//        bundle.addHolder( new GowingPackableEntityHolder( OUTER_MAP, packedMapping, packer, true ) );
//
//        return bundle;
//
//    }
//
//    @Override
//    public boolean finishUnpacking( @NotNull final GowingUnPacker unPacker ) {
//
//        // Our parent class has no finishUnpacking method so we skip the step of letting it finish unpacking.
//
//        // Check if our outer map's instance is ready for use.
//
//        if ( !unPacker.isEntityFinished( _outerMapReference ) ) {
//
//            return false;
//
//        }
//
//        GowingPackable packable = unPacker.resolveReference( _outerMapReference );
//        if ( !( packable instanceof GowingPackableCollection ) ) {
//
//            throw new IllegalArgumentException(
//                    "TwoDimensionalTreeMap(Gowing):  expected a GowingPackableCollection but found a " +
//                    ( packable == null ? null : packable.getClass().getCanonicalName() )
//            );
//
//        } else {
//
//            @SuppressWarnings("unchecked")
//            GowingPackableCollection<GowingPackableKeyValuePair<T1, GowingPackableCollection<GowingPackableKeyValuePair<T2, V>>>> packedMapping =
//                    (GowingPackableCollection<GowingPackableKeyValuePair<T1, GowingPackableCollection<GowingPackableKeyValuePair<T2, V>>>>)packable;
//
//            for ( GowingPackableKeyValuePair<T1, GowingPackableCollection<GowingPackableKeyValuePair<T2, V>>> outerPair : packedMapping ) {
//
//                GowingPackableCollection<GowingPackableKeyValuePair<T2, V>> outerMap = outerPair.getValue();
//
//                for ( GowingPackableKeyValuePair<T2, V> innerMap : outerMap ) {
//
//                    put( outerPair.getKey(), innerMap.getKey(), innerMap.getValue() );
//
//                }
//
//            }
//
//        }
//
//        return true;
//
//    }

    @Override
    public V put( final T1 key1, final T2 key2, final V value ) {

        SortedMap<T2,V> innerMap = getNotNullInnerMap( key1 );

        @SuppressWarnings("UnnecessaryLocalVariable") V rval = innerMap.put( key2, value );

        return rval;

    }

    @Override
    @Nullable
    public SortedMap<T2,V> getInnerMap( final T1 key1, final boolean forceCreate ) {

        SortedMap<T2,V> innerMap = _map.get( key1 );
        if ( innerMap == null && forceCreate ) {

            innerMap = new TreeMap<>();
            _map.put( key1, innerMap );

        }

        return innerMap;

    }

    @Override
    @NotNull
    public SortedMap<T2,V> getNotNullInnerMap( final T1 key1 ) {

        @SuppressWarnings("UnnecessaryLocalVariable") SortedMap<T2, V> innerMap = _map.computeIfAbsent( key1, k -> new TreeMap<>() );

        return innerMap;

    }

    @Override
    @Nullable
    public SortedMap<T2,V> removeInnerMap( final T1 key ) {

        return _map.remove( key );

    }

    @Override
    @Nullable
    public V get( final T1 key1, final T2 key2 ) {

        SortedMap<T2,V> innerMap = _map.get( key1 );
        if ( innerMap == null ) {

            return null;

        }

        return innerMap.get( key2 );

    }

    @Override
    @Nullable
    public V remove( final T1 key1, final T2 key2 ) {

        V rval = null;

        SortedMap<T2,V> innerMap = _map.get( key1 );
        if ( innerMap != null ) {

            rval = innerMap.remove( key2 );

            // Note that we cannot remove this inner map if it is now empty since someone may already have a reference
            // to this particular inner map.

        }

        return rval;

    }

    @Override
    public int size() {

        int totalSize = 0;
        for ( SortedMap<T2,V> innerMap : innerMaps() ) {

            totalSize += innerMap.size();

        }

        return totalSize;

    }

    @Override
    public boolean isEmpty() {

        // We could just use "return size() == 0" but the short circuiting that we do below makes this approach
        // faster if the map is not empty.

        if ( _map.isEmpty() ) {

            return true;

        }

        for ( SortedMap<T2,V> innerMap : innerMaps() ) {

            if ( !innerMap.isEmpty() ) {

                return false;

            }

        }

        return true;

    }

    @Override
    @NotNull
    public Set<T1> outerKeys() {

        return _map.keySet();

    }

    @Override
    @NotNull
    public Collection<SortedMap<T2,V>> innerMaps() {

        return _map.values();

    }

    @Override
    public boolean containsKeys( final T1 key1, final T2 key2 ) {

        if ( _map.containsKey( key1 ) ) {

            @Nullable SortedMap<T2, V> innerMap = getInnerMap( key1, false );
            //noinspection RedundantIfStatement
            if ( innerMap != null && innerMap.containsKey( key2 ) ) {

                return true;

            }

        }

        return false;

    }

    @Override
    @NotNull
    public Iterator<V> iterator() {

        return new Iterator<V>() {

            private final Iterator<T1> _outerIterator;
            private T1 _activeOuterKey;
            private Iterator<V> _innerIterator;

            {

                _outerIterator = _map.keySet().iterator();

                findNextNonEmptyInnerMap();

            }

            private void findNextNonEmptyInnerMap() {

                _innerIterator = null;

                while ( _outerIterator.hasNext() ) {

                    _activeOuterKey = _outerIterator.next();

                    SortedMap<T2,V> innerMap = getInnerMap( _activeOuterKey, false );
                    //noinspection StatementWithEmptyBody
                    if ( innerMap == null || innerMap.isEmpty() ) {

                        // skip this one

                    } else {

                        _innerIterator = innerMap.values().iterator();
                        break;

                    }

                }

            }

            public boolean hasNext() {

                return _innerIterator != null && _innerIterator.hasNext();

            }

            public V next() {

                if ( !hasNext() ) {

                    throw new NoSuchElementException( "no more values" );

                }

                V next = _innerIterator.next();
                if ( _innerIterator != null && !_innerIterator.hasNext() ) {

                    findNextNonEmptyInnerMap();

                }

                return next;

            }

            public void remove() {

                throw new UnsupportedOperationException( "unable to remove values via this iterator" );

            }

        };

    }

    @Override
    public void clear() {

        _map.clear();

    }

    @NotNull
    public String toString() {

        return "TwoDimensionalTreeMap( size = " + size() + " )";

    }

}
