package smile.database.mongo;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.google.common.collect.Lists;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.annotation.AnnotationMap;
import org.smileframework.tool.annotation.AnnotationTools;
import org.smileframework.tool.json.JsonUtils;
import org.smileframework.tool.string.StringTools;
import smile.config.Table;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

/**
 * @Package: smile.database.mongo
 * @Description:
 * @author: liuxin
 * @date: 2018/3/22 下午5:26
 */
@SmileComponent
public class MongoDao {
    @InsertBean
    MongoClient mongoClient;

    @InsertBean
    MongoDatabase mongoDatabase;

    public MongoDatabase connect(String dbName) {
        return mongoClient.getDatabase(dbName);
    }

    public void insert(Object document) {
        MongoDatabase ddz = mongoDatabase;
        Annotation[] annotations = document.getClass().getAnnotations();
        Annotation annotation = null;
        for (int i = 0; i < annotations.length; i++) {
            boolean assignableFrom = Table.class.isAssignableFrom(annotations[i].getClass());
            if (assignableFrom) {
                annotation = annotations[i];
                break;
            }
        }
        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
        String userDBTableName = annotationAttributeAsMap.getString("name");
        try {
            ddz.createCollection(userDBTableName);
        } catch (MongoCommandException e) {
            System.err.println("重复创建:" + userDBTableName);
        }
        MongoCollection<Document> ddz_user = ddz.getCollection(userDBTableName);
        ddz_user.insertOne(convert(document));

    }


    public Document convert(Object document) {
        String toJson = JsonUtils.toJson(document);
        return Document.parse(toJson);
    }


    public <T> T findByUid(String uid, Class<T> cls) {
        String query = String.format("{\"uid\":\"%s\"}", uid);
        Annotation annotation = cls.getAnnotation(Table.class);
        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
        String userDBTableName = annotationAttributeAsMap.getString("name");
        MongoCollection<Document> collection = mongoDatabase.getCollection(userDBTableName);
        Document queryDoc = Document.parse(query);
        FindIterable<Document> documents = collection.find(queryDoc);
        Document first = documents.first();
        if (first==null){
            return null;
        }
        return JsonUtils.fromJson(first.toJson(), cls);

    }

    public <T> T findOne(String query, Class<T> cls) {
        Annotation annotation = cls.getAnnotation(Table.class);
        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
        String userDBTableName = annotationAttributeAsMap.getString("name");
        MongoCollection<Document> collection = mongoDatabase.getCollection(userDBTableName);
        Document queryDoc = Document.parse(query);
        FindIterable<Document> documents = collection.find(queryDoc);
        Document first = documents.first();
        if (first==null){
            return null;
        }
        return JsonUtils.fromJson(first.toJson(), cls);

    }

    public <T> List<T> findAll(Class<T> cls) {
        Annotation annotation = cls.getAnnotation(Table.class);
        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
        String userDBTableName = annotationAttributeAsMap.getString("name");
        FindIterable<Document> documents = mongoDatabase.getCollection(userDBTableName).find();
        MongoCursor<Document> iterator = documents.iterator();
        List<T> list = Lists.newArrayList();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            String toJson = next.toJson();
            T t = JsonUtils.fromJson(toJson, cls);
            list.add(t);
        }
        return list;
    }

    public boolean del(String query,String collectionName){
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document parse = Document.parse(query);
       return collection.deleteOne(parse).getDeletedCount()>0?true:false;
    }
}
