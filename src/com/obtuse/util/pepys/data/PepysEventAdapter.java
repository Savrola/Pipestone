package com.obtuse.util.pepys.data;

import com.obtuse.util.pepys.Pepys;
import org.jetbrains.annotations.NotNull;

/**
 An adapter for Pepys events.
 <p/>
 This class implements all the {@link PepysEventListener} methods as empty methods.
 The user of this class should override those methods which are of interest.
 */

@SuppressWarnings("unused")
public class PepysEventAdapter implements PepysEventListener {

    @Override
    public void PepysSourceCreated( final @NotNull Pepys.PepysEvent event ) {

    }

    @Override
    public void PepysSourceChanged( final @NotNull Pepys.PepysEvent event ) {

    }

    @Override
    public void PepysSourceGone( final @NotNull Pepys.PepysEvent event ) {

    }

}
