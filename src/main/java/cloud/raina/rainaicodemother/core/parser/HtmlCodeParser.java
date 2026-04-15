package cloud.raina.rainaicodemother.core.parser;

import cloud.raina.rainaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /** 方案三：用于判断内容是否具备基本 HTML 结构特征 */
    private static final Pattern HTML_STRUCTURE_PATTERN = Pattern.compile("(?i)<(!DOCTYPE\\s+html|html[\\s>]|head[\\s>]|body[\\s>])");

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码块
        String htmlCode = extractHtmlCode(codeContent);
        // 方案一A：只有当代码块存在且内容通过 HTML 合法性校验时才设置，不再有兜底逻辑
        if (htmlCode != null && !htmlCode.trim().isEmpty() && isValidHtml(htmlCode)) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 解析失败时 htmlCode 保持 null，由上层决定是否跳过保存
        return result;
    }

    /**
     * 提取 HTML 代码块内容
     *
     * @param content 原始内容
     * @return 代码块内的 HTML 字符串，未找到时返回 null
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 方案三：校验内容是否包含基本 HTML 结构标签
     * 防止将纯文字（如闲聊回复）误认为合法 HTML 写入文件
     *
     * @param content 待校验内容
     * @return 包含 HTML 结构标签时返回 true
     */
    private boolean isValidHtml(String content) {
        return HTML_STRUCTURE_PATTERN.matcher(content).find();
    }
}
