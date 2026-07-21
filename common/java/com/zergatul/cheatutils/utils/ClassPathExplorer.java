package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@NullMarked
public class ClassPathExplorer {

    public static final ClassPathExplorer INSTANCE = new ClassPathExplorer();

    private final Logger logger = LogManager.getLogger();
    private @Nullable List<String> classes;

    private ClassPathExplorer() {}

    public synchronized List<String> getClasses() {
        if (classes == null) {
            classes = getClassesInternal();
        }

        return classes;
    }

    private List<String> getClassesInternal() {
        Set<String> output = new HashSet<>(32768);
        for (String path : getClassPaths()) {
            getClassesByPath(path, output);
        }
        for (String jar : ModLoaderBridgeInstance.get().getModsJars()) {
            getClassesByPath(jar, output);
        }

        List<String> result = new ArrayList<>(output);
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(result);
    }

    private List<String> getClassPaths() {
        List<String> list = new ArrayList<>();
        char separator = File.pathSeparatorChar;
        String classPath = System.getProperty("java.class.path");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < classPath.length(); i++) {
            if (classPath.charAt(i) == separator) {
                list.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(classPath.charAt(i));
            }
        }
        list.add(builder.toString());
        return list;
    }

    private void getClassesByPath(String path, Set<String> output) {
        File file = new File(path);
        if (file.isDirectory()) {
            getClassesByDirectory(file, output);
        } else if (file.isFile()) {
            getClassesByFile(file, output);
        }
    }

    private void getClassesByDirectory(File directory, Set<String> output) {
        try {
            Path root = directory.toPath();
            Files.walkFileTree(root, new Visitor(this, root, output));
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void getClassesByFile(File file, Set<String> output) {
        String name = file.getName();
        if (name.endsWith(".class")) {
            String className = name.substring(0, name.length() - 6);
            addClass(className, output);
        } else if (name.endsWith(".jar")) {
            getClassesByJarFile(file, output);
        }
    }

    private void getClassesByJarFile(File jar, Set<String> output) {
        try (ZipFile zip = new ZipFile(jar, ZipFile.OPEN_READ)) {
            ZipEntryInputStreamFactory factory = new ZipFileInputStreamFactory(zip);
            Iterator<? extends ZipEntry> iterator = zip.stream().iterator();
            while (iterator.hasNext()) {
                processZipEntry(iterator.next(), factory, output);
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private static void processZipEntry(ZipEntry entry, ZipEntryInputStreamFactory factory, Set<String> output) throws IOException {
        if (entry.isDirectory()) {
            return;
        }

        if (entry.getName().endsWith(".jar")) {
            try (InputStream jarStream = factory.getContent(entry)) {
                try (ZipInputStream zipStream = new ZipInputStream(jarStream)) {
                    ZipEntryInputStreamFactory innerFactory = new ZipStreamInputStreamFactory(zipStream);

                    while (true) {
                        ZipEntry innerEntry = zipStream.getNextEntry();
                        if (innerEntry == null) {
                            break;
                        }

                        processZipEntry(innerEntry, innerFactory, output);
                    }
                }
            }
            return;
        }

        if (entry.getName().endsWith(".class")) {
            if (entry.getName().startsWith("META-INF/")) {
                // we can ignore version .class files, since they are duplicates
                return;
            }

            String className = relativeToClassName(entry.getName());
            addClass(className, output);
        }
    }

    private static void addClass(String className, Set<String> output) {
        if (className.equals("package-info") || className.endsWith(".package-info")) {
            return;
        }

        if (className.equals("module-info") || className.endsWith(".module-info")) {
            return;
        }

        output.add(className);
    }

    private static String relativeToClassName(String path) {
        return path
                .substring(0, path.length() - 6)
                .replace('\\', '.')
                .replace('/', '.');
    }

    private static class Visitor extends SimpleFileVisitor<Path> {

        private final ClassPathExplorer explorer;
        private final Path root;
        private final Set<String> output;

        public Visitor(ClassPathExplorer explorer, Path root, Set<String> output) {
            this.explorer = explorer;
            this.root = root;
            this.output = output;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (!attrs.isRegularFile()) {
                return FileVisitResult.CONTINUE;
            }

            String relative = root.relativize(file).toString();
            if (relative.endsWith(".class")) {
                String className = relativeToClassName(relative);
                addClass(className, output);
            } else if (relative.endsWith(".jar")) {
                explorer.getClassesByJarFile(file.toFile(), output);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

    private static abstract class ZipEntryInputStreamFactory {
        public abstract InputStream getContent(ZipEntry entry) throws IOException;
    }

    private static class ZipFileInputStreamFactory extends ZipEntryInputStreamFactory {

        private final ZipFile zip;

        public ZipFileInputStreamFactory(ZipFile zip) {
            this.zip = zip;
        }

        @Override
        public InputStream getContent(ZipEntry entry) throws IOException {
            return zip.getInputStream(entry);
        }
    }

    private static class ZipStreamInputStreamFactory extends ZipEntryInputStreamFactory {

        private final ZipInputStream zip;

        public ZipStreamInputStreamFactory(ZipInputStream zip) {
            this.zip = zip;
        }

        @Override
        public InputStream getContent(ZipEntry entry) throws IOException {
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            while (true) {
                int read = zip.read(buffer);
                if (read == -1) {
                    break;
                }
                content.write(buffer, 0, read);
            }

            return new ByteArrayInputStream(content.toByteArray());
        }
    }
}