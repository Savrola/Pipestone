package com.obtuse.util.gowing.p2a;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

import com.obtuse.util.gowing.GowingMetaDataHandler;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;

/**
 %%% Something clever goes here.
 */

public interface GowingTokenizer extends Closeable {

    void putBackToken( StdGowingTokenizer.GowingToken2 token );

    @NotNull
    StdGowingTokenizer.GowingToken2 getNextToken( boolean identifierAllowed, @NotNull StdGowingTokenizer.TokenType requiredType )
	    throws IOException, GowingUnpackingException;

    @NotNull
    StdGowingTokenizer.GowingToken2 getNextToken( boolean identifierAllowed )
	    throws IOException, GowingUnpackingException;

    /**
     Register a metadata handler.
     @param handler the metadata handler. The type-appropriate metadata handler method will be called
     when a metadata line is encountered while parsing an input file.
     <p>Notes:</p>
     <ul>
     <li>
     the input file is still being parsed when the metadata handler method is called (in other words, be careful to not
     mess up the state of the unpacking process from within a metadata handler).
     Note that the metadata handler method is called immediately after the metadata line is encountered which means that
     </li>
     <li>Every metadata handler that has been registered when a metadata line is encountered will be invoked for the metadata
     line so a metadata handler needs to be prepared to ignore metadata elements that it doesn't recognize.</li>
     </ul>
     */

    void registerMetaDataHandler( @NotNull GowingMetaDataHandler handler );

}
