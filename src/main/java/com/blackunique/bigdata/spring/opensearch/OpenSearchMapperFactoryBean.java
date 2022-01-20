package com.blackunique.bigdata.spring.opensearch;

import club.kingon.sql.opensearch.DefaultOpenSearchSqlClient;
import club.kingon.sql.opensearch.OpenSearchSqlClient;
import club.kingon.sql.opensearch.SearchQueryModeEnum;
import club.kingon.sql.opensearch.api.Endpoint;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dragons
 * @date 2021/12/14 10:09
 */
public class OpenSearchMapperFactoryBean<T> implements FactoryBean<T>, EnvironmentAware {

    private Class<T> mapperInterface;

    private Environment environment;

    private String dataSourceConfigPrefix;

    private static final Map<String, OpenSearchSqlClient> openSearchSqlClientMap = new ConcurrentHashMap<>();

    public OpenSearchMapperFactoryBean() {
        // intentionally empty
    }

    public OpenSearchMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        OpenSearchSqlClient client = getOpenSearchSqlClient();
        T bean = (T) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{mapperInterface}, new OpenSearchBaseMapperJdkProxyHandler(mapperInterface, client)
        );
        return bean;
    }

    private OpenSearchSqlClient getOpenSearchSqlClient() {
        String accessKey = Objects.requireNonNull(environment.getProperty(dataSourceConfigPrefix + ".access-key", environment.getProperty(dataSourceConfigPrefix + ".accessKey")));
        String secret = Objects.requireNonNull(environment.getProperty(dataSourceConfigPrefix + ".secret"));
        Endpoint endpoint = Endpoint.valueOf(environment.getProperty(dataSourceConfigPrefix + ".endpoint", "SHENZHEN"));
        String appName = environment.getProperty(dataSourceConfigPrefix + ".app-name", environment.getProperty(dataSourceConfigPrefix + ".appName"));
        boolean intranet = Boolean.parseBoolean(environment.getProperty(dataSourceConfigPrefix + ".intranet", "false"));
        int connectTimeout = Integer.parseInt(environment.getProperty(dataSourceConfigPrefix + ".connect-timeout", "10000"));
        int readTimeout = Integer.parseInt(environment.getProperty(dataSourceConfigPrefix + ".read-timeout", "5000"));
        int startWaitMills = Integer.parseInt(environment.getProperty(dataSourceConfigPrefix + ".start-wait-mills", environment.getProperty(dataSourceConfigPrefix + ".startWithMills", "2000")));
        boolean enableManagement = Boolean.parseBoolean(environment.getProperty(dataSourceConfigPrefix + ".enable-management", environment.getProperty(dataSourceConfigPrefix +".enableManagement", "true")));
        SearchQueryModeEnum mode = SearchQueryModeEnum.valueOf(environment.getProperty(dataSourceConfigPrefix + ".default-search-mode", environment.getProperty(dataSourceConfigPrefix + ".defaultSearchMode", "HIT")));

        String key = Base64.getEncoder().encodeToString((secret + accessKey + secret).getBytes()) + endpoint + appName + intranet + connectTimeout + readTimeout;
        OpenSearchSqlClient client = openSearchSqlClientMap.get(key);
        if (client == null) {
            synchronized (openSearchSqlClientMap) {
                client = openSearchSqlClientMap.get(key);
                if (client == null) {
                    client = new DefaultOpenSearchSqlClient(accessKey, secret, endpoint, intranet, appName, startWaitMills, enableManagement, connectTimeout, readTimeout, mode);
                    openSearchSqlClientMap.put(key, client);
                }
            }
        }
        return client;
    }


    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public void setDataSourceConfigPrefix(String dataSourceConfigPrefix) {
        this.dataSourceConfigPrefix = dataSourceConfigPrefix;
    }


    public String getDataSourceConfigPrefix() {
        return dataSourceConfigPrefix;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
