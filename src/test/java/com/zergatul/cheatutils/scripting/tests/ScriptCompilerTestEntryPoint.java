package com.zergatul.cheatutils.scripting.tests;

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
        var compiler = new ScriptingLanguageCompiler(TestRoot.class);
        try {
            ClassLoader classLoader = ScriptCompilerTestEntryPoint.class.getClassLoader();
            InputStream stream = classLoader.getResourceAsStream("test-script.txt");
            String code = IOUtils.toString(stream, Charset.defaultCharset());

            Runnable program = compiler.compile(code);
            program.run();
        } catch (ParseException | ScriptCompileException | IOException e) {
            e.printStackTrace();
        }

        int successCount = TestRoot.Assert.getSuccess().size();
        int failCount = TestRoot.Assert.getFail().size();
        System.out.println("Success: " + successCount +", Fail: " + failCount);
    }

    public static class TestRoot {
        public static Assertion Assert = new Assertion();
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
    }
}