/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import com.obtuse.db.PostgresConnection;
import com.obtuse.exceptions.HowDidWeGetHereError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

/**
 * A sometimes motley collectino of utility methods which I have found useful.
 */

@SuppressWarnings({ "UnusedDeclaration", "SameParameterValue" })
public class ObtuseUtil {

    private static final Pattern HEX_STRING_PATTERN = Pattern.compile( "([0-9a-f][0-9a-f])*" );

    /**
     Detect if we are running within the Java debugger.
     <p/>Be very very careful! Code that behaves differently in a debugger is, obviously, difficult to debug!!!
     <p/>This trick found at {@code http://www.rgagnon.com/javadetails/java-detect-if-running-in-debug-mode.html}
     @return {@code true} if the current JVM is under the control of the Java debugger; {@code false} otherwise.
     */

    public static boolean inJavaDebugger() {

	boolean isDebug =
		java.lang.management.ManagementFactory.getRuntimeMXBean().
			getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

	return isDebug;

    }

    @Nullable
    public static String extractKeywordValueSemiColon( String url, String keyword ) {

        String wrappedURL = ";" + url;
        String wrappedKeyword = ";" + keyword + "=";
        int keywordOffset = wrappedURL.indexOf( wrappedKeyword );
        if ( keywordOffset < 0 ) {

            Logger.logErr( "unable to find keyword \"" + keyword + "\" in URL " + url );

            return null;

        }

        int valueOffset = keywordOffset + wrappedKeyword.length();
        String valueInURL = wrappedURL.substring( valueOffset );
        int endValueOffset = valueInURL.indexOf( ";" );
        if ( endValueOffset < 0 ) {

            Logger.logErr( "unable to find end of value for keyword \"" + keyword + "\" in URL " + url );

            return null;

        }

        return valueInURL.substring( 0, endValueOffset + 1 );

    }

    /**
     Generate a hexadecimal string representing a specified long value and then strip off the unnecessary leading zeros.
     If the specified long value was equal to zero then the return string will contain the single character <tt>0</tt>.
     @param value the long value to be converted to a minimal hex string.
     @return the converted long value.
     */

    @NotNull
    public static String getMinimalHexValue( long value ) {

        String original = hexvalue( value );
        int off = 0;
        char[] originalChars = original.toCharArray();
        while ( off < 15 && originalChars[off] == '0' ) {
            off += 1;
        }

        return original.substring( off );

    }

    /**
     * Decodes the percent encoding scheme. <br/> For example: "an+example%20string" -> "an example string"
     *
     * @param str the string to be decoded.
     *
     * @return the decoded string.
     */

    public static String decodePercent( String str ) {
        try {
            StringBuilder sb = new StringBuilder();
            @SuppressWarnings("TooBroadScope") int i = 0;
            //noinspection ForLoopWithMissingComponent
            for ( ; i < str.length(); i++ ) {
                char c = str.charAt( i );
                switch ( c ) {
                    case '+':
                        sb.append( ' ' );
                        break;
                    case '%':
                        //noinspection MagicNumber
                        sb.append( (char)Integer.parseInt( str.substring( i + 1, i + 3 ), 16 ) );
                        i += 2;
                        break;
                    default:
                        sb.append( c );
                        break;
                }
            }
            return new String( sb.toString().getBytes() );
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException( "ERROR: Bad percent-encoding.", e );
        }
    }

    /**
     A derivative of the {@link Hashtable} which whose instances start out mutable but can be made immutable upon request (there is no
     mechanism provided to make an immutable instance mutable again).
     <p/>This class is probably not perfectly immutable as it is a fair bit simpler than the unmodifiable ones implemented in
     {@link java.util.Collections}. It is, I (Danny) believe, better than not having an immutable hashtable class at all. 2017-01-07
     @param <K> The type of the keys for the hash table.
     @param <V> The type of the values in the hash table.
     */

    private static class UnmodifiableHashtable<K,V> extends Hashtable<K,V> {

        private boolean _readonly;
        private final Hashtable<? extends K,? extends V> _ht;

        private UnmodifiableHashtable( Hashtable<? extends K,? extends V> ht ) {

            super( ht );
            _ht = ht;
            _readonly = false;
        }

        private void makeReadonly() {

            _readonly = true;

        }

        public void clear() {

            if ( _readonly ) {

                throw new UnsupportedOperationException( "attempt to modify an unmodifiable Dictionary/Hashtable" );

            } else {

                super.clear();

            }

        }

        @NotNull
        public Set<Map.Entry<K,V>> entrySet() {

            if ( _readonly ) {

                //noinspection unchecked,rawtypes

                return (Set)Collections.unmodifiableSet( _ht.entrySet() );

            } else {

                return super.entrySet();

            }

        }

        @NotNull
        public Set<K> keySet() {

            if ( _readonly ) {

                return Collections.unmodifiableSet( _ht.keySet() );

            } else {

                return super.keySet();

            }

        }

        public V put( K key, V value ) {

            if ( _readonly ) {

                throw new UnsupportedOperationException( "attempt to modify an unmodifiable Dictionary/Hashtable" );

            } else {

                return super.put( key, value );

            }

        }

        public void putAll( Map<? extends K,? extends V> t ) {

            if ( _readonly ) {

                throw new UnsupportedOperationException( "attempt to modify an unmodifiable Dictionary/Hashtable" );

            } else {

                super.putAll( t );

            }

        }

        public void rehash() {

            //noinspection StatementWithEmptyBody
            if ( _readonly ) {

                // Been there, done that.

            } else {

                super.rehash();

            }

        }

        public V remove( Object key ) {

            if ( _readonly ) {

                throw new UnsupportedOperationException( "attempt to modify an unmodifiable Dictionary/Hashtable" );

            } else {

                return super.remove( key );

            }

        }

        @NotNull
        public Collection<V> values() {

            if ( _readonly ) {

                return Collections.unmodifiableCollection( _ht.values() );

            } else {

                return super.values();

            }

        }

    }

    /**
     This is a class of utility methods - no point letting anyone instantiate instances.
     */

    private ObtuseUtil() {

        super();
    }

    /**
     * Turn a serializable object into a byte array. No muss, no fuss, no exceptions!
     *
     * @param thing                  the object which is to be serialized.
     * @param printStackTraceOnError true if a stack trace should be printed if anything goes wrong.
     * @return the serialized form of <tt>thing</tt> or null if serialization fails.
     */

    @SuppressWarnings({ "SameParameterValue" })
    @Nullable
    public static byte[] getSerializedVersion( Serializable thing, boolean printStackTraceOnError ) {

        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        try {

            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream( bos );
            oos.writeObject( thing );
            oos.flush();

            return bos.toByteArray();

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return null;

        } finally {

            ObtuseUtil.closeQuietly( oos );
            ObtuseUtil.closeQuietly( bos );

        }

    }

    /**
     * Write a serializable object to a file. No muss, no fuss, no exceptions!
     *
     * @param thing the object which is to be serialized and written to the file.
     * @param outputFile the file that the serialized object is to be written to.
     * @param printStackTraceOnError true if a stack trace is to be printed if anything goes wrong.
     * @return true if it worked; false otherwise.
     */

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public static boolean writeSerializableObjectToFile( Serializable thing, File outputFile, boolean printStackTraceOnError ) {

        try {

            ObtuseUtil.writeSerializableObjectToFile( thing, outputFile );

            return true;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                e.printStackTrace();

            }

            return false;

        }

    }

    /**
     * Write a serializable object to a file.
     * <p/>
     * If it worked then nothing is returned. If it failed then an exception is thrown.
     *
     * @param thing the object which is to be serialized and written to the file.
     * @param outputFile the file that the serialized object is to be written to.
     * @throws java.io.IOException if something goes wrong. The most likely reasons are (probably)
     * that it was not possible to create the output file or some part of <tt>thing</tt> is not serializable.
     */

    public static void writeSerializableObjectToFile( Serializable thing, File outputFile )
            throws IOException {

        ObjectOutputStream oos = null;

        try {

            oos = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( outputFile ) ) );
            oos.writeObject( thing );
            oos.flush();

        } finally {

            ObtuseUtil.closeQuietly( oos );

        }

    }

    /**
     * Turn the contents of a file back into a serializable thing. The first serialized object from the file is read
     * and de-serialized.
     *
     * @param inputFile the file that the serialized object is to be read from.
     * @return the de-serialized object or <tt>null</tt> if something goes wrong.
     * @throws java.io.IOException    if an I/O error occurs opening or reading from the file or a deserialization
     *                                error occurs (see {@link java.io.ObjectInputStream#readObject()} for details on what can
     *                                go wrong).
     * @throws ClassNotFoundException if the class of a serialized object in the file cannot be found.
     */

    @Nullable
    public static Serializable recoverSerializedVersion( File inputFile )
            throws IOException, ClassNotFoundException {

        return ObtuseUtil.recoverSerializedVersion( new BufferedInputStream( new FileInputStream( inputFile ) ) );

    }

    /**
     * Turn the contents of a file back into a serializable thing. The first serialized object from the file is read
     * and de-serialized.
     *
     * @param inputFile the file that the serialized object is to be read from.
     * @param printStackTraceOnError true if a stack trace is to be printed if anything goes wrong.
     * @return the de-serialized object. If something goes wrong then <tt>null</tt> is returned. Note that it is possible to
     * serialize a null pointer which means that de-serializing can legitimately yield a null pointer.
     * Use {@link #recoverSerializedVersion(File)} if this is a concern as it will throw an exception of the de-serialization fails.
     */

    public static Serializable recoverSerializedVersion( File inputFile, boolean printStackTraceOnError ) {

        try {

            return ObtuseUtil.recoverSerializedVersion( new BufferedInputStream( new FileInputStream( inputFile ) ) );

        } catch ( ClassNotFoundException | IOException e ) {

            if ( printStackTraceOnError ) {

                e.printStackTrace();

            }

            return null;

        }

    }

    /**
     * Turn a byte array back into a serializable thing. Throws an exception if something goes wrong.
     *
     * @param sv the serialized form of the object.
     * @return the de-serialized object or null if de-serialization fails.
     * @throws java.io.IOException    if an I/O error occurs reading from the {@link java.io.InputStream} or a deserialization
     *                                error occurs (see {@link java.io.ObjectInputStream#readObject()} for details on what can
     *                                go wrong).
     * @throws ClassNotFoundException if the class of a serialized object in the {@link java.io.InputStream} cannot be found.
     * <p/>
     * This method was marked as deprecated. I don't know why so I have disabled the deprecation marking.
     * Danny 2017-01-07
     * at-deprecated Use {@link #recoverSerializedVersion(java.io.File)} or {@link #recoverSerializedVersion(java.io.InputStream)} instead.
     */

    public static Serializable recoverSerializedVersion( byte[] sv )
            throws ClassNotFoundException, IOException {

        ByteArrayInputStream bis = null;

        try {

            bis = new ByteArrayInputStream( sv );

            return ObtuseUtil.recoverSerializedVersion( bis );

        } finally {

            ObtuseUtil.closeQuietly( bis );

        }

    }

    /**
     * Turn an {@link java.io.InputStream} back into a serializable thing. The next serialized object from the stream is read
     * and de-serialized. The stream is closed when this operation completes.
     *
     * @param is the input stream that the serialized object is to be read from.
     * @return the de-serialized object or null if something goes wrong.
     * @throws java.io.IOException    if an I/O error occurs reading from the {@link java.io.InputStream} or a deserialization
     *                                error occurs (see {@link java.io.ObjectInputStream#readObject()} for details on what can
     *                                go wrong).
     * @throws ClassNotFoundException if the class of a serialized object in the {@link java.io.InputStream} cannot be found.
     */

    private static Serializable recoverSerializedVersion( InputStream is )
            throws ClassNotFoundException, IOException {

        ObjectInputStream ois = null;
        try {

            ois = new ObjectInputStream( is );

            return (Serializable)ois.readObject();

        } finally {

            ObtuseUtil.closeQuietly( ois );

        }

    }

    /**
     * Turn a byte array back into a serializable thing. Prints a stack trace and returns null if something goes wrong.
     *
     * @param sv the serialized form of the object.
     * @param printStackTraceOnError true if a stack trace should be printed if anything goes wrong.
     * @return the de-serialized object. If something goes wrong then <tt>null</tt> is returned. Note that it is possible to
     * serialize a null pointer which means that de-serializing can legitimately yield a null pointer.
     * Use {@link #recoverSerializedVersion(byte[])} if this is a concern as it will throw an exception of the de-serialization fails.
     */

    public static Serializable recoverSerializedVersion(
            byte[] sv,
            boolean printStackTraceOnError
    ) {

        ByteArrayInputStream bis = new ByteArrayInputStream( sv );
        try {

            return ObtuseUtil.recoverSerializedVersion( bis, printStackTraceOnError );

        } finally {

            ObtuseUtil.closeQuietly( bis );

        }

    }

    /**
     * Turn an {@link java.io.InputStream} back into a serializable thing. The next serialized object from the stream is read
     * and de-serialized. The stream is closed when this call completes.
     *
     * @param is the input stream that the serialized object is to be read from.
     * @param printStackTraceOnError true if a stack trace should be printed if anything goes wrong.
     * @return the de-serialized object. If something goes wrong then <tt>null</tt> is returned. Note that it is possible to
     * serialize a null pointer which means that de-serializing can legitimately yield a null pointer.
     * Use {@link #recoverSerializedVersion(InputStream)} if this is a concern as it will throw an exception of the de-serialization fails.
     */

    public static Serializable recoverSerializedVersion( InputStream is, boolean printStackTraceOnError ) {

        ObjectInputStream ois = null;

        try {

            ois = new ObjectInputStream( is );
            @SuppressWarnings("UnnecessaryLocalVariable")
            Serializable thing = (Serializable)ois.readObject();

            return thing;

        } catch ( Exception e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return null;

        } finally {

            ObtuseUtil.closeQuietly( ois );
            ObtuseUtil.closeQuietly( is );

        }

    }

    /**
     * Read the contents of a file into a byte array without needing to worry about exceptions.
     *
     * @param fname                  the name of the file to be read.
     * @param maxLength              the maximum number of bytes to read (if the file is longer than this then the
     *                               excess data is silently not returned).
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return a byte array containing the data read from the file or null if the file could not be read for any reason.
     *         A zero-length byte array is returned if the file exists but contains no data.
     */

    public static byte[] readEntireFile( String fname, int maxLength, boolean printStackTraceOnError ) {

        if ( fname == null ) {

            return null;

        }

        return ObtuseUtil.readEntireFile( new File( fname ), maxLength, printStackTraceOnError );

    }

    /**
     * Read the contents of a file into a byte array without needing to worry about exceptions.
     *
     * @param file                   the file to be read.
     * @param maxLength              the maximum number of bytes to read (if the file is longer than this then the
     *                               excess data is silently not returned).
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return a byte array containing the data read from the file or null if the file could not be read for any reason.
     *         A zero-length byte array is returned if the file exists but contains no data.
     */

    public static byte[] readEntireFile( File file, int maxLength, boolean printStackTraceOnError ) {

        if ( file == null ) {

            return null;

        }

        FileInputStream fs = null;
        try {

            fs = new FileInputStream( file );

            //noinspection UnnecessaryLocalVariable
            byte[] contents = ObtuseUtil.readEntireStream( fs, maxLength, printStackTraceOnError );

            return contents;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return null;

/*
            e.printStackTrace();
            System.err.println( "ObtuseUtil:readEntireFile - unable to read contents of \""
                                + fname + "\" - bye!" );
            System.exit( 1 );
            return null;    // keep the compiler happy.
*/

        } finally {

            ObtuseUtil.closeQuietly( fs );

        }

    }

    /**
     * Read the contents of an {@link java.io.InputStream} into a byte array and return the byte array without needing to worry
     * about exceptions.
     *
     * @param is                     the {@link java.io.InputStream} to read the data from.
     * @param maxLength              the maximum number of bytes to read (if the stream's contents are longer than this
     *                               then the excess data is left in the stream).
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return a byte array containing the data read from the file or null if the file could not be read for any reason.
     *         A zero-length byte array is returned if the stream contains no data.
     */

    public static byte[] readEntireStream( InputStream is, int maxLength, boolean printStackTraceOnError ) {

        if ( is == null ) {

            return null;

        }

        try {

            byte[] tmp = new byte[maxLength];
            int actualLen = is.read( tmp );
            if ( actualLen <= 0 ) {

                return new byte[0];

            }

            byte[] contents = new byte[actualLen];
            System.arraycopy( tmp, 0, contents, 0, actualLen );

            return contents;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return null;    // keep the compiler happy.

        }

    }

    /**
     * Write the contents of a byte array into a file without needing to worry about exceptions.
     *
     * @param bytes                  the byte array to be written.
     * @param fname                  the name of the file to be written.
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return true if the operation succeeded and false otherwise.
     */

    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
    public static boolean writeBytesToFile( byte[] bytes, String fname, boolean printStackTraceOnError ) {

        return ObtuseUtil.writeBytesToFile( bytes, new File( fname ), printStackTraceOnError );

    }

    /**
     * Write the contents of a byte array into a file without needing to worry about exceptions.
     *
     * @param bytes                  the byte array to be written.
     * @param file                   the file to be written.
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return true if the operation succeeded and false otherwise.
     */

    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
    public static boolean writeBytesToFile( byte[] bytes, File file, boolean printStackTraceOnError ) {

        FileOutputStream fs = null;
        try {

            fs = new FileOutputStream( file );

            //noinspection UnnecessaryLocalVariable
            boolean rval = ObtuseUtil.writeBytesToStream( bytes, fs, printStackTraceOnError );

            return rval;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return false;

        } finally {

            ObtuseUtil.closeQuietly( fs );

        }

    }

    /**
     * Append the contents of a byte array into a file without needing to worry about exceptions.
     *
     * @param bytes                  the byte array to be written.
     * @param file                   the file to be written.
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return true if the operation succeeded and false otherwise.
     */

    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
    public static boolean appendBytesToFile( byte[] bytes, File file, boolean printStackTraceOnError ) {

        FileOutputStream fs = null;
        try {

            fs = new FileOutputStream( file, true );

            //noinspection UnnecessaryLocalVariable
            boolean rval = ObtuseUtil.writeBytesToStream( bytes, fs, printStackTraceOnError );

            return rval;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return false;

        } finally {

            ObtuseUtil.closeQuietly( fs );

        }

    }

    /**
     * Write the contents of a byte array to an {@link java.io.OutputStream} without needing to worry about exceptions.
     *
     * @param bytes                  the byte array to be written to the stream.
     * @param os                     the {@link java.io.OutputStream} to write the byte array to.
     * @param printStackTraceOnError specifies whether or not a stack trace is to be printed if an i/o error occurs.
     * @return true if it worked, false otherwise.
     */

    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
    public static boolean writeBytesToStream( byte[] bytes, OutputStream os, boolean printStackTraceOnError ) {

        try {

            os.write( bytes );

            return true;

        } catch ( IOException e ) {

            if ( printStackTraceOnError ) {

                //noinspection CallToPrintStackTrace
                e.printStackTrace();

            }

            return false;

        }

    }

    /**
     * Get the size of the serialized version of an object.
     *
     * @param thing the object which is to be serialized so that the length of its serialized form can be returned (the
     *              serialized form of the object is discarded - it is often much more sensible to just serialize the
     *              object using {@link #getSerializedVersion} and then just get the length of the returned byte
     *              array).
     * @return the length of the serialized form of the object.
     */

    public static int getSerializedSize( Serializable thing ) {

        byte[] sv = ObtuseUtil.getSerializedVersion( thing, false );
        if ( sv == null ) {

            return 0;

        } else {

            return sv.length;

        }

    }

    /**
     * Validate the number and type of arguments passed to an XML-RPC routine.
     *
     * @param methodName the method which is to be called.
     * @param actual     the actual parameters which are to be passed to the method.
     * @param expected   a description of the parameter types which are expected.
     * @return null if arguments are valid and an appropriate error message otherwise.
     */

    @SuppressWarnings({ "RawUseOfParameterizedType", "CollectionDeclaredAsConcreteClass" })
    public static String validateArgs(
            String methodName,
            @SuppressWarnings("rawtypes") Vector actual,
            @SuppressWarnings("rawtypes") Class[] expected
    ) {

        if ( actual.size() != expected.length ) {

            return methodName + " requires " + expected.length + " arguments but caller provided " +
                   actual.size();

        }

        for ( int i = 0; i < expected.length; i += 1 ) {

            Object arg = actual.elementAt( i );
            if ( arg == null ) {

                return "XMLRPC is not supposed to support null values but argument "
                       + ( i + 1 ) + " (one-origin) to " + methodName +
                       " is null (impressive! WRONG but still impressive!)";

            }

            if ( !arg.getClass().equals( expected[i] ) ) {

                return "argument " + ( i + 1 ) + " (one-origin) parameter to " + methodName +
                       " is of the wrong type (expected "
                       + expected[i] + ", received " + arg.getClass() + ")";

            }

        }

        return null;

    }

    private static DecimalFormat s_readable = new DecimalFormat( "###,###,###,###,###,###,##0" );

    public static String readable( long value ) {

        return ObtuseUtil.s_readable.format( value );

    }

    private static String readable( String sValue ) {

        int offset = sValue.indexOf( '.' );
        if ( offset < 0 ) {

            offset = sValue.length();

        }

        offset -= 3;
        String rval = sValue;
        int lastPlace = rval.startsWith( "-" ) ? 1 : 0;

        while ( offset > lastPlace ) {

            rval = rval.substring( 0, offset ) + ',' + rval.substring( offset );
            offset -= 3;

        }

        return rval;

    }

    public static String lpadReadable( long value, int w ) {

        return ObtuseUtil.lpad( ObtuseUtil.readable( value ), w );

    }

    public static String lpadReadable( float value, int width, int digits ) {

        return ObtuseUtil.lpad( ObtuseUtil.readable( ObtuseUtil.lpad( value, 0, digits ) ), width );

    }

    public static String lpadReadable( double value, int width, int digits ) {

        return ObtuseUtil.lpad( ObtuseUtil.readable( ObtuseUtil.lpad( value, 0, digits ) ), width );

    }

    public static String rpadReadable( long value, int w ) {

        return ObtuseUtil.rpad( ObtuseUtil.readable( value ), w );

    }

    public static String rpadReadable( float value, int width, int digits ) {

        return ObtuseUtil.rpad( ObtuseUtil.readable( ObtuseUtil.lpad( value, 0, digits ) ), width );

    }

    public static String rpadReadable( double value, int width, int digits ) {

        return ObtuseUtil.rpad( ObtuseUtil.readable( ObtuseUtil.lpad( value, 0, digits ) ), width );

    }

    public static String lpadReadable0( float value, int width, int digits ) {

        return ObtuseUtil.lpad( ObtuseUtil.readable( ObtuseUtil.lpad0( value, 0, digits ) ), width );

    }

    public static String lpadReadable0( double value, int width, int digits ) {

        return ObtuseUtil.lpad( ObtuseUtil.readable( ObtuseUtil.lpad0( value, 0, digits ) ), width );

    }

    public static String rpadReadable0( float value, int width, int digits ) {

        return ObtuseUtil.rpad( ObtuseUtil.readable( ObtuseUtil.lpad0( value, 0, digits ) ), width );

    }

    public static String rpadReadable0( double value, int width, int digits ) {

        return ObtuseUtil.rpad( ObtuseUtil.readable( ObtuseUtil.lpad0( value, 0, digits ) ), width );

    }

    public static String lpad( float value, int width, int digits ) {

        return ObtuseUtil.lpad( (double) value, width, digits );

    }

    private static DecimalFormat[] s_cachedFormats = new DecimalFormat[1];

    public static String lpad( double di, int w, int v ) {

        if ( Double.isNaN( di ) ) {

            return ObtuseUtil.lpad( "NaN", w );

        }

        if ( Double.isInfinite( di ) ) {

            if ( di < 0 ) {

                return ObtuseUtil.lpad( "-Inf", w );

            } else {

                return ObtuseUtil.lpad( "+Inf", w );

            }

        }

        if ( v >= ObtuseUtil.s_cachedFormats.length ) {

            DecimalFormat[] tmp = new DecimalFormat[v + 1];
            System.arraycopy( ObtuseUtil.s_cachedFormats, 0, tmp, 0, ObtuseUtil.s_cachedFormats.length );
            ObtuseUtil.s_cachedFormats = tmp;

        }

        if ( ObtuseUtil.s_cachedFormats[v] == null ) {

            StringBuilder formatBuilder = new StringBuilder( "0." );
            for ( int i = 0; i < v; i += 1 ) {

                formatBuilder.append( '#' );

            }

	    String format = formatBuilder.toString();
	    ObtuseUtil.s_cachedFormats[v] = new DecimalFormat( format );

        }

        return ObtuseUtil.lpad( ObtuseUtil.s_cachedFormats[ v ].format( di ), w );

    }

    private static DecimalFormat[] s_cachedZeroFormats = new DecimalFormat[1];

    public static String lpad0( double di, int w, int v ) {

        if ( Double.isNaN( di ) ) {

            return ObtuseUtil.lpad( "NaN", w );

        }

        if ( v >= ObtuseUtil.s_cachedZeroFormats.length ) {

            DecimalFormat[] tmp = new DecimalFormat[v + 1];
            System.arraycopy( ObtuseUtil.s_cachedZeroFormats, 0, tmp, 0, ObtuseUtil.s_cachedZeroFormats.length );
            ObtuseUtil.s_cachedZeroFormats = tmp;

        }

        if ( ObtuseUtil.s_cachedZeroFormats[v] == null ) {

            StringBuilder formatBuilder = new StringBuilder( "0." );
            for ( int i = 0; i < v; i += 1 ) {

                formatBuilder.append( "0" );

            }

	    String format = formatBuilder.toString();
	    ObtuseUtil.s_cachedZeroFormats[v] = new DecimalFormat( format );

        }

        return ObtuseUtil.lpad( ObtuseUtil.s_cachedZeroFormats[ v ].format( di ), w );

    }

//    public static String lpad( double value, int width, int digits ) {
//
//        if ( value < 0.0 ) {
//
//            return lpad( "-" + lpad( -value, 0, digits ), width );
//
//        }
//
//        long mult = 1L;
//        for ( int i = 0; i < digits; i += 1 ) {
//
//            //noinspection MagicNumber
//            mult *= 10L;
//
//        }
//
//        long lv = Math.round( value * (double)mult );
////        if ( lv == 0 ) {
////
////            return lpad( rpad( "0.", d + 2, '0' ), w );
////
////        }
//        String rv = "" + lv;
//        while ( rv.length() < digits + 1 ) {
//
//            rv = "0" + rv;
//
//        }
//
//        rv = rv.substring( 0, rv.length() - digits ) + '.' + rv.substring( rv.length() - digits );
//
//        return lpad( rv, width );
//
//    }

    /**
     * Pad a string on the left to a specified width using a specified padding character.
     *
     * @param s the string to be padded.
     * @param w the width (i.e. length) of the padded string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @param p the character to be used to pad the string on the left if it is shorter than <tt>w</tt>.
     * @return the padded string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String lpad( String s, int w, char p ) {

        String str = s == null ? "null" : s;
        return ObtuseUtil.generatePaddingString( w, p, str ) + str;

    }

    public static String generatePaddingString( int w, char p, String str ) {

        int padding = w - str.length();
        String padString;
        if ( padding > 0 ) {

            char[] padArray = new char[padding];
            for ( int i = 0; i < padding; i += 1 ) {

                padArray[i] = p;

            }

            padString = new String( padArray );

        } else {

            padString = "";

        }

        return padString;

    }

    /**
     * Pad a string on the left and on the right to a specified width using blanks as the padding character.
     * <p/>What happens conceptually is that while the string is narrower than the requested width,
     * a single space character is added on an alternating basis on the left of the string or on the right of the string.
     * For example, if the input string is two characters long and the requested width is five characters then the resulting
     * string will start with two space characters followed by the two characters of the input string followed by one space character.
     * <p/>
     * Note that a call to this method using <tt>center( s, w )</tt> is equivalent to a call to {@link #center(String, int, char)}
     * using <tt>center( s, w, '&nbsp;' )</tt>.
     *
     * @param s the string to be centered by appropriate padding on the left and the right.
     * @param w the width (i.e. length) of the centered string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @return the centered string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String center( String s, int w ) {

	return center( s, w, ' ' );

    }

    /**
     * Pad a string on the left and on the right to a specified width using a specified padding character.
     * <p/>What happens conceptually is that while the string is narrower than the requested width,
     * a padding character is added on an alternating basis on the left of the string or on the right of the string.
     * For example, if the input string is two characters long and the requested width is five characters then the resulting
     * string will start with two padding characters followed by the two characters of the input string followed by one padding character.
     * <p/>A call to this method using <tt>center( null, w )</tt> is treated as thought it was a call to this method using <tt>center( "null", w )</tt>.
     *
     * @param s the string to be centered by appropriate padding on the left and the right (if <tt>s</tt> is <tt>null</tt> then the result
     *          is as though <tt>s</tt> were the six character string <tt>"null"</tt>).
     * @param w the width (i.e. length) of the centered string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @param ch the padding character.
     * @return the centered string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String center( String s, int w, char ch ) {

	if ( s == null ) {

	    return center( "null", w, ch );

	}

	int actualLength = s.length();
	if ( actualLength >= w ) {

	    return s;

	}

	int leftPadding = ( w - actualLength + 1 ) >> 1;
	int rightPadding = ( w - actualLength ) >> 1;

	return generatePaddingString( leftPadding, ch, "" ) + s + generatePaddingString( rightPadding, ch, "" );

    }

    /**
     * Pad a string on the left to a specified width using blanks as the padding character. Note that a call to this
     * method using <tt>lpad( s, w )</tt> is equivalent to a call to {@link #lpad(String, int, char)} using <tt>lpad( s,
     * w, '&nbsp;' )</tt>.
     *
     * @param s the string to be padded.
     * @param w the width (i.e. length) of the padded string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @return the padded string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String lpad( String s, int w ) {

        return ObtuseUtil.lpad( s, w, ' ' );

    }

    /**
     * Pad the string representation of a long on the left to a specified width using a specified padding character.
     *
     * @param l the long whose string representation is to be padded.
     * @param w the width (i.e. length) of the padded string representation (if the string representation is already
     *          longer than this then the string representation itself is returned as-is).
     * @param p the character to be used to pad the string representation on the left if it is shorter than <tt>w</tt>.
     * @return the padded string representation or the actual string representation if it is already at least as wide as
     *         <tt>w</tt>.
     */

    public static String lpad( long l, int w, char p ) {

        return ObtuseUtil.lpad( "" + l, w, p );

    }

    /**
     * Pad the string representation of a long on the left to a specified width using blanks as the padding character.
     * Note that a call to this method using <tt>lpad( l, w )</tt> is equivalent to a call to {@link
     * #lpad(long, int, char)} using <tt>lpad( w, w, '&nbsp;' )</tt>.
     *
     * @param l the long whose string representation is to be padded.
     * @param w the width (i.e. length) of the padded string representation (if the string representation is already
     *          longer than this then the string representation itself is returned as-is).
     * @return the padded string representation or the actual string representation if it is already at least as wide as
     *         <tt>w</tt>.
     */

    public static String lpad( long l, int w ) {

        return ObtuseUtil.lpad( "" + l, w );

    }

    /**
     * Pad a string on the right to a specified width using a specified padding character.
     *
     * @param s the string to be padded.
     * @param w the width (i.e. length) of the padded string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @param p the character to be used to pad the string on the right if it is shorter than <tt>w</tt>.
     * @return the padded string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String rpad( String s, int w, char p ) {

        String str = s == null ? "null" : s;
        return str + ObtuseUtil.generatePaddingString( w, p, str );

//        String rval = s == null ? "null" : s;
//        while ( rval.length() < w ) {
//
//            rval += p;
//
//        }
//
//        return rval;

    }

    /**
     * Pad a string on the right to a specified width using blanks as the padding character. Note that a call to this
     * method using <tt>lpad( s, w )</tt> is equivalent to a call to {@link #lpad(String, int, char)} using <tt>lpad( s,
     * w, '&nbsp;' )</tt>.
     *
     * @param s the string to be padded.
     * @param w the width (i.e. length) of the padded string (if the string is already longer than this then the string
     *          itself is returned as-is).
     * @return the padded string or the original string if it is already at least as wide as <tt>w</tt>.
     */

    public static String rpad( String s, int w ) {

        return ObtuseUtil.rpad( s, w, ' ' );

    }

    /**
     * Pad the string representation of a long on the right to a specified width using a specified padding character.
     *
     * @param l the long whose string representation is to be padded.
     * @param w the width (i.e. length) of the padded string representation (if the string representation is already
     *          longer than this then the string representation itself is returned as-is).
     * @param p the character to be used to pad the string representation on the right if it is shorter than
     *          <tt>w</tt>.
     * @return the padded string representation or the actual string representation if it is already at least as wide as
     *         <tt>w</tt>.
     */

    public static String rpad( long l, int w, char p ) {

        return ObtuseUtil.rpad( "" + l, w, p );

    }

    /**
     * Pad the string representation of a long on the right to a specified width using blanks as the padding character.
     * Note that a call to this method using <tt>lpad( l, w )</tt> is equivalent to a call to {@link
     * #lpad(long, int, char)} using <tt>lpad( w, w, '&nbsp;' )</tt>.
     *
     * @param l the long whose string representation is to be padded.
     * @param w the width (i.e. length) of the padded string representation (if the string representation is already
     *          longer than this then the string representation itself is returned as-is).
     * @return the padded string representation or the actual string representation if it is already at least as wide as
     *         <tt>w</tt>.
     */

    public static String rpad( long l, int w ) {

        return ObtuseUtil.rpad( "" + l, w );

    }

    /**
     * Replicate a string a specified number of times.
     * For example, <tt>replicate( "hello", 3 )</tt> yields <tt>"hellohellohello"</tt>.
     *
     * @param str   the string to replicate.
     * @param count the number of copies to be made.
     * @return the replicated string.
     */

    public static String replicate( String str, int count ) {

        StringBuilder rval = new StringBuilder();
        for ( int i = 0; i < count; i += 1 ) {

            rval.append( str );

        }

        return rval.toString();

    }

    /**
     * Return a String containing the hex representation of a long value. For example, <tt>hexvalue( 27L )</tt> yields
     * <tt>"000000000000001b"</tt>. Note that the string is always 16 characters long.
     *
     * @param v the long value whose hex representation is to be returned.
     * @return the hex representation of <tt>v</tt>.
     */

    @SuppressWarnings({ "MagicNumber" })
    public static String hexvalue( long v ) {

        //noinspection UnnecessaryParentheses

        return ""
               + ObtuseUtil.hexvalue( (int) ( ( v >> 32 ) & 0x00000000ffffffffL ) )
               + ObtuseUtil.hexvalue( (int) ( v & 0x00000000ffffffffL ) );

    }

    /**
     * Return a String containing the hex representation of an int value. For example, <tt>hexvalue( 27 )</tt> yields
     * <tt>"0000001b"</tt>. Note that the string is always 8 characters long.
     *
     * @param v the int value whose hex representation is to be returned.
     * @return the hex representation of <tt>v</tt>.
     */

    @SuppressWarnings({ "UnnecessaryParentheses", "MagicNumber" })
    public static String hexvalue( int v ) {

        return ""
               + ObtuseUtil.hexvalue( (byte) ( ( v >> 24 ) & 0xff ) )
               + ObtuseUtil.hexvalue( (byte) ( ( v >> 16 ) & 0xff ) )
               + ObtuseUtil.hexvalue( (byte) ( ( v >> 8 ) & 0xff ) )
               + ObtuseUtil.hexvalue( (byte) ( v & 0xff ) );

    }

    /**
     * Return a String containing the hex representation of a single byte value. For example, <tt>hexvalue( (byte)27
     * )</tt> yields <tt>"1b"</tt>. Note that the returned string is always two characters long.
     *
     * @param v the byte value whose hex representation is to be returned.
     * @return the hex representation of <tt>v</tt>.
     */

    @SuppressWarnings({ "UnnecessaryParentheses", "MagicNumber" })
    public static String hexvalue( byte v ) {

        int high = ( v >> 4 ) & 0xf;
        int low = (int)v & 0xf;

        return ""
               + "0123456789abcdef".charAt( high )
               + "0123456789abcdef".charAt( low );

    }

    /**
     * Return a String containing the hex representation of a single char value. For example, <tt>hexvalue( ' ' )</tt> yields <tt>"1b"</tt>.
     *
     * @param v the byte value whose hex representation is to be returned.
     * @return the hex representation of <tt>v</tt>.
     */

    @SuppressWarnings({ "UnnecessaryParentheses", "MagicNumber" })
    public static String hexvalue( char v ) {

        return ObtuseUtil.hexvalue( Character.toString( v ).getBytes() );

    }

    /**
     * Convert a byte array to its hexadecimal representation. For example, <tt>hexvalue( new byte[] { 1, 10, 100 }
     * )</tt> yields <tt>"010a64"</tt>. Note that the returned string always contains twice as many characters as the
     * input array contains bytes.
     * <p/>
     * While this method uses a {@link StringBuffer} to avoid creating lots and lots of dead strings, it will still
     * consume quite a bit of memory if the byte array is sufficiently large.
     *
     * @param bv the byte array to be converted.
     * @return the hex representation of <tt>v</tt>.
     */

    public static String hexvalue( byte[] bv ) {

        if ( bv == null ) {

            return "null";

        }

        StringBuilder rval = new StringBuilder();
        for ( byte b : bv ) {

            rval.append( ObtuseUtil.hexvalue( b ) );

        }

        return rval.toString();

    }

    /**
     * Convert a byte array to its hexadecimal representation. For example, <tt>hexvalue( new byte[] { 1, 10, 100 }
     * )</tt> yields <tt>"010a64"</tt>. Note that the returned string always contains twice as many characters as the
     * input array contains bytes.
     * <p/>
     * While this method uses a {@link StringBuffer} to avoid creating lots and lots of dead strings, it will still
     * consume quite a bit of memory if the byte array is sufficiently large.
     *
     * @param bv the byte array to be converted.
     * @param off offset within the array to start converting at.
     * @param len number of bytes to convert.
     * @return the hex representation of <tt>v</tt>.
     */

    public static String hexvalue( byte[] bv, int off, int len ) {

        if ( bv == null ) {

            return "null";

        }

        StringBuilder rval = new StringBuilder();
        for ( int ix = off; ix < len; ix += 1 ) {

            byte b = bv[ix];
            rval.
                    append( "0123456789abcdef".charAt( ( b >> 4 ) & 0xf ) ).
                    append( "0123456789abcdef".charAt( b & 0xf ) );

        }

        return rval.toString();

    }

    /**
     * Decode a string of hex digits as a byte array.
     */

    @NotNull
    public static byte[] decodeHexAsByteArray( @NotNull String hexString ) {

        String hex = hexString.toLowerCase();

        Matcher m;
        synchronized ( HEX_STRING_PATTERN ) {

            m = HEX_STRING_PATTERN.matcher( hex );

        }

        if ( !m.matches() ) {

            throw new NumberFormatException( "not a hex string" );

        }

        byte[] rval = new byte[ hex.length() >> 1 ];
        char[] hexChars = hex.toCharArray();
        int inputIx = 0;
        int outputIx = 0;
        while ( inputIx < hex.length() ) {

            char ch1 = hexChars[inputIx];
            if ( !( ch1 >= '0' && ch1 <= '9' || ch1 >= 'a' && ch1 <= 'f' ) ) {

                throw new NumberFormatException( "msb at offset " + inputIx + " (" + ch1 + ") is not a valid hex digit" );

            }

            inputIx += 1;

            char ch2 = hexChars[inputIx];
            if ( !( ch2 >= '0' && ch2 <= '9' || ch2 >= 'a' && ch2 <= 'f' ) ) {

                throw new NumberFormatException( "lsb at offset " + inputIx + " (" + ch2 + ") is not a valid hex digit" );

            }

            inputIx += 1;

            int highNibble = ( ch1 >= '0' && ch1 <= '9' ) ? ch1 - '0' : 10 + ch1 - 'a';
            int lowNibble = ( ch2 >= '0' && ch2 <= '9' ) ? ch2 - '0' : 10 + ch2 - 'a';
            rval[outputIx] = (byte) ( ( highNibble << 4 ) | lowNibble );

            outputIx += 1;

        }

        return rval;

    }

    /**
     * Capitalizes the first letter of the specified string.
     * @param str the string whose first letter is to be capitalized.
     * @return the capitalized string.
     * @throws IndexOutOfBoundsException if the string is empty.
     */

    public static String capitalize( @NotNull String str ) {

        return str.substring( 0, 1 ).toUpperCase() + str.substring( 1 );

    }

    /**
     * Sleep for specified number of milliseconds without having to bother catching the InterruptedException potentially
     * thrown by the regular sleep method.
     *
     * @param milliseconds the minimum number of milliseconds that the calling thread should be suspended for (the
     *                     thread could conceivably be suspended for an arbitrary number of additional milliseconds
     *                     depending upon far too many factors to enumerate here).
     */

    public static void safeSleepMillis( long milliseconds ) {

        try {

            Thread.sleep( milliseconds );

        } catch ( InterruptedException e ) {

            //noinspection CallToPrintStackTrace
            e.printStackTrace();

        }

    }

    /**
     * Dump a byte array in hex - prints the contents of a byte array onto {@link System#out} in geek-readable form.
     *
     * @param data the byte array to be formatted and printed onto {@link System#out}.
     */

    @SuppressWarnings({ "MagicNumber" })
    public static void dump( byte[] data ) {

        for ( int offset = 0; offset < data.length; offset += 16 ) {

            StringBuilder rval = new StringBuilder( ObtuseUtil.hexvalue( offset ) ).append( " " );
            for ( int j = 0; j < 16; j += 1 ) {

                if ( j % 4 == 0 ) {

                    rval.append( ' ' );

                }

                if ( offset + j < data.length ) {

                    rval.append( ObtuseUtil.hexvalue( data[ offset + j ] ) );

                } else {

                    rval.append( "  " );

                }

            }

            rval.append( " *" );

            for ( int j = 0; j < 16 && offset + j < data.length; j += 1 ) {

                byte b = data[offset + j];
                //noinspection ImplicitNumericConversion
                if ( b < ' ' || b > '~' ) {

                    rval.append( '.' );

                } else {

                    rval.append( (char)b );

                }

            }

            Logger.logMsg( rval.append( "*" ).toString() );

        }

    }

    /**
     * Escape ampersands and less-than characters in a string using HTML-style &amp;amp; and &amp;lt; constructs.
     *
     * @param str the string to be escaped.
     * @return the escaped string.
     */

    public static String htmlEscape( String str ) {

        String rval;
        String s = str;
	StringBuilder rvalBuilder = new StringBuilder();
	while ( true ) {

            int ix1 = s.indexOf( (int)'&' );
            int ix2 = s.indexOf( (int)'<' );
            if ( ix1 < 0 && ix2 < 0 ) {

                break;

            }

            int ix;
            if ( ix1 < 0 ) {

                ix = ix2;

            } else if ( ix2 < 0 ) {

                ix = ix1;

            } else {

                ix = ix1 < ix2 ? ix1 : ix2;

            }

            rvalBuilder.append( s.substring( 0, ix ) );
            s = s.substring( ix );
            if ( s.startsWith( "&" ) ) {

                rvalBuilder.append( "&amp;" );

            } else {

                rvalBuilder.append( "&lt;" );

            }

            s = s.substring( 1 );

        }
	rval = rvalBuilder.toString();

	rval += s;

        return rval;

    }

    /**
     * Close something while ignoring any {@link java.io.IOException}s.
     *
     * @param thing the thing to be closed (which can be null in which case nothing is done).
     */

    public static void closeQuietly( @Nullable Closeable thing ) {

        try {

            if ( thing != null ) {

                thing.close();

            }

        } catch ( IOException e ) {

            // Ignore close failures.

        }

    }

    public static void closeQuietly( @Nullable ServerSocket sock ) {

        try {

            if ( sock != null ) {

                sock.close();

            }

        } catch ( IOException e ) {

            // Ignore close failures.

        }

    }

    public static void closeQuietly( @Nullable Socket sock ) {

        try {

            if ( sock != null ) {

                sock.close();

            }

        } catch ( IOException e ) {

            // Ignore close failures.

        }

    }

    public static void closeQuietly( @Nullable ZipFile zipFile ) {

        try {

            if ( zipFile != null ) {

                zipFile.close();

            }

        } catch ( IOException e ) {

            // Ignore close failures.

        }

    }

    public static void closeQuietly( @Nullable ResultSet rs ) {

        try {

            if ( rs != null ) {

                rs.close();

            }

        } catch ( SQLException e ) {

            Logger.logErr( "close of result set failed", e );

        }

    }

    public static void closeQuietly( @Nullable PreparedStatement rs ) {

        try {

            if ( rs != null ) {

                rs.close();

            }

        } catch ( SQLException e ) {

            Logger.logErr( "close of prepared statement failed", e );

        }

    }

    public static void closeQuietly( @Nullable PostgresConnection postgresConnection ) {

        try {

	    if ( postgresConnection != null ) {

		postgresConnection.close();

	    }

	} catch ( SQLException e ) {

            Logger.logErr( "close of PostgresConnection failed", e );

        }
    }

    /**
     * A method which deliberately does nothing.
     * Useful as a statement upon which to set a breakpoint.
     */

    public static void doNothing() {

    }

    /**
     * Use double quotes to escape commas and quotes in the way that MS Excel seems to do it.
     * <p/>
     * Some examples are probably in order.
     * <ul>
     * <li>if the string is
     * <blockquote><tt>hello world</tt></blockquote>
     * then the output is the string itself (unchanged).
     * <li>if the string is
     * <blockquote><tt>hello,world</tt></blockquote>
     * then the output is
     * <blockquote><tt>"hello,world"</tt></blockquote>
     * <li>if the string is
     * <blockquote><tt>"hello" world</tt></blockquote>
     * then the output is
     * <blockquote><tt>"""hello"" world"</tt></blockquote>
     * <li>if the string is
     * <blockquote><tt>hello ",world"</tt></blockquote>
     * then the output is
     * <blockquote><tt>"hello "",world"""</tt></blockquote>
     * </ul>
     * Note that all the double quotes in the above examples actually appear in the strings.
     * In other words, the above examples do <b>NOT</b> use double quotes to enclose strings.
     *
     * @param string the string to be enquoted.
     * @return the original string if it does not contain quotes or commas; the enquoted string otherwise.
     * @throws NullPointerException if <tt>string</tt> is null.
     */

    public static String enquoteForCSV( String string ) {

        if ( !string.contains( "," ) && !string.contains( "\"" ) ) {

            return string;

        }

        StringBuilder rval = new StringBuilder( "\"" );
        int quoteOffset = -1;
        while ( true ) {

            int newQuoteOffset = string.indexOf( '"', quoteOffset + 1 );

            if ( newQuoteOffset < 0 ) {

                break;

            }

            rval.append( string.substring( quoteOffset + 1, newQuoteOffset + 1 ) ).append( '"' );

            quoteOffset = newQuoteOffset;

        }

        return rval.append( string.substring( quoteOffset + 1 ) ).append( '"' ).toString();

    }

    public static String enquoteForJavaString( String string ) {

	if ( string == null ) {

	    return "null";

	}

	StringBuilder rval = new StringBuilder( "\"" );
	for ( char c : string.toCharArray() ) {

	    switch ( c ) {

		case '\b':
		    rval.append( "\\b" );
		    break;

		case '\n':
		    rval.append( "\\n" );
		    break;

		case '\r':
		    rval.append( "\\r" );
		    break;

		case '\t':
		    rval.append( "\\t" );
		    break;

		case '\\':
		    rval.append( "\\\\" );
		    break;

		case '"':
		    rval.append( "\\\"" );
		    break;

		default:
		    rval.append( c );

	    }

	}

	rval.append( '"' );
	return rval.toString();

    }

    public static String enquoteForJavaCharacter( char ch ) {

	StringBuilder rval = new StringBuilder( "'" );

	switch ( ch ) {

	    case '\n':
		rval.append( "\\n" );
		break;

	    case '\r':
		rval.append( "\\r" );
		break;

	    case '\t':
		rval.append( "\\t" );
		break;

	    case '\\':
		rval.append( "\\\\" );
		break;

	    case '\'':
		rval.append( "\\\'" );
		break;

	    default:
		rval.append( ch );

	}

	rval.append( "'" );

	return rval.toString();

    }

    private static       MessageDigest s_md5Algorithm = null;
    @SuppressWarnings("ConstantNamingConvention")
    private static final Long          _md5Lock       = 0L;

    @NotNull
    public static String computeMD5( InputStream is )
            throws IOException {

        synchronized ( ObtuseUtil._md5Lock ) {

            if ( ObtuseUtil.s_md5Algorithm == null ) {

                MessageDigest alg;
                try {

                    alg = MessageDigest.getInstance( "MD5" );

                } catch ( NoSuchAlgorithmException e ) {

                    // There's no real risk that this can happen is there?

                    throw new IllegalArgumentException( "This version of Java does not support MD5 checksums" );

                }

                ObtuseUtil.s_md5Algorithm = alg;

            }

            BufferedInputStream fis = null;

            try {

                ObtuseUtil.s_md5Algorithm.reset();
                fis = new BufferedInputStream( is );

                //noinspection MagicNumber
                byte[] buffer = new byte[1024];
                while ( true ) {

                    int rLen = fis.read( buffer );
                    if ( rLen < 0 ) {

                        break;

                    }

                    ObtuseUtil.s_md5Algorithm.update( buffer, 0, rLen );

                }

                byte[] digest = ObtuseUtil.s_md5Algorithm.digest();

                return ObtuseUtil.hexvalue( digest );

            } finally {

                ObtuseUtil.closeQuietly( fis );

            }

        }

    }

    @NotNull
    public static String computeMD5( File file )
            throws IOException {

        FileInputStream fis = new FileInputStream( file );
        try {

            return ObtuseUtil.computeMD5( fis );

        } finally {

            ObtuseUtil.closeQuietly( fis );

        }

    }

    public static int safeDivide( int numerator, int denominator ) {

        return denominator == 0 ? 0 : numerator / denominator;

    }

    public static int safeDivide( int numerator, int denominator, int safeReturnValue ) {

        return denominator == 0 ? safeReturnValue : numerator / denominator;

    }

    public static long safeDivide( long numerator, long denominator ) {

        return denominator == 0 ? 0 : numerator / denominator;

    }

    public static long safeDivide( long numerator, long denominator, long safeReturnValue ) {

        return denominator == 0 ? safeReturnValue : numerator / denominator;

    }

    public static double safeDivide( double numerator, double denominator ) {

        return denominator == 0.0 ? 0.0 : numerator / denominator;

    }

    public static double safeDivide( double numerator, double denominator, double safeReturnValue ) {

        return denominator == 0.0 ? safeReturnValue : numerator / denominator;

    }

    /**
     * Add the contents of an array to a collection and return the collection.
     * <p/>Returning the collection facilitates certain constructs including:
     * <blockquote>
     * <tt>doit( ObtuseUtil.addAll( new LinkedList&lt;String>(), new String[] { "hello", "there", "world" } );</tt>
     * </blockquote>
     *
     * @param collection  the collection to which things are to be added.
     * @param newElements the new elements to add to the collection.
     * @return the collection after the elements have been added.
     */

    @SafeVarargs
    public static <T> Collection<T> addAll(
            Collection<T> collection,
            T... newElements
    ) {

        Collections.addAll( collection, newElements );
        return collection;

    }

//    private static void doit( String s ) {
//
//        System.out.println( "<" + s + "> yielded <" + enquoteForCSV( s ) + ">" );
//
//    }
//
//    public static void main( String[] args ) {
//
//        doit( "hello world" );
//        doit( "" );
//        doit( "\"hello world\"" );
//        doit( "hello,world" );
//        doit( "\"\"\"\"" );
//        doit( "\"hello,world\"" );
//        doit( "\"hello world" );
//        doit( "hello world\"" );
//        System.exit( 0 );
//
//    }

    /**
     * Returns an unmodifiable view of the specified hash table.
     * This method allows modules to provide users with "read-only" access to internal hash tables (including {@link java.util.Dictionary}s).
     * Query operations on the returned hash table "read through" to the specified hash table,
     * and attempts to modify the returned hash table, whether direct or via its collection views,
     * result in an UnsupportedOperationException.
     * <p/>This method is probably less than perfect in the sense that there probably are at least somewhat sneaky ways
     * to modify the 'unmodifiable' hashtable that it returns. That said, it is far better than nothing.
     * @param ht the hash table for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified hash table.
     */

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    public static <K,V> Hashtable<K,V> unmodifiableHashtable( final Hashtable<? extends K,? extends V> ht ) {

        UnmodifiableHashtable<K,V> unmodifiableHashtable = new UnmodifiableHashtable<>( ht );

        unmodifiableHashtable.makeReadonly();

        return unmodifiableHashtable;

    }

    public static void main( String[] args ) {

        BasicProgramConfigInfo.init( "Obtuse", "ObtuseUtil", "testing", null );

	String s = "hello";

	for ( int w = s.length() - 1; w <= s.length() + 4; w += 1 ) {

	    Logger.logMsg( "center( \"" + s + "\", " + w + " ) yields \"" + center( s, w ) + '"' );

	}

	for ( String value : new String[]{ "hello world", "", null } ) {

	    if ( ObtuseUtil.writeSerializableObjectToFile( value, new File( "test.ser" ), true ) ) {

		try {

		    String recoveredVersion = (String) ObtuseUtil.recoverSerializedVersion( new File( "test.ser" ) );
		    if ( recoveredVersion == null ) {

			if ( value == null ) {

			    Logger.logMsg( "null made the round trip alive" );

			} else {

			    Logger.logMsg( "got null back from serialized version of \"" + enquoteForJavaString( value ) + "\"" );

			}

		    } else if ( "Hello world".equals( recoveredVersion ) ) {

			Logger.logMsg( "serialization and de-serialization to/from files seems to work" );

		    }

		    doNothing();

		} catch ( IOException e ) {

		    Logger.logErr( "IOException trying to recover serialized version of \"" + enquoteForJavaString( value ) + "\"", e );

		} catch ( ClassNotFoundException e ) {

		    Logger.logErr( "ClassNotFoundException trying to recover serialized version of \"" + enquoteForJavaString( value ) + "\"", e );

		}

	    } else {

		Logger.logMsg( "unable to serialize test.ser" );

	    }
	}

    }

    /**
     * Get our process id (not guaranteed to work on all platforms).
     * <p/>Found at
     * <blockquote><tt>http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id</tt></blockquote>
     * (last referenced 2013/09/24)
     */

    private static Integer s_pid = null;

    public static int getPid() {

        if ( ObtuseUtil.s_pid == null ) {

            ObtuseUtil.s_pid = -1;
            try {

                java.lang.management.RuntimeMXBean runtime =
                        java.lang.management.ManagementFactory.getRuntimeMXBean();
                java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
                jvm.setAccessible(true);
                sun.management.VMManagement mgmt =
                        (sun.management.VMManagement) jvm.get(runtime);
                java.lang.reflect.Method pidMethod =
                        mgmt.getClass().getDeclaredMethod("getProcessId");
                pidMethod.setAccessible( true );

                ObtuseUtil.s_pid = (Integer) pidMethod.invoke(mgmt);

            } catch ( InvocationTargetException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e ) {
                // we did our best
            }

	}

        return ObtuseUtil.s_pid.intValue();

    }

    @NotNull
    public static String fDim( @NotNull String name, Dimension d ) {

	return name + "=" + ObtuseUtil.fDim( d );

    }

    @NotNull
    public static String fDim( Dimension d ) {

	return d == null ? "null" : ObtuseUtil.fDim( d.width, d.height );


    }

    @NotNull
    public static String fDim( int width, int height ) {

	return "(" + width + "," + height + ")";

    }

    @NotNull
    public static String fBounds( String name, Rectangle r ) {

	return name + "=" + ( r == null ? "null" : ObtuseUtil.fBounds( r ) );

    }

    @NotNull
    public static String fBounds( Rectangle r ) {

	return r == null ? "null" : ObtuseUtil.fBounds( r.x, r.y, r.width, r.height );


    }

    @NotNull
    public static String fBounds( int x, int y, int width, int height ) {

	return "@( " + x + ", " + y + ", " + width + "x" + height + " )";

    }

    @NotNull
    public static String fInsets( Insets in ) {

	return "i( l=" + in.left + ", r=" + in.right + ", t=" + in.top + ", b=" + in.bottom + " )";

    }

    /**
     Always returns true (avoids having to fool Java compiler when you want an always {@code true} value).
     @return {@code true}
     */

    public static boolean always() {

	return true;

    }

    /**
     Never returns true (avoids having to fool Java compiler when you want an always {@code false} value).
     @return {@code true}
     */

    public static boolean never() {

	return false;

    }

    private static String[] s_maskExBits = new String[32];
    static {

	rememberBitName( InputEvent.SHIFT_MASK, "Shift" );
	rememberBitName( InputEvent.CTRL_MASK, "Ctrl" );
	rememberBitName( InputEvent.META_MASK, "Meta" );
	rememberBitName( InputEvent.ALT_MASK, "Alt" );
	rememberBitName( InputEvent.ALT_GRAPH_MASK, "AltGraph" );
	rememberBitName( InputEvent.BUTTON1_MASK, "Button_1" );
	rememberBitName( InputEvent.BUTTON2_MASK, "Button_2" );
	rememberBitName( InputEvent.BUTTON3_MASK, "Button_3" );
	rememberBitName( InputEvent.SHIFT_DOWN_MASK, "ShiftDown" );
	rememberBitName( InputEvent.CTRL_DOWN_MASK, "CtrlDown" );
	rememberBitName( InputEvent.META_DOWN_MASK, "MetaDown" );
	rememberBitName( InputEvent.ALT_DOWN_MASK, "AltDown" );
	rememberBitName( InputEvent.BUTTON1_DOWN_MASK, "Button_1_Down" );
	rememberBitName( InputEvent.BUTTON2_DOWN_MASK, "Button_2_Down" );
	rememberBitName( InputEvent.BUTTON3_DOWN_MASK, "Button_3_Down" );
	rememberBitName( InputEvent.ALT_GRAPH_DOWN_MASK, "AltGraphDown" );

	ObtuseUtil.doNothing();

    }

    public static String getBitMaskName( int mask ) {

	int numberOfTrailingZeros = Integer.numberOfTrailingZeros( mask );
	int lowestOneBit = Integer.lowestOneBit( mask );
	int highestOneBit = Integer.highestOneBit( mask );
	try {

	    if ( mask < 0 || highestOneBit != lowestOneBit || numberOfTrailingZeros >= 32 ) {

		return "<mask " + Integer.toBinaryString( mask ) + ">";

	    } else {

		return "<" + s_maskExBits[ numberOfTrailingZeros ] + ">";

	    }

	} catch ( Exception e ) {

	    throw new HowDidWeGetHereError( "mask=" + mask + "= " + Integer.toBinaryString( mask ) + " lowestOneBit=" + lowestOneBit + ", highestOneBit=" + highestOneBit );

	}

    }

    private static void rememberBitName( int maskValue, String name ) {

	if ( maskValue == 0 ) {

	    Logger.logMsg( "rememberBitName:  mask named \"" + name + "\" has value 0 - ignored" );

	} else {

	    if ( Integer.highestOneBit( maskValue ) == Integer.lowestOneBit( maskValue ) ) {

		int bitIx = Integer.numberOfTrailingZeros( maskValue );

		if ( s_maskExBits[bitIx] == null ) {

		    Logger.logMsg( "rememberBitName:  1 << " + bitIx + " is named \"" + name + "\"" );
		    s_maskExBits[bitIx] = name;

		} else {

		    Logger.logMsg( "rememberBitName:  1 << " + bitIx + " is already called \"" + s_maskExBits[bitIx] + "\" when attempting to assign new name \"" + name + "\" - new name ignored" );

		}

	    } else {

		Logger.logMsg( "rememberBitName:  bit mask " + Integer.toBinaryString( maskValue ) + " called \"" + name + "\" has more than one bit set - ignored" );

	    }

	}

    }

}
