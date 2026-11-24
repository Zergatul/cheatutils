package com.zergatul.cheatutils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImmutableConfigGeneratorTests {

    @Test
    fun generatesAllOptionsExceptCode() {
        val generated = ImmutableConfigGenerator.generate(config("""
            boolean enabled,
            String code,
            Map<String, Integer> limits
        """.trimIndent()))

        assertTrue(generated.contains("""
            |    @Override
            |    public ExampleConfig withOptions(UpdateOptions options) {
            |        return Builder.from(this)
            |                .enabled(options.enabled())
            |                .limits(options.limits())
            |                .build();
            |    }
        """.trimMargin()))
        assertTrue(generated.contains("""
            |    public record UpdateOptions(
            |            boolean enabled,
            |            Map<String, Integer> limits
            |    ) {}
        """.trimMargin()))
        assertTrue(generated.contains("public ExampleConfig withCode(String code)"))
    }

    @Test
    fun requiresStringCodeComponent() {
        for (components in listOf("boolean enabled", "boolean enabled,\nint code", "String source")) {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                ImmutableConfigGenerator.generate(config(components))
            }
            assertEquals("ImmutableConfigWithUpdateOptions requires a String code component", exception.message)
        }
    }

    @Test
    fun onlyTheMainRecordInterfacesEnableOptionsGeneration() {
        val source = config("String code", "ImmutableConfig")
            .replace("// <generated-code>", "// ImmutableConfigWithUpdateOptions<ExampleConfig.UpdateOptions>\n    // <generated-code>")
        val generated = ImmutableConfigGenerator.generate(source)

        assertFalse(generated.contains("public record UpdateOptions("))
        assertFalse(generated.contains("withOptions(UpdateOptions options)"))
        assertTrue(generated.contains("public ExampleConfig withCode(String code)"))
    }

    @Test
    fun supportsMultilineImplementsDeclaration() {
        val generated = ImmutableConfigGenerator.generate(config(
            "boolean enabled,\nString code",
            "ImmutableConfig,\n        ImmutableConfigWithUpdateOptions<ExampleConfig.UpdateOptions>"
        ))

        assertTrue(generated.contains("public record UpdateOptions("))
    }

    @Test
    fun codeOnlyConfigHasEmptyOptions() {
        val generated = ImmutableConfigGenerator.generate(config("String code"))

        assertTrue(generated.contains("    public record UpdateOptions(\n    ) {}"))
        assertTrue(generated.contains("return Builder.from(this)\n                .build();"))
    }

    @Test
    fun regenerationReplacesOldOptionsAndIsIdempotent() {
        val first = ImmutableConfigGenerator.generate(config("boolean enabled,\nString code"))
        val updated = first.replace("boolean enabled,", "boolean enabled,\n        int count,")
        val generated = ImmutableConfigGenerator.generate(updated)

        assertTrue(generated.contains(".count(options.count())"))
        assertTrue(generated.contains("            int count\n    ) {}"))
        assertEquals(generated, ImmutableConfigGenerator.generate(generated))
        assertFalse(generated.contains('\r'))
    }

    private fun config(
        components: String,
        interfaces: String = "ImmutableConfigWithUpdateOptions<ExampleConfig.UpdateOptions>"
    ): String = """
        |import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
        |
        |public record ExampleConfig(
        |${components.prependIndent("        ")}
        |) implements $interfaces {
        |
        |    // <generated-code>
        |    // </generated-code>
        |}
    """.trimMargin()
}