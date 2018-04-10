package smile.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.smileframework.ioc.bean.annotation.SmileBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.string.StringTools;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: smile.config
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/21 下午1:59
 */
@SmileComponent
public class MongoConfig {

    private String hostports="127.0.0.1:27017";

    private String maxConnect="12";

    private String maxWaitThread="2";

    private String maxTimeOut="1000";

    private String maxWaitTime="1000";


    private String username="liuxin";

    private String password="liuxin";

    private String database="ddz";



    @SmileBean
    public MongoClient mongoClient() {
        MongoClient mongoClient = null;
        MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        build.connectionsPerHost(Integer.valueOf(maxConnect));
        build.threadsAllowedToBlockForConnectionMultiplier(Integer.valueOf(maxWaitThread));
        build.connectTimeout(Integer.valueOf(maxTimeOut) * 1000);
        build.maxWaitTime(Integer.valueOf(maxWaitTime) * 1000);
        MongoClientOptions options = build.build();
        try {
            List<ServerAddress> addrs = new ArrayList<>();
            for (String hostport : hostports.split(", *")) {
                if (StringTools.isBlank(hostport)) {
                    continue;
                }
                hostport = hostport.trim();

                ServerAddress serverAddress = new ServerAddress(hostport.split(":")[0],Integer.valueOf(hostport.split(":")[1]));
                addrs.add(serverAddress);
            }

            MongoCredential credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
            List<MongoCredential> credentials = new ArrayList<MongoCredential>();
            credentials.add(credential);

            mongoClient = new MongoClient(addrs,credentials, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoClient;
    }

    @SmileBean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        return mongoDatabase;
    }

//    @SmileBean
//    public MongoCollection<Document> mongoCollection(MongoDatabase mongoDatabase) {
//        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);
//        return mongoCollection;
//    }

}
