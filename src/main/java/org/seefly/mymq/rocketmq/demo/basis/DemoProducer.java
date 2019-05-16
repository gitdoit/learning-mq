package org.seefly.mymq.rocketmq.demo.basis;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.seefly.mymq.rocketmq.util.PropertiesReader;

import java.nio.charset.StandardCharsets;

/**
 * 生产者启动的时候会跟NameServer通讯，拿到路由信息，随后以30s/次的频率拉取路由信息。
 * 路由信息也就是哪些Topic放在了哪些Broker上，然后根据需要跟指定的Broker建立长连接
 * 在发送消息前，根据Topic找到指定的路由信息(TopicPublishInfo)，拿到该Topic的消息队列，然后轮询的方式
 * 向某个队列发送消息。
 *
 * 另外对于Broker，每一个Broker都通过唯一的CommitLog文件进行实际的消息存储，包括元数据及消息体。
 * 由于这一个文件存储所有类型的消息，那么再查询Topic的时候就很慢了，所以会有一个类似二级索引的consumerLog的文件
 * 这个文件和Topic是一对一的关系(我猜的)，它存储的内容就是该topic下的每个消息对应在commitLog文件中的偏移量以及大小。
 * 这样就能根据consumerLog来很快的定位消息了。
 *
 *
 * 重复消费的问题
 *  RocketMQ只保证消息一定投递，但不保证消息不会重复，在极端的网络环境下，生产者的消息投递到Broker之后，Broker的确认消息可能会丢失
 *  这样生产者会重复投递。解决重复消费的方法有两种，只能在消费者端做处理
 *  1、实现消费逻辑幂等性，即多次调用和一次调用的效果是一样的
 *  2、本地维护一个已消费的消息记录，消费之前查记录。
 *
 * 生产者使用示例
 * @author liujianxin
 * @date 2019-04-24 14:09
 */
public class DemoProducer {
    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer("Producer_Group_A");
        // 若异步发送消息失败，则重试2次。极端情况下可能会导致消息重复发送
        producer.setRetryTimesWhenSendAsyncFailed(2);
        // 消息发送失败的时候是否尝试发送到另一个broker上，默认false
        producer.setRetryAnotherBrokerWhenNotStoreOK(false);
        producer.setNamesrvAddr(PropertiesReader.build("private.properties").read("nameserver"));
        producer.start();
        Message message = new Message("TopicA","async","Hello RocketMQ".getBytes(StandardCharsets.UTF_8));
        // 异步回调的方法
        sendAsyncAndCallBack(producer,message);

        //sendOneway(producer,message);
        producer.shutdown();
    }


    /**
     * 自定义发送消息
     * 默认情况下一个topic下有多个MessageQueue，你发送消息的时候
     * 具体这个消息是发送到哪个队列上是不确定的。如果业务需求根据消息的
     * 某个字段判断进而发送到不同的队列上，可以使用队列选择器。或者直接传入一个队列。
     * 我用过一次就是业务需要消息有序，根据客户端id进行散列再对队列长度取模，拿到队列索引
     * 这样一个客户端的消息就只能发送到一个队列里面了。
     */
    public static  void sendMessageByCustom(DefaultMQProducer producer,Message message) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        producer.send(message, (mqs, msg, arg) -> {
            int index = arg.hashCode() % mqs.size();
            return mqs.get(index);
        },"Client_ID");

    }


    /**
     * 延时发送消息，不是生产者在生产消息之后等待指定的时间再发送到broker上
     * 而是直接发送到broker，由broker控制在合适的时间交付给消费者。
     *  rocketmq默认提供18个延时等级，使用的时候传入level级别即可
     *  1s/5s/1 Os/30s/I m/2m/3m/4m/5m/6m/7m/8m/9m/1 Om/20m/30m/1 h/2h
     *
     *  broker上：
     *  定时消息会暂存在名为SCHEDULE_TOPIC_XXXX的topic中，并根据delayTimeLevel存入特定的queue，queueId = delayTimeLevel – 1，
     *  即一个queue只存相同延迟的消息，保证具有相同发送延迟的消息能够顺序消费。broker会调度地消费SCHEDULE_TOPIC_XXXX，将消息写入真实的topic。
     */
    private static void delaySend(DefaultMQProducer producer) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        Message message = new Message("TopicA","async","Hello RocketMQ".getBytes(StandardCharsets.UTF_8));
        // 消息延时10秒钟，就很棒！
        message.setDelayTimeLevel(3);
        producer.send(message);
    }



    /**
     * 单向发送，不需要发送结果。跟UDP差不多的意思。适用于日志服务等
     */
    private static void sendOneway(DefaultMQProducer producer,Message message) throws RemotingException, MQClientException, InterruptedException {
        producer.sendOneway(message);
    }

    /**
     * 使用异步回调的方式
     * 关于发送结果
     * 有四种
     * FLUSH DISK TIMEOUT：意思是刷盘超时，只有在broker设置的是同步刷盘的时候才会出现这个结果，异步刷盘不会的。
     * FLUSH SLAVE TIMEOUT：意思是主从同步超时，应该只有在broker设置同步刷盘，且主从设置数据同步才会出现。
     * SLAVE NOT AVAILABLE:跟上面一条类似，意思是broker同步刷盘，且主从同步数据。但是没有找到可用的从属
     * SEND OK：就是发送成功了，也就是没有出现上面的三种情况。但是需要结合具体的配置策略来说明。
     *      例如如果broker设置的是异步刷盘，那么此时返回这个状态码能不能表明就刷盘成功了呢？我不知道
     *      例如broker设置的是同步刷盘，但是主从之间是异步同步，那么返回这个状态码不能确定数据是否正确同步到从上
     */
    private static void sendAsyncAndCallBack(DefaultMQProducer producer,Message message) throws RemotingException, MQClientException, InterruptedException {
        producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("消息发送成功：brokerName="+sendResult.getMessageQueue().getBrokerName()+"\r\n状态"+sendResult.getSendStatus());

            }
            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 同步发送消息
     * 同步发送消息可以指定消息队列，发送超时时间，批量发送等
     *
     */
    private static void syncSend(DefaultMQProducer producer) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("TopicTest","TagA",("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 发送消息的时候最好设置key，这样在追踪消息的时候好定位，以及在发送出错的时候将key打印出来就很 nice
                msg.setKeys("业务相关的key，如orderID");
                // 调用producer的send()方法发送消息
                // 这里调用的是同步的方式，所以会有返回结果
                SendResult sendResult = null;
                try{
                    sendResult = producer.send(msg);
                    //打印返回结果，可以看到消息发送的状态以及一些相关信息
                    System.out.println(sendResult);
                }catch (MQClientException | RemotingException | MQBrokerException | InterruptedException ex){
                    ex.printStackTrace();
                    System.out.println("发送失败,发送状态->"+sendResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
    }


}
