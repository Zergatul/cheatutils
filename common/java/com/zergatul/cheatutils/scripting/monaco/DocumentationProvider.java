package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.type.*;

import java.util.List;

public class DocumentationProvider {

    public String getTypeDocs(SType type) {
        if (type == SBoolean.instance) {
            return "true or false value";
        }
        if (type == SInt.instance) {
            return "32-bit signed integer";
        }
        if (type == SChar.instance) {
            return "Single character";
        }
        if (type == SFloat.instance) {
            return "Double-precision floating-point number";
        }
        if (type == SString.instance) {
            return "Text as sequence of characters";
        }
        return null;
    }

    public Suggestion getTypeSuggestion(SType type) {
        if (type instanceof SPredefinedType) {
            return new Suggestion(
                    type.toString(),
                    null,
                    getTypeDocs(type),
                    type.toString(),
                    CompletionItemKind.CLASS);
        }
        return null;
    }

    public Suggestion getStaticKeywordSuggestion() {
        return new Suggestion(
                "static",
                null,
                null,
                "static",
                CompletionItemKind.KEYWORD);
    }

    public Suggestion getVoidKeywordSuggestion() {
        return new Suggestion(
                "void",
                null,
                null,
                "void",
                CompletionItemKind.KEYWORD);
    }

    public Suggestion getAwaitKeywordSuggestion() {
        return new Suggestion(
                "await",
                null,
                null,
                "await",
                CompletionItemKind.KEYWORD);
    }

    public List<Suggestion> getCommonStatementStartSuggestions() {
        return List.of(
                new Suggestion("for", null, null, "for", CompletionItemKind.KEYWORD),
                new Suggestion("foreach", null, null, "foreach", CompletionItemKind.KEYWORD),
                new Suggestion("if", null, null, "if", CompletionItemKind.KEYWORD),
                new Suggestion("return", null, null, "return", CompletionItemKind.KEYWORD),
                new Suggestion("while", null, null, "while", CompletionItemKind.KEYWORD));
    }

    public Suggestion getLocalVariableSuggestion(LocalVariable variable) {
        return new Suggestion(
                variable.getName(),
                variable.getType().toString(),
                null,
                variable.getName(),
                CompletionItemKind.VARIABLE);
    }

    public Suggestion getStaticConstantSuggestion(StaticVariable variable) {
        return new Suggestion(
                variable.getName(),
                type(variable.getType()),
                null,
                variable.getName(),
                CompletionItemKind.VALUE);
    }

    public Suggestion getStaticVariableSuggestion(StaticVariable variable) {
        return new Suggestion(
                variable.getName(),
                type(variable.getType()),
                null,
                variable.getName(),
                CompletionItemKind.VARIABLE);
    }

    public Suggestion getFunctionSuggestion(Function function) {
        return new Suggestion(
                function.getName(),
                function.getFunctionType().toString(),
                null,
                function.getName(),
                CompletionItemKind.FUNCTION);
    }

    public Suggestion getPropertySuggestion(PropertyReference property) {
        return new Suggestion(
                property.getName(),
                type(property.getType()),
                null,
                property.getName(),
                CompletionItemKind.PROPERTY);
    }

    public Suggestion getMethodSuggestion(MethodReference method) {
        return new Suggestion(
                method.getName(),
                "...",
                null,
                method.getName(),
                CompletionItemKind.METHOD);
    }

    private String type(SType type) {
        if (type instanceof SClassType classType) {
            Class<?> clazz = classType.getJavaClass();
            if (clazz.getName().startsWith("com.zergatul.cheatutils.scripting")) {
                return clazz.getSimpleName();
            } else {
                return clazz.getName();
            }
        } else {
            return type.toString();
        }
    }
}