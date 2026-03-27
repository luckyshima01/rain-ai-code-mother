package cloud.raina.rainaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("cloud.raina.rainaicodemother.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class RainAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(RainAiCodeMotherApplication.class, args);
    }

}
