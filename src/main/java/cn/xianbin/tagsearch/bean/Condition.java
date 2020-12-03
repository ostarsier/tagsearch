package cn.xianbin.tagsearch.bean;

import cn.xianbin.tagsearch.enums.TagOperator;
import lombok.Data;

/**
 * 标签过滤条件
 */
@Data
public class Condition {

    private String tagCode;

    /**
     * 过滤条件:wildcard, =, !=, >, >=, <, <=
     */
    private TagOperator operator;

    private String tagValue;
}