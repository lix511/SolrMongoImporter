package org.apache.solr.handler.dataimport;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class MongoDataSourceTests {

    @Test
    public void testOne() {
        Date d = new Date();
        String aa = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(d);
        Assert.assertNotNull(aa);
    }

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
//        Iterator<Map<String, Object>> iterator = ds.getData("{'collectTime':{'$lt':{'$date':'2019-05-24T19:04:26.650Z'}}}", "book_info");
        Iterator<Map<String, Object>> iterator = ds.getData("{'collectTime':{'$lt':{'$date':'2019-05-24T19:04:26Z'}}}", "book_info");
        while (iterator.hasNext()) {
            Map<String, Object> row = iterator.next();
            System.out.println(row.get("_id"));
        }


    }
}
