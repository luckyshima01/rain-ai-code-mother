package cloud.raina.rainaicodemother.core.saver;

import cloud.raina.rainaicodemother.ai.model.HtmlCodeResult;
import cloud.raina.rainaicodemother.exception.BusinessException;
import cloud.raina.rainaicodemother.exception.ErrorCode;
import cloud.raina.rainaicodemother.model.enums.CodeGenTypeEnum;
import cn.hutool.core.util.StrUtil;

/**
 * HTML 代码保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
        }
    }
}
