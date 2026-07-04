package com.zergatul.cheatutils.mcp.utility;

import com.zergatul.scripting.completion.SuggestionFactory;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;

import java.util.List;

public class McpSuggestionFactory implements SuggestionFactory<McpSuggestion> {

    @Override
    public McpSuggestion getKeywordSuggestion(TokenType type) {
        String text = switch (type) {
            case META_CAST -> "#cast";
            case META_TYPE -> "#type";
            case META_TYPE_OF -> "#typeof";
            default -> type.toString().toLowerCase();
        };
        return suggestion(text, "keyword");
    }

    @Override
    public List<McpSuggestion> getTypeSuggestion(SType type) {
        if (type == SInt.instance) {
            return List.of(
                    suggestion("int", "type", "int"),
                    suggestion("int32", "type", "int"));
        }
        if (type == SInt64.instance) {
            return List.of(
                    suggestion("long", "type", "long"),
                    suggestion("int64", "type", "long"));
        }
        if (type.isPredefined()) {
            String text = type.toString();
            return List.of(suggestion(text, "type", text));
        }
        return List.of();
    }

    @Override
    public McpSuggestion getCustomTypeSuggestion(Class<?> clazz) {
        CustomType type = clazz.getAnnotation(CustomType.class);
        return suggestion(type.name(), "type");
    }

    @Override
    public McpSuggestion getClassSuggestion(ClassSymbol clazz) {
        return suggestion(clazz.getName(), "type");
    }

    @Override
    public McpSuggestion getTypeAliasSuggestion(SAliasType type) {
        return suggestion(type.toString(), "type");
    }

    @Override
    public McpSuggestion getThisSuggestion(SType type) {
        return suggestion("this", "keyword", type(type));
    }

    @Override
    public McpSuggestion getBaseSuggestion(SType type) {
        return suggestion("base", "keyword", type(type));
    }

    @Override
    public McpSuggestion getPropertySuggestion(PropertyReference property) {
        return suggestion(property.getName(), "property", type(property.getType()));
    }

    @Override
    public McpSuggestion getMethodSuggestion(MethodReference method) {
        if (method instanceof UnknownMethodReference) {
            return null;
        }

        String returnType = type(method.getReturn());
        String signature = getMethodSignature(method);
        return new McpSuggestion(
                method.getName(),
                "method",
                returnType,
                signature,
                method.getDescription().orElse(null));
    }

    @Override
    public McpSuggestion getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable) {
        return suggestion(variable.getName(), "value", type(variable.getType()));
    }

    @Override
    public McpSuggestion getStaticFieldSuggestion(DeclaredStaticVariable variable) {
        return suggestion(variable.getName(), "variable", type(variable.getType()));
    }

    @Override
    public McpSuggestion getFunctionSuggestion(Function function) {
        return new McpSuggestion(
                function.getName(),
                "function",
                null,
                function.getFunctionType().toString(),
                null);
    }

    @Override
    public McpSuggestion getLocalVariableSuggestion(LocalVariable variable) {
        return suggestion(variable.getName(), "variable", type(variable.getType()));
    }

    @Override
    public McpSuggestion getInputParameterSuggestion(String name, SType type) {
        return suggestion(name, "variable", type(type));
    }

    private McpSuggestion suggestion(String name, String kind) {
        return suggestion(name, kind, null);
    }

    private McpSuggestion suggestion(String name, String kind, String type) {
        return new McpSuggestion(name, kind, type, null, null);
    }

    private String getMethodSignature(MethodReference method) {
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
        return sb.toString();
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