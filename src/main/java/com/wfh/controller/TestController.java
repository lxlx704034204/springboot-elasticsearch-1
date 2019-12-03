package com.wfh.controller;

import com.wfh.entity.Item;
import com.wfh.repository.ItemRepository;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.List;


/**
 * 可以根据类的信息自动生成，也可以手动指定indexName和Settings
 *
 *
 *
 *
 */
public class TestController {


    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;
    @Test
    public void createIndex(){
        elasticsearchTemplate.createIndex(Item.class);
    }
    @Test
    public void deleteIndex(){
        elasticsearchTemplate.deleteIndex(Item.class);
    }

    /**
     *  AggregationBuilders：聚合的构建工厂类。所有聚合都由这个类来构建，看看他的静态方法：
     *
     *    （1）统计某个字段的数量
     *       ValueCountBuilder vcb=  AggregationBuilders.count("count_uid").field("uid");
     *     （2）去重统计某个字段的数量（有少量误差）
     *      CardinalityBuilder cb= AggregationBuilders.cardinality("distinct_count_uid").field("uid");
     *     （3）聚合过滤
     *     FilterAggregationBuilder fab= AggregationBuilders.filter("uid_filter").filter(QueryBuilders.queryStringQuery("uid:001"));
     *     （4）按某个字段分组
     *     TermsBuilder tb=  AggregationBuilders.terms("group_name").field("name");
     *     （5）求和
     *     SumBuilder  sumBuilder=	AggregationBuilders.sum("sum_price").field("price");
     *     （6）求平均
     *     AvgBuilder ab= AggregationBuilders.avg("avg_price").field("price");
     *     （7）求最大值
     *     MaxBuilder mb= AggregationBuilders.max("max_price").field("price");
     *     （8）求最小值
     *     MinBuilder min=	AggregationBuilders.min("min_price").field("price");
     *     （9）按日期间隔分组
     *     DateHistogramBuilder dhb= AggregationBuilders.dateHistogram("dh").field("date");
     *     （10）获取聚合里面的结果
     *     TopHitsBuilder thb=  AggregationBuilders.topHits("top_result");
     *     （11）嵌套的聚合
     *     NestedBuilder nb= AggregationBuilders.nested("negsted_path").path("quests");
     *     （12）反转嵌套
     *     AggregationBuilders.reverseNested("res_negsted").path苹果XSMax("kps ");
     *
     *   AggregatedPage：聚合查询的结果类。它是Page<T>的子接口：
     *
     *
     */
    @Test
    public void testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand"));
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }

    }

    @Test
    public void testSubAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand")
                        .subAggregation(AggregationBuilders.avg("priceAvg").field("price")) // 在品牌聚合桶内进行嵌套聚合，求平均值
        );
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称  3.5、获取桶中的文档数量
            System.out.println(bucket.getKeyAsString() + "，共" + bucket.getDocCount() + "台");

            // 3.6.获取子聚合结果：
            InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
            System.out.println("平均售价：" + avg.getValue());
        }

    }

}
