package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
        CompletionProvider completionProvider = new CompletionProvider(documentationProvider);

        Pattern regex = Pattern.compile("Java<com\\.zergatul\\.cheatutils\\.scripting\\.modules\\.(.+)>");

        server.createContext(prefix, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    String path = exchange.getRequestURI().getPath();
                    if (path.equals(prefix + "tokenize")) {
                        Gson gson = new GsonBuilder().create();
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        String request = new String(data, Charset.defaultCharset());
                        String code = gson.fromJson(request, String.class);

                        Lexer lexer = new Lexer(new LexerInput(code));
                        LexerOutput output = lexer.lex();
                        Json.sendResponse(exchange, output);
                    } else if (path.equals(prefix + "diagnostics")) {
                        Gson gson = new GsonBuilder().create();
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
                        Json.sendResponse(exchange, Arrays.stream(TokenType.values()).map(Enum::name).toArray());
                    } else if (path.equals(prefix + "nodes")) {
                        Json.sendResponse(exchange, Arrays.stream(NodeType.values()).map(Enum::name).toArray());
                    } else if (path.equals(prefix + "token-rules/light")) {
                        Json.sendResponse(exchange, Arrays.stream(TokenType.values()).map(type -> new TokenRule(type.name(), light.getTokenColor(type))).toArray());
                    } else if (path.equals(prefix + "token-rules/dark")) {
                        Json.sendResponse(exchange, Arrays.stream(TokenType.values()).map(type -> new TokenRule(type.name(), dark.getTokenColor(type))).toArray());
                    } else if (path.startsWith(prefix + "hover/")) {
                        String theme = path.substring(path.indexOf("/hover/") + 7);

                        Gson gson = new GsonBuilder().create();
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        Parser parser = new Parser(lexerOutput);
                        ParserOutput parserOutput = parser.parse();

                        Binder binder = new Binder(parserOutput, resolver.resolve(request.type));
                        BinderOutput binderOutput = binder.bind();

                        List<BoundNode> chain = new ArrayList<>();
                        findChain(chain, binderOutput.unit(), request.line, request.column);

                        Json.sendResponse(exchange, new HoverProvider(theme.equals("light") ? light : dark, documentationProvider).get(chain));
                    } else if (path.equals(prefix + "definition")) {
                        Gson gson = new GsonBuilder().create();
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
                        Gson gson = new GsonBuilder().create();
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        CompletionRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), CompletionRequest.class);

                        LexerInput lexerInput = new LexerInput(request.code);
                        Lexer lexer = new Lexer(lexerInput);
                        LexerOutput lexerOutput = lexer.lex();

                        boolean sent = false;
                        for (Token token : lexerOutput.tokens()) {
                            if (token.type == TokenType.COMMENT) {
                                CommentToken comment = (CommentToken) token;
                                boolean inside;
                                if (comment.ending) {
                                    inside = comment.getRange().containsOrEnds(request.line, request.column);
                                } else {
                                    inside = comment.getRange().contains(request.line, request.column);
                                }
                                if (inside) {
                                    sent = true;
                                    Json.sendResponse(exchange, List.of());
                                    break;
                                }
                            }
                        }

                        if (!sent) {
                            Parser parser = new Parser(lexerOutput);
                            ParserOutput parserOutput = parser.parse();

                            CompilationParameters parameters = resolver.resolve(request.type);
                            Binder binder = new Binder(parserOutput, parameters);
                            BinderOutput binderOutput = binder.bind();

                            Json.sendResponse(exchange, completionProvider.get(parameters, binderOutput, request.line, request.column));
                        }
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                    exchange.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(503, 0);
                    exchange.close();
                }
            }
        });

        /*server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }

                Path filepath = Path.of(".\\src\\main\\resources\\web", path);
                if (Files.exists(filepath)) {
                    if (path.endsWith(".js")) {
                        exchange.getResponseHeaders().add("Content-Type", "text/javascript");
                    } else if (path.endsWith(".html")) {
                        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                    } else if (path.endsWith(".ttf")) {
                        exchange.getResponseHeaders().add("Content-Type", "font/ttf");
                    }

                    long size = Files.size(filepath);
                    exchange.sendResponseHeaders(200, size);
                    byte[] data = Files.readAllBytes(filepath);
                    exchange.getResponseBody().write(data);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }

                exchange.close();
            }
        });*/
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

    private static void findChain(List<BoundNode> chain, BoundNode node, int line, int column) {
        if (node.getRange().contains(line, column)) {
            for (BoundNode child : node.getChildren()) {
                if (child.getRange().contains(line, column)) {
                    findChain(chain, child, line, column);
                }
            }
            chain.add(node);
        }
    }

    public record TokenRule(String token, String foreground) {}

    public record DiagnosticsRequest(String code, String type) {}

    public record DiagnosticsResponseItem(TextRange range, String message) {}

    public record HoverRequest(String code, String type, int line, int column) {}

    public record CompletionRequest(String code, String type, int line, int column) {}
}