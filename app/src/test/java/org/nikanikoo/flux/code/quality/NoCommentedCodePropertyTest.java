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
 * Property-based tests for code quality - checking for commented code.
 * 
 * Feature: app-release-optimization, Property 11: No commented code
 * Validates: Requirements 2.3
 * 
 * Tests that Java source files do not contain blocks of commented-out code.
 * Commented code is identified by patterns like:
 * - Multiple consecutive lines starting with //
 * - Block comments containing code-like patterns (method calls, assignments, etc.)
 */
@RunWith(JUnitQuickcheck.class)
public class NoCommentedCodePropertyTest {

    // Patterns to identify commented code (not documentation)
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile(".*\\w+\\s*\\(.*\\)\\s*;.*");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(".*\\w+\\s*=\\s*.*");
    private static final Pattern CONTROL_FLOW_PATTERN = Pattern.compile(".*(if|for|while|switch|try|catch)\\s*\\(.*");
    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*import\\s+.*");
    private static final Pattern CLASS_DECLARATION_PATTERN = Pattern.compile(".*(class|interface|enum)\\s+\\w+.*");
    
    /**
     * Property: Java files should not contain commented-out code
     * 
     * For any Java source file in the project, the file should not contain
     * blocks of commented-out code (3+ consecutive comment lines with code patterns).
     */
    @Property(trials = 100)
    public void javaFilesShouldNotContainCommentedCode(
            @From(JavaFileGenerator.class) File javaFile) throws IOException {
        
        assertTrue("Java file should exist", javaFile.exists());
        assertTrue("Should be a file", javaFile.isFile());
        assertTrue("Should be a Java file", javaFile.getName().endsWith(".java"));
        
        List<CommentedCodeBlock> commentedBlocks = findCommentedCodeBlocks(javaFile);
        
        // If commented code blocks are found, create a detailed error message
        if (!commentedBlocks.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Found ").append(commentedBlocks.size())
                       .append(" block(s) of commented code in ")
                       .append(javaFile.getPath()).append(":\n");
            
            for (CommentedCodeBlock block : commentedBlocks) {
                errorMessage.append("  Lines ").append(block.startLine)
                           .append("-").append(block.endLine)
                           .append(": ").append(block.preview).append("\n");
            }
            
            assertFalse(errorMessage.toString(), true);
        }
    }

    /**
     * Property: Java files should not have long comment blocks with code patterns
     * 
     * For any Java source file, block comments should not contain
     * code-like patterns unless they are clearly documentation.
     */
    @Property(trials = 100)
    public void javaFilesShouldNotHaveBlockCommentedCode(
            @From(JavaFileGenerator.class) File javaFile) throws IOException {
        
        assertTrue("Java file should exist", javaFile.exists());
        
        String content = readFileContent(javaFile);
        List<BlockCommentedCode> blockComments = findBlockCommentedCode(content, javaFile);
        
        if (!blockComments.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Found ").append(blockComments.size())
                       .append(" block comment(s) with code patterns in ")
                       .append(javaFile.getPath()).append(":\n");
            
            for (BlockCommentedCode block : blockComments) {
                errorMessage.append("  Line ").append(block.lineNumber)
                           .append(": ").append(block.preview).append("\n");
            }
            
            assertFalse(errorMessage.toString(), true);
        }
    }

    /**
     * Find blocks of commented-out code in a Java file.
     * A block is considered commented code if:
     * - 3 or more consecutive lines start with //
     * - At least one line contains code patterns (method calls, assignments, etc.)
     * - Lines are not JavaDoc or regular documentation comments
     */
    private List<CommentedCodeBlock> findCommentedCodeBlocks(File file) throws IOException {
        List<CommentedCodeBlock> blocks = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            List<String> currentBlock = new ArrayList<>();
            int blockStartLine = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                
                // Check if line is a single-line comment
                if (trimmed.startsWith("//")) {
                    if (currentBlock.isEmpty()) {
                        blockStartLine = lineNumber;
                    }
                    currentBlock.add(trimmed.substring(2).trim());
                } else {
                    // End of comment block
                    if (currentBlock.size() >= 3) {
                        if (isCommentedCode(currentBlock)) {
                            String preview = currentBlock.get(0);
                            if (preview.length() > 50) {
                                preview = preview.substring(0, 50) + "...";
                            }
                            blocks.add(new CommentedCodeBlock(
                                blockStartLine, 
                                lineNumber - 1, 
                                preview
                            ));
                        }
                    }
                    currentBlock.clear();
                }
            }
            
            // Check last block
            if (currentBlock.size() >= 3 && isCommentedCode(currentBlock)) {
                String preview = currentBlock.get(0);
                if (preview.length() > 50) {
                    preview = preview.substring(0, 50) + "...";
                }
                blocks.add(new CommentedCodeBlock(
                    blockStartLine, 
                    lineNumber, 
                    preview
                ));
            }
        }
        
        return blocks;
    }

    /**
     * Check if a block of comments contains code patterns.
     */
    private boolean isCommentedCode(List<String> comments) {
        int codePatternCount = 0;
        
        for (String comment : comments) {
            // Skip empty lines and pure documentation
            if (comment.isEmpty() || isDocumentation(comment)) {
                continue;
            }
            
            // Check for code patterns
            if (METHOD_CALL_PATTERN.matcher(comment).matches() ||
                ASSIGNMENT_PATTERN.matcher(comment).matches() ||
                CONTROL_FLOW_PATTERN.matcher(comment).matches() ||
                IMPORT_PATTERN.matcher(comment).matches() ||
                CLASS_DECLARATION_PATTERN.matcher(comment).matches()) {
                codePatternCount++;
            }
        }
        
        // If at least 30% of non-empty lines contain code patterns, it's likely commented code
        int nonEmptyLines = 0;
        for (String comment : comments) {
            if (!comment.isEmpty() && !isDocumentation(comment)) {
                nonEmptyLines++;
            }
        }
        
        return nonEmptyLines > 0 && ((double) codePatternCount / nonEmptyLines) >= 0.3;
    }

    /**
     * Check if a comment line is documentation (not code).
     */
    private boolean isDocumentation(String comment) {
        String lower = comment.toLowerCase();
        
        // Common documentation keywords
        return lower.startsWith("todo") ||
               lower.startsWith("fixme") ||
               lower.startsWith("note") ||
               lower.startsWith("@") ||
               lower.matches("^[A-Z][a-z].*") || // Starts with capital letter (sentence)
               comment.length() < 10; // Very short comments are likely not code
    }

    /**
     * Find block comments that contain code patterns.
     */
    private List<BlockCommentedCode> findBlockCommentedCode(String content, File file) {
        List<BlockCommentedCode> blocks = new ArrayList<>();
        
        int pos = 0;
        int lineNumber = 1;
        
        while (pos < content.length()) {
            // Count lines up to current position
            for (int i = 0; i < pos && i < content.length(); i++) {
                if (content.charAt(i) == '\n') {
                    lineNumber++;
                }
            }
            
            // Find block comment start
            int blockStart = content.indexOf("/*", pos);
            if (blockStart == -1) {
                break;
            }
            
            // Find block comment end
            int blockEnd = content.indexOf("*/", blockStart + 2);
            if (blockEnd == -1) {
                break;
            }
            
            String blockContent = content.substring(blockStart + 2, blockEnd);
            
            // Skip JavaDoc comments
            if (!blockContent.trim().startsWith("*")) {
                // Check if block contains code patterns
                if (containsCodePatterns(blockContent)) {
                    String preview = blockContent.trim();
                    if (preview.length() > 50) {
                        preview = preview.substring(0, 50) + "...";
                    }
                    blocks.add(new BlockCommentedCode(lineNumber, preview));
                }
            }
            
            pos = blockEnd + 2;
        }
        
        return blocks;
    }

    /**
     * Check if block comment content contains code patterns.
     */
    private boolean containsCodePatterns(String content) {
        String[] lines = content.split("\n");
        int codePatternCount = 0;
        int totalLines = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("*")) {
                continue;
            }
            
            totalLines++;
            
            if (METHOD_CALL_PATTERN.matcher(trimmed).matches() ||
                ASSIGNMENT_PATTERN.matcher(trimmed).matches() ||
                CONTROL_FLOW_PATTERN.matcher(trimmed).matches() ||
                IMPORT_PATTERN.matcher(trimmed).matches()) {
                codePatternCount++;
            }
        }
        
        return totalLines > 0 && ((double) codePatternCount / totalLines) >= 0.3;
    }

    /**
     * Read entire file content as string.
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Class representing a block of commented code.
     */
    private static class CommentedCodeBlock {
        final int startLine;
        final int endLine;
        final String preview;

        CommentedCodeBlock(int startLine, int endLine, String preview) {
            this.startLine = startLine;
            this.endLine = endLine;
            this.preview = preview;
        }
    }

    /**
     * Class representing block commented code.
     */
    private static class BlockCommentedCode {
        final int lineNumber;
        final String preview;

        BlockCommentedCode(int lineNumber, String preview) {
            this.lineNumber = lineNumber;
            this.preview = preview;
        }
    }

    /**
     * Generator for Java files from the project.
     */
    public static class JavaFileGenerator extends com.pholser.junit.quickcheck.generator.Generator<File> {
        
        private static List<File> javaFiles = null;

        public JavaFileGenerator() {
            super(File.class);
        }

        @Override
        public File generate(
                com.pholser.junit.quickcheck.random.SourceOfRandomness random,
                com.pholser.junit.quickcheck.generator.GenerationStatus status) {
            
            if (javaFiles == null) {
                javaFiles = findAllJavaFiles();
            }
            
            if (javaFiles.isEmpty()) {
                throw new IllegalStateException("No Java files found in project");
            }
            
            return random.choose(javaFiles);
        }

        /**
         * Find all Java files in the project source directory.
         * Tries multiple strategies to locate the source directory.
         */
        private static List<File> findAllJavaFiles() {
            List<File> files = new ArrayList<>();
            
            // Strategy 1: Try user.dir
            File projectRoot = new File(System.getProperty("user.dir"));
            File sourceDir = new File(projectRoot, "app/src/main/java");
            
            // Strategy 2: If we're already in the app directory
            if (!sourceDir.exists()) {
                sourceDir = new File(projectRoot, "src/main/java");
            }
            
            // Strategy 3: Go up one level (in case we're in a subdirectory)
            if (!sourceDir.exists()) {
                projectRoot = projectRoot.getParentFile();
                if (projectRoot != null) {
                    sourceDir = new File(projectRoot, "app/src/main/java");
                }
            }
            
            // Strategy 4: Use relative path from test directory
            if (!sourceDir.exists()) {
                sourceDir = new File("../main/java");
            }
            
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                collectJavaFiles(sourceDir, files);
            }
            
            return files;
        }

        /**
         * Recursively collect all Java files from a directory.
         */
        private static void collectJavaFiles(File directory, List<File> files) {
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        collectJavaFiles(child, files);
                    } else if (child.isFile() && child.getName().endsWith(".java")) {
                        files.add(child);
                    }
                }
            }
        }
    }
}
