/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.ui;

import com.obtuse.exceptions.HowDidWeGetHereError;
import com.obtuse.util.BasicProgramConfigInfo;
import com.obtuse.util.Logger;
import com.obtuse.util.ObtuseUtil;
import com.obtuse.util.Trace;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings({ "ClassWithoutToString", "UnusedDeclaration", "unchecked" })
public class LogsWindow extends WindowWithMenus {

    private static final DateFormat s_dateFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    public static class TimestampedMessage {

        private final Date timestamp;
        private final String message;

        public TimestampedMessage( final @NotNull String message ) {
            super();

            this.timestamp = new Date();
            this.message = message;

        }

        public TimestampedMessage( final Date timestamp, final @NotNull String message ) {
            super();

            this.timestamp = timestamp;
            this.message = message;

        }

        public String toString() {

            return "TimestampedMessage( " + LogsWindow.format( timestamp, message ) + " )";

        }

        @NotNull
        public String format() {

            return LogsWindow.format( this.timestamp, this.message );

        }

    }

    private JPanel _contentPane;

    @SuppressWarnings({ "UnusedDeclaration" })
    private JScrollPane _messageWindowScrollPane;

    private JList _messageWindowList;

    private JButton _closeButton;

    private final DefaultListModel _messagesListModel = new DefaultListModel();

    private static final Long WINDOW_LOCK = 0L;

    @SuppressWarnings({ "FieldAccessedSynchronizedAndUnsynchronized" })
    private static LogsWindow s_logsWindow = null;

    private static boolean s_useHTML = false;

    private static final List<TimestampedMessage> s_asyncMessages = new ArrayList<>();
    private static Runnable s_backgroundMessageProcessor = null;

    public static final int MAX_LINES_IN_MESSAGE_WINDOW = 1000;

    @SuppressWarnings("CanBeFinal")
    private Clipboard _systemClipboard;

    public LogsWindow( final String windowPrefsName ) {

        super( windowPrefsName, true );

        _systemClipboard = getToolkit().getSystemClipboard();

        setContentPane( _contentPane );

        // Handle the close button.

        //noinspection ClassWithoutToString
        _closeButton.addActionListener(
                new MyActionListener() {
                    public void myActionPerformed( final ActionEvent actionEvent ) {

                        WindowWithMenus.setAllShowLogsModeInMenu( false );
                        setVisible( false );

                    }
                }
        );

        setTitle( BasicProgramConfigInfo.getApplicationName() + " Log Messages" );

        // call onCancel() when cross is clicked
        setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );

        //noinspection RefusedBequest,ClassWithoutToString
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( final WindowEvent e ) {

                        WindowWithMenus.setAllShowLogsModeInMenu( false );

                    }
                }
        );

        _messageWindowList.setModel( _messagesListModel );
        _messageWindowList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
        _messageWindowList.addListSelectionListener(
                listSelectionEvent -> {

                    if ( !listSelectionEvent.getValueIsAdjusting() ) {

                        try {

                            JList list = (JList)listSelectionEvent.getSource();
                            int[] selectedIndices = list.getSelectedIndices();

                            if ( selectedIndices.length > 0 ) {

                                setMenuEnabled( "LW:LW", getEditMenu().getCopyMenuItem(), true );

                            } else {

                                setMenuEnabled( "LW:LW", getEditMenu().getCopyMenuItem(), false );

                            }

                        } catch ( ClassCastException e ) {

                            Logger.logErr(
                                    "unexpected object type in log message window's selection listener (" +
                                    listSelectionEvent.getSource().getClass() + ") - selection ignored"
                            );

                        }

                    }

                }
        );

        WindowWithMenus.setAllShowLogsModeInMenu( false );
        setVisible( false );

        pack();

        restoreWindowGeometry( getWidth(), getHeight() );

    }

    @NotNull
    public static String format( final Date timestamp, final String message ) {

        synchronized ( LogsWindow.s_dateFormatter ) {

            @SuppressWarnings("UnnecessaryLocalVariable") String rval = LogsWindow.s_dateFormatter.format( timestamp ) +
                                                                        " " +
                                                                        ObtuseUtil.enquoteToJavaString( message );

            return rval;

        }

    }

    private static void processAsyncMessages() {

        synchronized ( s_asyncMessages ) {

            if ( SwingUtilities.isEventDispatchThread() ) {

                for ( TimestampedMessage msg : s_asyncMessages ) {

                    try {

                        LogsWindow.getInstance()
                                  .insertMessageAtEnd( msg.format() );

                    } catch ( RuntimeException e ) {

                        Logger.logErr( "unable to insert message " + msg + " into log messages", e );

                    }

                }

                s_asyncMessages.clear();

            } else {

                throw new HowDidWeGetHereError( "LogsWindow.processAsyncMessages:  must be called on event thread" );

            }

            s_backgroundMessageProcessor = null;

        }

    }

    /**
     Timestamp and queue a message.
     <p>Messages are guaranteed to appear in the log window in the order in which this method timestamps them.</p>
     @param msg the message.
     */

    public static void addMessage( final @NotNull String msg ) {

        synchronized ( s_asyncMessages ) {

            final TimestampedMessage timestampedMessage = new TimestampedMessage( msg );

            s_asyncMessages.add( timestampedMessage );

            if ( SwingUtilities.isEventDispatchThread() ) {

                processAsyncMessages();

            } else if ( s_backgroundMessageProcessor == null ) {

                s_backgroundMessageProcessor = () -> processAsyncMessages();

                SwingUtilities.invokeLater( s_backgroundMessageProcessor );

            }

        }

    }

    /**
     Utility method to trace the setting of a button's enabled state.
     <p/>This method lives in this class instead of say the Trace class because adding this method to the
     Trace class would result in any program that uses the Trace class implicitly sucking in a huge chunk of Swing.
     Since this class already uses Swing, putting the method here does no real harm.

     @param button the menu item in question.
     @param value  its new state.
     */

    public static void setButtonEnabled( final JButton button, final boolean value ) {

        String text = button.getText();
        if ( text == null ) {

            text = "<unknown>";

        }

        Trace.event( "button \"" + text + "\" set to " + ( value ? "enabled" : "not enabled" ) );

        button.setEnabled( value );

    }

    /**
     Utility method to trace the setting of a label's enabled state.
     <p/>This method lives in this class instead of say the Trace class because adding this method to the
     Trace class would result in any program that uses the Trace class implicitly sucking in a huge chunk of Swing.
     Since this class already uses Swing, putting the method here does no real harm.

     @param label the menu item in question.
     @param value its new state.
     */

    public static void setLabelEnabled( final JLabel label, final boolean value ) {

        String text = label.getText();
        if ( text == null ) {

            text = "<unknown>";

        }

        Trace.event( "label \"" + text + "\" set to " + ( value ? "enabled" : "not enabled" ) );

        label.setEnabled( value );

    }

    private void insertMessageAtEnd( final String timeStampedMessage ) {

        int listSize = _messagesListModel.getSize();
        if ( listSize >= LogsWindow.MAX_LINES_IN_MESSAGE_WINDOW ) {

            _messagesListModel.remove( 0 );
            listSize -= 1;

        }

        int lastVisibleIx = _messageWindowList.getLastVisibleIndex();

        if ( LogsWindow.s_useHTML ) {

            //noinspection unchecked
            _messagesListModel.addElement( "<html><tt>" + ObtuseUtil.htmlEscape( timeStampedMessage ) + "</tt></html>" );

        } else {

            //noinspection unchecked
            _messagesListModel.addElement( timeStampedMessage );

        }

        if ( lastVisibleIx + 1 == listSize ) {

            _messageWindowList.ensureIndexIsVisible( lastVisibleIx + 1 );

        }

        WindowWithMenus.setAllShowLogsModeInMenu( true );
        if ( !isVisible() ) {

            setVisible( true );

        }

    }

    @SuppressWarnings({ "UnusedDeclaration" })
    public void setUseHTML( final boolean useHTML ) {

        //noinspection AssignmentToStaticFieldFromInstanceMethod
        LogsWindow.s_useHTML = useHTML;

    }

    @SuppressWarnings({ "UnusedDeclaration" })
    public boolean useHTML() {

        return LogsWindow.s_useHTML;

    }

    public static LogsWindow getInstance() {

        synchronized ( LogsWindow.WINDOW_LOCK ) {

            if ( LogsWindow.s_logsWindow == null ) {

                if ( BasicProgramConfigInfo.getApplicationName() == null ) {

                    OkPopupMessageWindow.fatal(
                            "Application has not registered its name using BasicProgramConfigInfo.",
                            "Unable to continue.",
                            "I Will Submit A Bug Report"
                    );

                }

                if ( BasicProgramConfigInfo.getPreferences() == null ) {

                    OkPopupMessageWindow.fatal(
                            "Application has not registered its preferences object using BasicProgramConfigInfo.",
                            "Unable to continue.",
                            "I Will Submit A Bug Report"
                    );

                }

                LogsWindow.s_logsWindow = new LogsWindow( "LogsWindow" );

            }

        }

        return LogsWindow.s_logsWindow;

    }

    public static void launch() {

        WindowWithMenus.setAllShowLogsModeInMenu( true );
        LogsWindow.getInstance().setVisible( true );

    }

    protected BasicEditMenu defineEditMenu() {

        BasicEditMenu editMenu = new BasicEditMenu( "Edit" );

        JMenuItem selectAllMenuItem = new JMenuItem( "Select All" );
        setMenuEnabled( "LW:dEM", selectAllMenuItem, true );
        selectAllMenuItem.addActionListener(
                new MyActionListener() {

                    public void myActionPerformed( final ActionEvent actionEvent ) {

                        _messageWindowList.getSelectionModel().setSelectionInterval(
                                0,
                                _messagesListModel.size() - 1
                        );

                    }

                }
        );

        //noinspection MagicConstant
        selectAllMenuItem.setAccelerator(

                KeyStroke.getKeyStroke(
                        KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                )

        );

        JMenuItem cutMenuItem = new JMenuItem( "Cut" );

        //noinspection MagicConstant
        cutMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                )
        );

        setMenuEnabled( "LW:dEM", cutMenuItem, false );

        JMenuItem copyMenuItem = new JMenuItem( "Copy" );

        setMenuEnabled( "LW:dEM", copyMenuItem, false );

        copyMenuItem.addActionListener(
                new MyActionListener() {

                    public void myActionPerformed( final ActionEvent actionEvent ) {

                        StringWriter lines = new StringWriter();
                        PrintWriter writer = new PrintWriter( lines );
                        int[] selectedIndices = _messageWindowList.getSelectedIndices();
                        List selectedValues = _messageWindowList.getSelectedValuesList();

                        int ix = 0;
                        for ( Object sv : selectedValues ) {

                            writer.print( sv );
                            if ( ix < selectedIndices.length - 1 ) {

                                writer.println();

                            }

                            ix += 1;

                        }

                        writer.flush();
                        StringSelection selection = new StringSelection( lines.getBuffer().toString() );
                        _systemClipboard.setContents( selection, selection );

                    }

                }

        );

        //noinspection MagicConstant
        copyMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                )
        );

        JMenuItem pasteMenuItem = new JMenuItem( "Paste" );

        //noinspection MagicConstant
        pasteMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                )
        );

        setMenuEnabled( "LW:dEM", pasteMenuItem, false );

        editMenu.setCutMenuItem( cutMenuItem );   // never enabled (yet?)
        editMenu.setCopyMenuItem( copyMenuItem );
        editMenu.setPasteMenuItem( pasteMenuItem ); // never enabled (yet?)
        editMenu.setSelectAllMenuItem( selectAllMenuItem );

        return editMenu;

    }

    public String toString() {

        return "LogsWindow()";

    }

}
