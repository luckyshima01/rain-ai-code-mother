package cloud.raina.rainaicodeuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("cloud.raina.rainaicodeuser.mapper")
@ComponentScan("cloud.raina")
@EnableDubbo
public class RainAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(RainAiCodeUserApplication.class, args);
    }
}
