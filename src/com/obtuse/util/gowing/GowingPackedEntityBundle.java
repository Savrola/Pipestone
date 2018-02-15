package com.obtuse.util.gowing;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.FormattingLinkedList;
import com.obtuse.util.Logger;
import com.obtuse.util.ObtuseUtil;
import com.obtuse.util.gowing.p2a.GowingEntityReference;
import com.obtuse.util.gowing.p2a.holders.GowingAbstractPackableHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.TreeMap;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Carry around the packed form of an entity.
 */

@SuppressWarnings("unused")
public class GowingPackedEntityBundle extends TreeMap<EntityName, GowingPackableThingHolder> {

    private final EntityTypeName _typeName;

    private final int _version;

    private final GowingPackedEntityBundle _superBundle;

    private final int _typeId;

    public GowingPackedEntityBundle(
            final @NotNull EntityTypeName typeName,
            final int version,
            final @NotNull GowingPackedEntityBundle superBundle,
            final @NotNull GowingPackerContext packerContext
    ) {

        super();

        _typeName = typeName;
        _version = version;
        _typeId = packerContext.rememberTypeName( typeName );
        _superBundle = superBundle;

        if ( version <= 0 ) {

            throw new IllegalArgumentException( "GowingPackedEntityBundle( packing, tn=" + typeName + ", ti=" + _typeId + ", v=" + version + ", sb=set ):  version is invalid" );

        }

    }

    public GowingPackedEntityBundle(
            final @NotNull EntityTypeName typeName,
            final int version,
            final @NotNull GowingPackerContext packerContext
    ) {

        super();

        _typeName = typeName;
        _version = version;
        _typeId = packerContext.rememberTypeName( typeName );
        _superBundle = null;

        if ( version <= 0 ) {

            throw new IllegalArgumentException( "GowingPackedEntityBundle( packing, tn=" + typeName + ", v=" + version + ", sb=omitted ):  version is invalid" );

        }

    }

    public GowingPackedEntityBundle(
            final @NotNull EntityTypeName typeName,
            final int typeId,
            final @NotNull GowingPackedEntityBundle superBundle,
            final int version,
            final @NotNull GowingUnPackerContext unPackerContext
    ) {
        super();

        if ( typeName.equals( unPackerContext.getTypeByTypeReferenceId( typeId ) ) ) {

            if ( ObtuseUtil.never() ) {

                Logger.logMsg( "GPEB:  got expected type name " + typeName + " for type id " + typeId );

            }

        } else {

            Logger.logMsg(
                    "GPEB:  got unexpected type name " + unPackerContext.getTypeByTypeReferenceId( typeId ) +
                    " for type id " + typeId + " (expected " + typeName + ")"
            );

        }

        _typeName = typeName;
        _typeId = typeId; // unPackerContext.rememberTypeName( typeName );
        _superBundle = superBundle;
        _version = version;

        if ( version <= 0 ) {

            throw new IllegalArgumentException( "GowingPackedEntityBundle( unpacking, tn=" + typeName + ", ti=" + typeId + ", v=" + version + ", sb=set ):  version is invalid" );

        }

    }

    public GowingPackedEntityBundle(
            final EntityTypeName typeName,
            final int typeId,
            final int version
    ) {
        super();

        _typeName = typeName;
        _typeId = typeId;
        _version = version;
        _superBundle = null;

        if ( version <= 0 ) {

            throw new IllegalArgumentException( "GowingPackedEntityBundle( short, tn=" + typeName + ", ti=" + typeId + ", v=" + version + ", sb=set ):  version is invalid" );

        }

    }

    @NotNull
    public GowingPackedEntityBundle getSuperBundle() {

        if ( _superBundle == null ) {

            throw new HowDidWeGetHereError( "GowingPackedEntityBundle:  request for null super bundle" );

        }

        return _superBundle;

    }

    public boolean hasSuperBundle() {

        return _superBundle != null;

    }

    public int getVersion() {

        return _version;

    }

    @SuppressWarnings("WeakerAccess")
    public EntityTypeName getTypeName() {

        return _typeName;

    }

    public int getTypeId() {

        return _typeId;

    }

    public String toString() {

        return "GowingPackedEntityBundle( " + getTypeId() + ":" + getTypeName() + ", " + keysToString() + " )";

    }

    @SuppressWarnings("WeakerAccess")
    public String keysToString() {

        FormattingLinkedList<EntityName> keys = new FormattingLinkedList<>( keySet() );

        return keys.toString();

    }

    public GowingPackedEntityBundle addHolder( final @NotNull GowingPackableThingHolder holder ) {

        if ( containsKey( holder.getName() ) ) {

            throw new IllegalArgumentException( "GowingPackedEntityBundle.addHolder:  duplicate key " + ObtuseUtil.enquoteToJavaString( holder.getName().toString() ) );

        }

        put( holder.getName(), holder );

        return this;

    }

    @Nullable
    public GowingPackableThingHolder removeHolderByName( final @NotNull EntityName holderName ) {

        return remove( holderName );

    }

    /**
     Helper method to get an optional entity reference field.

     @param name the name of the optional entity reference field.
     @return the entity reference specified by the field, or null if the field does not exist within this bundle or exists but is null.
     @throws ClassCastException if the specified field exists but is not a GowingEntityReference value.
     */

    @Nullable
    public GowingEntityReference getOptionalEntityReference( final @NotNull EntityName name ) {

        GowingPackableThingHolder ref = getNullableField( name );
        if ( ref == null ) {

            return null;

        } else {

            return ((GowingAbstractPackableHolder)ref).EntityTypeReference();

        }

    }

    /**
     Helper method to get a mandatory entity reference field.

     @param name the name of the mandatory entity reference field.
     @return the entity reference specified by the field (will not be null).
     @throws IllegalArgumentException if the specified field does not exist within this bundle.
     @throws NullPointerException     if the specified field exists within this bundle but is null.
     @throws ClassCastException       if the specified field exists but is not a GowingEntityReference value.
     */

    @NotNull
    public GowingEntityReference getMandatoryEntityReference( final @NotNull EntityName name ) {

        GowingPackableThingHolder ref = getNotNullField( name );

        return ((GowingAbstractPackableHolder)ref).EntityTypeReference();

    }

    /**
     Get a field which must exist and must have a non-null value.

     @param name the name of the desired field.
     @return the holder containing the field.
     @throws IllegalArgumentException if the specified field does not exist within this bundle.
     @throws NullPointerException     if the field specified exists within this bundle but is null.
     */

    public GowingPackableThingHolder getNotNullField( final EntityName name ) {

        GowingPackableThingHolder holder = get( name );
        if ( holder == null ) {

            throw new IllegalArgumentException( "required field \"" + name + "\" is missing" );

        }

        if ( holder.isNull() ) {

            throw new NullPointerException( "field \"" + name + "\" is null" );

        }

        return holder;

    }

    /**
     Get a field which must exist but which might have a null value.

     @param name the name of the desired field.
     @return the holder containing the field.
     */

    public GowingPackableThingHolder getNullableField( final EntityName name ) {

        GowingPackableThingHolder holder = get( name );
        if ( holder == null ) {

            throw new IllegalArgumentException( "required field \"" + name + "\" is missing" );

        }

        return holder;

    }

    /**
     Determine if a field exists.
     <p/>Makes it possible to have totally optional fields.

     @param tag the name of the optional field.
     @return {@code true} if a field by that name exists; {@code false} otherwise.
     */

    public boolean doesFieldExist( final EntityName tag ) {

        return containsKey( tag );

    }

    public URI recoverURI( final EntityName tag ) {

        String s = getNullableField( tag ).StringValue();
        if ( s == null ) {

            return null;

        } else {

            try {

                return new URI( s );

            } catch ( URISyntaxException e ) {

                throw new HowDidWeGetHereError( "syntax error parsing URI " + ObtuseUtil.enquoteToJavaString( s ), e );

            }

        }

    }

    public URL recoverURL( final EntityName tag ) {

        String s = getNullableField( tag ).StringValue();
        if ( s == null ) {

            return null;

        } else {

            try {

                return new URL( s );

            } catch ( MalformedURLException e ) {

                throw new HowDidWeGetHereError( "syntax error parsing URL " + ObtuseUtil.enquoteToJavaString( s ), e );

            }

        }

    }

    public File recoverFile( final EntityName tag ) {

        String s = getNullableField( tag ).StringValue();
        if ( s == null ) {

            return null;

        } else {

            return new File( s );

        }

    }

    @Nullable
    public String StringValue( final EntityName gtag ) {

        return getNullableField( gtag ).StringValue();

    }

    @NotNull
    public String MandatoryStringValue( final EntityName gtag ) {

        return getNotNullField( gtag ).MandatoryStringValue();

    }

    @Nullable
    public String[] StringArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).StringArrayValue();

    }

    @NotNull
    public String[] MandatoryStringArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).StringArrayValue();

    }

    public byte byteValue( final EntityName gtag ) {

        return getNotNullField( gtag ).byteValue();

    }

    @Nullable
    public byte[] byteArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveByteArrayValue();

    }

    @NotNull
    public byte[] MandatoryPrimitiveByteArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveByteArrayValue();

    }

    @Nullable
    public Byte ByteValue( final EntityName gtag ) {

        return getNullableField( gtag ).ByteValue();

    }

    @Nullable
    public Byte[] ByteArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerByteArrayValue();

    }

    @NotNull
    public Byte[] MandatoryByteArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerByteArrayValue();

    }

    public short shortValue( final EntityName gtag ) {

        return getNotNullField( gtag ).shortValue();

    }

    @Nullable
    public short[] shortArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveShortArrayValue();

    }

    @NotNull
    public short[] MandatoryPrimitiveShortArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveShortArrayValue();

    }

    @Nullable
    public Short ShortValue( final EntityName gtag ) {

        return getNullableField( gtag ).ShortValue();

    }

    @Nullable
    public Short[] ShortArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerShortArrayValue();

    }

    @NotNull
    public Short[] MandatoryShortArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerShortArrayValue();

    }

    public int intValue( final EntityName gtag ) {

        return getNotNullField( gtag ).intValue();

    }

    @Nullable
    public int[] intArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveIntArrayValue();

    }

    @NotNull
    public int[] MandatoryPrimitiveIntArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveIntArrayValue();

    }

    @Nullable
    public Integer IntegerValue( final EntityName gtag ) {

        return getNullableField( gtag ).IntegerValue();

    }

    @Nullable
    public Integer[] IntegerArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerIntegerArrayValue();

    }

    @NotNull
    public Integer[] MandatoryIntegerArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerIntegerArrayValue();

    }

    public long longValue( final EntityName gtag ) {

        return getNotNullField( gtag ).longValue();

    }

    @Nullable
    public long[] longArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveLongArrayValue();

    }

    @NotNull
    public long[] MandatoryPrimitiveLongArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveLongArrayValue();

    }

    @Nullable
    public Long LongValue( final EntityName gtag ) {

        return getNullableField( gtag ).LongValue();

    }

    @Nullable
    public Long[] LongArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerLongArrayValue();

    }

    @NotNull
    public Long[] MandatoryLongArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerLongArrayValue();

    }

    public float floatValue( final EntityName gtag ) {

        return getNotNullField( gtag ).floatValue();

    }

    @Nullable
    public float[] floatArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveFloatArrayValue();

    }

    @NotNull
    public float[] MandatoryPrimitiveFloatArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveFloatArrayValue();

    }

    @Nullable
    public Float FloatValue( final EntityName gtag ) {

        return getNullableField( gtag ).FloatValue();

    }

    @Nullable
    public Float[] FloatArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerFloatArrayValue();

    }

    @NotNull
    public Float[] MandatoryFloatArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerFloatArrayValue();

    }

    public double doubleValue( final EntityName gtag ) {

        return getNotNullField( gtag ).doubleValue();

    }

    @Nullable
    public double[] doubleArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveDoubleArrayValue();

    }

    @NotNull
    public double[] MandatoryPrimitiveDoubleArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveDoubleArrayValue();

    }

    @Nullable
    public Double DoubleValue( final EntityName gtag ) {

        return getNullableField( gtag ).DoubleValue();

    }

    @Nullable
    public Double[] DoubleArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerDoubleArrayValue();

    }

    @NotNull
    public Double[] MandatoryDoubleArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerDoubleArrayValue();

    }

    public char charValue( final EntityName gtag ) {

        return getNotNullField( gtag ).charValue();

    }

    @Nullable
    public char[] charArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveCharArrayValue();

    }

    @NotNull
    public char[] MandatoryPrimitiveCharArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveCharArrayValue();

    }

    @Nullable
    public Character CharacterValue( final EntityName gtag ) {

        return getNullableField( gtag ).CharacterValue();

    }

    @Nullable
    public Character[] CharacterArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerCharacterArrayValue();

    }

    @NotNull
    public Character[] MandatoryCharacterArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerCharacterArrayValue();

    }

    public boolean booleanValue( final EntityName gtag ) {

        return getNotNullField( gtag ).booleanValue();

    }

    @Nullable
    public boolean[] booleanArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).PrimitiveBooleanArrayValue();

    }

    @NotNull
    public boolean[] MandatoryPrimitiveBooleanArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).PrimitiveBooleanArrayValue();

    }

    @Nullable
    public Boolean BooleanValue( final EntityName gtag ) {

        return getNullableField( gtag ).BooleanValue();

    }

    @Nullable
    public Boolean[] BooleanArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).ContainerBooleanArrayValue();

    }

    @NotNull
    public Boolean[] MandatoryBooleanArrayValue( final EntityName gtag ) {

        return getNotNullField( gtag ).ContainerBooleanArrayValue();

    }

    @Nullable
    public Number NumberValue( final EntityName gtag ) {

        return getNullableField( gtag ).NumberValue();

    }

    @Nullable
    public Number[] NumberArrayValue( final EntityName gtag ) {

        return getNullableField( gtag ).NumberArrayValue();

    }

}
