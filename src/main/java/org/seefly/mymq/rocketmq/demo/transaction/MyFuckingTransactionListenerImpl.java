package org.seefly.mymq.rocketmq.demo.transaction;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liujianxin
 * @date 2019-05-20 14:58
 */
@Slf4j
public class MyFuckingTransactionListenerImpl implements TransactionListener {
    private AtomicInteger transactionIndex = new AtomicInteger(0);
    private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    /**
     * 预备！
     * 跑！！
     * "Transaction.begin" 消息发送成功之后，这个方法被调用。
     * 这里执行事务逻辑
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        int value = transactionIndex.getAndIncrement();
        int status = value % 3;
        localTrans.put(msg.getTransactionId(), status);
        try {
            // 这块模拟执行本地事务逻辑
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }catch (Exception ex){
            return LocalTransactionState.UNKNOW;
        }
        // 返回UNKNOW 让MQ回查
        return LocalTransactionState.UNKNOW;
    }

    /**
     * 哎呀，你怎么不提交事务呀
     * 消息回查
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.info("事务回查！！！");
        Integer status = localTrans.get(msg.getTransactionId());
        if (null != status) {
            switch (status) {
                case 0:
                    return LocalTransactionState.UNKNOW;
                case 1:
                    return LocalTransactionState.COMMIT_MESSAGE;
                case 2:
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                default:
                    return LocalTransactionState.COMMIT_MESSAGE;
            }
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
