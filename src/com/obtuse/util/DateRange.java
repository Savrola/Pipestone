/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A single date range from a starting date through to an ending date inclusive.
 * <p/>
 * Intended for use with the {@link Range} and {@link Ranges} classes.
 */

@SuppressWarnings("UnusedDeclaration")
public class DateRange extends Range<Date> {

    private static final SimpleDateFormat YYYYMMDD = new SimpleDateFormat( "yyyy-MM-dd" );

    public DateRange( final Date startDate, final Date endDate ) {
        super( startDate, endDate, JulianDate.toJulian( startDate ), JulianDate.toJulian( endDate ) );

    }

    public int hashCode() {

        return getStartValue().hashCode() ^ getEndValue().hashCode();

    }

    public boolean equals( final Object rhs ) {

        //noinspection OverlyStrongTypeCast
        return rhs instanceof DateRange &&
               ( (DateRange)rhs ).getStartValue().equals( getStartValue() ) &&
               ( (DateRange)rhs ).getEndValue().equals( getEndValue() );

    }

    @SuppressWarnings( { "RefusedBequest" } )
    public String format( final Date value ) {

        return DateRange.YYYYMMDD.format( value );

    }

}
