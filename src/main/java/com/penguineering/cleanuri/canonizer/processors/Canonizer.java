package com.penguineering.cleanuri.canonizer.processors;

import java.net.URI;

import net.jcip.annotations.ThreadSafe;

/**
 * A URI canonizer brings URIs to a canonized form, so that redundant
 * information is removed and URIs can be compared.
 */
@ThreadSafe
public interface Canonizer {
    /**
     * Check if this canonizer is suitable for the provided URI.
     *
     * @param uri
     *            The URI to check.
     * @return true if the URI can be canonized, false if not.
     * @throws NullPointerException
     *             if the URI argument is null
     */
    boolean isSuitable(URI uri);

    /**
     * Canonize the provided URI. Two URIs are considered equal if their
     * canonized forms are equal.
     *
     * @param uri
     *            The URI to canonize.
     * @return The canonized URI.
     * @throws NullPointerException
     *             if the URI argument is null
     * @throws IllegalArgumentException
     *             if the URI cannot be canonized by the canonizer. In this case
     *             isSuitable should return false and this exception must not be
     *             thrown if isSuitable returns true.
     */
    URI canonize(URI uri);
}
