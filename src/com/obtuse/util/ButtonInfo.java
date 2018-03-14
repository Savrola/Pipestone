/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Manage the pressed and unpressed versions of an icon/image used as a button.
 * <p/>
 */

@SuppressWarnings( { "UnusedDeclaration" } )
public class ButtonInfo {

    private final JLabel _button;
    private final ImageIcon _pressedIcon;
    private final ImageIcon _unpressedIcon;
    private final Runnable _action;
    private static final float DEFAULT_DARKENING_FACTOR = 0.8f;
    private static float s_defaultDarkeningFactor = ButtonInfo.DEFAULT_DARKENING_FACTOR;

    private ButtonInfo( final JLabel button, final ImageIcon pressedIcon, final ImageIcon unpressedIcon, final Runnable action ) {
        super();

        _button = button;
        _pressedIcon = pressedIcon;
        _unpressedIcon = unpressedIcon;
        _action = action;

    }

    public static void setDefaultDarkeningFactor( final float factor ) {

        ButtonInfo.s_defaultDarkeningFactor = factor;

    }

    public static float getDefaultDarkeningFactor() {

        return ButtonInfo.s_defaultDarkeningFactor;

    }

    public Runnable getAction() {

        return _action;

    }

    public ImageIcon getPressedIcon() {

        return _pressedIcon;

    }

    public ImageIcon getUnpressedIcon() {

        return _unpressedIcon;

    }

    public JLabel getButton() {

        return _button;

    }

    public static ButtonInfo makeButtonLabel(
            final ButtonOwner buttonOwner,
            final JLabel button,
            final Runnable action,
            final String buttonName
    ) {

        return ButtonInfo.makeButtonLabel(
                buttonOwner,
                button,
                action,
                buttonName,
                ImageIconUtils.getDefaultResourceBaseDirectory(),
                ButtonInfo.s_defaultDarkeningFactor
        );

    }

    public static ButtonInfo makeButtonLabel(
            final ButtonOwner buttonOwner,
            final JLabel button,
            final Runnable action,
            final String buttonName,
            final String resourceBaseDirectory,
            final float darkeningFactor
    ) {

        ImageIcon unpressedIcon = ImageIconUtils.fetchIconImage(
                "button-" + buttonName + ".png",
                0,
                resourceBaseDirectory
        );

        // Create a somewhat darker version of the unpressed icon.

        ImageIcon pressedIcon = new ImageIcon(
                ImageIconUtils.changeImageBrightness( unpressedIcon.getImage(), darkeningFactor )
        );


        return ButtonInfo.makeButtonLabel( buttonOwner, button, action, unpressedIcon, pressedIcon );

    }

    public static ButtonInfo makeButtonLabel(
            final ButtonOwner buttonOwner,
            final JLabel button,
            final Runnable action,
            final ImageIcon unpressedIcon,
            final ImageIcon pressedIcon
    ) {

        int width = Math.max( pressedIcon.getIconWidth(), unpressedIcon.getIconWidth() );
        int height = Math.max( pressedIcon.getIconHeight(), unpressedIcon.getIconHeight() );

        final ButtonInfo bi = new ButtonInfo( button, pressedIcon, unpressedIcon, action );

        button.addMouseListener(
                new MouseListener() {
                    public void mouseClicked( final MouseEvent mouseEvent ) {

                        if ( bi.getButton().isEnabled() ) {

                            bi.getAction().run();
                            buttonOwner.setButtonStates();

                        }

                    }

                    public void mousePressed( final MouseEvent mouseEvent ) {

                        if ( bi.getPressedIcon() != null && bi.getButton().isEnabled() ) {

                            bi.getButton().setIcon( bi.getPressedIcon() );

                        }

                    }

                    public void mouseReleased( final MouseEvent mouseEvent ) {

                        if ( bi.getButton().isEnabled() ) {

                            bi.getButton().setIcon( bi.getUnpressedIcon() );

                        }

                    }

                    public void mouseEntered( final MouseEvent mouseEvent ) {

                        if ( bi.getButton().isEnabled() ) {

                            buttonOwner.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );

                        }

                    }

                    public void mouseExited( final MouseEvent mouseEvent ) {

                        if ( bi.getButton().isEnabled() ) {

                            bi.getButton().setIcon( bi.getUnpressedIcon() );
                            buttonOwner.setCursor( Cursor.getDefaultCursor() );

                        }

                    }

                }
        );

        button.setIcon( bi.getUnpressedIcon() );
        button.setText( null );
        button.setMinimumSize( new Dimension( width, height ) );
        button.setMaximumSize( new Dimension( width, height ) );

        return bi;
    }

}
