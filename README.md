
# 给力详解： https://blog.csdn.net/chen_2890/article/details/83895646    SpringBoot整合Elasticsearch

# 其他详解： https://blog.csdn.net/li521wang/article/details/83792552#3_97

# git地址
# https://github.com/wfh123456/springboot-elasticsearch.git
# https://github.com/lxlx704034204/springboot-elasticsearch-1

# keyword: blog.csdn.net/chen_2890 new Item(1L, "苹果XSMax", " 手机"
#参考推荐 https://github.com/lajihuancheng/elaseach


排错：
    https://blog.csdn.net/HcJsJqJSSM/article/details/83686997  高版本的是post json请求格式


SpringBoot整合Elasticsearch   
    https://www.jianshu.com/p/4467cfe4e651      Windows 下安装 ElasticSearch & ElasticSearch head
    https://blog.csdn.net/chen_2890/article/details/83757022    Elasticsearch环境搭建和介绍（Windows） 
    https://blog.52itstyle.vip/archives/2339/#IKAnalysisforElasticsearch    JavaWeb项目架构之Elasticsearch日志处理系统
    
    
# curl模拟post-json请求    
    curl http://www.jianshu.com/login   -X POST -H "Content-Type:application/json" -d "{title=comewords&content=articleContent}"
    curl http://localhost:9200/_analyze?pretty -X POST -H "Content-Type:application/json" -d '{"analyzer":"ik_smart","text":"我爱你中国"}'
    curl -H "Content-Type:application/json" -X POST -d '{"analyzer":"ik_smart","text":"我爱你中国"}' http://localhost:9200/_analyze?pretty
    curl -H "Content-Type: application/json" -XPOST "localhost:9200/how2java/product/_bulk?refresh" --data-binary "@products.json"