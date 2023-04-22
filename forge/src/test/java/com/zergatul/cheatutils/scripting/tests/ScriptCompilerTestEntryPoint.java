package com.zergatul.cheatutils.scripting.tests;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.cheatutils.scripting.generated.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ScriptCompilerTestEntryPoint {
    public static void main(String[] args) {
        testFile("test-script.txt");
        testFile("variable-script.txt");
        testFile("casts.txt");
        testFile("parameters.txt");
    }

    private static void testFile(String name) {
        var compiler = new ScriptingLanguageCompiler(TestRoot.class, new ApiType[0]);
        try {
            ClassLoader classLoader = ScriptCompilerTestEntryPoint.class.getClassLoader();
            InputStream stream = classLoader.getResourceAsStream(name);
            String code = IOUtils.toString(stream, Charset.defaultCharset());

            Runnable program = compiler.compile(code);
            program.run();
        } catch (ParseException | ScriptCompileException | IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            System.out.println("Generic exception!");
            e.printStackTrace();
        }

        int successCount = TestRoot.Assert.getSuccess().size();
        int failCount = TestRoot.Assert.getFail().size();
        System.out.println("Success: " + successCount +", Fail: " + failCount);
        if (failCount > 0) {
            System.out.println("Fails:");
            for (var x : TestRoot.Assert.getFail()) {
                System.out.println(x);
            }
        }
    }

    public static class TestRoot {
        public static Assertion Assert = new Assertion();
        public static Deep deep = new Deep();
        public static Methods methods = new Methods();
    }

    public static class Assertion {

        private List<String> success = new ArrayList<>();
        private List<String> fail = new ArrayList<>();

        public List<String> getSuccess() {
            return success;
        }

        public List<String> getFail() {
            return fail;
        }

        public void isTrue(String name, boolean value) {
            (value ? success : fail).add(name);
        }

        public void isFalse(String name, boolean value) {
            (!value ? success : fail).add(name);
        }

        public void equals(String name, double value1, double value2) {
            (value1 == value2 ? success : fail).add(name);
        }

        public void notEquals(String name, double value1, double value2) {
            (value1 != value2 ? success : fail).add(name);
        }

        public void clear() {
            success.clear();
            fail.clear();
        }
    }

    public static class Deep {

        public Deep1 deep = new Deep1();

        public int getValue() {
            return 987;
        }
    }

    public static class Deep1 {

        public Deep2 deep = new Deep2();

        public int getValue() {
            return 101;
        }
    }

    public static class Deep2 {

        public Deep3 deep = new Deep3();

        public int getValue() {
            return 654;
        }
    }

    public static class Deep3 {

        public Deep4 deep = new Deep4();

        public int getValue() {
            return 321;
        }
    }

    public static class Deep4 {

        public int getValue() {
            return 100;
        }
    }

    public static class Methods {
        public double m1(int x, int y, int z, String s) {
            return x;
        }

        public double m1(double x, double y, double z, String s) {
            return y;
        }

        public String toString(int value) {
            return "int";
        }

        public String toString(double value) {
            return "double";
        }
    }
}