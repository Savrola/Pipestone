/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

/**
 * A long {@link ArgParser} argument.
 */

@SuppressWarnings("UnusedDeclaration")
public abstract class ArgLong extends Arg {

    protected ArgLong( String keyword ) {
        super( keyword );

    }

    public final void process( String keyword, String arg ) {

        try {

            process( keyword, Long.parseLong( arg ) );

        } catch ( NumberFormatException e ) {

            throw new IllegalArgumentException( "invalid \"" + keyword + "\" argument (" + arg + ") - must be a long", e );

        }

    }

    public abstract void process( String keyword, long arg );

    public String toString() {

        return "ArgLong( " + getKeyword() + " )";

    }

}
