package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.webui.ApiException;
import com.zergatul.cheatutils.webui.HttpResponseCodes;
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
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.ValueToken;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Integration {

    private static final Pattern SCRIPTING_CLASS_PATTERN = Pattern.compile(
            "Java<com\\.zergatul\\.cheatutils\\.scripting\\.(?:modules|api\\.modules)\\.(.+)>");
    private static final Pattern RGB_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final Pattern RGBA_PATTERN = Pattern.compile("^#[0-9a-fA-F]{8}$");

    private final CompletionProviderFactory<Suggestion> completionProviderFactory;
    private final HoverProvider<List<String>> hoverProvider;

    public Integration() {
        TypeDisplayFormatter formatter = new TypeDisplayFormatter(clazz ->
                clazz.getName().startsWith("com.zergatul.cheatutils.scripting")
                        ? clazz.getSimpleName()
                        : clazz.getName());
        completionProviderFactory = new CompletionProviderFactory<>(
                new MappedSuggestionFactory<>(
                        new SuggestionInfoFactory(formatter),
                        new MonacoSuggestionMapper()));
        hoverProvider = new HoverProvider<>(
                new MappedHoverFactory<>(
                        new HoverInfoFactory(formatter),
                        new MonacoHoverMapper()));
    }

    public void attach(HttpServer server, String prefix) {
        if (!prefix.endsWith("/")) {
            throw new IllegalArgumentException("Code API prefix must end with '/'.");
        }
        server.createContext(prefix, exchange -> handle(exchange, prefix));
    }

    private void handle(HttpExchange exchange, String prefix) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String endpoint = path.startsWith(prefix) ? path.substring(prefix.length()) : "";

            if (endpoint.equals("token-types")) {
                requireMethod(exchange, "GET");
                sendResponse(exchange, SemanticTokenType.values());
                return;
            }
            if (endpoint.equals("token-modifiers")) {
                requireMethod(exchange, "GET");
                sendResponse(exchange, SemanticTokenModifier.values());
                return;
            }

            requireMethod(exchange, "POST");
            requireJsonContentType(exchange);
            switch (endpoint) {
                case "tokenize" -> tokenize(exchange);
                case "color-strings" -> colorStrings(exchange);
                case "diagnostics" -> diagnostics(exchange);
                case "hover" -> hover(exchange);
                case "definition" -> definition(exchange);
                case "completion" -> completion(exchange);
                default -> throw new ApiException("Code API handler not found", HttpResponseCodes.NOT_FOUND);
            }
        } catch (ApiException e) {
            WebHelper.sendException(exchange, e.getCode(), e);
        } catch (Throwable e) {
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void tokenize(HttpExchange exchange) throws IOException, ApiException {
        CodeRequest request = readJson(exchange, CodeRequest.class);
        AnalysisResult result = analyze(request.code(), request.type());
        HighlightingProvider provider = new HighlightingProvider(result.lexerOutput(), result.binderOutput());
        sendResponse(exchange, provider.get().stream().map(MonacoSemanticToken::new).toList());
    }

    private void colorStrings(HttpExchange exchange) throws IOException, ApiException {
        String code = readJson(exchange, String.class);
        if (code == null) {
            throw badRequest("Code is required");
        }

        LexerOutput output = new Lexer(new LexerInput(code)).lex();
        List<MonacoColoredTokenEntry> entries = new ArrayList<>();
        for (Token token : output.tokens()) {
            if (token.is(TokenType.STRING_LITERAL)) {
                String value = ((ValueToken) token).value;
                if (RGB_PATTERN.matcher(value).matches() || RGBA_PATTERN.matcher(value).matches()) {
                    entries.add(new MonacoColoredTokenEntry(ColorUtils.parseColor2(value), token.getRange()));
                }
            }
        }
        sendResponse(exchange, entries);
    }

    private void diagnostics(HttpExchange exchange) throws IOException, ApiException {
        CodeRequest request = readJson(exchange, CodeRequest.class);
        BinderOutput output = analyze(request.code(), request.type()).binderOutput();
        sendResponse(exchange, output.diagnostics().stream()
                .map(diagnostic -> new DiagnosticsResponseItem(
                        diagnostic.range,
                        simplifyDiagnostic(diagnostic.message)))
                .toArray());
    }

    private void hover(HttpExchange exchange) throws IOException, ApiException {
        PositionRequest request = readPositionRequest(exchange);
        BinderOutput output = analyze(request.code(), request.type()).binderOutput();
        sendResponse(exchange, hoverProvider.get(output, request.line(), request.column()));
    }

    private void definition(HttpExchange exchange) throws IOException, ApiException {
        PositionRequest request = readPositionRequest(exchange);
        BinderOutput output = analyze(request.code(), request.type()).binderOutput();
        sendResponse(exchange, new DefinitionProvider().get(output, request.line(), request.column()), TextRange.class);
    }

    private void completion(HttpExchange exchange) throws IOException, ApiException {
        PositionRequest request = readPositionRequest(exchange);
        CompilationParameters parameters = resolve(request.type());
        BinderOutput output = new Analyzer().analyze(request.code(), parameters).binderOutput();
        sendResponse(exchange, completionProviderFactory.getSuggestions(
                parameters,
                output,
                request.line(),
                request.column()));
    }

    private AnalysisResult analyze(String code, String type) throws ApiException {
        requireCodeAndType(code, type);
        return new Analyzer().analyze(code, resolve(type));
    }

    private CompilationParameters resolve(String type) throws ApiException {
        if (type == null || type.isBlank()) {
            throw badRequest("Script type is required");
        }
        try {
            return ScriptCompilerRegistry.INSTANCE.getParameters(ScriptType.valueOf(type));
        } catch (IllegalArgumentException e) {
            throw new ApiException("Unsupported script type: " + type, HttpResponseCodes.BAD_REQUEST, e);
        }
    }

    private PositionRequest readPositionRequest(HttpExchange exchange) throws IOException, ApiException {
        PositionRequest request = readJson(exchange, PositionRequest.class);
        requireCodeAndType(request.code(), request.type());
        if (request.line() < 1 || request.column() < 1) {
            throw badRequest("Line and column must be positive");
        }
        return request;
    }

    private static void requireCodeAndType(String code, String type) throws ApiException {
        if (code == null) {
            throw badRequest("Code is required");
        }
        if (type == null || type.isBlank()) {
            throw badRequest("Script type is required");
        }
    }

    private static <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException, ApiException {
        String body = WebHelper.readBody(exchange);
        try {
            T result = MonacoJson.GSON.fromJson(body, type);
            if (result == null) {
                throw badRequest("JSON body is required");
            }
            return result;
        } catch (JsonParseException e) {
            throw new ApiException("Invalid JSON body", HttpResponseCodes.BAD_REQUEST, e);
        }
    }

    private static void requireMethod(HttpExchange exchange, String method) throws ApiException {
        if (!exchange.getRequestMethod().equals(method)) {
            throw new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
        }
    }

    private static void requireJsonContentType(HttpExchange exchange) throws ApiException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
            throw badRequest("Content-Type must be application/json");
        }
    }

    private static ApiException badRequest(String message) {
        return new ApiException(message, HttpResponseCodes.BAD_REQUEST);
    }

    private static String simplifyDiagnostic(String message) {
        Matcher matcher = SCRIPTING_CLASS_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(matcher.group(1)));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static void sendResponse(HttpExchange exchange, Object response) throws IOException {
        WebHelper.sendJson(exchange, HttpResponseCodes.OK, MonacoJson.toJson(response));
    }

    private static void sendResponse(HttpExchange exchange, Object response, java.lang.reflect.Type type) throws IOException {
        WebHelper.sendJson(exchange, HttpResponseCodes.OK, MonacoJson.toJson(response, type));
    }

    public record CodeRequest(String code, String type) {}

    public record PositionRequest(String code, String type, int line, int column) {}

    public record DiagnosticsResponseItem(TextRange range, String message) {}

    public record MonacoSemanticToken(int type, int modifiers, TextRange range) {

        public MonacoSemanticToken(SemanticToken token) {
            this(token.type().ordinal(), toModifierFlags(token.modifiers()), token.range());
        }

        private static int toModifierFlags(List<SemanticTokenModifier> modifiers) {
            int flags = 0;
            for (SemanticTokenModifier modifier : modifiers) {
                flags |= 1 << modifier.ordinal();
            }
            return flags;
        }
    }

    public record MonacoColor(float red, float green, float blue, float alpha) {}

    public record MonacoRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {}

    public record MonacoColoredTokenEntry(MonacoColor color, MonacoRange range) {

        public MonacoColoredTokenEntry(Color color, TextRange range) {
            this(
                    new MonacoColor(
                            color.getRed() / 255f,
                            color.getGreen() / 255f,
                            color.getBlue() / 255f,
                            color.getAlpha() / 255f),
                    new MonacoRange(
                            range.getLine1(),
                            range.getColumn1(),
                            range.getLine2(),
                            range.getColumn2()));
        }
    }
}