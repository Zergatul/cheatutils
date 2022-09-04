package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.scripting.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        Node first = statement.jjtGetChild(0);
        if (first instanceof ASTEmptyStatement) {
            return;
        }
        if (first instanceof ASTMethodCall methodCall) {
            ScriptingLanguageType type = compile(methodCall, visitor);
            if (type != ScriptingLanguageType.VOID) {
                visitor.visitInsn(POP);
            }
            return;
        }
        if (first instanceof ASTBlockStatement blockStatement) {
            compile(blockStatement, visitor);
            return;
        }
        if (first instanceof ASTIfStatement ifStatement) {
            compile(ifStatement, visitor);
            return;
        }
        throw new ScriptCompileException("ASTStatement case not implemented: " + first.getClass().getName() + ".");
    }

    private ScriptingLanguageType compile(ASTMethodCall methodCall, CompilerMethodVisitor visitor) throws ScriptCompileException {
        var name = (ASTName) methodCall.jjtGetChild(0);
        var arguments = (ASTArguments) methodCall.jjtGetChild(1);
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

        int argsLength = arguments.jjtGetNumChildren();
        ScriptingLanguageType[] methodArgumentTypes = new ScriptingLanguageType[argsLength];
        BufferVisitor[] methodArgumentVisitors = new BufferVisitor[argsLength];
        for (int i = 0; i < argsLength; i++) {
            methodArgumentVisitors[i] = new BufferVisitor();
            methodArgumentTypes[i] = compile((ASTExpression) arguments.jjtGetChild(i), methodArgumentVisitors[i]);
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
        if (node instanceof ASTExpression innerExpression) {
            return compile(innerExpression, visitor);
        }
        if (node instanceof ASTMethodCall methodCall) {
            return compile(methodCall, visitor);
        }
        if (node instanceof ASTLiteral literal) {
            return compile(literal, visitor);
        }

        throw new ScriptCompileException("ASTExpression case not implemented: " + node.getClass().getName() + ".");
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
        return false;
    }

    private ScriptingLanguageType cast(ScriptingLanguageType type, Class<?> methodParameterClass, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (methodParameterClass == double.class && type.getJavaClass() == int.class) {
            visitor.visitInsn(I2D);
            return ScriptingLanguageType.DOUBLE;
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

    private abstract class CompilerMethodVisitor {
        public abstract void visitInsn(final int opcode);
        public abstract void visitIntInsn(final int opcode, final int operand);
        public abstract void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor);
        public abstract void visitJumpInsn(final int opcode, final Label label);
        public abstract void visitLabel(final Label label);
        public abstract void visitLdcInsn(final Object value);
        public abstract void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);
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
    }
}