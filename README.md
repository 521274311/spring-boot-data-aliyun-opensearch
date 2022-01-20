## 阿里云OpenSearchSql与SpringBoot集成


### 将aliyun-opensearch-sql包、sql-builder包与spring集成, 开箱即用(目前仅实现查询功能, 其他功能正在开发中)
### PS: 查询的SqlBuilder参数必须通过QuerySqlBuilder.INSTANCE来构建, SelectSqlBuilder 必须通过SqlBuilder.select(...)构建

##### 使用示例
1. 配置文件 application.yml
```yaml
spring:
  datasource:
    # 阿里云ack
    access-key: xxx
    # 阿里云secret
    secret: xxx
    # OpenSearch区域, Endpoint enum
    endpoint: SHENZHEN
    # 应用名称, 非必填, 填上可获得sql优化
    app-name: bd_tbk_price_comparison_search
    # 是否内网
    intranet: false
    # 连接超时
    connect-timeout: 10000
    # 读取超时
    read-timeout: 5000
```
2.Application类 添加 Enable 注解

```java
import EnableOpenSearchMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableOpenSearchMapper
public class XXXApplication {
    public static void main(String[] args) {
        SpringApplication.run(XXXApplication.class, args);
    }
} 
```
3.编写实体类
```java
import lombok.Data;
import club.kingon.sql.builder.annotation.Table;

@Data
@Table("xxx")
public class Item {

    private String category;

    private String goodsId;

    private String goodsName;
}
```
4.编写dao层接口

```java
import club.kingon.sql.builder.spring.annotation.Mapper;
import OpenSearchBaseMapper;
import com.example.entity.Item;

// 若接口实现了OpenSearchBaseMapper接口可不加@Mapper注解, 否则必须加上@Mapper注解
@Mapper
public interface ItemMapper extends OpenSearchBaseMapper<Item> {
}
```
5.编写控制器测试

```java
import club.kingon.sql.builder.SqlBuilder;
import club.kingon.sql.builder.enums.Operator;
import club.kingon.sql.builder.spring.QuerySqlBuilder;
import com.example.dao.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Autowired
    private ItemMapper mapper;

    @GetMapping("/xxx")
    public Object items() {
        return mapper.selectGroup(
            QuerySqlBuilder.INSTANCE
                .where("goods_id", Operator.IN, (1111, 2222, 3333 , 4444))
        );
    }
}
```
