package org.seefly.mymq.rocketmq.demo.rebalance;

/**
 * RocketMq的负载
 *  要做负载均衡必须直到一些全局信息，由于消费者跟nameServer的通讯会获取到这些东西
 * 所以RocketMq的负载均衡实在消费者这边做的。
 *
 *
 * DefaultMQPushConsumer
 *      这种形式的负载均衡不需要使用者关心，它在启动的时候会执行doRebalance动作
 *  如果一个消费者加入消费者组的话，组内的各个消费者都会指定这个动作。
 *
 *      负载均衡策略有5中，默认使用AllocateMessageQueueAveragely平均分配
 *  这个策略的结果和Topic下的MessageQueue、消费者组内的消费者数量有关。
 *  例如一个Topic下有3个MessageQueue，这个Topic对应的消费者组内有2两个消费者
 *  那么其中一个消费者消费2个MessageQueue另一个消费一个。 如果有4个消费者的话，那么就有一个消费者消费不到消息。
 *  通常情况下一个Topic下的MessageQueue应该有16个
 *
 * DefaultMQPullConsumer
 *     这种形式的负载均衡需要使用者自己维护了，因为他会看到所有的MessageQueue，并且自己维护这些Offset
 * 不过可以使用registerMessageQueueListener方法来监听消费者的加入或者退出，做出对应的均衡策略。
 *
 * @author liujianxin
 * @date 2019-05-17 15:46
 */
public class RebalanceDemo {
}
