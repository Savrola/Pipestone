/*
 * Copyright © 2018 Daniel Boulet
 * All rights reserved.
 */

package com.obtuse.util.kv;

import com.obtuse.util.gowing.*;
import com.obtuse.util.gowing.p2a.GowingEntityReference;
import com.obtuse.util.gowing.p2a.GowingUnPackerParsingException;
import com.obtuse.util.gowing.p2a.holders.GowingPackableCollection;
import com.obtuse.util.gowing.p2a.holders.GowingPackableEntityHolder;
import com.obtuse.util.gowing.p2a.holders.GowingStringHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Describe a keyword.
 <p>Instances of this class are immutable.</p>
 */

public class ObtuseKeywordInfo extends GowingAbstractPackableEntity implements Comparable<ObtuseKeywordInfo> {

    private static final EntityTypeName ENTITY_TYPE_NAME = new EntityTypeName( ObtuseKeywordInfo.class );
    private static final int VERSION = 1;

    private static final EntityName KEYWORD_STRING = new EntityName( "_ks" );

    public static final GowingEntityFactory FACTORY = new GowingEntityFactory( ENTITY_TYPE_NAME ) {

        @Override
        public int getOldestSupportedVersion() {

            return VERSION;
        }

        @Override
        public int getNewestSupportedVersion() {

            return VERSION;
        }

        @SuppressWarnings("RedundantThrows")
        @NotNull
        @Override
        public GowingPackable createEntity(
                @NotNull final GowingUnPacker unPacker, @NotNull final GowingPackedEntityBundle bundle, final GowingEntityReference er
        )
                throws GowingUnPackerParsingException {

            return new ObtuseKeywordInfo( unPacker, bundle );

        }

    };

    /**
     Describes the form of valid keywords.
     <p>Keywords must:
     <ol>
     <li>start and end with an uppercase letter (A-Z)</li>
     <li>contain only uppercase letters and underscores</li>
     <li>not contain more than one underscore in a row ({@code "A_B_C"} is ok but {code "A__B_C"} is not)</li>
     </ol>
     </p>
     */
    public static Pattern VALID_KEYWORD_PATTERN = Pattern.compile( "[A-Z][A-Z_]*[A-Z]" );

    private final String _keywordString;

    /**
     Create a clone of an existing keyword description.
     */

    public ObtuseKeywordInfo( @NotNull final ObtuseKeywordInfo rValue ) {
        this( rValue.getKeywordName() );

    }

    /**
     Create a keyword description.

     @param keywordString the keyword's string name.
     @throws IllegalArgumentException if the keyword is not matched by {@link #VALID_KEYWORD_PATTERN}.
     */

    public ObtuseKeywordInfo( @NotNull final String keywordString ) {
        super( new GowingNameMarkerThing() );

        Matcher m = VALID_KEYWORD_PATTERN.matcher( keywordString );

        if ( m.matches() ) {

            _keywordString = keywordString;

        } else {

            throw new IllegalArgumentException( "ObtuseKeywordInfo:  invalid keyword \"" + keywordString + "\"" );

        }

    }

    public ObtuseKeywordInfo(
    @SuppressWarnings("unused") @NotNull final GowingUnPacker unPacker,
    @NotNull final GowingPackedEntityBundle bundle
    ) {

        super( unPacker, bundle.getSuperBundle() );

        _keywordString = bundle.getNotNullField( KEYWORD_STRING ).StringValue();

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

        bundle.addHolder( new GowingStringHolder( KEYWORD_STRING, _keywordString, true ) );

        return bundle;

    }

    @Override
    public boolean finishUnpacking( @NotNull final GowingUnPacker unPacker ) {

        return true;

    }

    /**
     Get this keyword in {@link String} form.

     @return this keyword in {@link String} form.
     */

    public String getKeywordName() {

        return _keywordString;

    }

    /**
     Get the proper form of a reference to this keyword.

     @return returns {@code "$(" + getKeywordName() + ")"}
     */

    public String getKeywordReferenceString() {

        return "$(" + getKeywordName() + ")";

    }

    @Override
    public String toString() {

        return getKeywordName();

    }

    @Override
    public int compareTo( @NotNull final ObtuseKeywordInfo rhs ) {

        return getKeywordName().compareTo( rhs.getKeywordName() );

    }

    @Override
    public int hashCode() {

        return getKeywordName().hashCode();

    }

    @Override
    public boolean equals( @Nullable final Object rhs ) {

        return rhs instanceof ObtuseKeywordInfo && getKeywordName().equals( ( (ObtuseKeywordInfo)rhs ).getKeywordName() );

    }

}