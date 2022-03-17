package com.starxg.mybatislog.gui;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColorChooserService;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextArea;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Objects;

import static com.starxg.mybatislog.MyBatisLogConsoleFilter.*;

/**
 * SettingsDialogWrapper
 */
public class SettingsDialogWrapper extends DialogWrapper {

    private final Project project;
    private final MyBatisLogManager manager;

    private JPanel rootPanel;
    private JPanel filterTab;
    private JPanel colorTab;
    private JTextField txtPreparing;
    private JTextField txtParameters;
    private JBTextArea txtKeywords;
    private JPanel insertColor;
    private JPanel deleteColor;
    private JPanel updateColor;
    private JPanel selectColor;


    public SettingsDialogWrapper(Project project, MyBatisLogManager manager) {
        super(Objects.requireNonNull(project, "project"), false);

        this.project = project;
        this.manager = manager;

        setTitle("Settings");

        init();

        initColorEvent();

        getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (isOK()) {
                    saveProperties();
                }
            }
        });

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        rootPanel.setPreferredSize(new Dimension(600, 400));

        final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);

        final int insertColorRGB = propertiesComponent.getInt(INSERT_SQL_COLOR_KEY, ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        final int deleteColorRGB = propertiesComponent.getInt(DELETE_SQL_COLOR_KEY, ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        final int updateColorRGB = propertiesComponent.getInt(UPDATE_SQL_COLOR_KEY, ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        final int selectColorRGB = propertiesComponent.getInt(SELECT_SQL_COLOR_KEY, ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());

        insertColor.setBackground(new JBColor(insertColorRGB, insertColorRGB));
        deleteColor.setBackground(new JBColor(deleteColorRGB, deleteColorRGB));
        updateColor.setBackground(new JBColor(updateColorRGB, updateColorRGB));
        selectColor.setBackground(new JBColor(selectColorRGB, selectColorRGB));

        insertColor.setMinimumSize(new Dimension(20, 20));
        deleteColor.setMinimumSize(new Dimension(20, 20));
        updateColor.setMinimumSize(new Dimension(20, 20));
        selectColor.setMinimumSize(new Dimension(20, 20));

        filterTab.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        colorTab.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        txtPreparing.setText(propertiesComponent.getValue(PREPARING_KEY, "Preparing: "));
        txtParameters.setText(propertiesComponent.getValue(PARAMETERS_KEY, "Parameters: "));
        txtKeywords.setText(StringUtils.defaultString(propertiesComponent.getValue(KEYWORDS_KEY)));
        txtKeywords.setBorder(new LineBorder(JBColor.border(), 1));

        return rootPanel;
    }

    private void initColorEvent() {

        final MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!(e.getSource() instanceof JPanel)) {
                    return;
                }

                final String title;
                if (e.getSource() == insertColor) {
                    title = "Insert";
                } else if (e.getSource() == deleteColor) {
                    title = "Delete";
                } else if (e.getSource() == updateColor) {
                    title = "Update";
                } else if (e.getSource() == selectColor) {
                    title = "Select";
                } else {
                    title = StringUtils.EMPTY;
                }

                final Color color = ColorChooserService.getInstance().showDialog(project, getContentPane(), title + " SQL Color", ((JPanel) e.getSource()).getBackground(), true, Collections.emptyList(), true);
                if (Objects.nonNull(color)) {
                    ((JPanel) e.getSource()).setBackground(color);
                }

            }
        };

        insertColor.addMouseListener(listener);
        deleteColor.addMouseListener(listener);
        updateColor.addMouseListener(listener);
        selectColor.addMouseListener(listener);
    }

    private void saveProperties() {
        final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        final String preparing = getPreparing();
        final String parameters = getParameters();
        final String keywords = getKeywords();

        propertiesComponent.setValue(PREPARING_KEY, getPreparing());
        propertiesComponent.setValue(PARAMETERS_KEY, getParameters());
        propertiesComponent.setValue(KEYWORDS_KEY, getKeywords());

        propertiesComponent.setValue(INSERT_SQL_COLOR_KEY, getInsertColorRGB(), ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        propertiesComponent.setValue(DELETE_SQL_COLOR_KEY, getDeleteColorRGB(), ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        propertiesComponent.setValue(UPDATE_SQL_COLOR_KEY, getUpdateColorRGB(), ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());
        propertiesComponent.setValue(SELECT_SQL_COLOR_KEY, getSelectColorRGB(), ConsoleViewContentType.ERROR_OUTPUT.getAttributes().getForegroundColor().getRGB());


        manager.setPreparing(preparing);
        manager.setParameters(parameters);
        manager.resetKeywords(keywords);
    }

    public int getInsertColorRGB() {
        return insertColor.getBackground().getRGB();
    }

    public int getDeleteColorRGB() {
        return deleteColor.getBackground().getRGB();
    }

    public int getUpdateColorRGB() {
        return updateColor.getBackground().getRGB();
    }

    public int getSelectColorRGB() {
        return selectColor.getBackground().getRGB();
    }

    public String getPreparing() {
        return StringUtils.defaultIfBlank(txtPreparing.getText(), "Preparing: ");
    }

    public String getParameters() {
        return StringUtils.defaultIfBlank(txtParameters.getText(), "Parameters: ");
    }

    public String getKeywords() {
        return txtKeywords.getText();
    }
}
