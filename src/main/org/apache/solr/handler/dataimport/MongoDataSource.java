package org.apache.solr.handler.dataimport;


import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;

/**
 * User: James
 * Date: 13/08/12
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */


public class MongoDataSource extends DataSource<Iterator<Map<String, Object>>> {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateTransformer.class);

    private DBCollection mongoCollection;
    private DB mongoDb;
    private MongoClient mongoClient;

    private DBCursor mongoCursor;

    @Override
    public void init(Context context, Properties initProps) {
        String databaseName = initProps.getProperty(DATABASE);
        String host = initProps.getProperty(HOST, "localhost");
        String port = initProps.getProperty(PORT, "27017");
        String username = initProps.getProperty(USERNAME);
        String password = initProps.getProperty(PASSWORD);

        if (databaseName == null) {
            throw new DataImportHandlerException(SEVERE
                    , "Database must be supplied");
        }

        try {
            MongoClientOptions options = MongoClientOptions.builder()
                    .readPreference(ReadPreference.secondaryPreferred())
                    .build();

            ServerAddress serverAddress = new ServerAddress(host, Integer.parseInt(port));

            if (username == null) {
                this.mongoClient = new MongoClient(serverAddress, options);
            } else {
                MongoCredential credential = MongoCredential.createScramSha256Credential(username, databaseName, password.toCharArray());
                this.mongoClient = new MongoClient(serverAddress, credential, options);
            }

            this.mongoDb = mongoClient.getDB(databaseName);

        } catch (Exception e) {
            throw new DataImportHandlerException(SEVERE
                    , "Unable to connect to Mongo");
        }
    }

    @Override
    public Iterator<Map<String, Object>> getData(String query) {
        LOG.info("mongo query: " + query);

        BasicDBObject projection = null;
        if (query.startsWith("[")) {
            int endIndex = query.indexOf("]");
            String projectionQuery = query.substring(1, endIndex);
            LOG.info("mongo projection: " + projectionQuery);
            projection = BasicDBObject.parse(projectionQuery);

            query = query.substring(endIndex + 1);
        }

        DBObject queryObject;
        /* If querying by _id, since the id is a string now,
         * it has to be converted back to type ObjectId() using the
         * constructor
         */
        if (query.contains("_id")) {
            @SuppressWarnings("unchecked")
            Map<String, String> queryWithId = (Map<String, String>) JSON.parse(query);
            String id = queryWithId.get("_id");
            queryObject = new BasicDBObject("_id", new ObjectId(id));
        } else {
            queryObject = BasicDBObject.parse(query);
        }
        LOG.info("Executing MongoQuery: " + query);

        long start = System.currentTimeMillis();
        mongoCursor = this.mongoCollection.find(queryObject, projection);
        LOG.trace("Time taken for mongo :"
                + (System.currentTimeMillis() - start));

        ResultSetIterator resultSet = new ResultSetIterator(mongoCursor);
        return resultSet.getIterator();
    }

    public Iterator<Map<String, Object>> getData(String query, String collection) {
        this.mongoCollection = this.mongoDb.getCollection(collection);
        return getData(query);
    }

    private class ResultSetIterator {
        DBCursor MongoCursor;

        Iterator<Map<String, Object>> rSetIterator;

        public ResultSetIterator(DBCursor MongoCursor) {
            this.MongoCursor = MongoCursor;


            rSetIterator = new Iterator<Map<String, Object>>() {
                public boolean hasNext() {
                    return hasnext();
                }

                public Map<String, Object> next() {
                    return getARow();
                }

                public void remove() {/* do nothing */
                }
            };


        }

        public Iterator<Map<String, Object>> getIterator() {
            return rSetIterator;
        }

        private Map<String, Object> getARow() {
            DBObject mongoObject = getMongoCursor().next();

            Map<String, Object> result = new HashMap<String, Object>();
            Set<String> keys = mongoObject.keySet();
            Iterator<String> iterator = keys.iterator();


            while (iterator.hasNext()) {
                String key = iterator.next();
                Object innerObject = mongoObject.get(key);

                result.put(key, innerObject);
            }

            return result;
        }

        private boolean hasnext() {
            if (MongoCursor == null)
                return false;
            try {
                if (MongoCursor.hasNext()) {
                    return true;
                } else {
                    close();
                    return false;
                }
            } catch (MongoException e) {
                close();
                wrapAndThrow(SEVERE, e);
                return false;
            }
        }

        private void close() {
            try {
                if (MongoCursor != null)
                    MongoCursor.close();
            } catch (Exception e) {
                LOG.warn("Exception while closing result set", e);
            } finally {
                MongoCursor = null;
            }
        }
    }

    private DBCursor getMongoCursor() {
        return this.mongoCursor;
    }

    @Override
    public void close() {
        if (this.mongoCursor != null) {
            this.mongoCursor.close();
        }

        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }


    public static final String DATABASE = "database";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

}

