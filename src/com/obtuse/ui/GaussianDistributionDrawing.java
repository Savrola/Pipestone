/*
 Copyright © 2014 Daniel Boulet
 */

package com.obtuse.ui;

import com.obtuse.util.GaussianDistribution;
import com.obtuse.util.WeightedGaussianDistribution;

/**
 * Draw a single gaussian distribution.
 */

@SuppressWarnings("UnusedDeclaration")
public class GaussianDistributionDrawing extends StackedGaussianDistributionsDrawing {

    public GaussianDistributionDrawing() {

        super();

    }

    public GaussianDistributionDrawing( final GaussianDistribution gd ) {

        this( gd, 0.0, 1.0 );

    }

    @SuppressWarnings("SameParameterValue")
    public GaussianDistributionDrawing(
            final GaussianDistribution gd,
            final double from,
            final double to
    ) {

        super(
                new WeightedGaussianDistribution[] {
                        new WeightedGaussianDistribution(
                                1,
                                gd.getCenter(),
                                gd.getStandardDeviation()
                        )
                },
                from,
                to
        );

    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDistribution( final GaussianDistribution gd ) {

        setDistributions(
                new WeightedGaussianDistribution[] {
                        new WeightedGaussianDistribution(
                                1,
                                gd.getCenter(),
                                gd.getStandardDeviation()
                        )
                }
        );

    }

}
