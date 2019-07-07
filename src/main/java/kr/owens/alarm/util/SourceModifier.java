package kr.owens.alarm.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SourceModifier {

    private static SourceModifier instance = null;

    private static final String SRC_DIR_IS_NULL = "Source Directory is null";
    private static final String SOURCE_EXTENSION = ".java";
    private static final String MUSIC_FILE = "alarm.wav";
    private static final String FILE_SEPARATOR_IN_SOURCE = "\\\\";
    private static final String DATE_AND_TIME_FORMAT = "Date date = (Date) dataForm.parse";
    private static final String SOURCE_LINE_START = "\t\t\tDate date = (Date) dataForm.parse(\"";
    private static final String MUSIC_FORMAT = "player.play(";
    private static final String MUSIC_START = "\t\t\t\tplayer.play(\"";
    private static final String MUSIC_END = "\");";
    private static final String BLANK = " ";
    private static final String SOURCE_LINE_END = "\");";

    public enum SOURCE_NAME {a, s, d}

    private static final int SOURCE_COUNT = 3;

    private String srcDir;

    private String date = null;
    private ArrayList<String> timeList = null;

    private SourceModifier(String srcDir) {
        this.srcDir = srcDir;
    }

    public static synchronized SourceModifier getInstance(String srcDir) {
        if (instance == null) {
            instance = new SourceModifier(srcDir);
        }

        return instance;
    }

    public void setRequired(String date, ArrayList<String> timeList) {
        this.date = date;
        this.timeList = timeList;
    }

    public boolean modifySource() {
        if (srcDir == null) {
            throw new NullPointerException(SRC_DIR_IS_NULL);
        }

        Path[] sources = new Path[SOURCE_COUNT];
        SOURCE_NAME[] sourceNames = SOURCE_NAME.values();

        for (int i = 0; i < sources.length; i++) {
            sources[i] = Paths.get(srcDir + File.separator + sourceNames[i] + SOURCE_EXTENSION);
        }

        int timeIndex = 0;
        try {
            for (Path source : sources) {
                ArrayList<String> sourceLines = new ArrayList<>(Files.readAllLines(source));
                String line;
                for (int i = 0; i < sourceLines.size(); i++) {
                    line = sourceLines.get(i);

                    if (line.contains(DATE_AND_TIME_FORMAT)) {
                        line = SOURCE_LINE_START + date + BLANK + timeList.get(timeIndex) + SOURCE_LINE_END;
                        sourceLines.set(i, line);
                        timeIndex++;

                        continue;
                    }

                    if (line.contains(MUSIC_FORMAT)) {
                        line = MUSIC_START + srcDir + File.separator + MUSIC_FILE + MUSIC_END;
                        line = line.replace(File.separator, FILE_SEPARATOR_IN_SOURCE);
                        sourceLines.set(i, line);
                    }
                }

                Files.delete(source);
                Files.createFile(source);
                Files.write(source, sourceLines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }
}
