package org.seefly.mymq.rocketmq.demo.priority;

/**
 * RocketMQ不支持消息级别或者Topic级别的消息优先级
 * 但可以通过一下几种方式实现要求不高的优先级场景
 * 1、Topic拆分
 *   如果同一个Topic下，共同处理所有门店的订单，如果A门店比其他所有门店的订单都要多，那么
 * 在A门店订单量 大的时候其他门店的消息处理速度肯定会收到影响，这个时候可以将该门店的Topic独立出去。不过
 * 这样会造成topic很多
 * 2、增加Message Queue
 *   上面这种情况，还可以使用增大MessageQueue数量来处理，就是按照门店标识来分配Message Queue，这样即使
 *   某一家的订单量很大，它的消息也会堆积在同一个Message Queue中。由于RocketMq是循环遍历Topice下所有
 *   MessageQueue，所以这种方式可以有效地避免订单量多的门店对订单量少的门店处理速度的影响。但这还不够，
 *   由于默认情况下消费者一次行最多可以从MessageQueue中拉取最大32条消息，所以可以将pullBatchSize设置成1
 *   这样更公平
 *
 *
 * @author liujianxin
 * @date 2019-05-16 18:20
 */
public class MessagePriorityDemo {
}
