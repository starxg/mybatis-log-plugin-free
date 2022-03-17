package com.starxg.mybatislog;

import org.junit.Assert;
import org.junit.Test;

public class MyBatisLogConsoleFilterTest {
    @Test
    public void test() {
        Assert.assertEquals(MyBatisLogConsoleFilter.parseParams("123(String)").element().getKey(), "123");
        Assert.assertEquals(MyBatisLogConsoleFilter.parseParams("123(String)").element().getValue(), "String");

        Assert.assertEquals(MyBatisLogConsoleFilter.parseParams("null, 1(Long), 张三(String), 18(Integer)").size(), 4);
        Assert.assertEquals(MyBatisLogConsoleFilter.parseParams("1(Long), 张三(String), null, 18(Integer)").size(), 4);

        Assert.assertEquals(MyBatisLogConsoleFilter.parseSql("UPDATE mp_user SET name=? WHERE id=? AND name=? AND age=? AND email=? AND deleted=0",
                        MyBatisLogConsoleFilter.parseParams("null, 1(Long), 张三(String), 18(Integer), x@y.com(String)")).toString(),
                "UPDATE mp_user SET name=null WHERE id=1 AND name='张三' AND age=18 AND email='x@y.com' AND deleted=0");

        Assert.assertEquals(MyBatisLogConsoleFilter.parseSql("UPDATE mp_user SET name=? WHERE id=? AND name=?",
                        MyBatisLogConsoleFilter.parseParams("null, null, null")).toString(),
                "UPDATE mp_user SET name=null WHERE id=null AND name=null");
    }
}