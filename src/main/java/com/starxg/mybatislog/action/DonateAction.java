package com.starxg.mybatislog.action;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.starxg.mybatislog.Icons;
import com.starxg.mybatislog.gui.DonateDialogWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * DonateAction
 * @author huangxingguang
 */
public class DonateAction extends AnAction {


    private boolean isVisible;

    public DonateAction(PropertiesComponent propertiesComponent) {
        super("Donate", "Donate", Icons.DONATE);
        isVisible = !propertiesComponent.getBoolean(getDonateKey(), false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }

        new DonateDialogWrapper(e.getProject()).showAndGet();

        PropertiesComponent.getInstance(e.getProject()).setValue(getDonateKey(), true);

        e.getPresentation().setVisible(isVisible = false);

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (Objects.isNull(e.getProject())) {
            return;
        }
        e.getPresentation().setVisible(isVisible);
    }

    private String getDonateKey() {
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.starxg.mybatis-log-plugin-free"));
        if (Objects.isNull(plugin)) {
            return DonateAction.class.getName();
        }
        return DonateAction.class.getName() + "@" + plugin.getVersion();
    }
}