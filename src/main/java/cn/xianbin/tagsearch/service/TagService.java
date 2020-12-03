package cn.xianbin.tagsearch.service;

import cn.xianbin.tagsearch.annotation.EsDocument;
import cn.xianbin.tagsearch.bean.Condition;
import cn.xianbin.tagsearch.bean.UserprofileTag;
import cn.xianbin.tagsearch.enums.TagOperator;
import cn.xianbin.tagsearch.vo.TagConditionVo;
import cn.xianbin.tagsearch.vo.UserProfileRespVo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TagService {

    private static final String CROWD_FIELD = "userlabels.";

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Autowired
    RestHighLevelClient client;

    public UserProfileRespVo search(TagConditionVo tagConditionVo) throws Exception {
        List<Condition> conditions = tagConditionVo.getConditions();

        SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(conditions);
        log.info("searchSourceBuilder={}", searchSourceBuilder);

        EsDocument doc = UserprofileTag.class.getAnnotation(EsDocument.class);
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse response = scroll(searchSourceBuilder, doc, scroll);
        long totalHits = response.getHits().getTotalHits();

        List<CompletableFuture<List<UserprofileTag>>> futureList = new ArrayList<>();
        while (response.getHits().getHits().length != 0) {
            SearchResponse resp = response;
            CompletableFuture<List<UserprofileTag>> future = CompletableFuture.supplyAsync(() -> getResult(resp), executorService);
            futureList.add(future);

            String scrollId = response.getScrollId();
            response = scroll(scroll, scrollId);
        }

        List<UserprofileTag> results = sequence(futureList).get().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        List<String> userIdList = results.stream().map(UserprofileTag::getUserid).collect(Collectors.toList());
        return new UserProfileRespVo(totalHits, userIdList);
    }

    private SearchResponse scroll(Scroll scroll, String scrollId) throws IOException {
        SearchResponse response;
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(scroll);
        response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        return response;
    }

    private SearchResponse scroll(SearchSourceBuilder searchSourceBuilder, EsDocument doc, Scroll scroll) throws IOException {
        SearchRequest searchRequest = new SearchRequest(doc.indexName());
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(scroll);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        return response;
    }

    private List<UserprofileTag> getResult(SearchResponse response) {
        List<UserprofileTag> esPersonList = Arrays.stream(response.getHits().getHits()).map(sc -> {
            UserprofileTag person = JSON.parseObject(sc.getSourceAsString(), UserprofileTag.class);
            return person;
        }).collect(Collectors.toList());
        return esPersonList;
    }

    private <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }


    private SearchSourceBuilder searchSourceBuilder(List<Condition> ruleList) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (Condition rule : ruleList) {
            TagOperator operator = rule.getOperator();
            if (operator.isRangeQuery()) {
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(CROWD_FIELD + rule.getTagCode());
                try {
                    Method method = RangeQueryBuilder.class.getMethod(operator.getMethodName(), Object.class);
                    ReflectionUtils.invokeMethod(method, rangeQuery, rule.getTagValue());
                    NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("userlabels", rangeQuery, ScoreMode.None);
                    queryBuilder.must(nestedQuery);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else if (operator == TagOperator.EQUAL) {
                TermQueryBuilder termQuery = QueryBuilders.termQuery(CROWD_FIELD + rule.getTagCode(), rule.getTagValue());
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("userlabels", termQuery, ScoreMode.None);
                queryBuilder.must(nestedQuery);
            } else if (operator == TagOperator.NOT_EQUAL) {
                TermQueryBuilder termQuery = QueryBuilders.termQuery(CROWD_FIELD + rule.getTagCode(), rule.getTagValue());
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("userlabels", termQuery, ScoreMode.None);
                queryBuilder.mustNot(nestedQuery);
            } else if (operator == TagOperator.CONTAINS) {
                WildcardQueryBuilder wildcardQuery = QueryBuilders.wildcardQuery(CROWD_FIELD + rule.getTagCode(), "*" + rule.getTagValue() + "*");
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("userlabels", wildcardQuery, ScoreMode.None);
                queryBuilder.must(nestedQuery);
            }
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(500);
        searchSourceBuilder.query(queryBuilder);

        return searchSourceBuilder;
    }

}
