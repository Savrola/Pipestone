/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import com.obtuse.exceptions.HowDidWeGetHereError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.DateFormat;
import java.util.prefs.Preferences;

/**
 Fundamental program configuration information and such.
 */

@SuppressWarnings({ "ClassNamingConvention", "UnusedDeclaration" })
public class BasicProgramConfigInfo {

    private static boolean s_initialized = false;

    private static File s_workingDirectory = null;

    private static String s_vendorName = null;

    private static String s_applicationName = null;

    private static String s_componentName = null;

    private static String s_logFileNameFormat = null;

    private static Preferences s_preferences = null;

    private static DateFormat s_dateFormat = null;

    /**
     Initialize this program's basic configuration info.
     <p/>
     Note that the three names passed to this method are used to determine file and/or directory names.
     Consequently, they may only contain characters allowed in file and directory names across whatever range
     of operating systems the calling application component is likely to run on.
     It is probably safest to restrict yourself to letters, digits, spaces, underscores and hyphens.
     Using non-printable ASCII characters or either forward or backward slashes would be a VERY bad idea.
     <p/>Note: spaces in the <tt>vendorName</tt>, <tt>applicationName</tt>, and <tt>componentName</tt> are turned
     into underscores
     to ensure that the generated file names do not contain spaces.
     <p/>Note: this method does not impose any rules on which characters may appear in the <tt>vendorName</tt>,
     <tt>applicationName</tt> or <tt>componentName</tt> parameters.
     Please keep in mind that if you ignore the above advice then either you or, more likely, your customers will
     suffer the consequences.
     As always, <i>caveat structor</i> (developer beware).
     <p/>
     This method MUST be called before using the {@link Logger} or the {@link Trace} facilities.

     @param vendorName      the program's vendor's name (must not be null).
     @param applicationName the application's name (must not be null).
     @param componentName   this component's name within the larger application (must not be null).
     @param workingDirectory where the log files should go (avoids auto-generation of log file destination
     based on vendor name and such).
     @param preferences     this application's preferences object (may be null if application has no use for
     preferences).
     This value may be <tt>null</tt> in which case the application name will generally be used.
     @throws java.lang.IllegalArgumentException if any of <tt>vendorName</tt>, <tt>applicationName</tt>, or
     <tt>componentName</tt> are null.
     */

    @SuppressWarnings("WeakerAccess")
    public static void initIncludingWorkingDirectory(
            @SuppressWarnings("SameParameterValue") final @NotNull String vendorName,
            @SuppressWarnings("SameParameterValue") final @NotNull String applicationName,
            @SuppressWarnings("SameParameterValue") final @NotNull String componentName,
            final @Nullable File workingDirectory,
            @SuppressWarnings("SameParameterValue") @Nullable final Preferences preferences
    ) {

        HowDidWeGetHereError.setStackTracePrintStream( System.err );

        if ( BasicProgramConfigInfo.s_initialized ) {

            throw new IllegalArgumentException( "BasicProgramConfigInfo already initialized" );

        }

        String home = System.getProperty( "user.home" );
        BasicProgramConfigInfo.s_vendorName = vendorName.replace( ' ', '_' );
        BasicProgramConfigInfo.s_applicationName = applicationName.replace( ' ', '_' );
        BasicProgramConfigInfo.s_componentName = componentName.replace( ' ', '_' );
        BasicProgramConfigInfo.s_preferences = preferences;

        if ( workingDirectory == null ) {

            if ( home == null ) {

                BasicProgramConfigInfo.s_workingDirectory = null;

            } else {

                // Mac OS has a convention as to where these sorts of things go.
                // Follow the convention if we are running on the Mac OS.

                if ( OSLevelCustomizations.onMacOsX() ) {

                    File dirLocation = new File(
                            new File( new File( home, "Library" ), "Application Support" ),
                            "ObtuseUtil"
                    );

                    BasicProgramConfigInfo.s_workingDirectory = new File( new File( new File(
                            dirLocation,
                            getVendorName()
                    ), getApplicationName() ), getComponentName() );

                } else {

                    BasicProgramConfigInfo.s_workingDirectory = new File(
                            new File(
                                    new File(
                                            new File(
                                                    new File( home ),
                                                    ".ObtuseUtil"
                                            ),
                                            getVendorName()
                                    ),
                                    getApplicationName()
                            ),
                            getComponentName()
                    );

                }

                //noinspection ResultOfMethodCallIgnored
                BasicProgramConfigInfo.s_workingDirectory.mkdirs();

            }

        } else {

            BasicProgramConfigInfo.s_workingDirectory = workingDirectory;

            //noinspection ResultOfMethodCallIgnored
            BasicProgramConfigInfo.s_workingDirectory.mkdirs();

        }

        BasicProgramConfigInfo.s_initialized = true;

    }

    /**
     A simpler and almost always preferred version of {@link #initIncludingWorkingDirectory(String, String, String, File, Preferences)}.
     <p>See {@link #initIncludingWorkingDirectory(String, String, String, File, Preferences)} for important information.
     Confusion is more than likely if both this method and {@code initIncludingWorkingDirectory} are both called
     within the same JVM instance.</p>
     @param vendorName      the program's vendor's name (must not be null).
     @param applicationName the application's name (must not be null).
     @param componentName   this component's name within the larger application (must not be null).

     */

    public static void init(
            @SuppressWarnings("SameParameterValue") final @NotNull String vendorName,
            @SuppressWarnings("SameParameterValue") final @NotNull String applicationName,
            @SuppressWarnings("SameParameterValue") final @NotNull String componentName
    ) {

        initIncludingWorkingDirectory( vendorName, applicationName, componentName, null, null );

    }

    /**
     Initialize and provide the base of the running application's preferences in (presumably) the user root
     of the global preferences tree.
     @param vendorName      the program's vendor's name (must not be null).
     @param applicationName the application's name (must not be null).
     @param componentName   this component's name within the larger application (must not be null).
     @param preferences     the base of the running applications preferences (NEW 2019-05-03:  must not be null).
     */

    public static void init(
            @SuppressWarnings("SameParameterValue") final @NotNull String vendorName,
            @SuppressWarnings("SameParameterValue") final @NotNull String applicationName,
            @SuppressWarnings("SameParameterValue") final @NotNull String componentName,
            @SuppressWarnings("SameParameterValue") final @NotNull Preferences preferences
    ) {

        initIncludingWorkingDirectory( vendorName, applicationName, componentName, null, preferences );

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInitialized() {

        return BasicProgramConfigInfo.s_initialized;

    }

    private BasicProgramConfigInfo() {

        super();

    }

    public static File getWorkingDirectory() {

        if ( !BasicProgramConfigInfo.s_initialized ) {

            throw new IllegalArgumentException( "BasicProgramConfigInfo.init not called yet" );

        }

        return BasicProgramConfigInfo.s_workingDirectory;

    }

    public static String getVendorName() {

        return BasicProgramConfigInfo.s_vendorName;

    }

    public static String getApplicationName() {

        return BasicProgramConfigInfo.s_applicationName;

    }

    public static String getComponentName() {

        return BasicProgramConfigInfo.s_componentName;

    }

    public static Preferences getPreferences() {

        return BasicProgramConfigInfo.s_preferences;

    }

    public static String getLogFileNameFormat() {

        return BasicProgramConfigInfo.s_logFileNameFormat;

    }

    public static String getPreferenceIfEnabled( final String prefsKey, final String defaultValue ) {

        if ( BasicProgramConfigInfo.s_preferences == null ) {

            return defaultValue;

        } else {

            return BasicProgramConfigInfo.s_preferences.get( prefsKey, defaultValue );

        }

    }

    public static void putPreferenceIfEnabled( final String prefsKey, final String value ) {

        if ( BasicProgramConfigInfo.s_preferences != null ) {

            BasicProgramConfigInfo.s_preferences.put( prefsKey, value );

        }

    }

    public static boolean isPreferencesEnabled() {

        return BasicProgramConfigInfo.s_preferences != null;

    }

    public static DateFormat getDateFormat() {

        return BasicProgramConfigInfo.s_dateFormat;

    }

    public static void setLogFileNameFormat( final String logFileNameFormat ) {

        BasicProgramConfigInfo.s_logFileNameFormat = logFileNameFormat;

    }

    public static void setDateFormat( final DateFormat dateFormat ) {

        BasicProgramConfigInfo.s_dateFormat = dateFormat;

    }

    public static void doOsSpecificCustomizations( final String programName ) {

        // Set the name of the application in case we are on a Mac (does nothing on other OSes).
        // Must be done BEFORE any other AWT or Swing classes are loaded.

        System.setProperty( "com.apple.mrj.application.apple.menu.about.name", programName );

        System.setProperty( "awt.useSystemAAFontSettings", "on" );
        System.setProperty( "swing.aatext", "true" );

        Logger.logMsg( "" + System.getProperty( "sun.arch.data.model" ) + " bit JVM" );

        // Get ourselves an OS-specific customizer.
        // Must be done BEFORE any other AWT or Swing classes are loaded which means that the
        // customizer class names must be given as strings rather than, for example,
        // invoking CustomizerClass.class.getCanonicalName().

        @SuppressWarnings({ "UnusedDeclaration" })
        OSLevelCustomizations customizer = OSLevelCustomizations.getCustomizer();

    }

}
