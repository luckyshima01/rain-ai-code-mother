package cloud.raina.rainaicodemother.core;

import cloud.raina.rainaicodemother.ai.AiCodeGeneratorService;
import cloud.raina.rainaicodemother.ai.AiCodeGeneratorServiceFactory;
import cloud.raina.rainaicodemother.ai.model.HtmlCodeResult;
import cloud.raina.rainaicodemother.ai.model.MultiFileCodeResult;
import cloud.raina.rainaicodemother.core.parser.CodeParserExecutor;
import cloud.raina.rainaicodemother.core.saver.CodeFileSaverExecutor;
import cloud.raina.rainaicodemother.exception.BusinessException;
import cloud.raina.rainaicodemother.exception.ErrorCode;
import cloud.raina.rainaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMsg = "不支持的生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMsg);
            }
        };
    }

    /**
     * 统一入口：根据类型生成保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMsg = "不支持的生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMsg);
            }
        };
    }

    /**
     * 生成多文件模式的代码并保存（流式)
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后，保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                // 使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenTypeEnum, appId);
                log.info("保存成功，目录为{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败：{}", e.getMessage());
            }
        });
    }

}
