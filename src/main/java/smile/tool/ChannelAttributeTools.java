package smile.tool;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2015 The Smile-Boot Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ChannelAttributeTools {

    private final static Map<String,AttributeKey>cache=new ConcurrentHashMap<>();
    /**
     * 当可变参数长度为1时候 该方法是从指定的连接中获取信息
     * 当可变参数长度为2时候 该方法是将key-value保存到该连接中
     * @param channel
     * @param params
     * @return
     */
    public static String attr(Channel channel, String... params) {
        if (params.length == 0) {
            return null;
        }
        AttributeKey attributeKey = cache.get(params[0]);
        if (attributeKey==null){
            attributeKey = AttributeKey.valueOf(params[0]);
            cache.put(params[0],attributeKey);
        }
        if (params.length == 1) {
            return (String) channel.attr(attributeKey).get();
        } else {
            channel.attr(attributeKey).set(params[1]);
            return params[1];
        }
    }

}
