package com.zergatul.cheatutils.utils.mappings;

import com.zergatul.cheatutils.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Mappings {

    public static void process(BiConsumer<String, String> consumer) {
        String path = System.getenv("USERPROFILE") + "\\.gradle\\caches\\fabric-loom\\1.21\\loom.mappings.1_21.layered+hash.2198-v2\\mappings.jar";
        String mappings = null;
        try {
            ZipFile file = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("mappings/mappings.tiny")) {
                    InputStream stream = file.getInputStream(entry);
                    mappings = IOUtils.toString(stream, Charset.defaultCharset());
                    stream.close();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read mappings.tiny.", e);
        }

        if (mappings == null) {
            throw new RuntimeException("mappings = null");
        }

        Pattern pattern = Pattern.compile("^c\\t[a-z$]+\\t(?<obf>[a-zA-Z/_0-9$]+)\\t(?<norm>[a-zA-Z/_0-9$]+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mappings);
        while (matcher.find()) {
            String obf = matcher.group("obf");
            String norm = matcher.group("norm");
            consumer.accept(obf, norm);
        }
    }
}