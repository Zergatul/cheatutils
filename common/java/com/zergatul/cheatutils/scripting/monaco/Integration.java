package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.completion.CompletionProviderFactory;
import com.zergatul.scripting.highlighting.HighlightingProvider;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.hover.HoverProvider;
import com.zergatul.scripting.hover.Theme;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Integration {

    public void attach(HttpServer server, String prefix) {
        CompilationParametersResolver resolver = type -> ScriptType.valueOf(type).createParameters();

        Theme dark = new DarkTheme();
        Theme light = new WhiteTheme();
        DocumentationProvider documentationProvider = new DocumentationProvider();
        DefinitionProvider definitionProvider = new DefinitionProvider();
        CompletionProviderFactory<Suggestion> completionProviderFactory = new CompletionProviderFactory<>(new MonacoSuggestionFactory(documentationProvider));

        Pattern regex = Pattern.compile("Java<com\\.zergatul\\.cheatutils\\.scripting\\.modules\\.(.+)>");
        Pattern rgbRegex = Pattern.compile("^#[0-9a-fA-F]{6}$");
        Pattern rgbaRegex = Pattern.compile("^#[0-9a-fA-F]{8}$");

        Gson gson = new GsonBuilder().create();

        server.createContext(prefix, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    String path = exchange.getRequestURI().getPath();
                    if (path.equals(prefix + "tokenize")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        TokenizeRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), TokenizeRequest.class);

                        Lexer lexer = new Lexer(new LexerInput(request.code));
                        LexerOutput lexerOutput = lexer.lex();
                        ParserOutput parserOutput = new Parser(lexerOutput).parse();
                        BinderOutput binderOutput = new Binder(parserOutput, resolver.resolve(request.type)).bind();
                        HighlightingProvider provider = new HighlightingProvider(lexerOutput, binderOutput);

                        Json.sendResponse(exchange, provider.get().stream().map(MonacoSemanticToken::new).filter(t -> t.type >= 0).toList());
                    } else if (path.equals(prefix + "color-strings")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        String code = gson.fromJson(new String(data, Charset.defaultCharset()), String.class);

                        Lexer lexer = new Lexer(new LexerInput(code));
                        LexerOutput lexerOutput = lexer.lex();

                        List<MonacoColoredTokenEntry> entries = new ArrayList<>();
                        for (Token token : lexerOutput.tokens()) {
                            if (token.is(TokenType.STRING_LITERAL)) {
                                ValueToken strToken = (ValueToken) token;
                                if (rgbRegex.matcher(strToken.value).matches() || rgbaRegex.matcher(strToken.value).matches()) {
                                    Color color = ColorUtils.parseColor2(strToken.value);
                                    entries.add(new MonacoColoredTokenEntry(color, token.getRange()));
                                }
                            }
                        }

                        Json.sendResponse(exchange, entries);
                    } else if (path.equals(prefix + "diagnostics")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        DiagnosticsRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), DiagnosticsRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        Parser parser = new Parser(lexerOutput);
                        ParserOutput parserOutput = parser.parse();

                        Binder binder = new Binder(parserOutput, resolver.resolve(request.type));
                        BinderOutput binderOutput = binder.bind();

                        Json.sendResponse(exchange, binderOutput.diagnostics()
                                .stream()
                                .map(d -> {
                                    StringBuilder sb = new StringBuilder();
                                    Matcher matcher = regex.matcher(d.message);
                                    while (matcher.find()) {
                                        matcher.appendReplacement(sb, "");
                                        sb.append(matcher.group(1));
                                    }
                                    matcher.appendTail(sb);
                                    return new DiagnosticsResponseItem(d.range, sb.toString());
                                })
                                .toArray());
                    } else if (path.equals(prefix + "tokens")) {
                        Json.sendResponse(exchange, monacoTokenList);
                    } else if (path.startsWith(prefix + "hover/")) {
                        String theme = path.substring(path.indexOf("/hover/") + 7);

                        byte[] data = exchange.getRequestBody().readAllBytes();
                        HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        Parser parser = new Parser(lexerOutput);
                        ParserOutput parserOutput = parser.parse();

                        Binder binder = new Binder(parserOutput, resolver.resolve(request.type));
                        BinderOutput binderOutput = binder.bind();

                        CustomHoverProvider provider = new CustomHoverProvider(theme.equals("light") ? light : dark);
                        HoverProvider.HoverResponse response = provider.get(binderOutput, request.line, request.column);
                        Json.sendResponse(exchange, response);
                    } else if (path.equals(prefix + "definition")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        Parser parser = new Parser(lexerOutput);
                        ParserOutput parserOutput = parser.parse();

                        Binder binder = new Binder(parserOutput, resolver.resolve(request.type));
                        BinderOutput binderOutput = binder.bind();

                        BoundNode node = find(binderOutput.unit(), request.line, request.column);
                        Json.sendResponse(exchange, definitionProvider.get(node), TextRange.class);
                    } else if (path.equals(prefix + "completion")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        CompletionRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), CompletionRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        Parser parser = new Parser(lexerOutput);
                        ParserOutput parserOutput = parser.parse();

                        CompilationParameters parameters = resolver.resolve(request.type);
                        Binder binder = new Binder(parserOutput, parameters);
                        BinderOutput binderOutput = binder.bind();

                        Json.sendResponse(exchange, completionProviderFactory.getSuggestions(parameters, binderOutput, request.line, request.column));
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                    exchange.close();
                } catch (Throwable e) {
                    exchange.sendResponseHeaders(503, 0);
                    exchange.close();
                }
            }
        });
    }

    private static BoundNode find(BoundNode node, int line, int column) {
        if (node.getRange().contains(line, column)) {
            for (BoundNode child : node.getChildren()) {
                if (child.getRange().contains(line, column)) {
                    return find(child, line, column);
                }
            }
            return node;
        } else {
            return null;
        }
    }

    private final String[] monacoTokenList = new String[] {
            "keyword", "method", "property", "variable", "type", "delimiter", "operator", "number", "string", "comment"
    };

    private static int toMonacoTokenIndex(SemanticTokenType type) {
        return switch (type) {
            case KEYWORD -> 0;
            case METHOD -> 1;
            case PROPERTY -> 2;
            case IDENTIFIER -> 3;
            case TYPE -> 4;
            case BRACKET -> -1;
            case SEPARATOR -> 5;
            case OPERATOR, ARROW -> 6;
            case NUMBER -> 7;
            case STRING -> 8;
            case COMMENT -> 9;
        };
    }

    public record TokenizeRequest(String code, String type) {}

    public record DiagnosticsRequest(String code, String type) {}

    public record DiagnosticsResponseItem(TextRange range, String message) {}

    public record HoverRequest(String code, String type, int line, int column) {}

    public record CompletionRequest(String code, String type, int line, int column) {}

    public record MonacoSemanticToken(int type, TextRange range) {
        public MonacoSemanticToken(SemanticToken token) {
            this(toMonacoTokenIndex(token.type()), token.range());
        }
    }

    public record MonacoColor(float red, float green, float blue, float alpha) {}

    public record MonacoRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {}

    public record MonacoColoredTokenEntry(MonacoColor color, MonacoRange range) {
        public MonacoColoredTokenEntry(Color color, TextRange range) {
            this(
                    new MonacoColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f),
                    new MonacoRange(range.getLine1(), range.getColumn1(), range.getLine2(), range.getColumn2()));
        }
    }
}