/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import org.jetbrains.annotations.NotNull;

/**
 * A yes/no {@link com.obtuse.util.ArgParser} argument.
 * <p/>Valid values are:
 * <ul>
 *     <li>y</li>
 *     <li>yes</li>
 *     <li>n</li>
 *     <li>no</li>
 * </ul>
 * in any mixture of upper and/or lower case.
 */

@SuppressWarnings("UnusedDeclaration")
public abstract class ArgYesNo extends Arg {

    protected ArgYesNo( final String keyword ) {
        super( keyword );

    }

    public final void process( @NotNull final String keyword, @NotNull final String arg ) {

        if ( "yes".equalsIgnoreCase( arg ) || "y".equalsIgnoreCase( arg ) ) {

            process( keyword, true );

        } else if ( "no".equalsIgnoreCase( arg ) || "n".equalsIgnoreCase( arg ) ) {

            process( keyword, false );

        } else {

            throw new IllegalArgumentException( "invalid \"" + keyword + "\" argument (" + arg + ") - must be YES, Y, NO or N (in any mixture of upper and lower case)" );

        }

    }

    public abstract void process( String keyword, boolean arg );

    public String toString() {

        return "ArgInt( " + getKeyword() + " )";

    }

}
