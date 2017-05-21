/*
 * Copyright © 2017 Daniel Boulet
 * All rights reserved.
 */

package com.obtuse.ui.layout.linear;

import com.obtuse.ui.layout.LinearOrientation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 A scrollable {@link LinearContainer3} variant.
 <p/>Provides an abstract class which makes creating {@link LinearContainer3}s that are scrollable easier.
 */

public abstract class AbstractScrollableLinearContainer3 extends LinearContainer3 implements Scrollable {

    public AbstractScrollableLinearContainer3( @NotNull String name, LinearOrientation orientation ) {
	this( name, orientation, null, null );

    }

    AbstractScrollableLinearContainer3(
	    @NotNull String name,
	    LinearOrientation orientation,
	    @SuppressWarnings("SameParameterValue") ContainerConstraints containerConstraints,
	    @SuppressWarnings("SameParameterValue") ComponentConstraints componentConstraints
    ) {
	super( name, orientation, containerConstraints, componentConstraints );

    }

}
