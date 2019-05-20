package org.seefly.mymq.rocketmq.demo.transaction;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.seefly.mymq.rocketmq.util.PropertiesReader;

/**
 * @author liujianxin
 * @date 2019-05-20 15:47
 */
public class TransactionConsumerDemo {
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("Transaction_group");
        String nameserver = PropertiesReader.build("private.properties").read("nameserver");
        consumer.setNamesrvAddr(nameserver);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe("Transaction_topic", "*");
        consumer.setMessageListener((MessageListenerOrderly) (msgs, ctx) -> {
            MessageExt messageExt = msgs.get(0);
            try {
                Thread.sleep(20);
                System.out.println("事务消费者:" + new String(messageExt.getBody()));
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 等等大爷
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
            return ConsumeOrderlyStatus.SUCCESS;
        });
        consumer.start();
    }
}
