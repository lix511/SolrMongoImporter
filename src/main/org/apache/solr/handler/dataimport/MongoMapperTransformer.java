package org.apache.solr.handler.dataimport;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: James
 * Date: 15/08/12
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public class MongoMapperTransformer extends Transformer {

    @Override
    public Object transformRow(Map<String, Object> row, Context context) {

        for (Map<String, String> map : context.getAllEntityFields()) {
            String columnFieldName = map.get(DataImporter.COLUMN);
            if (columnFieldName == null)
                continue;

            String toString = map.get(TO_STRING);
            //If the field is ObjectId convert it into String
            if (toString != null && Boolean.parseBoolean(toString)) {
                Object srcId = row.get(columnFieldName);
                row.put(columnFieldName, srcId.toString());
            }
            else{
                row.put(columnFieldName, row.get(columnFieldName));
            }
        }

        return row;
    }

    //To identify the _id field
    public static final String TO_STRING = "toString";
}
