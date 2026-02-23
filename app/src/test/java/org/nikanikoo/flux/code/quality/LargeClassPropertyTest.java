package org.nikanikoo.flux.code.quality;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Property-based tests for code quality - checking for large classes.
 * 
 * Feature: app-release-optimization, Property 9: Large classes are refactored
 * Validates: Requirements 3.1
 * 
 * Tests that Java classes do not exceed 500 lines of code.
 * Large classes should be refactored into smaller, more maintainable components.
 */
@RunWith(JUnitQuickcheck.class)
public class LargeClassPropertyTest {

    private static final int MAX_CLASS_LINES = 500;

    /**
     * Property: Java classes should not exceed 500 lines
     * 
     * For any Java source file in the project, the class should contain
     * fewer than 500 lines of code to maintain readability and maintainability.
     */
    @Property(trials = 100)
    public void javaClassesShouldNotExceed500Lines(
            @From(NoCommentedCodePropertyTest.JavaFileGenerator.class) File javaFile) throws IOException {
        
        assertTrue("Java file should exist", javaFile.exists());
        assertTrue("Should be a file", javaFile.isFile());
        assertTrue("Should be a Java file", javaFile.getName().endsWith(".java"));
        
        int lineCount = countLines(javaFile);
        
        String errorMessage = String.format(
            "Class %s has %d lines, which exceeds the maximum of %d lines. " +
            "Consider refactoring into smaller components.",
            javaFile.getName(),
            lineCount,
            MAX_CLASS_LINES
        );
        
        assertTrue(errorMessage, lineCount <= MAX_CLASS_LINES);
    }

    /**
     * Count the number of lines in a file.
     * Counts all lines including empty lines and comments.
     */
    private int countLines(File file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }
}
