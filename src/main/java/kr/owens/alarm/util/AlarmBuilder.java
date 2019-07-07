package kr.owens.alarm.util;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AlarmBuilder {

    private static final String EXTENSION_JAVA = ".java";
    private static final String EXTENSION_CLASS = ".class";
    private static final String EXTENSION_TIME_JOB = "$TimeJob.class";

    public static boolean buildAlarm(String alarmDir) {
        SourceModifier.SOURCE_NAME[] sourceNames = SourceModifier.SOURCE_NAME.values();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        if(compiler == null) {
            System.out.println("im null!");
        }
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Iterable<? extends JavaFileObject> input;
        CompilationTask compilationTask;

        for (SourceModifier.SOURCE_NAME s : sourceNames) {
            input = fileManager.getJavaFileObjects(
                    new File(alarmDir + File.separator + s + EXTENSION_JAVA));
            compilationTask = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, input);

            if (!compilationTask.call()) {
                return false;
            }
        }

        return true;
    }

    public static boolean deleteAlarm(String alarmDir) {
        SourceModifier.SOURCE_NAME[] sourceNames = SourceModifier.SOURCE_NAME.values();
        Path[] alarms = new Path[sourceNames.length * 2];
        int enumIndex = 0;

        for (int i = 0; i < alarms.length; i += 2) {
            alarms[i] = Paths.get(alarmDir + File.separator + sourceNames[enumIndex] + EXTENSION_CLASS);
            alarms[i + 1] = Paths.get(alarmDir + File.separator + sourceNames[enumIndex] + EXTENSION_TIME_JOB);
            enumIndex++;
        }

        try {
            for (Path p : alarms) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
