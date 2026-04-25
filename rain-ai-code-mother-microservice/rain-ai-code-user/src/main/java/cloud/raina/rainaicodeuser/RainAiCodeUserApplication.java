package cloud.raina.rainaicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("cloud.raina.rainaicodeuser.mapper")
@ComponentScan("cloud.raina")
public class RainAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(RainAiCodeUserApplication.class, args);
    }
}
