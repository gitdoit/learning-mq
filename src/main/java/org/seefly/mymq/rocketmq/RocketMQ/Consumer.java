package org.seefly.mymq.rocketmq.RocketMQ;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Properties;

/**
 * @author liujianxin
 * @date 2018-07-12 10:33
 * 描述信息：
 **/
public class Consumer {




    public static void main(String[] args) throws InterruptedException, MQClientException, IOException {
        ResourceLoader  resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:private.properties");
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        String nameserver = properties.getProperty("nameserver");
        //声明并初始化一个consumer
        //需要一个consumer group名字作为构造方法的参数，这里为consumer1
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer1");
        //同样也要设置NameServer地址，可以用 ip1:port1;ip2:port2这种形式设定多个nameserver
        consumer.setNamesrvAddr(nameserver);

        //这里设置的是一个consumer的消费策略
        //CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
        //CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
        //CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认是半个小时以前
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 消费者模式有两种
        // 第一种是集群方式，即多个消费者所处同一个消费者组，且订阅相同topic，此时该topic下的消息会以负载均衡的方式分发到这些消费者手中
        // 第二种是广播方式，即多个消费者所处同一个消费者组，且订阅相同topic，此时该topic下每个消息都会发送到所有的消费者手中
        consumer.setMessageModel(MessageModel.BROADCASTING);
        //consumer.setMessageModel(MessageModel.CLUSTERING);

        // 设置consumer所订阅的Topic和Tag，*代表全部的Tag
        // 如果只想接收该topic下的指定tag消息，可以用 tag1 || tag2
        consumer.subscribe("TopicTest", "*");

        // 一次最多从服务器拉多少条消息
        consumer.setPullBatchSize(12);

        // 一次最多消费多少条消息,默认就是一个。这个限制listener中的msgs参数大小
        consumer.setConsumeMessageBatchMaxSize(1);

        //设置一个Listener，主要进行消息的逻辑处理
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, contest) ->{
            System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);
            //返回消费状态
            //CONSUME_SUCCESS 消费成功
            //RECONSUME_LATER 消费失败，需要稍后重新消费
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        //调用start()方法启动consumer
        consumer.start();

        System.out.println("Consumer Started.");
    }

}
