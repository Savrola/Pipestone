package com.obtuse.util.packers.packer1;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

import org.jetbrains.annotations.NotNull;

/**
 * Encapsulate a JVM-wide packable entity's id.
 * <p/>
 * Instances of this class are immutable.
 */

public class PackingId1 implements Comparable<PackingId1> {

    private final int _hashCode;

//    @NotNull
//    private Packer _packerContext;
    @NotNull
    private final Short _typeId;
    @NotNull
    private final Long _idWithinType;
//    private final Long _blendedId;

    public PackingId1( @NotNull short typeId, long idWithinType ) {
        super();

//        _packerContext = packerContext;
        _typeId = typeId;
        _idWithinType = idWithinType;
        _hashCode = new Long( typeId ^ _idWithinType ).hashCode();

    }

//    @SuppressWarnings("unused")
//    @NotNull
//    public Packer getPackerContext() {
//
//        return _packerContext;
//    }

    @SuppressWarnings("unused")
    public short getTypeId() {

        return _typeId;

    }

    @SuppressWarnings("unused")
    public long getIdWithinType() {

        return _idWithinType;

    }

    @Override
    public int compareTo( @NotNull PackingId1 rhs ) {

        if ( _typeId.shortValue() == rhs._typeId.shortValue() ) {

            return _idWithinType.compareTo( rhs._idWithinType );

        } else {

            return _typeId.compareTo( rhs._typeId );

        }

    }

    @Override
    public boolean equals( Object rhs ) {

        return rhs instanceof PackingId1 && compareTo( (PackingId1)rhs ) == 0;

    }

    @Override
    public int hashCode() {

        return _hashCode;

    }

    public String toString() {

	return "PackingId( " + _typeId + ", " + _idWithinType + " )";

    }

}