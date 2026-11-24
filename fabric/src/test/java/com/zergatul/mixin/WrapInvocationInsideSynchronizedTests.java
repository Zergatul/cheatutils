package com.zergatul.mixin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class WrapInvocationInsideSynchronizedTests {

    private static final ThreadLocal<State> CURRENT = new ThreadLocal<>();

    @BeforeEach
    public void setup() {
        CURRENT.set(new State());
    }

    @AfterEach
    public void checkReleased() {
        try {
            assertReleased();
        } finally {
            CURRENT.remove();
        }
    }

    @Test
    public void acquiresInOrderAndPreservesInvocationAndResult() throws Throwable {
        assertEquals(19, call(wrap("integer", "invokeInt"), "integer"));
        assertEquals(List.of("provider0", "provider1", "invoke"), CURRENT.get().events);
    }

    @Test
    public void preservesVoidAndAllValueCategoriesWithExistingStackOperands() throws Throwable {
        assertNull(call(wrap("nothing", "invokeVoid"), "nothing"));
        assertEquals(48L, call(wrap("wide", "invokeLong"), "wide", new Class<?>[] { long.class }, 41L));
        assertEquals(6.75, call(wrap("decimal", "invokeDouble"), "decimal", new Class<?>[] { double.class }, 4.25));
        assertEquals(7.5f, call(wrap("single", "invokeFloat"), "single", new Class<?>[] { float.class }, 4.5f));
        Object value = new Object();
        assertSame(value, call(wrap("reference", "invokeObject"), "reference", new Class<?>[] { Object.class }, value));
    }

    @Test
    public void supportsInterfaceReceiverAndWideArguments() throws Throwable {
        Receiver receiver = (value, fraction) -> {
            invoked();
            assertEquals(41L, value);
            assertEquals(2.5, fraction);
            return value + (long) fraction;
        };
        assertEquals(50L, call(wrap("receiver", "run"), "receiver",
                new Class<?>[] { Receiver.class, long.class, double.class }, receiver, 41L, 2.5));
    }

    @Test
    public void supportsSpecialInvocationWithoutMovingIt() throws Throwable {
        Class<?> fixture = wrap("special", "run");
        Object instance = fixture.getConstructor().newInstance();
        assertEquals(19, fixture.getMethod("special").invoke(instance));
    }

    @Test
    public void releasesOnRuntimeExceptionsAndErrorsWithoutChangingIdentity() throws Throwable {
        Class<?> fixture = wrap("integer", "invokeInt");
        for (Throwable failure : new Throwable[] { new IllegalStateException("invoke"), new AssertionError("invoke") }) {
            CURRENT.get().failure = failure;
            assertSame(failure, assertThrows(Throwable.class, () -> call(fixture, "integer")));
            assertReleased();
        }
    }

    @Test
    public void releasesEarlierMonitorsWhenProviderThrows() throws Throwable {
        State state = CURRENT.get();
        state.failure = new IllegalStateException("provider");
        state.failedProvider = 1;
        assertSame(state.failure, assertThrows(Throwable.class, () -> call(wrap("integer", "invokeInt"), "integer")));
        assertEquals(List.of("provider0", "provider1"), state.events);
    }

    @Test
    public void nullMonitorReleasesEarlierMonitors() throws Throwable {
        State state = CURRENT.get();
        state.monitors[1] = null;
        assertThrows(NullPointerException.class, () -> call(wrap("integer", "invokeInt"), "integer"));
        assertEquals(List.of("provider0", "provider1"), state.events);
    }

    @Test
    public void firstProviderFailureAndNullDoNotAcquireLaterMonitors() throws Throwable {
        State state = CURRENT.get();
        Class<?> fixture = wrap("integer", "invokeInt");
        state.failedProvider = 0;
        state.failure = new IllegalStateException("first provider");
        assertSame(state.failure, assertThrows(Throwable.class, () -> call(fixture, "integer")));
        assertEquals(List.of("provider0"), state.events);

        state.failedProvider = -1;
        state.monitors[0] = null;
        state.events.clear();
        assertThrows(NullPointerException.class, () -> call(fixture, "integer"));
        assertEquals(List.of("provider0"), state.events);
    }

    @Test
    public void existingCatchRunsAfterCleanupAndReceivesOriginalException() throws Throwable {
        CURRENT.get().failure = new IllegalStateException("caught");
        assertEquals(-1, call(wrap("caught", "invokeInt"), "caught"));
        assertEquals(List.of("provider0", "provider1", "invoke", "catch"), CURRENT.get().events);
    }

    @Test
    public void existingCatchAlsoHandlesProviderFailureAfterCleanup() throws Throwable {
        CURRENT.get().failedProvider = 1;
        CURRENT.get().failure = new IllegalStateException("provider");
        assertEquals(-1, call(wrap("caught", "invokeInt"), "caught"));
        assertEquals(List.of("provider0", "provider1", "catch"), CURRENT.get().events);
    }

    @Test
    public void existingFinallyRunsAfterCleanupOnNormalAndExceptionalPaths() throws Throwable {
        Class<?> fixture = wrap("withFinally", "invokeInt");
        assertEquals(19, call(fixture, "withFinally"));
        assertEquals("finally", CURRENT.get().events.getLast());

        CURRENT.get().failure = new AssertionError("finally");
        assertSame(CURRENT.get().failure, assertThrows(Throwable.class, () -> call(fixture, "withFinally")));
        assertEquals("finally", CURRENT.get().events.getLast());
    }

    @Test
    public void supportsMultipleCallSitesWithOperandStackPrefix() throws Throwable {
        assertEquals(38, call(wrap("twice", "invokeInt"), "twice"));
        assertEquals(List.of("provider0", "provider1", "invoke", "provider0", "provider1", "invoke"), CURRENT.get().events);
    }

    @Test
    public void supportsUninitializedObjectBelowInvocationOperands() throws Throwable {
        Object result = call(wrap("construct", "invokeInt"), "construct");
        assertEquals(19, ((Value) result).value);
    }

    @Test
    public void supportsOneOrSeveralMonitors() throws Throwable {
        for (int count : new int[] { 1, 3 }) {
            CURRENT.get().monitors = new Object[count];
            for (int index = 0; index < count; index++) {
                CURRENT.get().monitors[index] = new Object();
            }
            assertEquals(19, call(wrap("integer", "invokeInt"), "integer"));
            assertReleased();
        }
    }

    @Test
    public void supportsRepeatedMonitorObjectsAndExistingReentrantOwnership() throws Throwable {
        Object monitor = CURRENT.get().monitors[0];
        CURRENT.get().monitors[1] = monitor;
        Class<?> fixture = wrap("integer", "invokeInt");
        synchronized (monitor) {
            assertEquals(19, call(fixture, "integer"));
            assertTrue(Thread.holdsLock(monitor));
            CURRENT.get().failure = new IllegalStateException("reentrant");
            assertSame(CURRENT.get().failure, assertThrows(Throwable.class, () -> call(fixture, "integer")));
            assertTrue(Thread.holdsLock(monitor));
        }
    }

    public static Object monitor(int index) {
        State state = CURRENT.get();
        state.events.add("provider" + index);
        for (int previous = 0; previous < index; previous++) {
            assertTrue(Thread.holdsLock(state.monitors[previous]), "Earlier monitors must be held while obtaining later monitors");
        }
        if (state.failedProvider == index) {
            failIfRequested();
        }
        return state.monitors[index];
    }

    public static int invokeInt() {
        invoked();
        failIfRequested();
        return 19;
    }

    public static void invokeVoid() {
        invoked();
    }

    public static long invokeLong(long value) {
        invoked();
        return value;
    }

    public static double invokeDouble(double value) {
        invoked();
        return value;
    }

    public static float invokeFloat(float value) {
        invoked();
        return value;
    }

    public static Object invokeObject(Object value) {
        invoked();
        return value;
    }

    public static int caught(Throwable failure) {
        assertSame(CURRENT.get().failure, failure);
        assertReleased();
        CURRENT.get().events.add("catch");
        return -1;
    }

    public static void finished() {
        assertReleased();
        CURRENT.get().events.add("finally");
    }

    private static void invoked() {
        CURRENT.get().events.add("invoke");
        for (Object monitor : CURRENT.get().monitors) {
            assertTrue(Thread.holdsLock(monitor), "Invocation must hold every monitor");
        }
    }

    private static void failIfRequested() {
        Throwable failure = CURRENT.get().failure;
        if (failure instanceof RuntimeException exception) {
            throw exception;
        }
        if (failure instanceof Error error) {
            throw error;
        }
    }

    private static void assertReleased() {
        for (Object monitor : CURRENT.get().monitors) {
            if (monitor != null) {
                assertFalse(Thread.holdsLock(monitor), "Monitor leaked");
            }
        }
    }

    private static Class<?> wrap(String methodName, String invocationName) throws Exception {
        ClassNode node = new ClassNode();
        try (var stream = Fixture.class.getResourceAsStream("/" + Type.getInternalName(Fixture.class) + ".class")) {
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
        }
        MethodNode method = node.methods.stream().filter(candidate -> candidate.name.equals(methodName)).findFirst().orElseThrow();
        MethodInsnNode provider = new MethodInsnNode(INVOKESTATIC,
                Type.getInternalName(WrapInvocationInsideSynchronizedTests.class), "monitor", "(I)Ljava/lang/Object;", false);
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode invocation && invocation.name.equals(invocationName)) {
                int firstLocal = method.maxLocals;
                int monitorCount = CURRENT.get().monitors.length;
                method.maxLocals += monitorCount + 1;
                SynchronizedInvocation.wrap(method, invocation, provider, monitorCount, firstLocal);
                method.maxStack += 2;
                assertSame(invocation, method.instructions.get(method.instructions.indexOf(invocation)));
                count++;
            }
        }
        assertTrue(count > 0, "Fixture must contain at least one matching invocation");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return new FixtureLoader().define(node.name.replace('/', '.'), writer.toByteArray());
    }

    private static Object call(Class<?> fixture, String method) throws Throwable {
        return call(fixture, method, new Class<?>[0]);
    }

    private static Object call(Class<?> fixture, String method, Class<?>[] types, Object... arguments) throws Throwable {
        try {
            return fixture.getMethod(method, types).invoke(null, arguments);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private static class FixtureLoader extends ClassLoader {
        FixtureLoader() {
            super(WrapInvocationInsideSynchronizedTests.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    private static class State {
        Object[] monitors = { new Object(), new Object() };
        List<String> events = new ArrayList<>();
        int failedProvider = -1;
        Throwable failure;
    }

    public interface Receiver {
        long run(long value, double fraction);
    }

    public static class Base {
        public int run() {
            return invokeInt();
        }
    }

    public static class Value {
        public final int value;

        public Value(int value) {
            this.value = value;
        }
    }

    public static class Fixture extends Base {
        public static int integer() { return invokeInt(); }
        public static void nothing() { invokeVoid(); }
        public static long wide(long value) { return 7L + invokeLong(value); }
        public static double decimal(double value) { return 2.5 + invokeDouble(value); }
        public static float single(float value) { return 3.0f + invokeFloat(value); }
        public static Object reference(Object value) { return invokeObject(value); }
        public static long receiver(Receiver receiver, long value, double fraction) { return 7L + receiver.run(value, fraction); }
        public int special() { return super.run(); }
        public static int twice() { return invokeInt() + invokeInt(); }
        public static Value construct() { return new Value(invokeInt()); }

        public static int caught() {
            try {
                return invokeInt();
            } catch (Throwable failure) {
                return WrapInvocationInsideSynchronizedTests.caught(failure);
            }
        }

        public static int withFinally() {
            try {
                return invokeInt();
            } finally {
                finished();
            }
        }
    }
}