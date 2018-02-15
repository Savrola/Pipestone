/*
 * Copyright © 2017 Daniel Boulet
 * All rights reserved.
 */

package com.obtuse.util.names;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.Logger;
import com.obtuse.util.gowing.*;
import com.obtuse.util.gowing.p2a.GowingEntityReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 Represent an absolute path/name.
 <p/>Instances of this class are immutable.
 */

public class AbsolutePath extends RelativePath {

    private static final EntityTypeName ENTITY_TYPE_NAME = new EntityTypeName( AbsolutePath.class );

    private static final int VERSION = 1;

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
                final @NotNull GowingUnPacker unPacker,
                final @NotNull GowingPackedEntityBundle bundle,
                final @NotNull GowingEntityReference er
        ) {

            return new AbsolutePath( unPacker, bundle );

        }

    };

    public static final AbsolutePath ABSOLUTE_ROOT_PATH = new AbsolutePath();

    public AbsolutePath( final @NotNull SegmentName@NotNull[] segments ) {
        super( segments );
    }

    public AbsolutePath( final @NotNull Collection<SegmentName> segments ) {
        super( segments.toArray( new SegmentName[segments.size()] ) );

    }

    public AbsolutePath( final @NotNull SegmentName segmentName ) {
        super( new SegmentName[] { segmentName } );

    }

    public AbsolutePath( final @NotNull String segmentName ) {
        super( new SegmentName[] { new SegmentName( segmentName ) } );

    }

    public AbsolutePath() {
        super( new SegmentName[] { SegmentName.ROOT_SEGMENT } );

    }

    private AbsolutePath( final @NotNull GowingUnPacker unPacker, final @NotNull GowingPackedEntityBundle bundle ) {
        super( unPacker, bundle.getSuperBundle() );

    }

    @NotNull
    @Override
    public GowingPackedEntityBundle bundleThyself(
            final boolean isPackingSuper, final @NotNull GowingPacker packer
    ) {

        @SuppressWarnings("UnnecessaryLocalVariable") GowingPackedEntityBundle bundle = new GowingPackedEntityBundle(
                ENTITY_TYPE_NAME,
                VERSION,
                super.bundleThyself( true, packer ),
                packer.getPackingContext()
        );

        return bundle;

    }

    public boolean finishUnpacking( final @NotNull GowingUnPacker unPacker ) {

        if ( super.finishUnpacking( unPacker ) ) {

            Logger.logMsg( "done unpacking absolute path " + this );

            return true;

        } else {

            Logger.logMsg( "NOT done unpacking absolute path (too dangerous to invoke toString())" );

            return false;

        }

    }

    public static AbsolutePath concat( final @NotNull AbsolutePath head, final @NotNull SegmentName tail ) {

        return concat( head, new RelativePath( tail ) );

    }

    public static AbsolutePath concat( final @NotNull AbsolutePath head, final @NotNull RelativePath tail ) {

        RelativePath rval = concat( (RelativePath)head, tail );

        // rval simply must be an AbsolutePath since the combined path will have started with a ROOT_SEGMENT segment.
        if ( rval instanceof AbsolutePath ) {

            return (AbsolutePath)rval;

        } else {

            throw new HowDidWeGetHereError( "AbsolutePath concat( " + head + ", " + tail + " ) yielded a non-absolute result " + rval );

        }

    }

    public String toString() {

        return super.toString();

    }

}
