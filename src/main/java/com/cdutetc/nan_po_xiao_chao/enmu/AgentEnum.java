package com.cdutetc.nan_po_xiao_chao.enmu;

public enum AgentEnum {
    ROUTER(1, "路由"),
    USER_AGENT(2, "用户下单"),
    CHIEF_AGENT(3, "私人厨师"),
    // 路由置信度不足、或模型返回了不认识的场景名时兜底用
    UNKNOWN(0, "无法识别");

    private Integer value;
    private String desc;

    AgentEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }
    public String getDesc() {
        return desc;
    }

    public static AgentEnum getValue(Integer value) {
        for (AgentEnum agentEnum : AgentEnum.values()) {
            if (agentEnum.value.equals(value)) {
                return agentEnum;
            }
        }
        return null;
    }
}
