package com.obtuse.util.packers.packer2;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

import org.jetbrains.annotations.NotNull;

/**
 Describe something that is packable.
 */

public interface Packable2 {

    InstanceId getInstanceId();

    @NotNull
    PackedEntityBundle bundleThyself( boolean isPackingSuper, Packer2 packer );

    void finishUnpacking( UnPacker2 unPacker );

//    PackingId2 getPackingId();

}
