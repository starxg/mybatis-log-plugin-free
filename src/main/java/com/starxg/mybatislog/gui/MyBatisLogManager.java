package com.starxg.mybatislog.gui;

import static com.starxg.mybatislog.MyBatisLogConsoleFilter.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBusConnection;
import com.starxg.mybatislog.BasicFormatter;
import com.starxg.mybatislog.Icons;
import com.starxg.mybatislog.action.MyBatisLogAction;

/**
 * MyBatisLogManager
 * 
 * @author huangxingguang
 */
public class MyBatisLogManager implements Disposable {

    private static final Key<MyBatisLogManager> KEY = Key.create(MyBatisLogManager.class.getName());

    private final ConsoleView consoleView;

    private static final BasicFormatter FORMATTER = new BasicFormatter();

    private final Project project;

    private final RunContentDescriptor descriptor;

    private final AtomicInteger counter;

    private volatile String preparing;
    private volatile String parameters;
    private volatile boolean running = false;
    private final List<String> keywords = new ArrayList<>(0);

    private MyBatisLogManager(@NotNull Project project) {
        this.project = project;

        this.consoleView = createConsoleView();

        final JPanel panel = createConsolePanel(this.consoleView);

        RunnerLayoutUi layoutUi = getRunnerLayoutUi();

        Content content = layoutUi.createContent(UUID.randomUUID().toString(), panel, "SQL", Icons.MY_BATIS, panel);

        content.setCloseable(false);

        layoutUi.addContent(content);

        layoutUi.getOptions().setLeftToolbar(createActionToolbar(), "RunnerToolbar");

        final MessageBusConnection messageBusConnection = project.getMessageBus().connect();

        this.counter = new AtomicInteger();
        this.descriptor = getRunContentDescriptor(layoutUi);

        Disposer.register(this, consoleView);
        Disposer.register(this, content);
        Disposer.register(this, layoutUi.getContentManager());
        Disposer.register(this, messageBusConnection);
        Disposer.register(project, this);

        final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        this.preparing = propertiesComponent.getValue(PREPARING_KEY, "Preparing: ");
        this.parameters = propertiesComponent.getValue(PARAMETERS_KEY, "Parameters: ");
        resetKeywords(propertiesComponent.getValue(KEYWORDS, StringUtils.EMPTY));

        messageBusConnection.subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void toolWindowRegistered(@NotNull String id) {

            }

            @Override
            public void stateChanged() {
                if (!getToolWindow().isAvailable()) {
                    Disposer.dispose(MyBatisLogManager.this);
                }
            }
        });

        ExecutionManager.getInstance(project).getContentManager().showRunContent(MyBatisLogExecutor.getInstance(),
                descriptor);

        getToolWindow().activate(null);
    }

    private ConsoleView createConsoleView() {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        return consoleBuilder.getConsole();
    }

    private ActionGroup createActionToolbar() {
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new FilterAction(this));
        actionGroup.add(new RerunAction());
        actionGroup.add(new StopAction(this));

        final ConsoleViewImpl consoleView = (ConsoleViewImpl) this.consoleView;

        actionGroup.add(new ToggleUseSoftWrapsToolbarAction(SoftWrapAppliancePlaces.CONSOLE) {
            @Nullable
            @Override
            protected Editor getEditor(@NotNull AnActionEvent e) {
                return consoleView.getEditor();
            }
        });

        actionGroup.add(new ScrollToTheEndToolbarAction(consoleView.getEditor()));

        actionGroup.add(new DumbAwareAction("Clear All", "Clear All", AllIcons.Actions.GC) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                MyBatisLogManager.this.consoleView.clear();
            }
        });

        if (!PropertiesComponent.getInstance(project).getBoolean(DonateAction.class.getName(), false)) {
            actionGroup.add(new DonateAction());
        }

        return actionGroup;
    }

    private JPanel createConsolePanel(ConsoleView consoleView) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(consoleView.getComponent(), BorderLayout.CENTER);
        return panel;
    }

    private RunContentDescriptor getRunContentDescriptor(RunnerLayoutUi layoutUi) {
        RunContentDescriptor descriptor = new RunContentDescriptor(new RunProfile() {
            @Nullable
            @Override
            public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return "SQL";
            }

            @Override
            @Nullable
            public Icon getIcon() {
                return null;
            }
        }, new DefaultExecutionResult(), layoutUi);
        descriptor.setExecutionId(System.nanoTime());

        return descriptor;
    }

    private RunnerLayoutUi getRunnerLayoutUi() {

        return RunnerLayoutUi.Factory.getInstance(project).create("MyBatis Log", "MyBatis Log", "MyBatis Log", project);
    }

    public void println(String logPrefix, String sql) {
        consoleView.print(String.format("-- %s -- %s\n", counter.incrementAndGet(), logPrefix),
                ConsoleViewContentType.USER_INPUT);

        consoleView.print(String.format("%s\n", FORMATTER.format(sql)), ConsoleViewContentType.ERROR_OUTPUT);
    }

    public void run() {

        if (running) {
            return;
        }

        running = true;

    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;

    }

    @Nullable
    public static MyBatisLogManager getInstance(@NotNull Project project) {

        MyBatisLogManager manager = project.getUserData(KEY);

        if (Objects.nonNull(manager)) {
            if (!manager.getToolWindow().isAvailable()) {
                Disposer.dispose(manager);
                manager = null;
            }
        }

        return manager;

    }

    @NotNull
    public static MyBatisLogManager createInstance(@NotNull Project project) {

        MyBatisLogManager manager = getInstance(project);

        if (Objects.nonNull(manager) && !Disposer.isDisposed(manager)) {
            Disposer.dispose(manager);
        }

        manager = new MyBatisLogManager(project);
        project.putUserData(KEY, manager);

        return manager;

    }

    public ToolWindow getToolWindow() {
        return ToolWindowManager.getInstance(project).getToolWindow(MyBatisLogExecutor.TOOL_WINDOW_ID);
    }

    private void resetKeywords(String text) {

        keywords.clear();

        if (StringUtils.isBlank(text)) {
            return;
        }

        final String[] split = text.split("\n");

        final List<String> keywords = new ArrayList<>(split.length);

        for (String keyword : split) {
            if (StringUtils.isBlank(keyword)) {
                continue;
            }

            keywords.add(keyword);

        }

        this.keywords.addAll(keywords);
    }

    public String getPreparing() {
        return preparing;
    }

    public boolean isRunning() {
        return running;
    }

    public String getParameters() {
        return parameters;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    @Override
    public void dispose() {

        project.putUserData(KEY, null);

        stop();

        ExecutionManager.getInstance(project).getContentManager().removeRunContent(MyBatisLogExecutor.getInstance(),
                descriptor);

    }

    private static class FilterAction extends AnAction {
        private final MyBatisLogManager manager;

        FilterAction(MyBatisLogManager manager) {
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
            component.setValue(KEYWORDS, dialog.getKeywords());

            manager.preparing = preparing;
            manager.parameters = parameters;
            manager.resetKeywords(dialog.getKeywords());
        }

    }

    private static class DonateAction extends AnAction {
        DonateAction() {
            super("Donate", "Donate", Icons.DONATE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (Objects.isNull(e.getProject())) {
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> e.getPresentation().setVisible(false));

            new DonateDialogWrapper(e.getProject()).showAndGet();

            final PropertiesComponent component = PropertiesComponent.getInstance(e.getProject());
            component.setValue(DonateAction.class.getName(), true);

        }

    }

    private static class RerunAction extends AnAction {

        RerunAction() {
            super("Rerun", "Rerun", AllIcons.Actions.Restart);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (Objects.nonNull(e.getProject())) {
                new MyBatisLogAction().rerun(e.getProject());
            }
        }

    }

    private static class StopAction extends AnAction {

        private final MyBatisLogManager manager;

        StopAction(MyBatisLogManager manager) {
            super("Stop", "Stop", AllIcons.Actions.Suspend);
            this.manager = manager;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            manager.stop();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(manager.isRunning());
        }

    }

}