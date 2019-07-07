package kr.owens.alarm.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class CacheUtil {

    private static CacheUtil instance = null;

    private static final String CACHE_DIR = ".cache";
    private static final String CURRENT_DIR = ".";

    private static final Path CACHE_FILE = Paths.get(new File(CURRENT_DIR).getAbsolutePath()
            + File.separator + CACHE_DIR + File.separator + CacheInfo.SOURCE_DAT);

    private CacheUtil() {

    }

    public static synchronized CacheUtil getInstance() {
        if (instance == null) {
            instance = new CacheUtil();
        }

        return instance;
    }

    public CacheInfo createInfo() {
        return new CacheInfo();
    }

    public boolean isCacheNotExist() {
        return Files.notExists(CACHE_FILE);
    }

    public synchronized boolean saveCache(CacheInfo info) {
        try {

            if (Files.notExists(CACHE_FILE.getParent())) {
                Files.createDirectories(CACHE_FILE.getParent());
            }

            if (Files.notExists(CACHE_FILE)) {
                Files.createFile(CACHE_FILE);
            }

            if (Files.exists(CACHE_FILE)) {
                Files.write(CACHE_FILE, info.getSourcePath().getBytes(StandardCharsets.UTF_8));
            } else {
                throw new IOException();
            }

        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public synchronized boolean deleteCache() {
        try {
            Files.walk(CACHE_FILE.getParent())
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public String loadAlarmPath() {
        try {
            return new String(Files.readAllBytes(CACHE_FILE), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public class CacheInfo {

        private static final String SOURCE_DAT = "source.dat";

        private String sourcePath = null;

        String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }
    }
}
