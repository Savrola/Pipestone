package com.obtuse.util.gowing.p2a.holders;

import com.obtuse.util.gowing.EntityName;
import com.obtuse.util.gowing.GowingPacker;
import com.obtuse.util.gowing.p2a.GowingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Carry an integer value.
 */

public class GowingIntegerHolder extends GowingAbstractPackableHolder {

    public GowingIntegerHolder( final @NotNull EntityName name, @Nullable final Integer v, final boolean mandatory ) {

        super( name, GowingConstants.TAG_INTEGER, v, mandatory );

    }

    public GowingIntegerHolder( final @NotNull EntityName name, final int v ) {

        super( name, GowingConstants.TAG_INTEGER, v, true );

    }

    public GowingIntegerHolder( final @NotNull EntityName name, final int@NotNull[] v, @SuppressWarnings("SameParameterValue") final boolean mandatory ) {

        super( name, GowingConstants.TAG_INTEGER, v, mandatory, true );

    }

    public GowingIntegerHolder( final @NotNull EntityName name, @Nullable final Integer@NotNull[] v, @SuppressWarnings("SameParameterValue") final boolean mandatory ) {

        super( name, GowingConstants.TAG_INTEGER, v, mandatory, false );

    }

    public void emitRepresentation( final GowingPacker packer2 ) {

        Object value = getObjectValue();

        if ( isMandatory() || value != null ) {

            switch ( getKind() ) {

                case SCALAR:
                    packer2.emit( ( (Integer)value ).intValue() );
                    break;

                case PRIMITIVE_ARRAY:
                    packer2.emit( ( (int[])value ) );
                    break;

                case CONTAINER_ARRAY:
                    packer2.emit( ( (Integer[])value ) );
                    break;

            }

        } else {

            packer2.emitNull();

        }

    }

}
