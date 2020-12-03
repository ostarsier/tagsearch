博客：https://blog.csdn.net/appearbeauty/article/details/110563952

### 1.标签索引mapping
``` 
PUT userprofile_tags
{
  "mappings": {
    "_doc": {
      "properties": {
        "userid": {
          "type": "keyword"
        },
        "userlabels": {
          "type": "nested"
        },
        "data_date": {
          "type": "keyword"
        }
      },
      "dynamic_templates": [
        {
          "string_template": {
            "path_match": "userlabels.*",
            "mapping": {
              "type": "keyword"
            }
          }
        }
      ]
    }
  }
}
``` 
### 2.添加document
``` 
PUT userprofile_tags/_doc/1
{
    "userid" : "5",
    "userlabels" : {"tagCode1":"tagValue","tagCode2":"1"}
}
``` 
### 3.标签mysql元数据表
``` 
CREATE TABLE `userprofile_tag_metadata` (
  `id` varchar(50) NOT NULL COMMENT 'id',
  `code` varchar(50) DEFAULT NULL COMMENT '标签code',
  `name` varchar(50) DEFAULT NULL COMMENT '标签名',
  `is_mutex` tinyint(1) DEFAULT NULL COMMENT '是否标签互斥:0:否;1:是',
  `data_format` int(10) DEFAULT NULL COMMENT '数据格式:1-枚举(tagValue默认为1)；2-数值；3-文本;4-时间',
  `description` varchar(500) DEFAULT NULL COMMENT '标签说明',
  `category_id` varchar(50) DEFAULT NULL COMMENT '当前分类id',
  `all_category_id` varchar(1000) DEFAULT NULL COMMENT '全路径分类id',
  `top_category_id` varchar(50) DEFAULT NULL COMMENT '顶级分类id',
  `creator_id` varchar(200) DEFAULT NULL COMMENT '创建人',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1启用,0禁用',
  `tag_production_rule` varchar(500) DEFAULT NULL COMMENT '标签生成规则',
  `tag_update_rule` varchar(500) DEFAULT NULL COMMENT '标签更新规则',
  `tag_type` int(10) DEFAULT NULL COMMENT '标签类型:1-统计；2-规则',
  `tag_value` varchar(200) DEFAULT NULL COMMENT '标签值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户画像-标签元数据表';

INSERT INTO `userprofile_tag_metadata` VALUES ('69e48e958e892ed6aad02326d8ce131c', 'A111H001_001', '男', 1, 1, NULL, '336bd40fa83b4792f1c84d6a3cd4421b', '04cc50f4b5b04fa35ba16d8ac4f29a24/336bd40fa83b4792f1c84d6a3cd4421b', '04cc50f4b5b04fa35ba16d8ac4f29a24', NULL, '2020-08-26 16:53:39', '2020-08-26 16:53:39', 1, NULL, NULL, 2, NULL);
INSERT INTO `userprofile_tag_metadata` VALUES ('78be6f7ba95649c95ed6df5459c38bbb', 'A121H051_001', '年龄', 1, 2, NULL, '04cc50f4b5b04fa35ba16d8ac4f29a24', '04cc50f4b5b04fa35ba16d8ac4f29a24', '04cc50f4b5b04fa35ba16d8ac4f29a24', NULL, '2020-08-26 16:53:39', '2020-08-26 16:53:39', 1, NULL, NULL, 2, NULL);
``` 