package com.obtuse.util.gowing.p2a.holders;

import com.obtuse.util.gowing.EntityName;
import com.obtuse.util.gowing.GowingPacker;
import com.obtuse.util.gowing.p2a.GowingConstants;
import org.jetbrains.annotations.NotNull;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Pack a byte value.
 */

public class GowingByteHolder extends GowingAbstractPackableHolder {

    public GowingByteHolder( @NotNull EntityName name, Byte v, @SuppressWarnings("SameParameterValue") boolean mandatory ) {
	super( name, GowingConstants.TAG_BYTE, v, mandatory );

    }

    public void emitRepresentation( GowingPacker packer2 ) {

	Object value = getObjectValue();

	if ( isMandatory() || value != null ) {

	    packer2.emit( ( (Byte) value ).byteValue() );

	} else {

	    packer2.emitNull();

	}

    }

//    public static IntegerHolder2 parse( @NotNull EntityName name, UnPacker2 unPacker, boolean mandatory ) {
//
//    }

}
