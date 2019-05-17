package org.seefly.mymq.rocketmq.demo.filter;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.seefly.mymq.rocketmq.util.PropertiesReader;

import java.io.UnsupportedEncodingException;

/**
 *
 *
 * @author liujianxin
 * @date 2019-05-16 19:08
 */
public class TagProducer {
    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer("Producer_Group_A");
        String nameserver = PropertiesReader.build("private.properties").read("nameserver");
        producer.setNamesrvAddr(nameserver);
        producer.start();

        String[] tags = new String[] {"TagA", "TagB", "TagC"};

        for (int i = 0; i < 60; i++) {
            Message msg = new Message("TagFilterTest",
                    tags[i % tags.length],
                    "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 用来给消息添加额外的属性，这样，消费者就可以使用sql语句根据这些属性在Broker上进行消息过滤了
            msg.putUserProperty("a",String.valueOf(i));
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }

        producer.shutdown();
    }
}
