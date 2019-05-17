package org.seefly.mymq.rocketmq.demo.filter;

import org.apache.rocketmq.common.filter.FilterContext;
import org.apache.rocketmq.common.filter.MessageFilter;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Objects;

/**
 * @author liujianxin
 * @date 2019-05-17 15:13
 */
public class MyFilter implements MessageFilter {
    @Override
    public boolean match(MessageExt messageExt, FilterContext filterContext) {
        String userId = messageExt.getUserProperty("userId");
        if(!Objects.isNull(userId)){
            Integer integer = Integer.valueOf(userId);
            return integer > 3;
        }
        return false;
    }
}
