package com.obtuse.util.gowing.p2a.holders;

import com.obtuse.util.gowing.EntityName;
import com.obtuse.util.gowing.GowingPacker;
import com.obtuse.util.gowing.p2a.GowingConstants;
import org.jetbrains.annotations.NotNull;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Pack a float value.
 */

public class GowingFloatHolder extends GowingAbstractPackableHolder {

    public GowingFloatHolder( @NotNull final EntityName name, final Float v, @SuppressWarnings("SameParameterValue") final boolean mandatory ) {

        super( name, GowingConstants.TAG_FLOAT, v, mandatory );

    }

    public GowingFloatHolder( @NotNull final EntityName name, final float[] v, @SuppressWarnings("SameParameterValue") final boolean mandatory ) {

        super( name, GowingConstants.TAG_FLOAT, v, mandatory, true );

    }

    public GowingFloatHolder( @NotNull final EntityName name, final Float[] v, @SuppressWarnings("SameParameterValue") final boolean mandatory ) {

        super( name, GowingConstants.TAG_FLOAT, v, mandatory, false );

    }

    public void emitRepresentation( final GowingPacker packer2 ) {

        Object value = getObjectValue();

        if ( isMandatory() || value != null ) {

            switch ( getKind() ) {

                case SCALAR:
                    packer2.emit( ( (Float)value ).floatValue() );
                    break;

                case PRIMITIVE_ARRAY:
                    packer2.emit( ( (float[])value ) );
                    break;

                case CONTAINER_ARRAY:
                    packer2.emit( ( (Float[])value ) );
                    break;

            }
//	    packer2.emit( ( (Float) value ).floatValue() );

        } else {

            packer2.emitNull();

        }

    }

}
