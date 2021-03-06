package com.obtuse.util.gowing;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.SimpleUniqueIntegerIdGenerator;
import com.obtuse.util.SimpleUniqueLongIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedMap;
import java.util.TreeMap;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 A value which uniquely identifies something which implements the {@link GowingPackable} interface.
 <p/>
 Notes:
 <ol>
 <li>Every packable entity must have a JVM-wide-unique instance id which never changes during the entity's lifespan within a particular JVM.</li>
 <li>The instance id of a packable entity must be created by invoking one of this class's constructors.</li>
 <li>Packable entities <b>MUST NOT</b> assume that their instance id will remain the same if they are packed and then later unpacked.</li>
 </ol>
 */

public final class GowingInstanceId implements Comparable<GowingInstanceId> {

    private static final SimpleUniqueLongIdGenerator s_idGenerator =
            new SimpleUniqueLongIdGenerator( GowingInstanceId.class.getCanonicalName() + " - entity id generator" );
    private static final SimpleUniqueIntegerIdGenerator s_typeIdGenerator =
            new SimpleUniqueIntegerIdGenerator( GowingInstanceId.class.getCanonicalName() + " - type id generator" );

    private static final SortedMap<String, Integer> s_typeNamesToTypeIds = new TreeMap<>();
    private static final SortedMap<Integer, String> s_typeIdsToTypeNames = new TreeMap<>();

    private final long _entityId;

    private final int _typeId;

    private final String _typeName;

    public GowingInstanceId( final @NotNull EntityTypeName typeName ) {

        this( typeName.getTypeName() );

    }

    public GowingInstanceId( final @Nullable String typeName ) {
        super();

        if ( typeName == null ) {

            throw new IllegalArgumentException( "GowingInstanceId:  no typeName specified" );

        }
        _typeId = allocateTypeId( typeName );

        _typeName = typeName;
        _entityId = s_idGenerator.getUniqueId();

        if ( _entityId == 0 ) {

            throw new HowDidWeGetHereError( "GowingInstanceId:  entity id of 0 generated - violates assumption that 0 marks a super-bundle entity" );

        }

    }

    public GowingInstanceId( final @NotNull Class<? extends GowingPackable> classObject ) {

        this( classObject.getCanonicalName() );

    }

    public static int allocateTypeId( final @NotNull String typeName ) {

        synchronized ( s_typeNamesToTypeIds ) {

            Integer typeId = lookupTypeId( typeName );
            if ( typeId == null ) {

                typeId = s_typeIdGenerator.getUniqueId();
                s_typeNamesToTypeIds.put( typeName, typeId );
                s_typeIdsToTypeNames.put( typeId, typeName );

            }

            return typeId.intValue();

        }

    }

    @Nullable
    public static String lookupTypeName( final int typeId ) {

        return s_typeIdsToTypeNames.get( typeId );

    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static Integer lookupTypeId( final @NotNull String typeName ) {

        return s_typeNamesToTypeIds.get( typeName );

    }

    public long getEntityId() {

        return _entityId;

    }

    public int getTypeId() {

        return _typeId;

    }

    @NotNull
    public String getTypeName() {

        return _typeName;

    }

    public int compareTo( final @NotNull GowingInstanceId rhs ) {

        int rval = _typeName.compareTo( rhs._typeName );
        if ( rval == 0 ) {

            rval = Long.compare( _entityId, rhs._entityId );

        }

        return rval;

    }

    public boolean equals( final Object rhs ) {

        return rhs instanceof GowingInstanceId && compareTo( (GowingInstanceId)rhs ) == 0;

    }

    public int hashCode() {

        return Long.hashCode( _entityId );

    }

    @NotNull
    public String shortForm() {

        return "GII( " + _typeId + ":" + _entityId + " )";

    }

    @NotNull
    public String toString() {

        return "GowingInstanceId( " + _typeName + ", " + _entityId + " )";

    }

}
