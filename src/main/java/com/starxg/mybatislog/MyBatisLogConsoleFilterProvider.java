package com.starxg.mybatislog;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

/**
 * MyBatisLogConsoleFilterProvider
 * 
 * @author huangxingguang
 */
public class MyBatisLogConsoleFilterProvider implements ConsoleFilterProvider {
    private final Key<MyBatisLogConsoleFilter> key = Key.create(MyBatisLogConsoleFilter.class.getName());

    public MyBatisLogConsoleFilterProvider() {

    }

    @Override
    public Filter @NotNull [] getDefaultFilters(@NotNull Project project) {
        MyBatisLogConsoleFilter filter = project.getUserData(key);
        if (Objects.isNull(filter)) {
            filter = new MyBatisLogConsoleFilter(project);
            project.putUserData(key, filter);
        }
        return new Filter[] { filter };
    }
}
