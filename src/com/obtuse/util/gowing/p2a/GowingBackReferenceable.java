package com.obtuse.util.gowing.p2a;

import com.obtuse.util.gowing.GowingPackable;
import com.obtuse.util.gowing.GowingUnPacker;

import java.util.List;

/**
 Something that is back-referenceable immediately after creation.
 <p>Instances of classes that implement this interface have their
 {@link com.obtuse.util.gowing.GowingPackable#finishUnpacking(GowingUnPacker)}
 method called immediately after being created. That call <b><u>MUST</u></b>
 return {@code true} and be the truth else chaos reign across the land.</p>
 */

public interface GowingBackReferenceable extends GowingPackable {

//    List<GowingBackReferenceable> getDependencies();

}
