package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.utils.ClassPathExplorer;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.webui.WebHelper;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.analysis.definition.DefinitionProvider;
import com.zergatul.scripting.analysis.hover.HoverInfoFactory;
import com.zergatul.scripting.analysis.hover.HoverProvider;
import com.zergatul.scripting.analysis.hover.MappedHoverFactory;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.completion.CompletionProviderFactory;
import com.zergatul.scripting.completion.MappedSuggestionFactory;
import com.zergatul.scripting.completion.SuggestionInfoFactory;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.highlighting.HighlightingProvider;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.highlighting.SemanticTokenModifier;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.lexer.*;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Integration {

    public void attach(HttpServer server, String prefix) {
        CompilationParametersResolver resolver = type -> ScriptCompilerRegistry.INSTANCE.getParameters(ScriptType.valueOf(type));

        TypeDisplayFormatter typeFormatter = new TypeDisplayFormatter(clazz ->
                clazz.getName().startsWith("com.zergatul.cheatutils.scripting") ? clazz.getSimpleName() : clazz.getName());
        CompletionProviderFactory<Suggestion> completionProviderFactory = new CompletionProviderFactory<>(
                new MappedSuggestionFactory<>(
                        new SuggestionInfoFactory(typeFormatter),
                        new MonacoSuggestionMapper()),
                ClassPathExplorer.INSTANCE);
        HoverProvider<List<String>> hoverProvider = new HoverProvider<>(
                new MappedHoverFactory<>(
                        new HoverInfoFactory(typeFormatter),
                        new MonacoHoverMapper()));

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

                        AnalysisResult result = new Analyzer().analyze(request.code, resolver.resolve(request.type));
                        HighlightingProvider provider = new HighlightingProvider(result.lexerOutput(), result.binderOutput());

                        Json.sendResponse(exchange, provider.get().stream().map(MonacoSemanticToken::new).toList());
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

                        BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

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
                    } else if (path.equals(prefix + "token-types")) {
                        Json.sendResponse(exchange, SemanticTokenType.values());
                    } else if (path.equals(prefix + "token-modifiers")) {
                        Json.sendResponse(exchange, SemanticTokenModifier.values());
                    } else if (path.equals(prefix + "hover")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                        BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

                        HoverProvider.HoverResponse<List<String>> response = hoverProvider.get(binderOutput, request.line, request.column);
                        Json.sendResponse(exchange, response);
                    } else if (path.equals(prefix + "definition")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                        BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

                        Json.sendResponse(exchange, new DefinitionProvider().get(binderOutput, request.line, request.column), TextRange.class);
                    } else if (path.equals(prefix + "completion")) {
                        byte[] data = exchange.getRequestBody().readAllBytes();
                        CompletionRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), CompletionRequest.class);

                        CompilationParameters parameters = resolver.resolve(request.type);
                        BinderOutput binderOutput = new Analyzer().analyze(request.code, parameters).binderOutput();

                        Json.sendResponse(exchange, completionProviderFactory.getSuggestions(parameters, binderOutput, request.line, request.column));
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                    exchange.close();
                } catch (Throwable throwable) {
                    WebHelper.sendException(exchange, throwable);
                }
            }
        });
    }

    public record TokenizeRequest(String code, String type) {}

    public record DiagnosticsRequest(String code, String type) {}

    public record DiagnosticsResponseItem(TextRange range, String message) {}

    public record HoverRequest(String code, String type, int line, int column) {}

    public record CompletionRequest(String code, String type, int line, int column) {}

    public record MonacoSemanticToken(int type, int modifiers, TextRange range) {

        public MonacoSemanticToken(SemanticToken token) {
            this(token.type().ordinal(), toModifierFlags(token.modifiers()), token.range());
        }

        private static int toModifierFlags(List<SemanticTokenModifier> modifiers) {
            int flags = 0;
            for (SemanticTokenModifier modifier : modifiers) {
                flags |= (1 << modifier.ordinal());
            }
            return flags;
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