package cloud.raina.rainaicodemother.service;

import cloud.raina.rainaicodemother.model.dto.app.AppAddRequest;
import cloud.raina.rainaicodemother.model.dto.app.AppQueryRequest;
import cloud.raina.rainaicodemother.model.dto.app.AppUserQueryRequest;
import cloud.raina.rainaicodemother.model.entity.App;
import cloud.raina.rainaicodemother.model.entity.User;
import cloud.raina.rainaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 服务层。
 *
 * @author LuckyShima
 */
public interface AppService extends IService<App> {

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
