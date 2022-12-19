package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.scripting.generated.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

public class ScriptingLanguageCompiler {

    private static AtomicInteger counter = new AtomicInteger(0);
    private static ScriptingClassLoader classLoader = new ScriptingClassLoader();
    private final Class<?> root;

    public ScriptingLanguageCompiler(Class<?> root) {
        this.root = root;
    }

    public Runnable compile(String program) throws ParseException, ScriptCompileException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicClass_" + counter.incrementAndGet();
        writer.visit(V1_5, ACC_PUBLIC, name, null, Type.getInternalName(Object.class), new String[] { Type.getInternalName(Runnable.class) });

        MethodVisitor methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();

        methodVisitor = writer.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        methodVisitor.visitCode();

        InputStream stream = new ByteArrayInputStream(program.getBytes(StandardCharsets.UTF_8));
        ScriptingLanguage parser = new ScriptingLanguage(stream);
        compile(parser.Input(), new MethodVisitorWrapper(methodVisitor));

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
        writer.visitEnd();

        byte[] code = writer.toByteArray();
        Class<?> dynamic = classLoader.defineClass(name.replace('/', '.'), code);

        Constructor<?> constructor;
        try {
            constructor = dynamic.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptCompileException("Cannot find constructor for dynamic class.");
        }

        Object instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new ScriptCompileException("Cannot instantiate dynamic class.");
        }

        return (Runnable) instance;
    }

    private void compile(ASTInput input, CompilerMethodVisitor visitor) throws ScriptCompileException {
        for (int i = 0; i < input.jjtGetNumChildren(); i++) {
            compile((ASTStatement) input.jjtGetChild(i), visitor);
        }
    }

    private void compile(ASTStatement statement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        Node node = statement.jjtGetChild(0);
        if (node instanceof ASTEmptyStatement) {
            return;
        }
        if (node instanceof ASTStatementExpression statementExpression) {
            compile(statementExpression, visitor);
            return;
        }
        if (node instanceof ASTBlock block) {
            compile(block, visitor);
            return;
        }
        if (node instanceof ASTIfStatement ifStatement) {
            compile(ifStatement, visitor);
            return;
        }
        throw new ScriptCompileException("ASTStatement case not implemented: " + node.getClass().getName() + ".");
    }

    private void compile(ASTStatementExpression statementExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        Node node = statementExpression.jjtGetChild(0);
        if (node instanceof ASTPrimaryExpression primaryExpression) {
            ScriptingLanguageType type = compile(primaryExpression, visitor);
            if (type != ScriptingLanguageType.VOID) {
                visitor.visitInsn(POP);
            }
            return;
        }
        throw new ScriptCompileException("ASTStatementExpression case not implemented: " + node.getClass().getName() + ".");
    }

    private void compile(ASTBlock block, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = block.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            compile((ASTBlockStatement) block.jjtGetChild(i), visitor);
        }
    }

    private ScriptingLanguageType compile(ASTPrimaryExpression primaryExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) primaryExpression.jjtGetChild(0);
        Node prefixNode = prefix.jjtGetChild(0);
        if (primaryExpression.jjtGetNumChildren() == 1) {
            if (prefixNode instanceof ASTLiteral literal) {
                return compile(literal, visitor);
            }
            if (prefixNode instanceof ASTName) {
                throw new ScriptCompileException("ASTPrimaryExpression cannot reference fields.");
            }
            if (prefixNode instanceof ASTExpression expression) {
                return compile(expression, visitor);
            }
            throw new ScriptCompileException("ASTPrimaryExpression(1) case not implemented: " + prefixNode.getClass().getName() + ".");
        } else {
            ASTPrimarySuffix suffix = (ASTPrimarySuffix) primaryExpression.jjtGetChild(1);
            if (prefixNode instanceof ASTLiteral) {
                throw new ScriptCompileException("ASTLiteral cannot have PrimarySuffix");
            }
            if (prefixNode instanceof ASTName name) {
                return compile(name, (ASTArguments) suffix.jjtGetChild(0), visitor);
            }
            if (prefixNode instanceof ASTExpression) {
                throw new ScriptCompileException("ASTExpression cannot have PrimarySuffix");
            }
            throw new ScriptCompileException("ASTPrimaryExpression(2) case not implemented: " + prefixNode.getClass().getName() + ".");
        }
    }

    private ScriptingLanguageType compile(ASTName name, ASTArguments arguments, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (name.jjtGetNumChildren() != 2) {
            throw new ScriptCompileException("Method call node should have 2 names.");
        }

        String fieldName = (String) ((SimpleNode) name.jjtGetChild(0)).jjtGetValue();
        String methodName = (String) ((SimpleNode) name.jjtGetChild(1)).jjtGetValue();

        Field field;
        try {
            field = root.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new ScriptCompileException("Cannot find field \"" + fieldName + "\".");
        }

        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(root),
                field.getName(),
                Type.getDescriptor(field.getType()));

        int argsLength;
        ScriptingLanguageType[] methodArgumentTypes;
        BufferVisitor[] methodArgumentVisitors;
        if (arguments.jjtGetNumChildren() == 0) {
            argsLength = 0;
            methodArgumentTypes = new ScriptingLanguageType[0];
            methodArgumentVisitors = new BufferVisitor[0];
        } else {
            var argumentList = (ASTArgumentList) arguments.jjtGetChild(0);
            argsLength = argumentList.jjtGetNumChildren();
            methodArgumentTypes = new ScriptingLanguageType[argsLength];
            methodArgumentVisitors = new BufferVisitor[argsLength];
            for (int i = 0; i < argsLength; i++) {
                methodArgumentVisitors[i] = new BufferVisitor();
                methodArgumentTypes[i] = compile((ASTExpression) argumentList.jjtGetChild(i), methodArgumentVisitors[i]);
            }
        }

        Method method = findMethod(field, methodName, methodArgumentTypes, methodArgumentVisitors);
        if (method == null) {
            throw new ScriptCompileException("Cannot find method \"" + methodName + "\".");
        }

        for (int i = 0; i < argsLength; i++) {
            methodArgumentVisitors[i].releaseBuffer(visitor);
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);

        return ScriptingLanguageType.fromJavaClass(method.getReturnType());
    }

    private void compile(ASTBlockStatement blockStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        for (int i = 0; i < blockStatement.jjtGetNumChildren(); i++) {
            compile((ASTStatement) blockStatement.jjtGetChild(i), visitor);
        }
    }

    private void compile(ASTIfStatement ifStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = ifStatement.jjtGetNumChildren();
        if (numChildren < 2) {
            throw new ScriptCompileException("ASTIfStatement invalid children count.");
        }

        ASTExpression ifExpr = (ASTExpression) ifStatement.jjtGetChild(0);
        ASTStatement thenStmt = (ASTStatement) ifStatement.jjtGetChild(1);
        ASTStatement elseStmt = numChildren > 2 ? (ASTStatement) ifStatement.jjtGetChild(2) : null;
        ScriptingLanguageType type = compile(ifExpr, visitor);
        if (type != ScriptingLanguageType.BOOLEAN) {
            throw new ScriptCompileException("Expression inside \"if\" statement should return boolean.");
        }

        if (elseStmt == null) {
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFEQ, endLabel);
            compile(thenStmt, visitor);
            visitor.visitLabel(endLabel);
        } else {
            Label elseLabel = new Label();
            visitor.visitJumpInsn(IFEQ, elseLabel);
            compile(thenStmt, visitor);
            Label endLabel = new Label();
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            compile(elseStmt, visitor);
            visitor.visitLabel(endLabel);
        }
    }

    private ScriptingLanguageType compile(ASTExpression expression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (expression.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("ASTExpression invalid children count.");
        }

        Node node = expression.jjtGetChild(0);
        if (node instanceof ASTConditionalExpression conditionalExpression) {
            return compile(conditionalExpression, visitor);
        }

        throw new ScriptCompileException("ASTExpression case not implemented: " + node.getClass().getName() + ".");
    }

    private ScriptingLanguageType compile(ASTConditionalExpression conditionalExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (conditionalExpression.jjtGetNumChildren() == 1) {
            Node node = conditionalExpression.jjtGetChild(0);
            if (node instanceof ASTConditionalOrExpression conditionalOrExpression) {
                return compile(conditionalOrExpression, visitor);
            }
            throw new ScriptCompileException("ASTConditionalExpression case not implemented: " + node.getClass().getName() + ".");
        }

        if (conditionalExpression.jjtGetNumChildren() == 3) {
            var condition = (ASTConditionalOrExpression) conditionalExpression.jjtGetChild(0);
            var expression1 = (ASTExpression) conditionalExpression.jjtGetChild(1);
            var expression2 = (ASTConditionalExpression) conditionalExpression.jjtGetChild(2);

            ScriptingLanguageType conditionReturnType = compile(condition, visitor);
            if (conditionReturnType != ScriptingLanguageType.BOOLEAN) {
                throw new ScriptCompileException("ASTConditionalExpression should return boolean.");
            }

            Label elseLabel = new Label();
            visitor.visitJumpInsn(IFEQ, elseLabel);
            ScriptingLanguageType expression1ReturnType = compile(expression1, visitor);
            Label endLabel = new Label();
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            ScriptingLanguageType expression2ReturnType = compile(expression2, visitor);
            visitor.visitLabel(endLabel);

            if (expression1ReturnType != expression2ReturnType) {
                throw new ScriptCompileException("ASTConditionalExpression return types don't match.");
            }

            return expression1ReturnType;
        }

        throw new ScriptCompileException("ASTConditionalExpression invalid children count.");
    }

    private ScriptingLanguageType compile(ASTConditionalOrExpression conditionalOrExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var conditionalAndExpression = (ASTConditionalAndExpression) conditionalOrExpression.jjtGetChild(0);
        ScriptingLanguageType type = compile(conditionalAndExpression, visitor);

        int numChildren = conditionalOrExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return type;
        }

        if (type != ScriptingLanguageType.BOOLEAN) {
            throw new ScriptCompileException("ASTConditionalOrExpression first expression is not Boolean.");
        }

        for (int i = 1; i < numChildren; i++) {
            type = compile((ASTConditionalAndExpression) conditionalOrExpression.jjtGetChild(i), visitor);
            if (type != ScriptingLanguageType.BOOLEAN) {
                throw new ScriptCompileException("ASTConditionalOrExpression one of expressions is not Boolean.");
            }
            visitor.visitInsn(IOR);
        }

        return ScriptingLanguageType.BOOLEAN;
    }

    private ScriptingLanguageType compile(ASTConditionalAndExpression conditionalAndExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var equalityExpression = (ASTEqualityExpression) conditionalAndExpression.jjtGetChild(0);
        ScriptingLanguageType type = compile(equalityExpression, visitor);

        int numChildren = conditionalAndExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return type;
        }

        if (type != ScriptingLanguageType.BOOLEAN) {
            throw new ScriptCompileException("ASTConditionalAndExpression first expression is not Boolean.");
        }

        for (int i = 1; i < numChildren; i++) {
            type = compile((ASTEqualityExpression) conditionalAndExpression.jjtGetChild(i), visitor);
            if (type != ScriptingLanguageType.BOOLEAN) {
                throw new ScriptCompileException("ASTConditionalAndExpression one of expressions is not Boolean.");
            }
            visitor.visitInsn(IAND);
        }

        return ScriptingLanguageType.BOOLEAN;
    }

    private ScriptingLanguageType compile(ASTEqualityExpression equalityExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var additiveExpression = (ASTAdditiveExpression) equalityExpression.jjtGetChild(0);
        ScriptingLanguageType type = compile(additiveExpression, visitor);

        int numChildren = equalityExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return type;
        }

        ScriptingLanguageType typeLeft = type;
        for (int i = 1; i < numChildren; i += 2) {
            Node operator = equalityExpression.jjtGetChild(i);
            BufferVisitor bufferVisitor = new BufferVisitor();
            ScriptingLanguageType typeRight = compile((ASTAdditiveExpression) equalityExpression.jjtGetChild(i + 1), bufferVisitor);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            if (typeLeft == typeRight) {
                bufferVisitor.releaseBuffer(visitor);
                switch (typeLeft) {
                    case BOOLEAN, INT -> {
                        visitor.visitJumpInsn(IF_ICMPEQ, elseLabel);
                        visitor.visitIntInsn(BIPUSH, operator instanceof ASTEquality ? 0 : 1);
                        visitor.visitJumpInsn(GOTO, endLabel);
                        visitor.visitLabel(elseLabel);
                        visitor.visitIntInsn(BIPUSH, operator instanceof ASTEquality ? 1 : 0);
                        visitor.visitLabel(endLabel);
                    }
                    case DOUBLE -> {
                        visitor.visitInsn(DCMPL);
                        visitor.visitInsn(ICONST_0);
                        visitor.visitJumpInsn(IF_ICMPEQ, elseLabel);
                        visitor.visitIntInsn(BIPUSH, operator instanceof ASTEquality ? 0 : 1);
                        visitor.visitJumpInsn(GOTO, endLabel);
                        visitor.visitLabel(elseLabel);
                        visitor.visitIntInsn(BIPUSH, operator instanceof ASTEquality ? 1 : 0);
                        visitor.visitLabel(endLabel);
                    }
                    case STRING -> {
                        Method equalsMethod;
                        try {
                            equalsMethod = Objects.class.getDeclaredMethod("equals", Object.class, Object.class);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                            throw new ScriptCompileException("ASTEqualityExpression cannot find Objects.equals method.");
                        }

                        visitor.visitMethodInsn(
                                INVOKESTATIC,
                                Type.getInternalName(Objects.class),
                                equalsMethod.getName(),
                                Type.getMethodDescriptor(equalsMethod),
                                false);

                        if (operator instanceof ASTInequality) {
                            visitor.visitInsn(ICONST_1);
                            visitor.visitInsn(SWAP);
                            visitor.visitInsn(ISUB);
                        }
                    }
                    case NULL -> throw new ScriptCompileException("ASTEqualityExpression Null not implemented.");
                    case VOID -> throw new ScriptCompileException("ASTEqualityExpression cannot compare Void.");
                    default -> throw new ScriptCompileException("ASTEqualityExpression type not implemented.");
                }
            } else {
                throw new ScriptCompileException("ASTEqualityExpression comparing different types not implemented.");
            }
            typeLeft = ScriptingLanguageType.BOOLEAN;
        }
        return ScriptingLanguageType.BOOLEAN;
    }

    private ScriptingLanguageType compile(ASTAdditiveExpression additiveExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var multiplicativeExpression = (ASTMultiplicativeExpression) additiveExpression.jjtGetChild(0);
        ScriptingLanguageType type = compile(multiplicativeExpression, visitor);

        int numChildren = additiveExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return type;
        }

        ScriptingLanguageType typeLeft = type;
        for (int i = 1; i < numChildren; i += 2) {
            Node operator = additiveExpression.jjtGetChild(i);
            BufferVisitor bufferVisitor = new BufferVisitor();
            ScriptingLanguageType typeRight = compile((ASTMultiplicativeExpression) additiveExpression.jjtGetChild(i + 1), bufferVisitor);
            if (operator instanceof ASTPlus) {
                if (typeLeft == typeRight) {
                    switch (typeLeft) {
                        case INT -> {
                            bufferVisitor.releaseBuffer(visitor);
                            visitor.visitInsn(IADD);
                        }
                        case BOOLEAN -> throw new ScriptCompileException("ASTAdditiveExpression cannot add 2 Booleans.");
                        case DOUBLE -> {
                            bufferVisitor.releaseBuffer(visitor);
                            visitor.visitInsn(DADD);
                        }
                        case STRING -> compileStringConcat(bufferVisitor, visitor);
                        case NULL -> throw new ScriptCompileException("ASTAdditiveExpression Null not implemented.");
                        case VOID -> throw new ScriptCompileException("ASTAdditiveExpression cannot add Void.");
                        default -> throw new ScriptCompileException("ASTAdditiveExpression type not implemented.");
                    }
                } else {
                    throw new ScriptCompileException("ASTAdditiveExpression adding different types not implemented.");
                }
                continue;
            }
            if (operator instanceof ASTMinus) {
                if (typeLeft == typeRight) {
                    switch (typeLeft) {
                        case INT -> {
                            bufferVisitor.releaseBuffer(visitor);
                            visitor.visitInsn(ISUB);
                        }
                        case BOOLEAN -> throw new ScriptCompileException("ASTAdditiveExpression cannot subtract 2 Booleans.");
                        case DOUBLE -> {
                            bufferVisitor.releaseBuffer(visitor);
                            visitor.visitInsn(DSUB);
                        }
                        case STRING -> throw new ScriptCompileException("ASTAdditiveExpression cannot subtract 2 Strings.");
                        case NULL -> throw new ScriptCompileException("ASTAdditiveExpression Null not implemented.");
                        case VOID -> throw new ScriptCompileException("ASTAdditiveExpression cannot subtract Void.");
                        default -> throw new ScriptCompileException("ASTAdditiveExpression type not implemented.");
                    }
                } else {
                    throw new ScriptCompileException("ASTAdditiveExpression subtracting different types not implemented.");
                }
                continue;
            }
            throw new ScriptCompileException("ASTAdditiveExpression invalid operator.");
        }
        return typeLeft;
    }

    private void compileStringConcat(BufferVisitor bufferVisitor, CompilerMethodVisitor visitor) throws ScriptCompileException {
        final int stringBuilderLocalVarIndex = 1;

        Constructor<StringBuilder> constructor;
        try {
            constructor = StringBuilder.class.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder constructor.");
        }

        visitor.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
        visitor.visitVarInsn(ASTORE, stringBuilderLocalVarIndex);

        visitor.visitVarInsn(ALOAD, stringBuilderLocalVarIndex);
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(StringBuilder.class),
                "<init>",
                Type.getConstructorDescriptor(constructor),
                false);

        visitor.visitVarInsn(ALOAD, stringBuilderLocalVarIndex);
        visitor.visitInsn(SWAP);

        Method method;
        try {
            method = StringBuilder.class.getDeclaredMethod("append", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder.append method.");
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(StringBuilder.class),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);

        bufferVisitor.releaseBuffer(visitor);

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(StringBuilder.class),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);

        try {
            method = StringBuilder.class.getDeclaredMethod("toString");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder.toString method.");
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(StringBuilder.class),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }

    private ScriptingLanguageType compile(ASTMultiplicativeExpression multiplicativeExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var unaryExpression = (ASTUnaryExpression) multiplicativeExpression.jjtGetChild(0);
        ScriptingLanguageType type = compile(unaryExpression, visitor);

        int numChildren = multiplicativeExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return type;
        }

        ScriptingLanguageType typeLeft = type;
        for (int i = 1; i < numChildren; i += 2) {
            Node operator = multiplicativeExpression.jjtGetChild(i);
            ScriptingLanguageType typeRight = compile((ASTUnaryExpression) multiplicativeExpression.jjtGetChild(i + 1), visitor);
            if (operator instanceof ASTMult) {
                if (typeLeft == typeRight) {
                    switch (typeLeft) {
                        case INT -> visitor.visitInsn(IMUL);
                        case BOOLEAN -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot multiply Booleans.");
                        case DOUBLE -> visitor.visitInsn(DMUL);
                        case STRING -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot multiply Strings.");
                        case NULL -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot multiply Nulls.");
                        case VOID -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot multiply Voids.");
                    }
                } else {
                    throw new ScriptCompileException("ASTMultiplicativeExpression multiplying different types not implemented.");
                }
                continue;
            }
            if (operator instanceof ASTDiv) {
                if (typeLeft == typeRight) {
                    switch (typeLeft) {
                        case INT -> visitor.visitInsn(IDIV);
                        case BOOLEAN -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot divide Booleans.");
                        case DOUBLE -> visitor.visitInsn(DDIV);
                        case STRING -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot divide Strings.");
                        case NULL -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot divide Nulls.");
                        case VOID -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot divide Voids.");
                    }
                } else {
                    throw new ScriptCompileException("ASTMultiplicativeExpression dividing different types not implemented.");
                }
                continue;
            }
            if (operator instanceof ASTMod) {
                if (typeLeft == typeRight) {
                    switch (typeLeft) {
                        case INT -> visitor.visitInsn(IREM);
                        case BOOLEAN -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot modulo Booleans.");
                        case DOUBLE -> visitor.visitInsn(DREM);
                        case STRING -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot modulo Strings.");
                        case NULL -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot modulo Nulls.");
                        case VOID -> throw new ScriptCompileException("ASTMultiplicativeExpression cannot modulo Voids.");
                    }
                } else {
                    throw new ScriptCompileException("ASTMultiplicativeExpression dividing different types not implemented.");
                }
                continue;
            }
            throw new ScriptCompileException("ASTMultiplicativeExpression invalid operator.");
        }
        return typeLeft;
    }

    private ScriptingLanguageType compile(ASTUnaryExpression unaryExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = unaryExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return compile((ASTUnaryExpressionNotPlusMinus) unaryExpression.jjtGetChild(0), visitor);
        }
        if (numChildren == 2) {
            Node operator = unaryExpression.jjtGetChild(0);
            ScriptingLanguageType type = compile((ASTUnaryExpression) unaryExpression.jjtGetChild(1), visitor);
            if (operator instanceof ASTPlus) {
                switch (type) {
                    case BOOLEAN -> throw new ScriptCompileException("ASTUnaryExpression cannot use Boolean.");
                    case INT, DOUBLE -> {}
                    case STRING -> throw new ScriptCompileException("ASTUnaryExpression cannot use String.");
                    case NULL -> throw new ScriptCompileException("ASTUnaryExpression cannot use Null.");
                    case VOID -> throw new ScriptCompileException("ASTUnaryExpression cannot use Void.");
                }
                return type;
            }
            if (operator instanceof ASTMinus) {
                switch (type) {
                    case BOOLEAN -> throw new ScriptCompileException("ASTUnaryExpression cannot use Boolean.");
                    case INT -> visitor.visitInsn(INEG);
                    case DOUBLE -> visitor.visitInsn(DNEG);
                    case STRING -> throw new ScriptCompileException("ASTUnaryExpression cannot use String.");
                    case NULL -> throw new ScriptCompileException("ASTUnaryExpression cannot use Null.");
                    case VOID -> throw new ScriptCompileException("ASTUnaryExpression cannot use Void.");
                }
                return type;
            }
            throw new ScriptCompileException("ASTUnaryExpression invalid operator.");
        }
        throw new ScriptCompileException("ASTUnaryExpression invalid children count.");
    }

    private ScriptingLanguageType compile(ASTUnaryExpressionNotPlusMinus unaryExpressionNotPlusMinus, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = unaryExpressionNotPlusMinus.jjtGetNumChildren();
        if (numChildren == 1) {
            return compile((ASTPrimaryExpression) unaryExpressionNotPlusMinus.jjtGetChild(0), visitor);
        }
        if (numChildren == 2) {
            Node operator = unaryExpressionNotPlusMinus.jjtGetChild(0);
            ScriptingLanguageType type = compile((ASTUnaryExpression) unaryExpressionNotPlusMinus.jjtGetChild(1), visitor);
            if (operator instanceof ASTNot) {
                if (type != ScriptingLanguageType.BOOLEAN) {
                    throw new ScriptCompileException("ASTUnaryExpressionNotPlusMinus Not operator can only be applied to Boolean.");
                }

                Label elseLabel = new Label();
                Label endLabel = new Label();
                visitor.visitJumpInsn(IFEQ, elseLabel);
                visitor.visitIntInsn(BIPUSH, 0);
                visitor.visitJumpInsn(GOTO, endLabel);
                visitor.visitLabel(elseLabel);
                visitor.visitIntInsn(BIPUSH, 1);
                visitor.visitLabel(endLabel);

                return ScriptingLanguageType.BOOLEAN;
            }
            if (operator instanceof ASTTilde) {
                throw new ScriptCompileException("ASTUnaryExpressionNotPlusMinus tilde operator not implemented.");
            }
            throw new ScriptCompileException("ASTUnaryExpressionNotPlusMinus invalid operator.");
        }
        throw new ScriptCompileException("ASTUnaryExpressionNotPlusMinus invalid children count.");
    }

    private ScriptingLanguageType compile(ASTLiteral literal, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (literal.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("ASTLiteral invalid children count.");
        }

        Node node = literal.jjtGetChild(0);
        if (node instanceof ASTStringLiteral stringLiteral) {
            String value = parseString((String) stringLiteral.jjtGetValue());
            visitor.visitLdcInsn(value);
            return ScriptingLanguageType.STRING;
        }
        if (node instanceof ASTBooleanLiteral booleanLiteral) {
            boolean value = (boolean) booleanLiteral.jjtGetValue();
            visitor.visitIntInsn(BIPUSH, value ? 1 : 0);
            return ScriptingLanguageType.BOOLEAN;
        }
        if (node instanceof ASTIntegerLiteral integerLiteral) {
            int value = Integer.parseInt((String) integerLiteral.jjtGetValue());
            visitor.visitIntInsn(BIPUSH, value);
            return ScriptingLanguageType.INT;
        }
        if (node instanceof ASTFloatingPointLiteral floatingPointLiteral) {
            double value = Double.parseDouble((String) floatingPointLiteral.jjtGetValue());
            visitor.visitLdcInsn(value);
            return ScriptingLanguageType.DOUBLE;
        }
        if (node instanceof ASTNullLiteral) {
            visitor.visitInsn(ACONST_NULL);
            return ScriptingLanguageType.NULL;
        }

        throw new ScriptCompileException("ASTLiteral case not implemented: " + node.getClass().getName() + ".");
    }

    private Method findMethod(Field field, String name, ScriptingLanguageType[] argumentTypes, BufferVisitor[] argumentVisitors) throws ScriptCompileException {
        Method method = Arrays.stream(field.getType().getDeclaredMethods()).filter(m -> {
            if (!m.getName().equals(name)) {
                return false;
            }
            Parameter[] parameters = m.getParameters();
            if (parameters.length != argumentTypes.length) {
                return false;
            }
            for (int i = 0; i < parameters.length; i++) {
                Class methodParameterClass = parameters[i].getType();
                Class scriptParameterClass = argumentTypes[i].getJavaClass();
                if (methodParameterClass == scriptParameterClass) {
                    continue;
                }
                if (canBeCast(methodParameterClass, scriptParameterClass)) {
                    continue;
                }
                return false;
            }
            return true;
        }).findFirst().orElse(null);

        if (method == null) {
            return null;
        }

        // check if we need to cast any parameters
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Class methodParameterClass = parameters[i].getType();
            Class scriptParameterClass = argumentTypes[i].getJavaClass();
            if (methodParameterClass == scriptParameterClass) {
                continue;
            }
            if (canBeCast(methodParameterClass, scriptParameterClass)) {
                argumentTypes[i] = cast(argumentTypes[i], methodParameterClass, argumentVisitors[i]);
            } else {
                throw new ScriptCompileException("Method parameter cannot be cast. Method: \"" + name + "\", index: " + i + ".");
            }
        }

        return method;
    }

    private boolean canBeCast(Class<?> methodParameterClass, Class<?> scriptParameterClass) {
        if (methodParameterClass == double.class && scriptParameterClass == int.class) {
            return true;
        }
        if (methodParameterClass == String.class && scriptParameterClass == int.class) {
            return true;
        }
        return false;
    }

    private ScriptingLanguageType cast(ScriptingLanguageType type, Class<?> methodParameterClass, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (methodParameterClass == double.class && type.getJavaClass() == int.class) {
            visitor.visitInsn(I2D);
            return ScriptingLanguageType.DOUBLE;
        }

        if (methodParameterClass == String.class && type.getJavaClass() == int.class) {
            Method method;
            try {
                method = Integer.class.getDeclaredMethod("toString", int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new ScriptCompileException("Cannot find Integer.toString() method.");
            }

            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(method.getDeclaringClass()),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);

            return ScriptingLanguageType.STRING;
        }

        throw new ScriptCompileException("Cannot cast.");
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
                        throw new ScriptCompileException("Cannot parse string literal.");// not implemented
                }
            }
        }
        return value;
    }

    private String replace(String value, int from, int length, String replacement) {
        return value.substring(0, from) + replacement + value.substring(from + length);
    }

    private void printTopStackInt(CompilerMethodVisitor visitor) throws ScriptCompileException {
        Method method;
        try {
            method = PrintStream.class.getDeclaredMethod("println", int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ScriptCompileException("printTopStackInt cannot find PrintStream.println(int) method.");
        }

        Field field;
        try {
            field = System.class.getDeclaredField("out");
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new ScriptCompileException("printTopStackInt cannot find System.out field");
        }

        visitor.visitInsn(DUP);
        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(System.class),
                field.getName(),
                Type.getDescriptor(field.getType()));
        visitor.visitInsn(SWAP);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(PrintStream.class),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }

    private abstract class CompilerMethodVisitor {
        public abstract void visitInsn(final int opcode);
        public abstract void visitIntInsn(final int opcode, final int operand);
        public abstract void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor);
        public abstract void visitJumpInsn(final int opcode, final Label label);
        public abstract void visitLabel(final Label label);
        public abstract void visitLdcInsn(final Object value);
        public abstract void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);
        public abstract void visitTypeInsn(final int opcode, final String descriptor);
        public abstract void visitVarInsn(final int opcode, final int index);
    }

    private class MethodVisitorWrapper extends CompilerMethodVisitor {

        private final MethodVisitor visitor;

        public MethodVisitorWrapper(MethodVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitInsn(int opcode) {
            visitor.visitInsn(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            visitor.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            visitor.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            visitor.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            visitor.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object value) {
            visitor.visitLdcInsn(value);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitTypeInsn(int opcode, String descriptor) {
            visitor.visitTypeInsn(opcode, descriptor);
        }

        @Override
        public void visitVarInsn(int opcode, int index) {
            visitor.visitVarInsn(opcode, index);
        }
    }

    private class BufferVisitor extends CompilerMethodVisitor {

        private final List<Consumer<CompilerMethodVisitor>> list = new ArrayList<>();

        public void releaseBuffer(CompilerMethodVisitor visitor) {
            list.forEach(c -> c.accept(visitor));
            list.clear();
        }

        @Override
        public void visitInsn(int opcode) {
            list.add(v -> v.visitInsn(opcode));
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            list.add(v -> v.visitIntInsn(opcode, operand));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            list.add(v -> v.visitFieldInsn(opcode, owner, name, descriptor));
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            list.add(v -> v.visitJumpInsn(opcode, label));
        }

        @Override
        public void visitLabel(Label label) {
            list.add(v -> v.visitLabel(label));
        }

        @Override
        public void visitLdcInsn(Object value) {
            list.add(v -> v.visitLdcInsn(value));
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            list.add(v -> v.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
        }

        @Override
        public void visitTypeInsn(int opcode, String descriptor) {
            list.add(v -> v.visitTypeInsn(opcode, descriptor));
        }

        @Override
        public void visitVarInsn(int opcode, int index) {
            list.add(v -> v.visitVarInsn(opcode, index));
        }
    }
}