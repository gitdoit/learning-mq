package org.seefly.mymq.rocketmq.demo.transaction;

/**
 * 事务
 *      比如银行转账， A 银行的某账户要转一万元到 B 银行的某账户 。 A 银
 * 行发送“B 银行账户增加一万元” 这个消息，要和“从 A 银行账户扣除一万元”
 * 这个操作同时成功或者同时失败。
 *      RocketMQ 采用两阶段提交 的方式实现事务消息， TransactionMQProducer
 * 处理上面情况的流程是，先发一个“准备从 B 银行账户增加一万元”的消息，
 * 发送成功后做从 A 银行账户扣除一万元的操作 ，根据操作结果是否成功，确定
 * 之前的“准备从 B 银行账户增加一万元”的消息是做 commit 还是 rollback
 * @author liujianxin
 * @date 2019-05-09 11:38
 */
public class TransactioProducerDemo {
}
