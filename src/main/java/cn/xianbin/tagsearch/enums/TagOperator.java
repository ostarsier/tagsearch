package cn.xianbin.tagsearch.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagOperator {

    CONTAINS("wildcard", null),
    EQUAL("=", null),
    NOT_EQUAL("!=", null),
    GTE(">=", "gte"),
    GT(">", "gt"),
    LTE("<=", "lte"),
    LT("<", "lt");

    private String opt;
    private String methodName;

    @JsonCreator
    public static TagOperator of(String opt) {
        for (TagOperator operator : TagOperator.values()) {
            if (operator.opt.equals(opt)) {
                return operator;
            }
        }
        return null;
    }

    public boolean isRangeQuery() {
        return this == GTE || this == GT || this == LTE || this == LT;
    }

}