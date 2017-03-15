package com.orange.common.logging.utils;

import com.google.common.base.Strings;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Utility class that generates a unique signature hash for any Java {@link Throwable error}
 *
 * @author pismy
 */
public class ErrorSignature {

    private ErrorSignature() {
    }

    /**
     * Generates a Hexadecimal signature hash for the given error
     * <p>
     * Two errors with the same signature hash are most probably same errors
     */
    public static String hexHash(Throwable error) {
        return hexHashes(error).peek();
    }

    /**
     * Generates and returns Hexadecimal signature hashes for the complete error stack
     * <p>
     * Two errors with the same signature hash are most probably same hashes
     */
    public static Deque<String> hexHashes(Throwable error) {
        Deque<String> hexHashes = new ArrayDeque<>();
        ErrorSignature.hash(error, hexHashes);
        return hexHashes;
    }

    /**
     * Generates a signature hash (int)
     * <p>
     * Two errors with the same signature hash are most probably same errors
     */
    private static int hash(Throwable error, Deque<String> hexHashes) {
        int hash = 0;

        // compute parent error hash
        if (error.getCause() != null && error.getCause() != error) {
            // has parent error
            hash = hash(error.getCause(), hexHashes);
        }

        // then this error hash
        // hash error classname
        hash = 31 * hash + error.getClass().getName().hashCode();
        // hash stacktrace
        for (StackTraceElement element : error.getStackTrace()) {
            if (skip(element)) {
                continue;
            }
            hash = 31 * hash + hash(element);
        }

        String hex = Strings.padStart(Integer.toHexString(hash), 8, '0');
        hexHashes.push(hex);

        return hash;
    }

    private static boolean skip(StackTraceElement element) {
        // skip null element or generated class
        return element == null || element.getFileName() == null || element.getLineNumber() < 0;
    }

    private static int hash(StackTraceElement element) {
        int result = element.getClassName().hashCode();
        result = 31 * result + element.getMethodName().hashCode();
        // filename is probably not necessary
        result = 31 * result + element.getLineNumber();
        return result;
    }
}
