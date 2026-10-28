package com.cdutetc.nan_po_xiao_chao.router;


import com.cdutetc.nan_po_xiao_chao.enmu.AgentEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouterAgent {

    private final @Qualifier("router") ChatClient router;

    /**
     * 路由结果,字段与 AiConstant.ROUTER 里约定的输出格式一一对应:
     * {"type": "USER_AGENT|CHIEF_AGENT|UNKNOWN", "confidence": 0.0~1.0}
     */
    public record RouteResult(AgentEnum type, double confidence) {}

    public AgentEnum route(String message) {
        // 注意:提示词要求输出的是一个 JSON 对象,所以这里必须按对象解析。
        // 之前写 .entity(AgentEnum.class) 会报
        // "Cannot deserialize AgentEnum from Object value (token JsonToken.START_OBJECT)"。
        RouteResult result = router.prompt()
                .user(message)
                .call()
                .entity(RouteResult.class);

        if (result == null) {
            log.warn("路由结果解析为空,按 UNKNOWN 处理");
            return AgentEnum.UNKNOWN;
        }

        log.info("路由结果: type={}, confidence={}", result.type(), result.confidence());

        // 置信度不足,或者模型给了枚举里没有的名字(反序列化成 null)都兜底
        if (result.type() == null || result.confidence() < 0.7) {
            return AgentEnum.UNKNOWN;
        }

        return result.type();
    }
}
