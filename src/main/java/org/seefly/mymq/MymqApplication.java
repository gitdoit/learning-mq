package org.seefly.mymq;

import cn.worken.common.log.EnableLogService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableLogService
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MymqApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymqApplication.class, args);
    }

}
