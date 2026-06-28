package com.zergatul.cheatutils.mcp;

import java.util.ArrayList;
import java.util.List;

public class URITemplate {

    private final String template;
    private final TemplateChunk[] chunks;

    private URITemplate(String template, TemplateChunk[] chunks) {
        this.template = template;
        this.chunks = chunks;
    }

    public static URITemplate parse(String template) {
        Parser parser = new Parser(template);
        List<TemplateChunk> chunks = parser.parse();
        return new URITemplate(template, chunks.toArray(TemplateChunk[]::new));
    }

    public String getTemplate() {
        return template;
    }

    public boolean matches(String uri) {
        int position = 0;
        for (TemplateChunk chunk : chunks) {
            AdvanceResult result = chunk.tryAdvance(uri, position);
            if (!result.success) {
                return false;
            } else {
                position += result.offset;
            }
        }

        return true;
    }

    private static class Parser {

        private final String input;
        private final List<TemplateChunk> chunks;
        private int position;

        public Parser(String input) {
            this.input = input;
            this.chunks = new ArrayList<>();
            this.position = 0;
        }

        public List<TemplateChunk> parse() {
            while (position < input.length()) {
                if (input.charAt(position) == '{') {
                    parseExpansion();
                } else {
                    parseConstant();
                }
            }

            return chunks;
        }

        private void parseConstant() {
            StringBuilder builder = new StringBuilder();
            while (position < input.length() && input.charAt(position) != '{') {
                builder.append(input.charAt(position++));
            }

            chunks.add(new ConstantChunk(builder.toString()));
        }

        private void parseExpansion() {
            if (input.charAt(position) != '{') {
                throw new IllegalStateException();
            }

            position++;

            StringBuilder builder = new StringBuilder();
            while (input.charAt(position) != '}') {
                builder.append(input.charAt(position++));
            }

            position++;

            chunks.add(new SimpleStringExpansionChunk(builder.toString()));
        }
    }

    private static abstract class TemplateChunk {
        public abstract AdvanceResult tryAdvance(String uri, int position);
    }

    private static class ConstantChunk extends TemplateChunk {

        private final String text;

        public ConstantChunk(String text) {
            this.text = text;
        }

        @Override
        public AdvanceResult tryAdvance(String uri, int position) {
            if (position + text.length() > uri.length()) {
                return AdvanceResult.fail();
            }

            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != uri.charAt(position + i)) {
                    return AdvanceResult.fail();
                }
            }

            return AdvanceResult.success(text.length());
        }
    }

    private static class SimpleStringExpansionChunk extends TemplateChunk {

        private final String name;

        public SimpleStringExpansionChunk(String name) {
            this.name = name;
        }

        @Override
        public AdvanceResult tryAdvance(String uri, int position) {
            int offset = 0;
            StringBuilder value = new StringBuilder();
            while (position + offset < uri.length()) {
                char current = uri.charAt(position + offset);
                boolean isOk =
                        'a' <= current && current <= 'z' ||
                        'A' <= current && current <= 'Z' ||
                        '0' <= current && current <= '9' ||
                        current == '_';
                if (isOk) {
                    value.append(current);
                    offset++;
                } else {
                    break;
                }
            }

            if (offset == 0) {
                return AdvanceResult.fail();
            }

            return AdvanceResult.success(offset);
        }
    }

    private record AdvanceResult(boolean success, int offset) {

        public static AdvanceResult fail() {
            return new AdvanceResult(false, -1);
        }

        public static AdvanceResult success(int offset) {
            return new AdvanceResult(true, offset);
        }
    }
}