/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;

/**
 * These are obsolete - see {@link FileSelectors2}.
 * Some utility methods that make using Swing and AWT file selection dialogs a bit easier.
 * <p/>
 * Possibly of more interest, these also provide examples of how to use the file selection dialogs.
 */

@Deprecated
@SuppressWarnings("UnusedDeclaration")
class FileSelectors {

    private FileSelectors() {
        super();

    }

    /**
     * Use the Swing {@link javax.swing.JFileChooser} to select a file.
     * @param parent the component that is requesting this dialog (ignored if null).
     * @param title the title for the dialog window and the approve button text.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param dialogType the type of dialog (either {@link JFileChooser#OPEN_DIALOG} or {@link JFileChooser#SAVE_DIALOG}).
     *                   Use {@link #swingSelectFile(Component, String, File, String, boolean, FileFilter)} if you want a custom
     * See {@link javax.swing.JFileChooser#setDialogType} for more info.
     * @param multiSelectionEnabled {@code true} if multiple files can be selected in one dialog; {@code false}.
     * @param fileFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     */

    public static @NotNull File@Nullable[] swingSelectFile(
            final Component parent,
            final String title,
            final File startingDirectory,
            final int dialogType,
            final boolean multiSelectionEnabled,
            final FileFilter fileFilter
    ) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( title );
        chooser.setApproveButtonText( title );
        chooser.setMultiSelectionEnabled( multiSelectionEnabled );

        if ( fileFilter != null ) {

            chooser.setFileFilter( fileFilter );

        }

        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        int rval;
        switch ( dialogType ) {

            case JFileChooser.OPEN_DIALOG:
                rval = chooser.showOpenDialog( parent );
                break;

            case JFileChooser.SAVE_DIALOG:
                rval = chooser.showSaveDialog( parent );
                break;

            case JFileChooser.CUSTOM_DIALOG:
                throw new IllegalArgumentException(
                        "FileSelectors.swingSelectFile:  " +
                        "this variant of swingSelectFile only supports OPEN and SAVE dialogs (not CUSTOM dialogs)"
                );

            default:
                throw new IllegalArgumentException(
                        "FileSelectors.swingSelectFile:  " +
                        "unknown dialogType " + dialogType + " " +
                        "(must be JFileChooser.OPEN_DIALOG or " +
                        "JFileChooser.SAVE_DIALOG for this variant of swingSelectFile)"
                );

        }

        if ( rval == JFileChooser.APPROVE_OPTION ) {

            return chooser.getSelectedFiles();

        } else {

            return null;

        }

    }

    /**
     * Use the Swing {@link javax.swing.JFileChooser} to select a file.
     * @param parent the component that is requesting this dialog (ignored if null).
     * @param title the title for the dialog window and the approve button text.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param dialogType the type of dialog (either {@link JFileChooser#OPEN_DIALOG} or {@link JFileChooser#SAVE_DIALOG}).
     *                   Use {@link #swingSelectFile(Component, String, File, String, boolean, FileFilter)} if you want a custom
     * See {@link javax.swing.JFileChooser#setDialogType} for more info.
     * @param multiSelectionEnabled {@code true} if multiple files can be selected in one dialog; {@code false}.
     * @param fileSelectionMode configure the chooser to select just files, just directories, or both files and directories.
     * See {@link javax.swing.JFileChooser#setFileSelectionMode(int)} for more info.
     * @param fileFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     * <p>I don't think that this can return an array with null elements but I'm not sure.
     * Consequently, I've declared it to return an array with elements which might be null.
     * Sorry.</p>
     */

    public static @NotNull Optional<@NotNull File@NotNull[]> swingSelectFile(
            final Component parent,
            final String title,
            final File startingDirectory,
            final int dialogType,
            final boolean multiSelectionEnabled,
            final int fileSelectionMode,
            final FileFilter fileFilter
    ) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( title );
        chooser.setApproveButtonText( title );
        chooser.setMultiSelectionEnabled( multiSelectionEnabled );
        chooser.setCurrentDirectory( startingDirectory );

        if ( fileFilter != null ) {

            chooser.setFileFilter( fileFilter );

        }

        chooser.setFileSelectionMode( fileSelectionMode );
        int chooserRval;
        File[] ourRval;
        switch ( dialogType ) {

            case JFileChooser.OPEN_DIALOG:

                chooserRval = chooser.showOpenDialog( parent );
                ourRval = FileSelectors2.getSelectedFilesArray( multiSelectionEnabled, chooser );

                break;

            case JFileChooser.SAVE_DIALOG:

                chooserRval = chooser.showSaveDialog( parent );
                ourRval = FileSelectors2.getSelectedFilesArray( multiSelectionEnabled, chooser );

                break;

            case JFileChooser.CUSTOM_DIALOG:

                throw new IllegalArgumentException(
                        "FileSelectors.swingSelectFile:  " +
                        "this variant of swingSelectFile only supports OPEN and SAVE dialogs (not CUSTOM dialogs)"
                );

            default:

                throw new IllegalArgumentException(
                        "FileSelectors.swingSelectFile:  " +
                        "unknown dialogType " + dialogType + " " +
                        "(must be JFileChooser.OPEN_DIALOG or " +
                        "JFileChooser.SAVE_DIALOG for this variant of swingSelectFile)"
                );

        }

        if ( chooserRval == JFileChooser.APPROVE_OPTION ) {

            return Optional.of( ourRval );

        } else {

            return Optional.empty();

        }

    }

    /**
     * Use the Swing {@link javax.swing.JFileChooser} to select a file using a custom button label.
     * @param parent the component that is requesting this dialog (ignored if null).
     * @param title the title for the dialog window and the approve button text.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param customLabel the custom label for this JFileChooser's approve button.
     *                   Use {@link #swingSelectFile(Component, String, File, int, boolean, FileFilter)}
     * See {@link javax.swing.JFileChooser#setDialogType} for more info.
     * @param multiSelectionEnabled {@code true} if multiple files can be selected in one dialog; {@code false}.
     * @param fileFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     */

    public static @NotNull File@Nullable[] swingSelectFile(
            final Component parent,
            final String title,
            final File startingDirectory,
            final String customLabel,
            final boolean multiSelectionEnabled,
            final FileFilter fileFilter
    ) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( title );
        chooser.setApproveButtonText( customLabel );
        chooser.setMultiSelectionEnabled( multiSelectionEnabled );

        if ( fileFilter != null ) {

            chooser.setFileFilter( fileFilter );

        }

        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        int rval = chooser.showDialog( parent, customLabel );
        if ( rval == JFileChooser.APPROVE_OPTION ) {

            return chooser.getSelectedFiles();

        } else {

            return null;

        }

    }

    /**
     * Use the AWT {@link java.awt.FileDialog} to select a file.
     * @param parent the {@link java.awt.Frame} that is requesting this dialog (ignored if null).
     * @param title the title for the dialog window.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param mode the mode of the dialog.
     * See {@link java.awt.FileDialog#setMode} for more info.
     * @param filenameFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     */

    public static File awtSelectFile(
            final Frame parent,
            final String title,
            final File startingDirectory,
            final int mode,
            final FilenameFilter filenameFilter
    ) {

        return FileSelectors.awtSelectFile( new FileDialog( parent, title, mode ), startingDirectory, filenameFilter );

    }

    /**
     * Use the AWT {@link java.awt.FileDialog} to select a file.
     * @param parent the {@link java.awt.Dialog} that is requesting this dialog (ignored if null).
     * @param title the title for the dialog window.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param mode the mode of the dialog.
     * See {@link java.awt.FileDialog#setMode} for more info.
     * @param filenameFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     */

    public static File awtSelectFile(
            final Dialog parent,
            final String title,
            final File startingDirectory,
            final int mode,
            final FilenameFilter filenameFilter
    ) {

        return FileSelectors.awtSelectFile( new FileDialog( parent, title, mode ), startingDirectory, filenameFilter );

    }

    /**
     * Use the AWT {@link java.awt.FileDialog} to select a file.
     * <p/>
     * This is a utility method used by the other two awtSelectFile methods to do the real work of selecting a file
     * once the {@link java.awt.FileDialog} instance has been created.  It is probably not very useful to anyone else but there
     * does not seem to be much harm in making it public.
     * @param dialog the FileDialog which is to be used to select a file.
     * @param startingDirectory where the game should begin (defaults to the user's home directory if null).
     * @param filenameFilter the optional file filter which selects which files should appear in the dialog.
     * @return the selected file if the "approve" button was clicked; null otherwise.
     */

    public static File awtSelectFile(
            final FileDialog dialog,
            final File startingDirectory,
            final FilenameFilter filenameFilter
    ) {

        return FileSelectors2.awtSelectFile( dialog, startingDirectory, filenameFilter );

    }

}
