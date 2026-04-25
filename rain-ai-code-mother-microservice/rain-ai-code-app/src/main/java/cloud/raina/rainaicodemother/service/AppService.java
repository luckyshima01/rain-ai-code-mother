package cloud.raina.rainaicodemother.service;

import cloud.raina.rainaicodemother.model.dto.app.AppAddRequest;
import cloud.raina.rainaicodemother.model.dto.app.AppQueryRequest;
import cloud.raina.rainaicodemother.model.entity.App;
import cloud.raina.rainaicodemother.model.entity.User;
import cloud.raina.rainaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 服务层。
 *
 * @author LuckyShima
 */
public interface AppService extends IService<App> {

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用ID
     * @param message   提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 部署应用
     *
     * @param appId     应用ID
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 异步生成应用截图并更新数据库
     *
     * @param appId  应用 Id
     * @param appUrl 应用 URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param loginUser     登录用户
     * @return 应用 Id
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 根据查询条件构造数据查询参数（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);
}
