package kr.owens.alarm;

import kr.owens.alarm.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Application {

    private static final String MAIN_TITLE = "Morning Alarm";
    private static final String FILE_CHOOSER_TITLE = "wav File select";
    private static final String ERROR_TITLE = "Error";
    private static final String CANCEL_MESSAGE = "Alarm path isn't selected. Exit the Tool.";
    private static final String DELETE_CACHE_FAILED = "Cache delete failed! Exit the Tool.";
    private static final String SAVE_CACHE_FAILED = "Save cache failed! Exit the Tool.";
    private static final String MODIFIED_FAILED = "Source modify failed! Exit the Tool.";
    private static final String DELETE_CLASS_FAILED = "Delete class failed! Exit the Tool.";
    private static final String BUILD_FAILED = "Build source failed! Exit the Tool.";
    private static final String CMD_EXECUTE_FAILED = "cmd.exe execution failed!";
    private static final String ONE_TIME = "07:40";
    private static final String TWO_TIME = "07:55";
    private static final String THREE_TIME = "08:40";
    private static final String SECONDS_TEXT = ":01";
    private static final String BUTTON_TEXT = "Build Alarm";
    private static final String RESET_BUTTON_TEXT = "Reset Cache";
    private static final String DATE_REPLACE_FORMAT = " / ";
    private static final String DATE_SEPARATOR = "-";

    private static final int size = 270;
    private static final int RETURN_VALUE = 0;
    private static final int MAIN_BORDER = 5;

    public static void main(String[] args) {
        JButton button = new JButton(BUTTON_TEXT);
        JButton resetButton = new JButton(RESET_BUTTON_TEXT);
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new GridLayout(3, 1));
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new GridLayout(4, 1));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));

        JFrame main = new JFrame(MAIN_TITLE);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setSize(size, size);
        main.setLocationRelativeTo(null);
        main.setResizable(false);
        mainPanel.add(datePanel);
        mainPanel.add(timePanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(MAIN_BORDER, MAIN_BORDER, MAIN_BORDER, MAIN_BORDER));
        main.add(mainPanel);

        JRadioButton[] dateButtons = new JRadioButton[2];
        JTextField[] timeEdit = new JTextField[3];

        for (int i = 0; i < dateButtons.length; i++) {
            dateButtons[i] = new JRadioButton();
        }

        for (int i = 0; i < timeEdit.length; i++) {
            timeEdit[i] = new JTextField();
        }

        timeEdit[0].setText(ONE_TIME);
        timeEdit[1].setText(TWO_TIME);
        timeEdit[2].setText(THREE_TIME);

        for (JTextField t : timeEdit) {
            timePanel.add(t);
        }

        timePanel.add(button);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy / MM / dd");

        Calendar calendar = new GregorianCalendar();

        calendar.add(Calendar.DATE, 0);

        dateButtons[0].setText(dateFormat.format(calendar.getTime()));
        dateButtons[0].setSelected(true);

        calendar.add(Calendar.DATE, 1);
        dateButtons[1].setText(dateFormat.format(calendar.getTime()));

        ButtonGroup dateGroup = new ButtonGroup();

        for (JRadioButton d : dateButtons) {
            dateGroup.add(d);
            datePanel.add(d);
        }

        datePanel.add(resetButton);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    //  크로스 플랫폼 지원을 위한 설계
        } catch (Exception e) {
            e.printStackTrace();
        }

        CacheUtil cacheUtil = CacheUtil.getInstance();
        CacheUtil.CacheInfo cacheInfo = cacheUtil.createInfo();

        if (cacheUtil.isCacheNotExist()) {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle(FILE_CHOOSER_TITLE);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int ret = jfc.showOpenDialog(main);

            switch (ret) {
                case JFileChooser.APPROVE_OPTION:
                    Path selectedFile = jfc.getSelectedFile().toPath();
                    cacheInfo.setSourcePath(selectedFile.toAbsolutePath().toString());
                    if (!cacheUtil.saveCache(cacheInfo)) {
                        JOptionPane.showMessageDialog(null, SAVE_CACHE_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                        System.exit(RETURN_VALUE);
                    }
                    break;
                case JFileChooser.CANCEL_OPTION:
                case JFileChooser.ERROR_OPTION:
                    JOptionPane.showMessageDialog(null, CANCEL_MESSAGE, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    System.exit(RETURN_VALUE);
            }
        }

        SourceModifier sourceModifier = SourceModifier.getInstance(cacheUtil.loadAlarmPath());

        resetButton.addActionListener(e -> {
            if (!cacheUtil.deleteCache()) {
                JOptionPane.showMessageDialog(main, DELETE_CACHE_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                System.exit(RETURN_VALUE);
            }

            main.dispose();
            Application.main(args);
        });

        button.addActionListener(e -> {
            ArrayList<String> timeList = new ArrayList<>();

            for (JTextField t : timeEdit) {
                timeList.add(t.getText() + SECONDS_TEXT);
            }

            for (JRadioButton b : dateButtons) {
                if (b.isSelected()) {
                    sourceModifier.setRequired(b.getText().replace(DATE_REPLACE_FORMAT, DATE_SEPARATOR), timeList);
                    break;
                }
            }

            if (!sourceModifier.modifySource()) {
                JOptionPane.showMessageDialog(main, MODIFIED_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                System.exit(RETURN_VALUE);
            }

            if (!AlarmBuilder.deleteAlarm(cacheUtil.loadAlarmPath())) {
                JOptionPane.showMessageDialog(main, DELETE_CLASS_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                System.exit(RETURN_VALUE);
            }

            if (!AlarmBuilder.buildAlarm(cacheUtil.loadAlarmPath())) {
                JOptionPane.showMessageDialog(main, BUILD_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                System.exit(RETURN_VALUE);
            }

            if (!CommandUtil.startCommandExe(cacheUtil.loadAlarmPath())) {
                JOptionPane.showMessageDialog(main, CMD_EXECUTE_FAILED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                System.exit(RETURN_VALUE);
            }
        });
        main.setVisible(true);
    }
}
