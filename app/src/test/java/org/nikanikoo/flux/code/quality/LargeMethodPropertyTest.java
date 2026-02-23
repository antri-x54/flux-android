package org.nikanikoo.flux.code.quality;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Property-based tests for code quality - checking for large methods.
 * 
 * Feature: app-release-optimization, Property 10: Large methods are refactored
 * Validates: Requirements 3.2
 * 
 * Tests that Java methods do not exceed 50 lines of code.
 * Large methods should be refactored into smaller, more focused methods.
 */
@RunWith(JUnitQuickcheck.class)
public class LargeMethodPropertyTest {

    private static final int MAX_METHOD_LINES = 50;
    
    // Pattern to detect method declarations
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s*(public|private|protected|static|final|synchronized|native|abstract|\\s)*" +
        "\\s+[\\w<>\\[\\]]+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{?\\s*$"
    );

    /**
     * Property: Java methods should not exceed 50 lines
     * 
     * For any Java source file in the project, all methods should contain
     * fewer than 50 lines of code to maintain readability and testability.
     */
    @Property(trials = 100)
    public void javaMethodsShouldNotExceed50Lines(
            @From(NoCommentedCodePropertyTest.JavaFileGenerator.class) File javaFile) throws IOException {
        
        assertTrue("Java file should exist", javaFile.exists());
        assertTrue("Should be a file", javaFile.isFile());
        assertTrue("Should be a Java file", javaFile.getName().endsWith(".java"));
        
        List<LargeMethod> largeMethods = findLargeMethods(javaFile);
        
        if (!largeMethods.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Found ").append(largeMethods.size())
                       .append(" method(s) exceeding ").append(MAX_METHOD_LINES)
                       .append(" lines in ").append(javaFile.getName()).append(":\n");
            
            for (LargeMethod method : largeMethods) {
                errorMessage.append("  - ").append(method.name)
                           .append(" (lines ").append(method.startLine)
                           .append("-").append(method.endLine)
                           .append(", ").append(method.lineCount).append(" lines)\n");
            }
            
            errorMessage.append("Consider refactoring these methods into smaller, more focused methods.");
            
            assertFalse(errorMessage.toString(), true);
        }
    }

    /**
     * Find all methods in a Java file that exceed the maximum line count.
     */
    private List<LargeMethod> findLargeMethods(File file) throws IOException {
        List<LargeMethod> largeMethods = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            String currentMethodName = null;
            int methodStartLine = 0;
            int braceDepth = 0;
            int methodBraceDepth = 0;
            boolean inMethod = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                
                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                    continue;
                }
                
                // Check for method declaration
                if (!inMethod && METHOD_PATTERN.matcher(trimmed).matches()) {
                    currentMethodName = extractMethodName(trimmed);
                    methodStartLine = lineNumber;
                    inMethod = true;
                    methodBraceDepth = braceDepth;
                    
                    // Count opening braces on the same line
                    for (char c : line.toCharArray()) {
                        if (c == '{') braceDepth++;
                    }
                }
                
                // Count braces
                if (inMethod) {
                    for (char c : line.toCharArray()) {
                        if (c == '{') {
                            braceDepth++;
                        } else if (c == '}') {
                            braceDepth--;
                            
                            // Check if we've closed the method
                            if (braceDepth == methodBraceDepth) {
                                int methodLineCount = lineNumber - methodStartLine + 1;
                                
                                if (methodLineCount > MAX_METHOD_LINES) {
                                    largeMethods.add(new LargeMethod(
                                        currentMethodName,
                                        methodStartLine,
                                        lineNumber,
                                        methodLineCount
                                    ));
                                }
                                
                                inMethod = false;
                                currentMethodName = null;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return largeMethods;
    }

    /**
     * Extract method name from a method declaration line.
     */
    private String extractMethodName(String line) {
        // Remove modifiers and return type
        String[] parts = line.trim().split("\\s+");
        
        for (String part : parts) {
            if (part.contains("(")) {
                return part.substring(0, part.indexOf('('));
            }
        }
        
        return "unknown";
    }

    /**
     * Class representing a large method.
     */
    private static class LargeMethod {
        final String name;
        final int startLine;
        final int endLine;
        final int lineCount;

        LargeMethod(String name, int startLine, int endLine, int lineCount) {
            this.name = name;
            this.startLine = startLine;
            this.endLine = endLine;
            this.lineCount = lineCount;
        }
    }
}
