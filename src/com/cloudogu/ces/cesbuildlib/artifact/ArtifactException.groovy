package com.cloudogu.ces.cesbuildlib.artifact

/**
 * Created by KeQingyuan on 2021/11/19.
 */
/**
 * An exception that can be thrown from the {@link com.cloudogu.ces.cesbuildlib.artifact.ArtifactException} classes.
 */
class ArtifactException extends Exception {
    /**
     * Construct the exception.
     *
     * @param message The exception message.
     */
    ArtifactException(String message = '') {
        super(message)
    }
}
