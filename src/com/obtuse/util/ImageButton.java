/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Manage the pressed and unpressed versions of an icon/image used as a button.
 * <p/>
 */

@SuppressWarnings( {"UnusedDeclaration"} )
public class ImageButton {

    private final JLabel _button;
    private final ImageIcon _pressedIcon;
    private final ImageIcon _unpressedIcon;
    private final Runnable _action;
    private final String _purpose;

    private JPopupMenu _popupMenu = null;

    private static final float DEFAULT_DARKENING_FACTOR = 0.8f;
    private static float s_defaultDarkeningFactor = ImageButton.DEFAULT_DARKENING_FACTOR;

    private ImageButton(
            final @NotNull JLabel button,
            final @NotNull String purpose,
            final @NotNull ImageIcon pressedIcon,
            final @NotNull ImageIcon unpressedIcon,
            final @NotNull Runnable action
    ) {

        super();

        _button = button;
        _purpose = purpose;
        _pressedIcon = pressedIcon;
        _unpressedIcon = unpressedIcon;
        _action = action;

    }

    public static void setDefaultDarkeningFactor( final float factor ) {

        ImageButton.s_defaultDarkeningFactor = factor;

    }

    public static float getDefaultDarkeningFactor() {

        return ImageButton.s_defaultDarkeningFactor;

    }

    @NotNull
    public Runnable getAction() {

        return _action;

    }

    @NotNull
    public ImageIcon getPressedIcon() {

        return _pressedIcon;

    }

    @NotNull
    public ImageIcon getUnpressedIcon() {

        return _unpressedIcon;

    }

    @NotNull
    public String getPurpose() {

        return _purpose;

    }

    public void setEnabled( final boolean isEnabled ) {

        _button.setEnabled( isEnabled );

    }

    public boolean isEnabled() {

        return _button.isEnabled();

    }

    @NotNull
    public JLabel getButton() {

        return _button;

    }

    @NotNull
    public static ImageButton makeImageButton(
            final @NotNull ImageButtonOwner imageButtonOwner,
            final @NotNull String purpose,
            final @NotNull JLabel button,
            final @NotNull Runnable action,
            final @NotNull String buttonFileName
    ) {

        return ImageButton.makeImageButton(
                imageButtonOwner,
                purpose,
                button,
                action,
                buttonFileName,
                ImageIconUtils.getDefaultResourceBaseDirectory(),
                ImageButton.s_defaultDarkeningFactor
        );

    }

    @NotNull
    public static ImageButton makeImageButton(
            final @NotNull ImageButtonOwner imageButtonOwner,
            final @NotNull String purpose,
            final @NotNull JLabel button,
            final @NotNull Runnable action,
            final @NotNull String buttonFileName,
            final @NotNull String resourceBaseDirectory,
            final float darkeningFactor
    ) {

        return ImageButton.makeImageButton(
                imageButtonOwner,
                purpose,
                button,
                action,
                buttonFileName,
                resourceBaseDirectory,
                ImageButton.class,
                darkeningFactor
        );

    }

    @NotNull
    public static ImageButton makeImageButton(
            final @NotNull ImageButtonOwner imageButtonOwner,
            final @NotNull String purpose,
            final @NotNull JLabel button,
            final @NotNull Runnable action,
            final @NotNull String buttonFileName,
            final @NotNull String resourceBaseDirectory,
            final @NotNull Class<?> representativeClass,
            final float darkeningFactor
    ) {

        Optional<ImageIcon> optUnpressedIcon = ImageIconUtils.fetchIconImage(
                "button-" + buttonFileName + ".png",
                0,
                representativeClass.getClassLoader(),
                resourceBaseDirectory
        );

        if ( optUnpressedIcon.isEmpty() ) {

            throw new IllegalArgumentException( "ImageButton.makeImageButton:  unable to fetch unpressed icon" );

        }

        ImageIcon unpressedIcon = optUnpressedIcon.get();

        // Create a somewhat darker icon for the pressed version.

        ImageIcon pressedIcon = new ImageIcon(
                ImageIconUtils.changeImageBrightness( unpressedIcon.getImage(), darkeningFactor, 0f )
        );

        return ImageButton.makeImageButton( imageButtonOwner, purpose, button, action, unpressedIcon, pressedIcon );

    }

    @NotNull
    public static ImageButton makeImageButton(
            final @NotNull ImageButtonOwner imageButtonOwner,
            final @NotNull String purpose,
            final @NotNull JLabel button,
            final @NotNull Runnable action,
            final @NotNull ImageIcon unpressedIcon,
            final float darkeningFactor
    ) {

        // Create a somewhat darker icon for the pressed version.

        ImageIcon pressedIcon = new ImageIcon(
                ImageIconUtils.changeImageBrightness( unpressedIcon.getImage(), darkeningFactor, 0f )
        );

        return ImageButton.makeImageButton( imageButtonOwner, purpose, button, action, unpressedIcon, pressedIcon );

    }

    @NotNull
    public static ImageButton makeImageButton(
            final @NotNull ImageButtonOwner imageButtonOwner,
            final @NotNull String purpose,
            final @NotNull JLabel button,
            final @NotNull Runnable action,
            final @NotNull ImageIcon unpressedIcon,
            final @NotNull ImageIcon pressedIcon
    ) {

        int width = Math.max( pressedIcon.getIconWidth(), unpressedIcon.getIconWidth() );
        int height = Math.max( pressedIcon.getIconHeight(), unpressedIcon.getIconHeight() );

        final ImageButton bi = new ImageButton( button, purpose, pressedIcon, unpressedIcon, action );

        button.addMouseListener(
                new MouseListener() {
                    public void mouseClicked( final MouseEvent mouseEvent ) {

                        if ( bi.getButton()
                                .isEnabled() ) {

                            if ( Clicks.isLeftClick( mouseEvent ) ) {

                                bi.getAction()
                                        .run();
                                imageButtonOwner.setButtonStates();

                            } else {

                                Logger.logMsg( "it's " + Clicks.describeInputBitMask( mouseEvent.getModifiersEx() ) );

                                ObtuseUtil.doNothing();

                            }

                        }

                    }

                    public void mousePressed( final MouseEvent mouseEvent ) {

                        if ( bi.getButton()
                                .isEnabled() ) {

                            bi.getButton()
                                    .setIcon( bi.getPressedIcon() );

                        }

                    }

                    public void mouseReleased( final MouseEvent mouseEvent ) {

                        if ( bi.getButton()
                                .isEnabled() ) {

                            bi.getButton()
                                    .setIcon( bi.getUnpressedIcon() );

                        }

                    }

                    public void mouseEntered( final MouseEvent mouseEvent ) {

                        if ( bi.getButton()
                                .isEnabled() ) {

                            imageButtonOwner.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );

                        }

                    }

                    public void mouseExited( final MouseEvent mouseEvent ) {

                        if ( bi.getButton()
                                .isEnabled() ) {

                            bi.getButton()
                                    .setIcon( bi.getUnpressedIcon() );
                            imageButtonOwner.setCursor( Cursor.getDefaultCursor() );

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

    @NotNull
    public ImageButton setComponentPopupMenu( JPopupMenu popupMenu ) {

        _popupMenu = popupMenu;
        _button.setComponentPopupMenu( _popupMenu );

        return this;

    }

    /**
     * Create an {@link ImageIcon} which displays a line of text.
     *
     * @param text      the text to be displayed.
     * @param textColor the colour of the text.
     * @param bgColor   the colour of the background.
     * @return the resulting {@link ImageIcon}.
     */

    public static ImageIcon makeTextImageIcon(
            @NotNull final String text,
            @Nullable final Color textColor,
            @Nullable final Color bgColor
    ) {

        return makeTextImageIcon( text, textColor, bgColor, 10F, 4 );
    }

    /**
     * Create an {@link ImageIcon} which displays a line of text.
     *
     * @param text      the text to be displayed.
     * @param textColor the colour of the text.
     * @param bgColor   the colour of the background.
     * @param fontSize  font size.
     * @return the resulting {@link ImageIcon}.
     */

    public static ImageIcon makeTextImageIcon(
            @NotNull final String text,
            @Nullable final Color textColor,
            @Nullable final Color bgColor,
            float fontSize,
            int borderSize
    ) {

        BufferedImage image = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = image.createGraphics();
        g.setFont( g.getFont()
                .deriveFont( fontSize ) );
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D textBounds = fm.getStringBounds( text, g );
        image = new BufferedImage(
                (int)Math.ceil( textBounds.getWidth() ) + borderSize,
                (int)Math.ceil( textBounds.getHeight() ) + borderSize,
                BufferedImage.TYPE_INT_RGB
        );
        g = image.createGraphics();
        g.setColor( bgColor );
        g.fillRect( 0, 0, image.getWidth(), image.getHeight() );
        g.setColor( textColor );
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        g.setRenderingHints( rh );
        g.setFont( g.getFont()
                .deriveFont( fontSize ) );
        g.drawString( text, 0, image.getHeight() - ( 2 + fm.getDescent() ) );

        return new ImageIcon( image );

    }

    public static void main( String[] args ) {

        BasicProgramConfigInfo.init( "Kenosee", "ImageButton", "testing" );
        ImageIcon ii = makeTextImageIcon( "Hello", Color.GREEN, Color.WHITE );
        Logger.logMsg( "image icon = " + ii );

        ObtuseUtil.doNothing();

    }

    public String toString() {

        return "ImageIcon( " + _purpose + " )";

    }

}
