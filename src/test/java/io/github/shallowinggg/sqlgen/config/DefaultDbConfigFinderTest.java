package io.github.shallowinggg.sqlgen.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ding shimin
 */
public class DefaultDbConfigFinderTest {

    @Test
    public void testLoad() {
        DbConfigFinder dbConfigFinder = new DefaultDbConfigFinder();
        DbConfig dbConfig = dbConfigFinder.find();
        Assert.assertNotNull(dbConfig);
        Assert.assertEquals("jdbc:mysql://localhost:3306/test", dbConfig.getUrl());
        Assert.assertEquals("com.mysql.jdbc.Driver", dbConfig.getDriverName());
        Assert.assertEquals("root", dbConfig.getUsername());
        Assert.assertEquals("root", dbConfig.getPassword());
    }

}
