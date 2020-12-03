package cn.xianbin.tagsearch.bean;

import cn.xianbin.tagsearch.annotation.EsDocument;
import lombok.Data;

@Data
@EsDocument(indexName = "userprofile_tags", type = "_doc")
public class UserprofileTag {

    private String userid;

    private Object userlabels;

    private String dataDate;
}
