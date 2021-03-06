package com.obtuse.ui;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

import com.obtuse.util.ObtuseUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 A message board that consists of a single {@link JTextComponent} or a {@link JLabel}.
 */

public class SingleLineMessageBoard implements MessageBoardInterface {

    private final JTextComponent _jTextComponent;
    private final JLabel _jLabel;

    private int _nextIdNumber = 0;

    public SingleLineMessageBoard( final @NotNull JTextComponent textComponent ) {

        super();

        _jTextComponent = textComponent;
        _jLabel = null;

    }

    public SingleLineMessageBoard( final @NotNull JLabel jLabel ) {

        super();

        _jTextComponent = null;
        _jLabel = jLabel;

    }

    public JTextComponent getJTextComponent() {

        return _jTextComponent;

    }

    public JLabel getJLabel() {

        return _jLabel;

    }

    @Override
    public int postMessage( final @NotNull String msg ) {

        setTextValue( msg );

        _nextIdNumber += 1;
        return _nextIdNumber - 1;

    }

    private void setTextValue( final @NotNull String msg ) {

        if ( _jLabel == null ) {

            assert _jTextComponent != null;
            _jTextComponent.setText( msg );

        } else {

            _jLabel.setText( msg );

        }

    }

    @Override
    public int getOldestValidIdNumber() {

        return _nextIdNumber - 1;

    }

    @Override
    public int getNewestValidIdNumber() {

        return _nextIdNumber - 1;

    }

    @Override
    public boolean clearAllMessages() {

        return clearMessages( getOldestValidIdNumber(), getNewestValidIdNumber() );

    }

    @Override
    public boolean clearMessages( final int oldestIdNumber, final int newestIdNumber ) {

        if ( oldestIdNumber > newestIdNumber ) {

            return clearMessages( newestIdNumber, oldestIdNumber );

        }

        if ( oldestIdNumber <= getOldestValidIdNumber() && newestIdNumber >= getNewestValidIdNumber() ) {

            setTextValue( "" );

            return true;

        }

        return false;

    }

    public String toString() {

        assert _jTextComponent != null;
        return "SingleLineMessageBoard( " + ObtuseUtil.enquoteToJavaString(
                _jLabel == null ? _jTextComponent.getText() : _jLabel.getText()
        ) + " )";

    }

}