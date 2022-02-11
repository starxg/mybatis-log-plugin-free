package com.starxg.mybatislog.gui;

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
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBusConnection;
import com.starxg.mybatislog.BasicFormatter;
import com.starxg.mybatislog.Icons;
import com.starxg.mybatislog.action.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.starxg.mybatislog.MyBatisLogConsoleFilter.*;

/**
 * MyBatisLogManager
 * 
 * @author huangxingguang
 */
public class MyBatisLogManager implements Disposable {

    private static final Key<MyBatisLogManager> KEY = Key.create(MyBatisLogManager.class.getName());
    private static final BasicFormatter FORMATTER = new BasicFormatter();


    private final ConsoleView consoleView;
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
        resetKeywords(propertiesComponent.getValue(KEYWORDS_KEY, StringUtils.EMPTY));

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
        final ConsoleViewImpl console = (ConsoleViewImpl) consoleBuilder.getConsole();
        // init editor
        console.getComponent();

        final Editor editor = console.getEditor();
        editor.getDocument().addDocumentListener(new RangeHighlighterDocumentListener(editor));

        return console;
    }

    private ActionGroup createActionToolbar() {

        final ConsoleViewImpl consoleView = (ConsoleViewImpl) this.consoleView;

        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RerunAction());
        actionGroup.add(new StopAction(this));
        actionGroup.add(new FilterAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new PreviousSqlAction(consoleView));
        actionGroup.add(new NextSqlAction(consoleView));
        actionGroup.addSeparator();

        actionGroup.add(new ToggleUseSoftWrapsToolbarAction(SoftWrapAppliancePlaces.CONSOLE) {
            @Nullable
            @Override
            protected Editor getEditor(@NotNull AnActionEvent e) {
                return consoleView.getEditor();
            }
        });

        actionGroup.add(new ScrollToTheEndToolbarAction(consoleView.getEditor()));
        actionGroup.add(new PrettyPrintToggleAction());
        actionGroup.addSeparator();
        actionGroup.add(new ClearAllAction(consoleView));
        actionGroup.addSeparator();
        actionGroup.add(new DonateAction(PropertiesComponent.getInstance(project)));

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

        consoleView.print(String.format("%s\n", isFormat() ? FORMATTER.format(sql) : StringUtils.removeEnd(sql, "\n")),
                ConsoleViewContentType.ERROR_OUTPUT);

    }

    private boolean isFormat() {
        return PropertiesComponent.getInstance(project).getBoolean(PrettyPrintToggleAction.class.getName());
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

    public void resetKeywords(String text) {

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

    public void setPreparing(String preparing) {
        this.preparing = preparing;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
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

    @Override
    public void dispose() {

        project.putUserData(KEY, null);

        stop();

        ExecutionManager.getInstance(project).getContentManager().removeRunContent(MyBatisLogExecutor.getInstance(),
                descriptor);

    }


    private static final class RangeHighlighterDocumentListener implements DocumentListener {

        private final Editor editor;

        private RangeHighlighterDocumentListener(Editor editor) {
            this.editor = editor;
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            final Document document = event.getDocument();
            final int textLength = document.getTextLength();
            if (textLength < 1) {
                return;
            }

            for (int i = event.getOffset(); i < textLength; ) {
                final int endOffset = document.getLineEndOffset(document.getLineNumber(i));
                final String text = document.getText(TextRange.create(i, endOffset));
                if (text.matches("^-- [\\d]+ -- .*")) {
                    editor.getMarkupModel().addRangeHighlighter(i, i + 1, JumpSqlAction.SQL_LAYER, TextAttributes.ERASE_MARKER, HighlighterTargetArea.EXACT_RANGE);
                }
                i = endOffset + 1;
            }
        }
    }


}