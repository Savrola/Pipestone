package com.obtuse.util.gowing.p2a;

import com.obtuse.util.ObtuseUtil;
import com.obtuse.util.gowing.EntityName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Carry around a (type id, entity id, &lt;optional> version) tuple.
 */

public class GowingEntityReference implements Comparable<GowingEntityReference> {

    private final Integer _typeId;
    private final Long _entityId;
    private final Integer _version;
    private final SortedSet<EntityName> _entityReferenceNames;

    public GowingEntityReference( final int typeId, final long entityId, @Nullable final Integer version ) {

        this( typeId, entityId, version, null );
    }

    public GowingEntityReference(
            final int typeId,
            final long entityId,
            @Nullable final Integer version,
            @Nullable final Collection<EntityName> entityReferenceNames
    ) {
        super();

        if ( typeId <= 0 ) {

            throw new IndexOutOfBoundsException( "type id (" + typeId + ") must be positive" );

        }

        if ( entityId < 0L ) {

            throw new IndexOutOfBoundsException( "entity id (" + entityId + ") must be non-negative" );

        }

        if ( version != null && version.intValue() <= 0 ) {

            throw new IndexOutOfBoundsException( "version (" + version + ") must be positive if it is provided" );

        }

        _typeId = typeId;

        _entityId = entityId;

        _version = version;

        _entityReferenceNames = Collections.unmodifiableSortedSet(
                entityReferenceNames == null
                        ?
                        new TreeSet<>()
                        :
                        new TreeSet<>( entityReferenceNames )
        );

    }

    public int getTypeId() {

        return _typeId.intValue();

    }

    public long getEntityId() {

        return _entityId.longValue();

    }

    @Nullable
    public Integer getVersion() {

        return _version;

    }

    @NotNull
    public SortedSet<EntityName> getEntityReferenceNames() {

        return _entityReferenceNames;

    }

    public static String formatNames( final Collection<EntityName> entityNames ) {

        StringBuilder sb = new StringBuilder();
        String slash = "";
        for ( EntityName entityName : entityNames ) {

            try {
                sb.append( slash ).append( ObtuseUtil.enquoteToJavaString( entityName.getName() ) );
            } catch ( Throwable e ) {
                e.printStackTrace();
            }
            slash = "/";

        }

        return sb.toString();

    }

    public int compareTo( final @NotNull GowingEntityReference rhs ) {

        int rval = _typeId.compareTo( rhs._typeId );
        if ( rval == 0 ) {

            rval = _entityId.compareTo( rhs._entityId );

        }

        return rval;

    }

    public boolean equals( final Object rhs ) {

        return rhs instanceof GowingEntityReference && compareTo( (GowingEntityReference)rhs ) == 0;

    }

    public int hashCode() {

        return _entityId.hashCode();

    }

    public String toString() {

        return "GER( " + format() + " )";

    }

    @NotNull
    public String format() {

        return "" + GowingConstants.TAG_ENTITY_REFERENCE +
               _typeId +
               ":" +
               _entityId +
               ( _version == null ? "" : "v" + _version ) +
               ( getEntityReferenceNames().isEmpty() ? "" : GowingConstants.ENTITY_NAME_CLAUSE_MARKER + formatNames( getEntityReferenceNames() ) );

    }

}
