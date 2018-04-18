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
import com.mongodb.client.result.UpdateResult;
import org.bson.BSON;
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

import javax.print.Doc;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @Package: smile.database.mongo
 * @Description: https://www.cnblogs.com/minsons/articles/7026600.html
 * @author: liuxin
 * @date: 2018/3/22 下午5:26
 */
@SmileComponent
public class MongoDao {
    @InsertBean
    MongoClient mongoClient;

    @InsertBean
    MongoDatabase mongoDatabase;

    public String getFindJson(String key,String value){
        return  String.format("{\"%s\":\"%s\"}", key,value);
    }

    public MongoDatabase connect(String dbName) {
        return mongoClient.getDatabase(dbName);
    }

    public String getDocumetName(Class cls){
        Annotation annotation = cls.getAnnotation(Table.class);
        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
        String userDBTableName = annotationAttributeAsMap.getString("name");
        return userDBTableName;
    }

    public void insert(Object document) {
        MongoDatabase ddz = mongoDatabase;
//        Annotation[] annotations = document.getClass().getAnnotations();
//        Annotation annotation = null;
//        for (int i = 0; i < annotations.length; i++) {
//            boolean assignableFrom = Table.class.isAssignableFrom(annotations[i].getClass());
//            if (assignableFrom) {
//                annotation = annotations[i];
//                break;
//            }
//        }
//
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(document.getClass());
        try {
            ddz.createCollection(userDBTableName);
        } catch (MongoCommandException e) {
            System.err.println("重复创建:" + userDBTableName);
        }
        MongoCollection<Document> ddz_user = ddz.getCollection(userDBTableName);
        ddz_user.insertOne(convert(document));

    }

    public boolean update(String query, Map<String, Object> update, Class cls) {
//        Annotation annotation = cls.getAnnotation(Table.class);
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(cls);
        MongoCollection<Document> collection = mongoDatabase.getCollection(userDBTableName);
        Document opt = new Document();
        for (Map.Entry<String, Object> entry : update.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            opt.append(key, value);
        }
        Document document = new Document("$set", opt);
        UpdateResult updateResult = collection.updateMany(Document.parse(query), document);
        return updateResult.getMatchedCount() > 0;
    }

    public Document convert(Object document) {
        String toJson = JsonUtils.toJson(document);
        return Document.parse(toJson);
    }


    public <T> T findByUid(String uid, Class<T> cls) {
        String query = String.format("{\"uid\":\"%s\"}", uid);
//        Annotation annotation = cls.getAnnotation(Table.class);
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(cls);
        MongoCollection<Document> collection = mongoDatabase.getCollection(userDBTableName);
        Document queryDoc = Document.parse(query);
        FindIterable<Document> documents = collection.find(queryDoc);
        Document first = documents.first();
        if (first == null) {
            return null;
        }
        return JsonUtils.fromJson(first.toJson(), cls);

    }

    public <T> T findOne(String query, Class<T> cls) {
//        Annotation annotation = cls.getAnnotation(Table.class);
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(cls);
        MongoCollection<Document> collection = mongoDatabase.getCollection(userDBTableName);
        Document queryDoc = Document.parse(query);
        FindIterable<Document> documents = collection.find(queryDoc);
        Document first = documents.first();
        if (first == null) {
            return null;
        }
        Set<String> strings = first.keySet();
        JSONObject jsonObject=new JSONObject();
        for (String k : strings) {
            Object v = first.get(k);
            jsonObject.put(k, v);
        }
        return JsonUtils.fromJson(jsonObject.toString(), cls);

    }

    public <T> List<T> findAll(String query, Class<T> cls) {
//        Annotation annotation = cls.getAnnotation(Table.class);
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(cls);
        FindIterable<Document> documents = mongoDatabase.getCollection(userDBTableName).find(Document.parse(query));
        MongoCursor<Document> iterator = documents.iterator();
        List<T> list = Lists.newArrayList();
        while (iterator.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Document next = iterator.next();
            String toJson = next.toJson();
            Set<String> strings = next.keySet();
            for (String k : strings) {
                Object v = next.get(k);
                jsonObject.put(k, v);
            }
            T t = JsonUtils.fromJson(jsonObject.toString(), cls);
            list.add(t);
        }
        return list;
    }

    public <T> List<T> findAll(Class<T> cls) {
//        Annotation annotation = cls.getAnnotation(Table.class);
//        AnnotationMap<String, Object> annotationAttributeAsMap = AnnotationTools.getAnnotationAttributeAsMap(annotation);
//        String userDBTableName = annotationAttributeAsMap.getString("name");
        String userDBTableName=getDocumetName(cls);
        FindIterable<Document> documents = mongoDatabase.getCollection(userDBTableName).find();
        MongoCursor<Document> iterator = documents.iterator();
        List<T> list = Lists.newArrayList();
        while (iterator.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Document next = iterator.next();
            String toJson = next.toJson();
            Set<String> strings = next.keySet();
            for (String k : strings) {
                Object v = next.get(k);
                jsonObject.put(k, v);
            }
            T t = JsonUtils.fromJson(jsonObject.toString(), cls);
            list.add(t);
        }
        return list;
    }

    public boolean del(String query, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document parse = Document.parse(query);
        return collection.deleteOne(parse).getDeletedCount() > 0 ? true : false;
    }
}
