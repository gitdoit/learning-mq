package org.seefly.mymq.rocketmq.demo.filter;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.seefly.mymq.rocketmq.util.PropertiesReader;

import java.io.IOException;

/**
 * 消息过滤
 *   消息过滤的好处是在Broker上就可以根据消费者的意愿来选择行的发送消息，而不是一股脑的将该Topic下的
 *   所有消息都发给他。
 *  方式
 * 1、使用Tag进行过滤，在订阅的时候指定参数 如 TagA || TagC
 * 2、使用Sql语句过滤，这种方式灵活性较高，消费者订阅的时候可以指定一条sql语句。这样Broker可以根据
 *    它进行消息过滤，但是由于Broker需要读取消息内容才能根据sql过滤所以这种方式的过滤性能没有Tag过滤好
 * 3、基于代码过滤，使用的时候需要在Broker的配置文件中添加 filterServerNums = 3 ，这样他在启动的时候会启动3个FilterServer,
 *    这必然会增加Broker的负担，并且传过去的代码逻辑不要太复杂以及不要申请太多内存。谨慎使用
 *
 *  消息过滤的sql语法
 *      数字比较 >, >=, <, <=, BETWEEN, =
 *      字符比较 =, <>, IN
 *      IS NULL 或者 IS NOT NULL
 *      逻辑操作 AND,OR,NOT
 *  支持的数据类型：
 *      数字型，比如 123、3.1415
 *      字符型 ，比如 'abe' 、注意必须用单引 号
 *      NULL ，这个特殊字符
 *      布尔型， TRUEorFALSE
 *
 * @author liujianxin
 * @date 2019-05-16 18:45
 */
public class FilterConsumer {

    public static void masin(String[] args) throws MQClientException, IOException {
        String nameserver = PropertiesReader.build("private.properties").read("nameserver");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer1");
        consumer.setNamesrvAddr(nameserver);


        // 1、基于Tag过滤
        consumer.subscribe("TagFilterTest","TagA || TagB");

        // 2、sql过滤
        // 要使用基于额外的属性过滤需要在Broker的配置文件中添加 enablePropertyFilter=true
        // 像是a is not null and a between 0 and 3 这个条件，就是根据消息上的properties来的
        consumer.subscribe("TagFilterTest",
                MessageSelector.bySql("(TAGS is not null and TAGS in ('TagA', 'TagB')) and (a is not null and a between 0 and 3)"));

        // 3、代码过滤
        // 把源码到Broker，Broker执行这段代码进行消息过滤。灵活性更高
        consumer.subscribe("","org.seefly.mymq.rocketmq.demo.filter.MyFilter", MixAll.file2String("E:\\ideaworkspace\\private\\mymq\\src\\main\\java\\org\\seefly\\mymq\\rocketmq\\demo\\filter\\MyFilter.java"));

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        System.out.printf("Consumer Started.%n");
    }

    public static void main(String[] args) throws IOException {
        String s = MixAll.file2String("E:\\ideaworkspace\\private\\mymq\\src\\main\\java\\org\\seefly\\mymq\\rocketmq\\demo\\filter\\MyFilter.java");
        System.out.println(s);
    }
}
