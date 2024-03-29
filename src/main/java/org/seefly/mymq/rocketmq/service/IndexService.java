package org.seefly.mymq.rocketmq.service;

import org.seefly.mymq.rocketmq.event.MyEvent;
import org.seefly.mymq.rocketmq.listener.MessageListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author liujianxin
 * @date 2018-07-12 16:34
 * 描述信息：
 **/
@Component
public class IndexService {

    @EventListener
    public void getService(MyEvent event){
        System.out.println("监听器："+event);
    }

    @EventListener
    public void getEvent(MessageListener msg){
        System.out.println("消息队列:"+msg);
    }
}
