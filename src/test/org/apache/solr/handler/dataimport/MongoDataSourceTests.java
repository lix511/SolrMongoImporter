package org.apache.solr.handler.dataimport;

import org.junit.Test;

import java.util.Properties;

public class MongoDataSourceTests {
    @Test
    public void testInit() {
        Properties properties = new Properties();
        properties.setProperty("name", "mongoDataSource");
        properties.setProperty("type", "MongoDataSource");
        properties.setProperty("database", "scrapy_books");
        properties.setProperty("host", "xiangzhi.iask.in");
        properties.setProperty("port", "27017");
        properties.setProperty("username", "readonly");
        properties.setProperty("password", "xiangzhi.123");

        MongoDataSource ds = new MongoDataSource();
        ds.init(null, properties);
    }
}
