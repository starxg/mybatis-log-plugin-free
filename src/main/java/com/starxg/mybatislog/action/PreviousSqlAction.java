package com.starxg.mybatislog.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.ex.MarkupIterator;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import org.jetbrains.annotations.NotNull;

/**
 * PreviousSqlAction
 * @author huangxingguang
 */
public class PreviousSqlAction extends JumpSqlAction {

    public PreviousSqlAction(ConsoleViewImpl consoleView) {
        super("Previous SQL", "Previous SQL", AllIcons.Actions.PreviousOccurence, consoleView);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final int offset = editor.getCaretModel().getPrimaryCaret().getOffset();
        if (offset <= 1) {
            return;
        }
        jump(0, offset - 1, false);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(hasPrev());
    }

    @Override
    protected boolean isValid(RangeHighlighterEx next, int startOffset, int endOffset) {
        return super.isValid(next, startOffset, endOffset) && editor.getDocument().getLineNumber(endOffset) != editor.getDocument().getLineNumber(next.getStartOffset());
    }

    private boolean hasPrev() {

        final int offset = editor.getCaretModel().getPrimaryCaret().getOffset();
        if (offset <= 1) {
            return false;
        }

        final MarkupModelEx model = (MarkupModelEx) editor.getMarkupModel();
        final MarkupIterator<RangeHighlighterEx> iterator = model.overlappingIterator(0, offset - 1);
        try {
            return iterator.hasNext() && isValid(iterator.next(), 0, offset - 1);
        } finally {
            iterator.dispose();
        }

    }
}
