package com.blackunique.bigdata.spring.opensearch.annotation;

import com.blackunique.bigdata.spring.opensearch.MapperScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author dragons
 * @date 2021/12/13 12:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Import(MapperScannerRegistrar.class)
@Documented
public @interface EnableOpenSearchMapper {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    /**
     * @return 返回配置前缀
     * 数据源配置项
     * 必填项:
     * ${dataSourceConfigPrefix}.access-key: ack配置
     * ${dataSourceConfigPrefix}.secret: secret配置
     * 可选项
     * ${dataSourceConfigPrefix}.endpoint: 地域配置, 默认: SHENZHEN
     * @see club.kingon.sql.opensearch.api.Endpoint 地域配置项
     * ${dataSourceConfigPrefix}.app-name: 应用名称(若配置则在查询该应用时将会优化查询)
     * ${dataSourceConfigPrefix}.intranet: 是否内网请求, 默认: false
     * ${dataSourceConfigPrefix}.connect-timeout: 连接超时时间,单位ms,默认10000
     * ${dataSourceConfigPrefix}.read-timeout: 读取超时时间, 单位ms,默认5000
     * ${dataSourceConfigPrefix}.start-wait-mills: 启动等待时间, 单位ms,默认2000(若开启management管理并且配置app-name的情况下, 管理线程会异步拉取应用相关信息, 信息加载完成之前若调用sql将不会对sql进行优化, 因此需要添加等待时间等待应用信息拉取完成)
     * ${dataSourceConfigPrefix}.enable-management: 开启应用管理状态, 默认true
     * ${dataSourceConfigPrefix}.default-search-mode: 默认搜索模式,值域[HIT, SCROLL], 若设置为hit将不在支持scroll滚动查询，未设置limit将默认limit 10
     */
    String dataSourceConfigPrefix() default "spring.datasource";
}
