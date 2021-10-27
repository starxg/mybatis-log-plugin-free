package com.starxg.mybatislog.action;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.starxg.mybatislog.gui.MyBatisLogManager;

/**
 * MyBatisLogAction
 * 
 * @author huangxingguang
 */
public class MyBatisLogAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }

        if (!project.isOpen() || !project.isInitialized()) {
            return;
        }

        if ("EditorPopup".equals(e.getPlace())) {
            final MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
            if (Objects.nonNull(manager) && manager.getToolWindow().isAvailable()) {
                if (!manager.isRunning()) {
                    manager.run();
                }
                manager.getToolWindow().activate(null);
                return;
            }
        }

        rerun(project);
    }

    public void rerun(final Project project) {
        final MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
        if (Objects.nonNull(manager)) {
            Disposer.dispose(manager);
        }
        MyBatisLogManager.createInstance(project).run();
    }
}
