package com.github.rvesse.airline.io;

import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * Class used to track the state of a control allowing it to be lazily written
 * to the output only if necessary and ensuring it can be appropriately reset
 * when necessary
 *
 * @param <T>
 *            Attribute source type
 */
public abstract class ControlTracker<T> {

    protected final ControlCodeSource<T> provider;
    private T current, previous;
    private boolean requireWrite = false;

    public ControlTracker(ControlCodeSource<T> provider) {
        Preconditions.checkNotNull(provider);
        this.provider = provider;
    }

    /**
     * Sets the value for the control
     * 
     * @param value
     *            Value
     */
    public final void set(T value) {
        this.current = value;
        this.requireWrite = this.current != null && !this.current.equals(this.previous);
    }

    /**
     * Resets the control to the default unintialized state, if necessary this
     * will will cause the
     * 
     * @throws IOException
     */
    public final void reset() throws IOException {
        if (this.previous != null) {
            this.resetInternal(this.previous);
            this.previous = null;
        }
    }

    /**
     * Method that derived classes must implement to add the actual logic for
     * resetting the control against the stream
     * 
     * @param value
     *            Value to be reset from
     * @throws IOException
     */
    protected abstract void resetInternal(T value) throws IOException;

    /**
     * Applies the control, if the current state of the control has not been
     * previously applied to the stream then it will be applied now
     * 
     * @throws IOException
     */
    public final void apply() throws IOException {
        if (this.requireWrite) {
            this.applyInternal(this.current);
            this.previous = this.current;
            this.requireWrite = false;
        }
    }

    /**
     * Method that derived classes must implement to add the actual logic for
     * applying the control to the stream
     * 
     * @param value
     *            Value to be applied
     * @throws IOException
     */
    protected abstract void applyInternal(T value) throws IOException;
}
