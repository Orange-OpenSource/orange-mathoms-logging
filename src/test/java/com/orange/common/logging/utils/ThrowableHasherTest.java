/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Deque;

import static org.junit.Assert.*;


public class ThrowableHasherTest {

    private static class StackTraceElementGenerator {
        public static void generateSingle() {
            oneSingle();
        }

        public static void oneSingle() {
            twoSingle();
        }

        private static void twoSingle() {
            threeSingle();
        }

        private static void threeSingle() {
            four();
        }

        private static void four() {
            five();
        }

        private static void five() {
            six();
        }

        private static void six() {
            seven();
        }

        private static void seven() {
            eight();
        }

        private static void eight() {
            throw new RuntimeException("message");
        }

        public static void generateCausedBy() {
            oneCausedBy();
        }

        private static void oneCausedBy() {
            twoCausedBy();
        }

        private static void twoCausedBy() {
            try {
                threeSingle();
            } catch (RuntimeException e) {
                throw new RuntimeException("wrapper", e);
            }
        }

        public static void generateSuppressed() {
            oneSuppressed();
        }

        private static void oneSuppressed() {
            twoSuppressed();
        }

        private static void twoSuppressed() {
            try {
                threeSingle();
            } catch (RuntimeException e) {
                RuntimeException newException = new RuntimeException();
                newException.addSuppressed(e);
                throw newException;
            }
        }
    }

    @Test
    public void one_hash_should_be_generated() {
        try {
            StackTraceElementGenerator.generateSingle();
            Assert.fail();
        } catch (RuntimeException e) {
            // GIVEN
            ThrowableHasher hasher = new ThrowableHasher();

            // WHEN
            Deque<String> hashes = hasher.hexHashes(e);

            // THEN
            Assert.assertEquals(1, hashes.size());
        }
    }

    @Test
    public void two_hashes_should_be_generated() {
        try {
            StackTraceElementGenerator.generateCausedBy();
            Assert.fail();
        } catch (RuntimeException e) {
            // GIVEN
            ThrowableHasher hasher = new ThrowableHasher();

            // WHEN
            Deque<String> hashes = hasher.hexHashes(e);

            // THEN
            Assert.assertEquals(2, hashes.size());
        }
    }
    private static class OnlyFromStackTraceElementGeneratorFilter extends StackElementFilter {
        @Override
        public boolean accept(StackTraceElement element) {
            return element.getClassName().equals(StackTraceElementGenerator.class.getName());
        }
    }

    /**
     * Warning: computes expected hash based on StackTraceElementGenerator elements
     *
     * do not change methods name, line or it will break the test
     */
    @Test
    public void expected_hash_should_be_generated() {
        try {
            StackTraceElementGenerator.generateSingle();
            Assert.fail();
        } catch (RuntimeException e) {
            // GIVEN
            ThrowableHasher hasher = new ThrowableHasher(new OnlyFromStackTraceElementGeneratorFilter());

            // WHEN
            Deque<String> hashes = hasher.hexHashes(e);

            // THEN
            Assert.assertEquals(1, hashes.size());
            Assert.assertEquals("b0627fad", hashes.getFirst());
        }
    }
}