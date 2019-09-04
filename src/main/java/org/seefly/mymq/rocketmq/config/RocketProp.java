package org.seefly.mymq.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.rocketmq")
public class RocketProp {

    private String namesrvAddr;
    private String instanceName;
    private String clientIP;
    private ProducerConfig producer;
    private ConsumerConfig consumer;


    @Data
    public static class ProducerConfig{
        private String tranInstanceName;
        private String group;
        private String instanceName;
    }

    @Data
    public static class ConsumerConfig{
        private String instanceName;
        private String group;
        private Integer minConsumers;
        private Integer maxConsumers;

        public String getGroup(String applicationName){
            return group == null ? applicationName : group;
        }

    }
}
