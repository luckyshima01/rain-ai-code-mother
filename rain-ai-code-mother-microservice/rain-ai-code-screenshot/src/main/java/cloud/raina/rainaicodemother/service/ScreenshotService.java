package cloud.raina.rainaicodemother.service;

/**
 * 截图服务
 */
public interface ScreenshotService {
    /**
     * 生成并上传截图
     *
     * @param webUrl 网页地址
     * @return 截图保存路径
     */
    String generateAndUploadScreenshot(String webUrl);
}
