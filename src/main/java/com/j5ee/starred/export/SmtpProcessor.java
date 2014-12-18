package com.j5ee.starred.export;

import com.j5ee.starred.export.domain.Item;
import com.mongodb.*;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmtpProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        MongoClient client = new MongoClient(new ServerAddress("localhost",
                27017));
        DB database = client.getDB("reader");

        DBCollection pagesCollection = database.getCollection("pages");
        DBObject pageObject = pagesCollection.findOne(new BasicDBObject("_id",
                1));
        int pageNo = ((Double) pageObject.get("page")).intValue();

        DBCollection starredCollection = database.getCollection("starred");

        DBCursor cursor = starredCollection.find()
                .sort(new BasicDBObject("_id", -1)).skip(pageNo * 485)
                .limit(485);

        List<Item> items = new ArrayList<Item>();
        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            Item item = new Item();
            item.setTitle((String) document.get("title"));

            String description = document.get("description")
                    + "<br><br>" + document.get("feedTitle") + "<br>"
                    + document.get("pubDate") + "<br>" + "<a href=\""
                    + document.get("link") + "\">"
                    + document.get("link") + "</a>";
            item.setDescription(description);

            item.setLink((String) document.get("link"));
            item.setGuid(item.getLink());
            item.setPubDate((String) document.get("pubDate"));
            items.add(item);
        }

        if (!items.isEmpty()) {

            CamelContext context = exchange.getContext();
            ProducerTemplate template = context.createProducerTemplate();

            for (Item item : items) {

                Map<String, Object> headerProperties = new HashMap<String, Object>();
                headerProperties
                        .put("To",
                                context.resolvePropertyPlaceholders("x@m.evernote.com"));
                headerProperties.put("From", context
                        .resolvePropertyPlaceholders("x@gmail.com"));
                headerProperties.put("Subject", item.getTitle());

                String message = item.getDescription();

                template.sendBodyAndHeaders(
                        "smtps://smtp.gmail.com:465?username=x@gmail.com&password=x&contentType=text/html;charset=UTF-8 ",
                        message, headerProperties);
            }
        }

        pagesCollection.update(new BasicDBObject("_id", 1), new BasicDBObject(
                "$inc", new BasicDBObject("page", 1)));
    }

}
