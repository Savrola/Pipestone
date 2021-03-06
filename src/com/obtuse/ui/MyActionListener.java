package com.obtuse.ui;

import com.obtuse.exceptions.HowDidWeGetHereError;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 An action listener that turns all thrown exceptions,
 whether runtime or  into {@link HowDidWeGetHereError} errors.
 */

public abstract class MyActionListener implements ActionListener {

    public final void actionPerformed( final ActionEvent actionEvent ) {

        try {

            ObtuseGuiEventUtils.event( "actionPerformed (" + getClass() + "):  ", actionEvent );
            myActionPerformed( actionEvent );

        } catch ( HowDidWeGetHereError e ) {

            throw e;

        } catch ( Throwable e ) {

            throw new HowDidWeGetHereError( "error processing action event", e );

        }

    }

    protected abstract void myActionPerformed( ActionEvent actionEvent );

}