package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.scripting.*;
import com.zergatul.cheatutils.scripting.api.Root;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ScriptingLanguageCompiler {

    public Runnable compile(String program) throws ParseException, ScriptCompileException {
        InputStream stream = new ByteArrayInputStream(program.getBytes(StandardCharsets.UTF_8));
        ScriptingLanguage parser = new ScriptingLanguage(stream);
        return compile(parser.Input());
    }

    private Runnable compile(ASTInput input) throws ScriptCompileException {
        List<Runnable> list = new ArrayList<>();
        for (int i = 0; i < input.jjtGetNumChildren(); i++) {
            list.add(compile((ASTStatement) input.jjtGetChild(i)));
        }
        list.removeIf(Objects::isNull);
        return () -> list.forEach(Runnable::run);
    }

    private Runnable compile(ASTStatement statement) throws ScriptCompileException {
        Node first = statement.jjtGetChild(0);
        if (first instanceof ASTEmptyStatement) {
            return null;
        }
        if (first instanceof ASTMethodCall methodCall) {
            var expressionReturn = compile(methodCall);
            return () -> expressionReturn.supplier.get();
        }
        if (first instanceof ASTBlockStatement blockStatement) {
            return compile(blockStatement);
        }
        if (first instanceof ASTIfStatement ifStatement) {
            return compile(ifStatement);
        }
        throw new ScriptCompileException();
    }

    private ExpressionReturn compile(ASTMethodCall methodCall) throws ScriptCompileException {
        var name = (ASTName) methodCall.jjtGetChild(0);
        var arguments = (ASTArguments) methodCall.jjtGetChild(1);
        if (name.jjtGetNumChildren() != 2) {
            throw new ScriptCompileException();
        }

        String fieldName = (String) ((SimpleNode) name.jjtGetChild(0)).jjtGetValue();
        String methodName = (String) ((SimpleNode) name.jjtGetChild(1)).jjtGetValue();

        Field field;
        try {
            field = Root.class.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            throw new ScriptCompileException();
        }

        int argsLength = arguments.jjtGetNumChildren();
        ExpressionReturn[] pairs = new ExpressionReturn[argsLength];
        Class[] parameterTypes = new Class[argsLength];
        for (int i = 0; i < argsLength; i++) {
            pairs[i] = compile((ASTExpression) arguments.jjtGetChild(i));
            parameterTypes[i] = pairs[i].type.getJavaClass();
        }

        Method method;
        try {
            method = field.getType().getDeclaredMethod(methodName, parameterTypes);
        }
        catch (NoSuchMethodException e) {
            throw new ScriptCompileException();
        }

        ScriptingLanguageType returnType = ScriptingLanguageType.fromJavaClass(method.getReturnType());
        return new ExpressionReturn(() -> {
            Object fieldValue;
            try {
                fieldValue = field.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            try {
                return method.invoke(fieldValue, Arrays.stream(pairs).map(er -> er.supplier.get()).toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }, returnType);
    }

    private Runnable compile(ASTBlockStatement blockStatement) throws ScriptCompileException {
        Runnable[] child = new Runnable[blockStatement.jjtGetNumChildren()];
        for (int i = 0; i < child.length; i++) {
            child[i] = compile((ASTStatement) blockStatement.jjtGetChild(i));
        }

        return () -> {
            for (int i = 0; i < child.length; i++) {
                child[i].run();
            }
        };
    }

    private Runnable compile(ASTIfStatement ifStatement) throws ScriptCompileException {
        int numChildren = ifStatement.jjtGetNumChildren();
        if (numChildren < 2) {
            throw new ScriptCompileException();
        }
        ASTExpression ifExpr = (ASTExpression) ifStatement.jjtGetChild(0);
        ASTStatement thenStmt = (ASTStatement) ifStatement.jjtGetChild(1);
        ASTStatement elseStmt = numChildren > 2 ? (ASTStatement) ifStatement.jjtGetChild(2) : null;
        ExpressionReturn condition = compile(ifExpr);
        if (condition.type != ScriptingLanguageType.BOOLEAN) {
            throw new ScriptCompileException();
        }
        Runnable thenCompiled = compile(thenStmt);
        Runnable elseCompiled = elseStmt != null ? compile(elseStmt) : null;
        if (elseCompiled == null) {
            return () -> {
                if ((boolean) condition.supplier.get()) {
                    thenCompiled.run();
                }
            };
        } else {
            return () -> {
                if ((boolean) condition.supplier.get()) {
                    thenCompiled.run();
                } else {
                    elseCompiled.run();
                }
            };
        }
    }

    private ExpressionReturn compile(ASTExpression expression) throws ScriptCompileException {
        if (expression.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException();
        }

        Node node = expression.jjtGetChild(0);
        if (node instanceof ASTExpression innerExpression) {
            return compile(innerExpression);
        }
        if (node instanceof ASTMethodCall methodCall) {
            return compile(methodCall);
        }
        if (node instanceof ASTName name) {
            throw new ScriptCompileException(); // TODO
        }
        if (node instanceof ASTLiteral literal) {
            return compile(literal);
        }

        throw new ScriptCompileException();
    }

    private ExpressionReturn compile(ASTLiteral literal) throws ScriptCompileException {
        if (literal.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException();
        }

        Node node = literal.jjtGetChild(0);
        if (node instanceof ASTStringLiteral stringLiteral) {
            String value = parseString((String) stringLiteral.jjtGetValue());
            return new ExpressionReturn(() -> value, ScriptingLanguageType.STRING);
        }
        if (node instanceof ASTBooleanLiteral booleanLiteral) {
            boolean value = (boolean) booleanLiteral.jjtGetValue();
            return new ExpressionReturn(() -> value, ScriptingLanguageType.BOOLEAN);
        }
        if (node instanceof ASTNullLiteral) {
            return new ExpressionReturn(() -> null, ScriptingLanguageType.NULL);
        }

        throw new ScriptCompileException();
    }

    private String parseString(String value) throws ScriptCompileException {
        value = value.substring(1, value.length() - 1); // truncate brackets
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                char next = value.charAt(i + 1);
                switch (next) {
                    case 'n': value = replace(value, i, 2, "\n"); break;
                    case 'r': value = replace(value, i, 2, "\r"); break;
                    case 't': value = replace(value, i, 2, "\t"); break;
                    case 'b': value = replace(value, i, 2, "\b"); break;
                    case 'f': value = replace(value, i, 2, "\f"); break;
                    case '\\': value = replace(value, i, 2, "\\"); break;
                    case '"': value = replace(value, i, 2, "\""); break;
                    case '\'': value = replace(value, i, 2, "'"); break;
                    default:
                        throw new ScriptCompileException();// not implemented
                }
            }
        }
        return value;
    }

    private String replace(String value, int from, int length, String replacement) {
        return value.substring(0, from) + replacement + value.substring(from + length);
    }

    private class ExpressionReturn {
        public Supplier<Object> supplier;
        public ScriptingLanguageType type;

        public ExpressionReturn(Supplier<Object> supplier, ScriptingLanguageType type) {
            this.supplier = supplier;
            this.type = type;
        }
    }
}
