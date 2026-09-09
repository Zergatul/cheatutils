package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.zergatul.scripting.utility.Lists;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonacoIntegration {

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

        server.createContext(prefix, exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                if (path.equals(prefix + "tokenize")) {
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
                    TokenizeRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), TokenizeRequest.class);

                    AnalysisResult result = new Analyzer().analyze(request.code, resolver.resolve(request.type));
                    HighlightingProvider provider = new HighlightingProvider(result.lexerOutput(), result.binderOutput());

                    Json.sendResponse(exchange, Lists.from(provider.get().stream().map(MonacoSemanticToken::new)));
                } else if (path.equals(prefix + "color-strings")) {
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
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
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
                    DiagnosticsRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), DiagnosticsRequest.class);

                    BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

                    Json.sendResponse(exchange, binderOutput.diagnostics()
                            .stream()
                            .map(d -> {
                                StringBuffer sb = new StringBuffer();
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
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
                    HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                    BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

                    HoverProvider.HoverResponse<List<String>> response = hoverProvider.get(binderOutput, request.line, request.column);
                    Json.sendResponse(exchange, response);
                } else if (path.equals(prefix + "definition")) {
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
                    HoverRequest request = gson.fromJson(new String(data, Charset.defaultCharset()), HoverRequest.class);

                    BinderOutput binderOutput = new Analyzer().analyze(request.code, resolver.resolve(request.type)).binderOutput();

                    Json.sendResponse(exchange, new DefinitionProvider().get(binderOutput, request.line, request.column), TextRange.class);
                } else if (path.equals(prefix + "completion")) {
                    byte[] data = IOUtils.toByteArray(exchange.getRequestBody());
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
        });
    }

    private static int toModifierFlags(List<SemanticTokenModifier> modifiers) {
        int flags = 0;
        for (SemanticTokenModifier modifier : modifiers) {
            flags |= (1 << modifier.ordinal());
        }
        return flags;
    }

    public static final class TokenizeRequest {
        public String code;
        public String type;
    }

    public static final class DiagnosticsRequest {
        public String code;
        public String type;
    }

    public static final class DiagnosticsResponseItem {

        public TextRange range;
        public String message;

        public DiagnosticsResponseItem(TextRange range, String message) {
            this.range = range;
            this.message = message;
        }
    }

    public static final class HoverRequest {
        public String code;
        public String type;
        public int line;
        public int column;
    }

    public static final class CompletionRequest {
        public String code;
        public String type;
        public int line;
        public int column;
    }

    public static final class MonacoSemanticToken {

        public int type;
        public int modifiers;
        public TextRange range;

        public MonacoSemanticToken() {}

        public MonacoSemanticToken(int type, int modifiers, TextRange range) {
            this.type = type;
            this.modifiers = modifiers;
            this.range = range;
        }

        public MonacoSemanticToken(SemanticToken token) {
            this(token.type().ordinal(), toModifierFlags(token.modifiers()), token.range());
        }

        public int type() {
            return type;
        }

        public int modifiers() {
            return modifiers;
        }

        public TextRange range() {
            return range;
        }
    }

    public static final class MonacoColor {

        public float red;
        public float green;
        public float blue;
        public float alpha;

        public MonacoColor(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    public static final class MonacoRange {

        public int startLineNumber;
        public int startColumn;
        public int endLineNumber;
        public int endColumn;

        public MonacoRange() {}

        public MonacoRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
            this.startLineNumber = startLineNumber;
            this.startColumn = startColumn;
            this.endLineNumber = endLineNumber;
            this.endColumn = endColumn;
        }
    }

    public static final class MonacoColoredTokenEntry {

        public MonacoColor color;
        public MonacoRange range;

        public MonacoColoredTokenEntry() {}

        public MonacoColoredTokenEntry(MonacoColor color, MonacoRange range) {
            this.color = color;
            this.range = range;
        }

        public MonacoColoredTokenEntry(Color color, TextRange range) {
            this(
                    new MonacoColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f),
                    new MonacoRange(range.getLine1(), range.getColumn1(), range.getLine2(), range.getColumn2()));
        }
    }
}