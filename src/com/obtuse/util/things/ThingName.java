/*
 * Copyright © 2017 Daniel Boulet
 * All rights reserved.
 */

package com.obtuse.util.things;

import com.obtuse.util.ObtuseUtil;
import org.jetbrains.annotations.NotNull;

/**
 Created by danny on 2017/10/07.
 */

public interface ThingName {

    @NotNull
    String getName();

    boolean actuallyExists();

    @NotNull
    default String enquote() {

        return ObtuseUtil.enquoteToJavaString( getName() );

    }

}