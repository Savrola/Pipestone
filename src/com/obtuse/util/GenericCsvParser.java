/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.exceptions.SyntaxErrorException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class GenericCsvParser extends CSVParser {

    private final String _description;
    private final TwoDimensionalSortedMap<Integer,String,String> _parsedData = new TwoDimensionalTreeMap<>();
    private final List<String> _titles;
    private int _nextLnum = 0;

//    private class ParsedCsvLine {
//
//        private final SortedMap<Integer,String> _fields = new TreeMap<Integer,String>();
//        private int _nextFieldIx;
//
//        private ParsedCsvLine() {
//            super();
//
//            _nextFieldIx = 0;
//
//        }
//    }

    public GenericCsvParser( String fileName )
            throws FileNotFoundException, SyntaxErrorException {
        this( fileName, new BufferedReader( new FileReader( fileName ) ) );

    }

    public GenericCsvParser( String description, BufferedReader input )
            throws SyntaxErrorException {
        super( input );

        _description = description;

        _titles = parseRawLine();

    }

    public void parse()
            throws SyntaxErrorException {

        if ( _nextLnum > 0 ) {

            throw new IllegalArgumentException( "file may only be parsed once" );

        }

        _nextLnum = 1;

        while ( true ) {

            int iCh = nextCh();
            if ( iCh == -1 ) {

                return;

            }

            char ch = (char)iCh;

            pushback( ch );
            List<String> fields = parseRawLine();

            Iterator<String> titleIter = _titles.iterator();
            Iterator<String> fieldIter = fields.iterator();

            while ( titleIter.hasNext() && fieldIter.hasNext() ) {

                String nextTitle = titleIter.next();
                String nextField = fieldIter.next();

                _parsedData.put( _nextLnum, nextTitle, nextField );

            }

            _nextLnum += 1;

        }

    }


    private List<String> parseRawLine()
            throws SyntaxErrorException {

        List<String> fields = new LinkedList<>();

        while ( true ) {

            int iCh = nextCh();
            if ( iCh == '\n' || iCh == -1 ) {

                return fields;

            }

            char ch = (char)iCh;

            pushback( ch );

            String field;

            if ( ch == (int)'\"' ) {

                field = getString();

            } else {

                field = getField();

            }

            fields.add( field );

            iCh = nextCh();
//            ch = (char)iCh;
            if ( iCh == '\n' || iCh == '\r' || iCh == -1 ) {

                return fields;

            } else if ( iCh != ',' ) {

                throw new HowDidWeGetHereError( "field not terminated by a comma" );

            }

        }

    }

    /**
     * Determine if a line of data exists.
     * <p/>
     * The header line is ignored and the first line of data (i.e. the second line in the file) is considered to be line 1.
     * @param lnum the line number of interest.
     * @return true if the line of data exists.
     */

    public boolean hasLine( int lnum ) {

        return lnum > 0 && lnum < _nextLnum;

    }

    public String getString( int lnum, String title ) {

        return _parsedData.get( lnum, title );

    }

    public String getDescription() {

        return _description;

    }

    public List<String> getTitles() {

        return Collections.unmodifiableList( _titles );

    }

    public int getLineCount() {

        return _nextLnum - 1;

    }

    public static GenericCsvParser parseQuietly( String fileName ) {

        try {

            @SuppressWarnings("UnnecessaryLocalVariable")
            GenericCsvParser gcp = new GenericCsvParser( fileName );

            return gcp;

        } catch ( FileNotFoundException | SyntaxErrorException e ) {

            return null;

        }

    }

    public static void main( String[] args ) {

        BasicProgramConfigInfo.init( "Obtuse", "Utils", "GenericCsvParser", null );

        GenericCsvParser gcp;

        try {

            gcp = new GenericCsvParser( "/Users/danny/Junk/test.csv" );

        } catch ( FileNotFoundException | SyntaxErrorException e ) {

            e.printStackTrace();
            System.exit( 1 );
            return;

        }

	try {

            gcp.parse();

        } catch ( SyntaxErrorException e ) {

            e.printStackTrace();
            System.exit( 1 );
            return;

        }

        for ( int i = 1; gcp.hasLine( i ); i += 1 ) {

            Logger.logMsg( ObtuseUtil.lpad( i, 5 ) + ":  " + gcp.getString( i, "n dstbs" ) );

        }

    }

}