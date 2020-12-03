package cn.xianbin.tagsearch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserProfileRespVo {

    private long total;

    private List<String> useridList;
}
