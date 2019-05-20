package org.seefly.mymq.rocketmq.demo.transaction;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.seefly.mymq.rocketmq.util.PropertiesReader;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事务
 *      比如银行转账， A 银行的某账户要转一万元到 B 银行的某账户 。 A 银
 * 行发送“B 银行账户增加一万元” 这个消息，要和“从 A 银行账户扣除一万元”
 * 这个操作同时成功或者同时失败。
 *      RocketMQ 采用两阶段提交 的方式实现事务消息， TransactionMQProducer
 * 处理上面情况的流程是，先发一个“准备从 B 银行账户增加一万元”的消息，
 * 发送成功后做从 A 银行账户扣除一万元的操作 ，根据操作结果是否成功，确定
 * 之前的“准备从 B 银行账户增加一万元”的消息是做 commit 还是 rollback
 *
 * 执行步骤
 * 1、发送方向MQ发送待确认消息
 * 2、MQ收到消息，持久化之后回一个成功响应
 * 3、发送方开始执行本地事务
 * 4、发送方根据本地事务成功与否向MQ发送提交 或者 回滚消息
 *    若成功则订阅放可以收到这条信息，否则收不到
 * 5、如果发送方暴毙了，提交或者回滚请求没有发给MQ，那么MQ在经过固定时间(1分钟?)后会发起回查请求
 * 6、发送方收到回查请求后查询本地事务到底咋回事啊，成功没？然后告诉MQ是提交还是回滚
 *    当然，如果一开始那个发送方如果挂掉了没法处理这个回查请求，这个回查请求会到同一个生产者组里的其他生产者实例上。
 * 7、MQ拿到回查请求的结果后，执行4的逻辑
 *
 * 那么问题就来了，发送方的事务执行好了也提交了。接收方在执行本地事务的时候死活不成功怎么办？
 * 能让发送方回滚吗？MQ没有这个支持。办法是人工解决。Nice啊
 *
 *
 * @author liujianxin
 * @date 2019-05-09 11:38
 */
public class TransactioProducerDemo {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        String nameserver = PropertiesReader.build("private.properties").read("nameserver");
        TransactionMQProducer producer = new TransactionMQProducer("GRP");
        producer.setNamesrvAddr(nameserver);
        // 方法过时了，后面会移除，推荐设置自定义线程池  啊哈哈 真棒
        // producer.setCheckThreadPoolMaxSize(20);.
        // producer.setCheckThreadPoolMinSize(5);
        //producer.setCheckRequestHoldMax(4);

        // 这个线程池会不会把系统搞炸掉，你瞅瞅这些参数 这是人干的事吗
        ExecutorService executorService = new ThreadPoolExecutor(2, 5,60L, TimeUnit.SECONDS,new SynchronousQueue<>(),r -> {
            Thread t = new Thread();
            t.setName("蓝Biubiu的小弟");
            return t;
        });
        // 那我还是自定义吧
        producer.setExecutorService(executorService);
        // 事务监听器，控制事务提交、回滚。提供回查功能
        producer.setTransactionListener(new MyFuckingTransactionListenerImpl());
        producer.start();

        String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("Transaction_topic", tags[i % tags.length], "KEY" + i,("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult sendResult = producer.sendMessageInTransaction(msg, null);
                System.out.printf("%s%n", sendResult);
                Thread.sleep(10);
            } catch (MQClientException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 100000; i++) {
            Thread.sleep(1000);
        }
        producer.shutdown();
    }
}
