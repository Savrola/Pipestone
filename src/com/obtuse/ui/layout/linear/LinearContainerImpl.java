package com.obtuse.ui.layout.linear;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

import com.obtuse.ui.layout.LinearOrientation;
import org.jetbrains.annotations.NotNull;

/**
 The current 'official' linear container implementation.
 */

public class LinearContainerImpl extends LinearContainer3 {

    public LinearContainerImpl( @NotNull String name, LinearOrientation orientation ) {

        this( name, orientation, null, null );

    }

    public LinearContainerImpl(
            @NotNull String name,
            LinearOrientation orientation,
            @SuppressWarnings("SameParameterValue") ContainerConstraints containerConstraints,
            @SuppressWarnings("SameParameterValue") ComponentConstraints componentConstraints
    ) {

        super( name, orientation, containerConstraints, componentConstraints );

    }

}
