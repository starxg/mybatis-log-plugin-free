package com.starxg.mybatislog.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.MarkupIterator;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * JumpSqlAction
 * @author huangxingguang
 */
public abstract class JumpSqlAction extends AnAction {

    public static final int SQL_LAYER = 506;

    protected final ConsoleViewImpl consoleView;
    protected final Editor editor;

    public JumpSqlAction(@Nullable String text, @Nullable String description, @Nullable Icon icon, ConsoleViewImpl consoleView) {
        super(text, description, icon);
        this.consoleView = consoleView;
        this.editor = consoleView.getEditor();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!e.getInputEvent().isShiftDown()) {
            editor.getSelectionModel().removeSelection();
        }
    }

    protected int jump(int startOffset, int endOffset, boolean canBreak) {
        final MarkupModelEx model = (MarkupModelEx) editor.getMarkupModel();
        final MarkupIterator<RangeHighlighterEx> iterator = model.overlappingIterator(startOffset, endOffset);

        int movedOffset = -1;

        try {
            while (iterator.hasNext()) {
                final RangeHighlighterEx next = iterator.next();
                if (isValid(next, startOffset, endOffset)) {
                    editor.getCaretModel().getPrimaryCaret().moveToOffset(movedOffset = next.getStartOffset());
                    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                    IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(editor.getContentComponent(), true));
                    if (canBreak) {
                        break;
                    }
                }
            }
        } finally {
            iterator.dispose();
        }

        return movedOffset;
    }

    protected boolean isValid(RangeHighlighterEx next, int startOffset, int endOffset) {
        return next.isValid() && next.getLayer() == SQL_LAYER;
    }
}
