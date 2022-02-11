package com.starxg.mybatislog.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * RerunAction
 * @author huangxingguang
 */
public class RerunAction extends AnAction {

    public RerunAction() {
        super("Rerun", "Rerun", AllIcons.Actions.Restart);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.nonNull(e.getProject())) {
            new MyBatisLogAction().rerun(e.getProject());
        }
    }

}
