package cloud.raina.rainaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import cloud.raina.rainaicodemother.model.entity.App;
import cloud.raina.rainaicodemother.mapper.AppMapper;
import cloud.raina.rainaicodemother.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author LuckyShima
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
