package cloud.raina.rainaicodemother.model.dto.app;

import cloud.raina.rainaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppUserQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
