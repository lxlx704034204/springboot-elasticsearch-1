package com.wfh;

import com.wfh.entity.Item;
import com.wfh.repository.ItemRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void index() {
        Item item = new Item(1L,"小米手机7", " 手机", "小米", 34666.00, "http://image.com/13123.jpg");
        itemRepository.save(item);
    }

    @Test
    public void testCreate(){
        // 创建索引，会根据Item类的@Document注解信息来创建
        elasticsearchTemplate.createIndex(Item.class);
        // 配置映射，会根据Item类中的id、Field等字段来自动完成映射
        elasticsearchTemplate.putMapping(Item.class);
    }

    @Test
    public void insertList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "11"));
        list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "22"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    /**
     * @Description:定义修改方法
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void update(){
        Item item = new Item(2L, "苹果XSMax", " 手机",
                "小米", 3499.00, "http://image.baidu.com/13123.jpg");
        itemRepository.save(item);
    }

    /**
     * @Description:定义查询方法,含对价格的降序、升序查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testQueryAll(){
        // 查找所有
        List<Long> ts = Arrays.asList(2L, 3L);
        Iterable<Item> all1 = itemRepository.findAllById(ts);//error resolve： https://github.com/lajihuancheng/elaseach
        for (Item item:all1){
            System.out.println(item);
        }
        // 对某字段排序查找所有 Sort.by("price").descending() 降序
        // Sort.by("price").ascending():升序
        /*Iterable<Item> list = itemRepository.findAll();

        for (Item item:list){
            System.out.println(item);
        }*/
    }
    /**
     * @Description:准备测试数据
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void insertList2() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.baidu.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }


    /**
     * @Description:按照价格区间查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void queryByPriceBetween(){
        List<Item> list = this.itemRepository.findByPriceBetween(2000.00, 3500.00);
        for (Item item : list) {
            System.out.println("item = " + item);
        }
    }

    /**
     * @Description:matchQuery底层采用的是词条匹配查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testMatchQuery(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米手机"));
        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("total = " + total);
        for (Item item : items) {
            System.out.println(item);
        }
    }

    /**
     *
     * @Description:matchQuery
     *@Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testMathQuery(){
        // 创建对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 在queryBuilder对象中自定义查询
        //matchQuery:底层就是使用的termQuery
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","坚果"));
        //查询，search 默认就是分页查找
        Page<Item> page = this.itemRepository.search(queryBuilder.build());
        //获取数据
        long totalElements = page.getTotalElements();
        System.out.println("获取的总条数:"+totalElements);

        for(Item item:page){
            System.out.println(item);
        }


    }


    /**
     * @Description:
     * termQuery:功能更强大，除了匹配字符串以外，还可以匹配
     * int/long/double/float/....
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testTermQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.termQuery("price",998.0));
        // 查找
        Page<Item> page = this.itemRepository.search(builder.build());

        for(Item item:page){
            System.out.println(item);
        }
    }
    /**
     * @Description:布尔查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testBooleanQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        builder.withQuery(
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("title","华为"))
                        .must(QueryBuilders.matchQuery("brand","华为"))
        );

        // 查找
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }
    }

    /**
     * @Description:模糊查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void testFuzzyQuery(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.fuzzyQuery("title","faceoooo"));
        Page<Item> page = this.itemRepository.search(builder.build());
        for(Item item:page){
            System.out.println(item);
        }

    }

    /**
     * @Description:分页查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void searchByPage(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));
        // 分页：
        int page = 0;
        int size = 2;
        //queryBuilder.withPageable(PageRequest.of(page,size));

        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);
        // 总页数
        System.out.println("总页数 = " + items.getTotalPages());
        // 当前页
        System.out.println("当前页：" + items.getNumber());
        // 每页大小
        System.out.println("每页大小：" + items.getSize());

        for (Item item : items) {
            System.out.println(item);
        }
    }

    /**
     * @Description:排序查询
     * @Author: https://blog.csdn.net/chen_2890
     */
    @Test
    public void searchAndSort(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));

        // 搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 总条数
        long total = items.getTotalElements();
        System.out.println("总条数 = " + total);

        for (Item item : items) {
            System.out.println(item);
        }
    }


    /**
     * @Description:按照品牌brand进行分组
     * @Author: https://blog.csdn.net/chen_2890
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
        //List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        /*for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }*/

    }


}