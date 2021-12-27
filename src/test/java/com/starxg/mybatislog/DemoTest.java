package com.starxg.mybatislog;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * DemoTest
 * 
 * @author huangxingguang
 */
public class DemoTest {
    private static final String PARAM_TYPE_REGEX = "\\(String\\),|\\(Timestamp\\),{0,1}|\\(Date\\),{0,1}|\\(Time\\),{0,1}|\\(LocalDate\\),{0,1}|\\(LocalTime\\),{0,1}|\\(LocalDateTime\\),{0,1}|\\(Byte\\),{0,1}|\\(Short\\),{0,1}|\\(Integer\\),{0,1}|\\(Long\\),{0,1}|\\(Float\\),{0,1}|\\(Double\\),{0,1}|\\(BigDecimal\\),{0,1}|\\(Boolean\\),{0,1}|\\(Null\\),{0,1}";

    @Test
    public void test() {
        String str = "hello(String), world(String), 123(Integer), 0(Long)";
        final String[] strings = StringUtils.splitByWholeSeparator(str, "), ");
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];

            String type = StringUtils.substringAfterLast(s, "(");
            String value = StringUtils.substringBeforeLast(s, "(");
            if (i + 1 == strings.length) {
                type = StringUtils.removeEnd(type, ")");
            }

            System.out.println(s + "\t" + value + "\t" + type);

        }
        System.out.println(strings.length + " : " + Arrays.toString(strings));
    }
}
