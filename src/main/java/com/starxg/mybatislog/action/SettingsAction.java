package com.starxg.mybatislog.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.starxg.mybatislog.gui.MyBatisLogManager;
import com.starxg.mybatislog.gui.SettingsDialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * SettingsAction
 */
public class SettingsAction extends AnAction {
    private final MyBatisLogManager manager;

    public SettingsAction(MyBatisLogManager manager) {
        super("Settings", "Settings", AllIcons.General.GearPlain);
        this.manager = manager;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        new SettingsDialogWrapper(e.getProject(), manager).showAndGet();
    }

}
