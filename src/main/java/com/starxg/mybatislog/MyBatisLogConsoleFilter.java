package com.starxg.mybatislog;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.starxg.mybatislog.gui.MyBatisLogManager;

/**
 * MyBatisLogConsoleFilter
 *
 * @author huangxingguang
 */
public class MyBatisLogConsoleFilter implements Filter {

    public static final String PREPARING_KEY = MyBatisLogConsoleFilter.class.getName() + ".Preparing";
    public static final String PARAMETERS_KEY = MyBatisLogConsoleFilter.class.getName() + ".Parameters";
    public static final String KEYWORDS = MyBatisLogConsoleFilter.class.getName() + ".Keywords";

    private static final char MARK = '?';

    private static final Set<String> NEED_BRACKETS;

    private final Project project;

    private String sql = null;

    static {
        Set<String> types = new HashSet<>(8);
        types.add("String");
        types.add("Date");
        types.add("Time");
        types.add("LocalDate");
        types.add("LocalTime");
        types.add("LocalDateTime");
        types.add("BigDecimal");
        types.add("Timestamp");
        NEED_BRACKETS = Collections.unmodifiableSet(types);
    }

    MyBatisLogConsoleFilter(Project project) {
        this.project = project;
    }

    @Override
    public @Nullable Result applyFilter(@NotNull String line, int entireLength) {

        final MyBatisLogManager manager = MyBatisLogManager.getInstance(project);
        if (Objects.isNull(manager)) {
            return null;
        }

        if (!manager.isRunning()) {
            return null;
        }

        final String preparing = manager.getPreparing();
        final String parameters = manager.getParameters();
        final List<String> keywords = manager.getKeywords();

        if (CollectionUtils.isNotEmpty(keywords)) {
            for (String keyword : keywords) {
                if (line.contains(keyword)) {
                    sql = null;
                    return null;
                }
            }
        }

        if (line.contains(preparing)) {
            sql = line;
            return null;
        }

        if (StringUtils.isNotBlank(sql) && !line.contains(parameters)) {
            return null;
        }

        if (StringUtils.isBlank(sql)) {
            return null;
        }

        final String logPrefix = StringUtils.substringBefore(sql, preparing);
        final StringBuilder sb = new StringBuilder(StringUtils.substringAfter(sql, preparing));
        final Queue<Map.Entry<String, String>> params = parseParams(StringUtils.substringAfter(line, parameters));

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) != MARK) {
                continue;
            }

            final Map.Entry<String, String> entry = params.poll();
            if (Objects.isNull(entry)) {
                continue;
            }

            boolean needBrackets = false;
            if (NEED_BRACKETS.contains(entry.getValue())) {
                int j = i - 1;
                needBrackets = j >= 0 && j < sb.length() && sb.charAt(j) != '\"' && sb.charAt(j) != '\'';
                j = i + 1;
                needBrackets = needBrackets && j < sb.length() && sb.charAt(j) != '\"' && sb.charAt(j) != '\'';
            }

            sb.deleteCharAt(i);

            if (needBrackets) {
                sb.insert(i, String.format("'%s'", entry.getKey()));
            } else {
                sb.insert(i, entry.getKey());
            }
        }

        manager.println(logPrefix, sb.toString());

        return null;
    }

    private static Queue<Map.Entry<String, String>> parseParams(String line) {
        line = StringUtils.removeEnd(line, "\n");

        final String[] strings = StringUtils.splitByWholeSeparator(line, "), ");
        final Queue<Map.Entry<String, String>> queue = new ArrayDeque<>(strings.length);

        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];

            String value = StringUtils.substringBeforeLast(s, "(");
            String type = StringUtils.substringAfterLast(s, "(");
            if (i + 1 == strings.length) {
                type = StringUtils.removeEnd(type, ")");
            }

            queue.offer(new AbstractMap.SimpleEntry<>(value, type));

        }

        return queue;
    }

}
