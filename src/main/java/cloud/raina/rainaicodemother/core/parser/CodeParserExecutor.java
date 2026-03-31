package cloud.raina.rainaicodemother.core.parser;

import cloud.raina.rainaicodemother.exception.BusinessException;
import cloud.raina.rainaicodemother.exception.ErrorCode;
import cloud.raina.rainaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 * 根据代码类型生成类型执行相应的解析逻辑
 */
public class CodeParserExecutor {

    public static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    public static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent     代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果（HTMLCodeResult/MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
