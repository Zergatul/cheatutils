package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GenerateEntityMappingApi extends ApiBase {

    @Override
    public String getRoute() {
        return "gen-entity-mapping";
    }

    @Override
    public String get() throws HttpException {
        /*String path = "C:\\Users\\Zergatul\\.gradle\\caches\\fabric-loom\\1.19.3\\loom.mappings.1_19_3.layered+hash.2198-v2\\mappings.jar";
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
            e.printStackTrace();
            return e.getMessage();
        }

        if (mappings == null) {
            return "mappings = null";
        }

        List<EntityUtils.EntityInfo> classes = EntityUtils.getEntityClasses();

        Pattern pattern = Pattern.compile("^c\\t[a-z$]+\\t(?<obf>[a-zA-Z/_0-9$]+)\\t(?<norm>[a-zA-Z/_0-9$/]+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mappings);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String obf = matcher.group("obf").replace('/', '.');
            String norm = matcher.group("norm").replace('/', '.');
            Class<?> clazz;
            try {
                clazz = Class.forName(norm, false, Minecraft.class.getClassLoader());
            } catch (Throwable e) {
                e.printStackTrace();
                continue;
            }
            if (classes.stream().anyMatch(i -> i.clazz == clazz)) {
                builder.append('"').append(obf).append(":").append(norm).append('"').append(",\r\n");
            }
        }

        return builder.toString();*/
        return "";
    }
}