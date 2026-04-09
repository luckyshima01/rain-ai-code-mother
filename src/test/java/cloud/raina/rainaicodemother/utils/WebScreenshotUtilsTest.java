package cloud.raina.rainaicodemother.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenShot() {
        String screenShot = WebScreenshotUtils.saveWebPageScreenShot("https://www.baidu.com");
        Assertions.assertNotNull(screenShot);
    }
}