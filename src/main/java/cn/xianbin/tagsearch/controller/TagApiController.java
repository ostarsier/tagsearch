package cn.xianbin.tagsearch.controller;

import cn.xianbin.tagsearch.service.TagService;
import cn.xianbin.tagsearch.vo.TagConditionVo;
import cn.xianbin.tagsearch.vo.UserProfileRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tag")
public class TagApiController {

    @Autowired
    private TagService tagService;

    @PostMapping("/search")
    public ResponseEntity searchList(@RequestBody TagConditionVo tagConditionDto) throws Exception {
        UserProfileRespVo userProfileRespVo = tagService.search(tagConditionDto);
        return ResponseEntity.ok(userProfileRespVo);
    }

}
