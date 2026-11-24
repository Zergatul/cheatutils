package com.zergatul.cheatutils

object ImmutableConfigGenerator {

    fun generate(source: String): String {
        val startMarker = "// <generated-code>"
        val endMarker = "// </generated-code>"
        val start = source.indexOf(startMarker)
        if (start == -1) {
            return source
        }

        val contentStart = start + startMarker.length
        val end = source.indexOf(endMarker, contentStart)
        require(end != -1) { "Missing $endMarker" }
        val contentEnd = source.lastIndexOf('\n', end) + 1
        require(contentEnd >= contentStart) { "Generated-code markers must be on separate lines" }

        val declaration = Regex("""\brecord\s+(\w+)\s*\(""").find(source)
        require(declaration != null) { "Missing record declaration" }
        val name = declaration.groupValues[1]
        val componentsStart = declaration.range.last + 1
        val componentsEnd = Regex("""(?m)^[ \t]*\)""").find(source, componentsStart)
        require(componentsEnd != null) { "Missing closing record declaration line" }
        val components = source.substring(componentsStart, componentsEnd.range.first)
            .lineSequence()
            .map { it.trim().removeSuffix(",") }
            .filter { it.isNotEmpty() }
            .map { line ->
                val separator = line.indexOfLast { it.isWhitespace() }
                Component(line.substring(0, separator).trimEnd(), line.substring(separator + 1))
            }
            .toList()

        val interfaces = source.substring(componentsEnd.range.last + 1).substringBefore("{")
        val withUpdateOptions = Regex("""\bImmutableConfigWithUpdateOptions\s*<""").containsMatchIn(interfaces)
        if (withUpdateOptions) {
            require(components.any { it.name == "code" && it.type == "String" }) {
                "ImmutableConfigWithUpdateOptions requires a String code component"
            }
        }

        val generated = buildString {
            appendLine()
            for (component in components) {
                val methodName = component.name.replaceFirstChar { it.uppercaseChar() }
                appendLine("    public $name with$methodName(${component.type} ${component.name}) {")
                appendLine("        return Builder.from(this).${component.name}(${component.name}).build();")
                appendLine("    }")
                appendLine()
            }
            if (withUpdateOptions) {
                val options = components.filter { it.name != "code" }
                appendLine("    @Override")
                appendLine("    public $name withOptions(UpdateOptions options) {")
                appendLine("        return Builder.from(this)")
                for (component in options) {
                    appendLine("                .${component.name}(options.${component.name}())")
                }
                appendLine("                .build();")
                appendLine("    }")
                appendLine()
                appendLine("    public record UpdateOptions(")
                options.forEachIndexed { index, component ->
                    val comma = if (index < options.lastIndex) "," else ""
                    appendLine("            ${component.type} ${component.name}$comma")
                }
                appendLine("    ) {}")
                appendLine()
            }
            appendLine("    public static class Builder {")
            appendLine()
            for (component in components) {
                appendLine("        private ${component.type} ${component.name};")
            }
            appendLine()
            appendLine("        private Builder() {}")
            appendLine()
            appendLine("        public static Builder from($name config) {")
            append("            return new Builder()")
            for (component in components) {
                appendLine()
                append("                    .${component.name}(config.${component.name})")
            }
            appendLine(";")
            appendLine("        }")
            appendLine()
            for (component in components) {
                appendLine("        public Builder ${component.name}(${component.type} ${component.name}) {")
                appendLine("            this.${component.name} = ${component.name};")
                appendLine("            return this;")
                appendLine("        }")
                appendLine()
            }
            appendLine("        public $name build() {")
            appendLine("            return new $name(${components.joinToString(", ") { it.name }});")
            appendLine("        }")
            appendLine("    }")
            appendLine()
        }

        return source.substring(0, contentStart) + "\n" + generated + source.substring(contentEnd)
    }

    private data class Component(val type: String, val name: String)
}