package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.type.*;

import java.util.List;
import java.util.Optional;

public class DocumentationProvider {

    public String getTypeDocs(SType type) {
        if (type == SBoolean.instance) {
            return "true or false value";
        }
        if (type == SInt.instance) {
            return "32-bit signed integer";
        }
        if (type == SInt64.instance) {
            return "64-bit signed integer";
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

    public List<Suggestion> getTypeSuggestion(SType type) {
        if (type == SInt.instance) {
            return List.of(
                    new Suggestion(
                            "int",
                            null,
                            getTypeDocs(type),
                            "int",
                            CompletionItemKind.CLASS),
                    new Suggestion(
                            "int32",
                            null,
                            getTypeDocs(type),
                            "int32",
                            CompletionItemKind.CLASS));
        }
        if (type == SInt64.instance) {
            return List.of(
                    new Suggestion(
                            "long",
                            null,
                            getTypeDocs(type),
                            "long",
                            CompletionItemKind.CLASS),
                    new Suggestion(
                            "int64",
                            null,
                            getTypeDocs(type),
                            "int64",
                            CompletionItemKind.CLASS));
        }
        if (type instanceof SPredefinedType) {
            return List.of(new Suggestion(
                    type.toString(),
                    null,
                    getTypeDocs(type),
                    type.toString(),
                    CompletionItemKind.CLASS));
        }
        return List.of();
    }

    public Suggestion getCustomTypeSuggestion(Class<?> clazz) {
        CustomType type = clazz.getAnnotation(CustomType.class);
        return new Suggestion(
                type.name(),
                null,
                null,
                type.name(),
                CompletionItemKind.CLASS);
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

    public Suggestion getLetKeywordSuggestion() {
        return new Suggestion(
                "let",
                null,
                null,
                "let",
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
        return getLocalVariableSuggestion(variable.getName(), variable.getType());
    }

    public Suggestion getLocalVariableSuggestion(String name, SType type) {
        return new Suggestion(
                name,
                type(type),
                null,
                name,
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
        if (method instanceof UnknownMethodReference) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(type(method.getReturn()));
        sb.append(' ');
        sb.append(type(method.getOwner()));
        sb.append('.');
        sb.append(method.getName());
        sb.append('(');
        List<MethodParameter> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(type(parameters.get(i).type()));
            sb.append(' ');
            sb.append(parameters.get(i).name());
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');

        return new Suggestion(
                method.getName(),
                sb.toString(),
                getMethodDocumentation(method).orElse(null),
                method.getName(),
                CompletionItemKind.METHOD);
    }

    public Optional<String> getMethodDocumentation(MethodReference method) {
        return method.getDescription();
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