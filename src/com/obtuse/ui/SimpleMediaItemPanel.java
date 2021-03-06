package com.obtuse.ui;

import com.obtuse.util.Logger;
import com.obtuse.util.lrucache.CachedThing;
import com.obtuse.util.lrucache.LruCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 A customized {@link JPanel} for displaying images.
 */

public class SimpleMediaItemPanel<IID extends ObtuseImageIdentifier> extends JLabel {

    @SuppressWarnings("rawtypes")
    public static class ScaledImageId<IID extends ObtuseImageIdentifier> implements Comparable {

        private final int _scaleGranularity;
        private final IID _imageID;
        private final int _orientation;

        public ScaledImageId( @NotNull final IID imageID, final int orientation, final int scaleGranularity ) {

            super();

            _imageID = imageID;
            _scaleGranularity = scaleGranularity;
            _orientation = orientation;

        }

        @SuppressWarnings("unused")
        @NotNull
        public IID getImageIdentifier() {

            return _imageID;

        }

        @NotNull
        public String format() {

            return "imageIdentifier=" + _imageID.format();

        }

        @Override
        public int compareTo( @NotNull final Object rhs ) {

            @SuppressWarnings("unchecked") ScaledImageId<IID> rhsAsId = (ScaledImageId<IID>)rhs;
            int rval = _imageID.compareTo( rhsAsId._imageID );
            if ( rval == 0 ) {

                rval = Integer.compare( _scaleGranularity, rhsAsId._scaleGranularity );
                if ( rval == 0 ) {

                    rval = Integer.compare( _orientation, rhsAsId._orientation );

                }

            }

            return rval;

        }

        public int getScaleGranularity() {

            return _scaleGranularity;

        }

        public int getOrientation() {

            return _orientation;

        }

        public String toString() {

            return "ScaledImageId( " +
                   format() + ", " +
                   "scaleGranularity=" + getScaleGranularity() + ", " +
                   "orientation=" + getOrientation() +
                   " )";

        }

    }

    private final LruCache<ScaledImageId<IID>, ImageIcon> _imageIconCache;

    private final @Nullable ImageIcon _fullSizeImage;
    private final IID _imageIdentifier;
    private int _orientation;

    private final int _scaleGranularity;
    private final Dimension _naturalSize;
    @NotNull private Dimension _currentSize;
    @Nullable private ImageIcon _currentSizeImage;

    public SimpleMediaItemPanel(
            @Nullable final ImageIcon fullSizeImage,
            @NotNull final IID imageIdentifier,
            @NotNull final LruCache<ScaledImageId<IID>, ImageIcon> scaledImageIconCache,
            int scaleGranularity,
            int initialOrientation
    ) {
        super();

        _fullSizeImage = fullSizeImage;
        _imageIdentifier = imageIdentifier;
        _imageIconCache = scaledImageIconCache;
        _orientation = initialOrientation;

        Logger.logMsg( "initial orientation is " + _orientation );

        setHorizontalAlignment( SwingConstants.LEFT );
        setVerticalAlignment( SwingConstants.TOP );

        if ( scaleGranularity <= 0 ) {

            throw new IllegalArgumentException( "SimpleMediaItemPanelBackup:  invalid scaleGranularity=" +
                                                scaleGranularity +
                                                " (must be a positive value)" );

        }

        _scaleGranularity = scaleGranularity;
        if ( fullSizeImage == null ) {

            _naturalSize = new Dimension( 100, 100 );

        } else {

            _naturalSize = new Dimension( fullSizeImage.getIconWidth(), fullSizeImage.getIconHeight() );

        }

        _currentSizeImage = fullSizeImage;
        _currentSize = new Dimension( _naturalSize );

    }

    @SuppressWarnings("unused")
    public SimpleMediaItemPanel(
            @Nullable final Image fullSizeImage,
            @NotNull final IID imageIdentifier,
            @NotNull final LruCache<ScaledImageId<IID>, ImageIcon> scaledImageIconCache,
            int scaleGranularity,
            int initialOrientation
    ) {
        this(
                fullSizeImage == null ? null : new ImageIcon( fullSizeImage ),
                imageIdentifier,
                scaledImageIconCache,
                scaleGranularity,
                initialOrientation
        );
    }

    public void setOrientation( int orientation, final double zoomFactor ) {

        if ( _orientation != orientation ) {

            _orientation = orientation;

        }

    }

    @SuppressWarnings("unused")
    public int getScaleGranularity() {

        return _scaleGranularity;

    }

    public Dimension refresh( double zoomFactor ) {

        Dimension naturalSize = ObtuseImageUtils.maybeRotateDimension( getNaturalSize(), getOrientation() );
        @SuppressWarnings("UnnecessaryLocalVariable") Dimension newCurrentSize = new Dimension(
                (int)Math.round( naturalSize.width * zoomFactor ),
                (int)Math.round( naturalSize.height * zoomFactor )
        );

        _currentSize = newCurrentSize;

        Optional<ImageIcon> ii = getScaledImage();
        ii.ifPresent( this::setIcon );

        revalidate();
        repaint();

        return _currentSize;

    }

    public int getOrientation() {

        return _orientation;

    }

    @NotNull
    public Dimension getNaturalSize() {

        return new Dimension( _naturalSize );

    }

    @SuppressWarnings("unused")
    @NotNull
    public Dimension getCurrentSize() {

        return new Dimension( _currentSize );

    }

    @SuppressWarnings("unused")
    public boolean hasActualImage() {

        return _fullSizeImage != null;

    }

    @SuppressWarnings("unused")
    @NotNull
    public Optional<ImageIcon> getOptFullSizeImage() {

        return Optional.ofNullable( _fullSizeImage );

    }

    @SuppressWarnings("unused")
    @NotNull Optional<ImageIcon> getOptCurrentSizeImage() {

        return Optional.ofNullable( _currentSizeImage );

    }

    //    public abstract ImageIcon getImageIcon();

    public Optional<ImageIcon> getScaledImage() {

        if ( _fullSizeImage == null ) {

            return Optional.empty();

        }

        Optional<CachedThing<ScaledImageId<IID>, ImageIcon>> optScaledImage = _imageIconCache.getOptional(
                new ScaledImageId<>(
                        _imageIdentifier,
                        _orientation,
                        Math.max( _currentSize.width, _currentSize.height )
                )
        );

        return optScaledImage.map( CachedThing::getThing );

    }

}
