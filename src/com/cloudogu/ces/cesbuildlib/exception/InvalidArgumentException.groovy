package com.cloudogu.ces.cesbuildlib.exception

/**
 * Created by KeQingyuan on 2021/11/19.
 */
/**
 * An exception that can be thrown if an argument is invalid or missing.
 */
class InvalidArgumentException extends Exception {
    /**
     * The stage where the exception was thrown.
     */
    final String argument

    /**
     * Construct the exception.
     *
     * @param argument   The argument name which value is invalid.
     * @param message    The exception message.
     */
    InvalidArgumentException(String argument, String message = '') {
        super(message ? message : "Argument \"${argument}\" is not provided or invalid")

        this.argument = argument
    }
}
