/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import com.obtuse.util.exceptions.SyntaxErrorException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

@SuppressWarnings({ "ClassWithoutToString", "NestedAssignment", "StandardVariableNames", "UnusedDeclaration" })
public class CSVParser implements Closeable {

    private BufferedReader _input;
    private int _lnum;
    private int _pushbackChar;
    private boolean _pushedBack = false;
    private boolean _atEOF = false;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    /**
     * Create a new comma-separated-value parser.
     *
     * @param input the input stream which is to be parsed.
     */

    public CSVParser( final BufferedReader input ) {
        super();

        _input = input;
        _lnum = 1;

    }

    /**
     * Close the input stream.
     *
     * @throws java.io.IOException if the call to {@link java.io.BufferedReader#close} fails.
     */

    public void close()
            throws IOException {

        _input.close();

    }

    /**
     * Get the data format that is being used to parse dates.
     *
     * @return the date format being used to parse dates.
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    public static DateFormat getDateFormat() {

        return CSVParser.DATE_FORMAT;

    }

    /**
     * Get the current line number.
     *
     * @return the current line number.
     */

    public int getLnum() {

        return _lnum;

    }

    /**
     * Return the next (possibly pushed back) character from the input stream.
     *
     * @return the next character or -1 if we've reached the end of the input stream.
     */

    protected int nextCh() {

        if ( _pushedBack ) {

            _pushedBack = false;
            if ( _pushbackChar == -1 ) {

                _atEOF = true;

            }

            return _pushbackChar;

        }

        try {

            int ch = _input.read();
            if ( ch == -1 ) {

                _atEOF = true;

            }

            return ch;

        } catch ( IOException e ) {

            // Pretend that nothing of substance happened (which is, pretty much, true)
            return -1;

        }

    }

    /**
     * Are we there yet?
     *
     * @return true if the end of the input file has been reached, false otherwise.
     */

    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
    protected boolean atEOF() {

        return _atEOF;

    }

    /**
     * Push a character back onto the input stream.
     *
     * @param ch the character to be pushed back.
     * @throws IllegalArgumentException if there is already one character pushed back onto the stream.
     */

    protected void pushback( final int ch ) {

        if ( _pushedBack ) {

            throw new IllegalArgumentException( "double pushback attempted" );

        }

        if ( ch != -1 ) {

            _atEOF = false;

        }

        _pushbackChar = ch;
        _pushedBack = true;

    }

    protected String getField() {

        StringBuilder rval = new StringBuilder();

        while ( true ) {

            int iCh = nextCh();
            if ( iCh == (int)',' || iCh == (int)'\n' || iCh == -1 ) {

                pushback( iCh );
                return rval.toString();

            }

            char ch = (char)iCh;

            if ( ch != '\r' ) {

                rval.append( ch );

            }

        }

    }

    /**
     * Get a sequence of characters from a specified set.
     *
     * @param charSet the set of characters to retrieve.
     * @return the sequence of characters.
     */

    protected String getWord( final String charSet ) {

        if ( charSet.indexOf( ',' ) >= 0 ) {

            throw new IllegalArgumentException( "words may not contain commas" );

        }

        StringBuilder rval = new StringBuilder();
        int ch;
        while ( charSet.indexOf( ch = nextCh() ) >= 0 ) {

            rval.append( (char)ch );

        }

        pushback( ch );

        // Logger.logMsg("word is \"" + rval + "\"");
        return rval.toString();

    }

    /**
     * Parse a bitmask value.
     * Any sequence of 0's and 1's that fits in an int when treated as a base-2 value is acceptable.
     * Note that the first bit in the input is the least significant bit in the result.
     *
     * @return the parsed value.
     * @throws com.obtuse.util.exceptions.SyntaxErrorException if something is encountered that is not acceptable.
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    protected int getBitMask()
            throws SyntaxErrorException {

        int rval = 0;

        //noinspection MagicNumber
        for ( int bitNumber = 0; bitNumber < 32; bitNumber += 1 ) {

            int ch = nextCh();

            if ( ch == (int)'1' ) {

                //noinspection UnnecessaryParentheses
                rval |= ( 1 << bitNumber );

            } else //noinspection StatementWithEmptyBody
                if ( ch == (int)'0' ) {

                // just ignore the zero bits

            } else {

                pushback( ch );
                return rval;

            }

        }

        // Oops - there's only room for 32 bits in an int!

        throw new SyntaxErrorException( "more than 32 bits in bitmask on line " + _lnum );

    }

    /**
     * Parse an integer value.
     * Anything that {@link Integer#parseInt(String)} accepts is considered valid.
     *
     * @return the parsed value.
     * @throws SyntaxErrorException if something is encountered that is not accepted by {@link Integer#parseInt(String)}.
     */

    protected int getInt()
            throws SyntaxErrorException {

        String s = null;

        try {

            getWord( " \t" );     // skip past any leading spaces
            s = getWord( "-0123456789" );
            getWord( " \t" );    // ignore any trailing spaces as well
            if ( atEOF() ) {

                return 0;

            }

            int rval = Integer.parseInt( s );

            return rval;

        } catch ( NumberFormatException e ) {

            throw new SyntaxErrorException( "unable to convert \"" + s + "\" on line " + _lnum, e );

        }

    }

    /**
     * Parse a double precision value.
     * Anything that {@link Double#parseDouble(String)} accepts is considered valid.
     *
     * @return the parsed value.
     * @throws SyntaxErrorException if something is encountered that is not accepted by {@link Double#parseDouble(String)}.
     */

    protected double getDouble()
            throws SyntaxErrorException {

        String s = null;

        try {

            s = getWord( "-0123456789." );
            if ( atEOF() ) {

                return 0.0;

            }

            double rval = Double.parseDouble( s );

            return rval;

        } catch ( NumberFormatException e ) {

            throw new SyntaxErrorException( "unable to convert \"" + s + "\" on line " + _lnum, e );

        }

    }

    protected String getString() throws SyntaxErrorException {

        return getString( -1 );

    }

    /**
     * Parse a string that's enclosed in double quotes.
     * The basic C-style escape characters (\n, \r, \t, \b and \\) are treated as one would expect.
     *
     * @return the parsed string.
     * @throws SyntaxErrorException if a backslash-escaped character other than the basic C-style set is encountered.
     */

    protected String getString( int lnum )
            throws SyntaxErrorException {

        int iDelim = nextCh();
        if ( iDelim == -1 ) {

            return "";

        }

        char delim = (char)iDelim;

        if ( delim != (int)'"' ) {

            throw new SyntaxErrorException( ( lnum <= 0 ? "" : ( "line " + lnum + " has " ) ) + "missing opening quote" );

        }

        int iCh;
        StringBuilder rval = new StringBuilder();
        while ( ( iCh = nextCh() ) != delim && iCh != (int)'\n' && iCh != -1 ) {

            char ch = (char)iCh;
            if ( ch == (int)'\\' ) {

                iCh = nextCh();

                switch ( iCh ) {

                    case 'n':
                        rval.append( '\n' );
                        break;

                    case 'r':
                        rval.append( '\r' );
                        break;

                    case 't':
                        rval.append( '\t' );
                        break;

                    case 'b':
                        rval.append( '\b' );
                        break;

                    case '\\':
                        rval.append( '\\' );
                        break;

                    case '"':
                        rval.append( '"' );
                        break;

                    default:
                        throw new SyntaxErrorException( ( lnum <= 0 ? "" : ( "line " + lnum + " has " ) ) + "illegal char after backslash" );

                }

            } else {

                rval.append( ch );

            }

        }

        if ( iCh == delim ) {

            return rval.toString();

        }

        throw new SyntaxErrorException( ( lnum <= 0 ? "" : ( "line " + lnum + " has " ) ) + "missing closing string delimiter" );

    }

    /**
     * Get the next character.
     *
     * @return the next character.
     * @throws SyntaxErrorException if the next character is a comma or a newline or if the end of the input
     *                              stream has been reached.
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    protected char getChar()
            throws SyntaxErrorException {

        int ch = nextCh();

        if ( ch == (int)',' || ch == (int)'\n' || ch == -1 ) {

            throw new SyntaxErrorException( "invalid char" );

        } else {

            return (char)ch;

        }

    }

    /**
     * Get a date of the format "YYYY-MM-DD HH:MM:SS".
     * Note that the date must be enclosed in double quotes.
     *
     * @return the parsed date.
     * @throws SyntaxErrorException if a syntax error is encountered (of course).
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    protected ImmutableDate getDate()
            throws SyntaxErrorException {

        String s = getString();

        if ( atEOF() ) {

            return null;

        }

        return CSVParser.parseDate( s );

    }

    /**
     * Parse a date of the format "YYYY-MM-DD HH:MM:SS" from a string.
     *
     * @param dateStr the string to be parsed.
     * @return the parsed date.
     * @throws SyntaxErrorException if a syntax error is encountered (of course).
     */

    public static ImmutableDate parseDate( final String dateStr )
            throws SyntaxErrorException {

        try {

            return new ImmutableDate( CSVParser.DATE_FORMAT.parse( dateStr ) );

        } catch ( ParseException e ) {

            throw new SyntaxErrorException( "invalid date format", e );

        }

    }

    /**
     * Convert an integer value into a hexadecimal string.
     *
     * @param val the value to convert
     * @return the resulting string
     */

    private String fmtHex( final int val ) {

        if ( val == 0 ) {

            return "0";

        } else {

            long lval = val;
            StringBuilder rval = new StringBuilder();
            while ( lval != 0L ) {

                //noinspection MagicNumber
                rval.insert( 0, "" + "0123456789abcdef".charAt( (int)( lval & 0xfL ) ) );
                //noinspection MagicNumber
                lval >>= 4L;

            }

            return rval.toString();

        }

    }

    /**
     * Get a comma-separated array of integers that is enclosed in square brackets.
     *
     * @return the array of ints.
     * @throws SyntaxErrorException if a syntax error is encountered (of course).
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    protected int[] getIntArray()
            throws SyntaxErrorException {

        Vector<Integer> v = new Vector<>();

        int ch = nextCh();
        if ( ch != (int)'[' ) {

            throw new SyntaxErrorException( "missing opening '['" );

        }

        while ( ( ch = nextCh() ) != (int)']' ) {

            pushback( ch );
            v.add( getInt() );
            ch = nextCh();
            //noinspection StatementWithEmptyBody
            if ( ch == (int)',' ) {

                // life is wonderful

            } else if ( ch == (int)']' ) {

                pushback( ch );

            } else {

                throw new SyntaxErrorException( "bogus char after int in int array" );

            }

        }

        Integer[] iArray = v.toArray( new Integer[0] );
        int[] rval = new int[iArray.length];
        for ( int i = 0; i < iArray.length; i += 1 ) {

            rval[i] = iArray[i].intValue();

        }

        return rval;

    }

    /**
     * Get a comma-separated array of doubles that is enclosed in square brackets.
     *
     * @return the array of ints.
     * @throws SyntaxErrorException if a syntax error is encountered (of course).
     */

    @SuppressWarnings({ "UnusedDeclaration" })
    protected double[] getDoubleArray()
            throws SyntaxErrorException {

        Vector<Double> v = new Vector<>();

        int ch = nextCh();
        if ( ch != (int)'[' ) {

            throw new SyntaxErrorException( "missing opening '['" );

        }

        while ( ( ch = nextCh() ) != (int)']' ) {

            pushback( ch );
            v.add( getDouble() );
            ch = nextCh();
            //noinspection StatementWithEmptyBody
            if ( ch == (int)',' ) {

                // life is wonderful

            } else if ( ch == (int)']' ) {

                pushback( ch );

            } else {

                throw new SyntaxErrorException( "bogus char after double in double array" );

            }

        }

        Double[] dArray = v.toArray( new Double[0] );
        double[] rval = new double[dArray.length];
        for ( int i = 0; i < dArray.length; i += 1 ) {

            rval[i] = dArray[i].doubleValue();

        }

        return rval;

    }

    /**
     * Consume the next character (which must be a comma).
     *
     * @throws SyntaxErrorException if the next character is not a comma.
     */

    protected void comma()
            throws SyntaxErrorException {

        int ch = nextCh();
        if ( ch != (int)',' ) {

            if ( ch >= (int)' ' && ch <= (int)'~' ) {

                throw new SyntaxErrorException( "next character ('" + (char)ch + "') is not a comma" );

            } else {

                throw new SyntaxErrorException( "next character (0x" + fmtHex( ch ) + ") is not a comma" );

            }

        }

    }

    /**
     * Consume the next character (which must be a newline character).
     *
     * @throws SyntaxErrorException if the next character is not a newline character.
     */

    protected void endOfLine()
            throws SyntaxErrorException {

        int ch = nextCh();
        if ( ch != (int)'\n' ) {

            if ( ch >= (int)' ' && ch <= (int)'~' ) {

                throw new SyntaxErrorException( "next character ('" + (char)ch + "') is not a newline character" );

            } else {

                throw new SyntaxErrorException( "next character (0x" + fmtHex( ch ) + ") is not a newline character" );

            }

        }

        _lnum += 1;

    }

    protected void flushRemainderOfLine()
            throws SyntaxErrorException {

        int ch = nextCh();
        while ( ch != (int)'\n' ) {

            ch = nextCh();

        }

        pushback( ch );

        endOfLine();

    }

}
