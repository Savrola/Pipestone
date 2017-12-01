package com.obtuse.util.gowing;

import com.obtuse.util.gowing.p2a.GowingDePackedEntityGroup;
import com.obtuse.util.gowing.p2a.GowingEntityReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Optional;

/*
 * Copyright © 2015 Obtuse Systems Corporation
 */

/**
 Top level unpacking API.
 */

public interface GowingUnPacker extends Closeable {

    @NotNull
    Optional<GowingDePackedEntityGroup> unPack();

    void registerMetaDataHandler( @NotNull GowingMetaDataHandler handler );

    GowingPackable resolveReference( GowingEntityReference er );

    GowingUnPackerContext getUnPackerContext();

    /**
     Determine if a specified entity has been declared done.
     @param entityReference the entity reference for the entity of interest.
     @return {@code true} if {@code entityReference} is {@code null} or if it has been declared done
     by an earlier return value of {@code true} from its {@link GowingPackable#finishUnpacking(GowingUnPacker)} method.
     */

    boolean isEntityFinished( @Nullable GowingEntityReference entityReference );

}
