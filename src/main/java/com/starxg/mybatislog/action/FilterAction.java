package com.starxg.mybatislog.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.starxg.mybatislog.gui.FilterDialogWrapper;
import com.starxg.mybatislog.gui.MyBatisLogManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.starxg.mybatislog.MyBatisLogConsoleFilter.*;

/**
 * FilterAction
 * @author huangxingguang
 */
public class FilterAction extends AnAction {
    private final MyBatisLogManager manager;

    public FilterAction(MyBatisLogManager manager) {
        super("Filter", "Filter", AllIcons.General.Filter);
        this.manager = manager;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        if (Objects.isNull(e.getProject())) {
            return;
        }

        final FilterDialogWrapper dialog = new FilterDialogWrapper(e.getProject(), manager);
        if (!dialog.showAndGet()) {
            return;
        }

        final PropertiesComponent component = PropertiesComponent.getInstance(e.getProject());
        final String preparing = dialog.getPreparing();
        final String parameters = dialog.getParameters();

        component.setValue(PREPARING_KEY, preparing);
        component.setValue(PARAMETERS_KEY, parameters);
        component.setValue(KEYWORDS_KEY, dialog.getKeywords());

        manager.setPreparing(preparing);
        manager.setParameters(parameters);
        manager.resetKeywords(dialog.getKeywords());
    }

}
