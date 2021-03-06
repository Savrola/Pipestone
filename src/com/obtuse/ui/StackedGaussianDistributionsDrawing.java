/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.ui;

import com.obtuse.util.BasicProgramConfigInfo;
import com.obtuse.util.WeightedGaussianDistribution;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Draw one or more stacked proportionally weighted Gaussian distributions.
 */

public class StackedGaussianDistributionsDrawing extends JPanel {

    private WeightedGaussianDistribution[] _gds;
    private final double _from;
    private final double _to;

    public StackedGaussianDistributionsDrawing() {
        //noinspection MagicNumber
        this(
                new WeightedGaussianDistribution[] {
                        new WeightedGaussianDistribution( 1.0, 0.5, 0.5 / 3 )
                },
                0.0,
                1.0
        );

    }

    public StackedGaussianDistributionsDrawing(
            final @NotNull WeightedGaussianDistribution@NotNull[] gds,
            final double from,
            final double to
    ) {

        super();

        _gds = Arrays.copyOf( gds, gds.length );
        _from = from;
        _to = to;

        //noinspection MagicNumber
        setMinimumSize( new Dimension( 400, 100 ) );
        //noinspection MagicNumber
        setMaximumSize( new Dimension( 400, 100 ) );

    }

    public void paint( final Graphics g ) {

        ( (Graphics2D)g ).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor( getBackground() );
        g.fillRect( 0, 0, getWidth(), getHeight() );

        double maxY = 0.0;
        for ( int pX = 0; pX < getWidth(); pX += 1 ) {

            double rX = mapXtoDrawing( pX, 0, getWidth() - 1, _from, _to );
            double rY = 0.0;
            for ( WeightedGaussianDistribution gd : _gds ) {

                rY += gd.getY( rX ) * gd.getWeight();

            }

            if ( rY > maxY ) {

                maxY = rY;

            }

        }

        if ( maxY == 0 ) {

            maxY = 1.0;

        }

        int height = getHeight();

        int[] x = new int[getWidth()];
        int[] y = new int[getWidth()];

        for ( int pX = 0; pX < getWidth(); pX += 1 ) {

            double rX = mapXtoDrawing( pX, 0, getWidth() - 1, _from, _to );
            double rY = 0.0;
            for ( WeightedGaussianDistribution gd : _gds ) {

                rY += gd.getY( rX ) * gd.getWeight();

            }

            x[pX] = pX;
            y[pX] = (int)( ( height - 1 ) * ( 1.0 - rY / maxY ) );

        }

        g.setColor( Color.BLACK );
        g.drawPolyline( x, y, getWidth() );

    }

    @SuppressWarnings("SameParameterValue")
    private double mapXtoDrawing( final int pX, final int minD, final int maxD, final double minR, final double maxR ) {

        //noinspection UnnecessaryParentheses
        return minR + ( ( pX - minD ) * ( maxR - minR ) ) / ( maxD - minD );

    }

    public static void main( final String[] args ) {

        BasicProgramConfigInfo.init( "Obtuse", "Shared", "GDD" );

        JFrame xx = new JFrame();
        StackedGaussianDistributionsDrawing gdd = new StackedGaussianDistributionsDrawing();
        xx.setContentPane( gdd );
        xx.pack();
        xx.setVisible( true );

    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDistributions( final Collection<WeightedGaussianDistribution> gds ) {

        _gds =
                gds.toArray( new WeightedGaussianDistribution[0] );

        repaint();

    }

    public void setDistributions( final @NotNull WeightedGaussianDistribution@NotNull[] gds ) {

        _gds = Arrays.copyOf( gds, gds.length );
        repaint();

    }

    public String toString() {

        return "StackedGaussianDistributionsDrawing( from=" + _from + ", to=" + _to + ", layerCount=" + _gds.length + " )";

    }

}
