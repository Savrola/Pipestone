package com.obtuse.util.gowing.p2a.exceptions;

import com.obtuse.util.ParsingLocation;
import com.obtuse.util.gowing.p2a.GowingToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Something went wrong unpacking a packed 'lump'.
 */

public class GowingUnpackingException extends Exception {

    private GowingToken _causeToken;

    @NotNull
    private final ParsingLocation _location;

    public GowingUnpackingException( final String msg, @NotNull ParsingLocation location ) {
        this( msg, null, location, null );

    }

    public GowingUnpackingException( final String msg, @Nullable final GowingToken causeToken ) {

        this( msg, causeToken, ParsingLocation.UNKNOWN, null );

    }

    public GowingUnpackingException(
            final String msg,
            @NotNull final ParsingLocation location,
            @Nullable final Throwable cause
    ) {

        this( msg, null, location, cause );

    }

    public GowingUnpackingException(
            final String msg,
            @Nullable final GowingToken causeToken,
            @NotNull final ParsingLocation parsingLocation,
            @Nullable final Throwable cause
    ) {

        super( msg, cause );

        _causeToken = causeToken;
        if ( causeToken == null ) {

            _location = parsingLocation;

        } else {

            _location = new ParsingLocation( causeToken.getLnum(), causeToken.getOffset() );

        }

    }

    @NotNull
    public ParsingLocation getLocation() {

        return _location;

    }

    @Nullable
    public GowingToken getCauseToken() {

        return _causeToken;

    }

    public void setCauseToken( final GowingToken token ) {

        if ( _causeToken == null ) {

            _causeToken = token;

        } else {

            throw new IllegalArgumentException( "token already set in " + this, this );

        }

    }

    public String toString() {

        return "GowingUnpackingException( \"" + getMessage() + "\", causeToken = " + _causeToken + " )";

    }

}