package org.apache.solr.handler.dataimport;


import java.util.List;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;

public class ISODateStringEvaluator extends Evaluator {

    @Override
    public String evaluate(String expression, Context context) {
        List<Object> l = parseParams(expression, context.getVariableResolver());
        if (l.size() == 0) {
            throw new DataImportHandlerException(SEVERE, "'isoDateString()' must have one parameter");
        }
        String date = l.get(0).toString();
        return date.replace(" ", "T") + "Z";
    }
}
