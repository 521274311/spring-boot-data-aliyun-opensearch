package com.blackunique.bigdata.spring.opensearch;

import club.kingon.sql.builder.SelectSqlBuilder;
import club.kingon.sql.builder.SqlBuilder;
import club.kingon.sql.builder.WhereSqlBuilder;
import club.kingon.sql.builder.spring.annotation.Delete;
import club.kingon.sql.builder.spring.annotation.Insert;
import club.kingon.sql.builder.spring.annotation.Select;
import club.kingon.sql.builder.spring.annotation.Update;
import club.kingon.sql.builder.spring.util.SqlUtils;
import club.kingon.sql.opensearch.OpenSearchQueryIterator;
import club.kingon.sql.opensearch.OpenSearchSqlClient;
import com.alibaba.fastjson.JSON;
import com.aliyun.opensearch.sdk.generated.search.Aggregate;
import com.aliyun.opensearch.sdk.generated.search.Distinct;
import com.aliyun.opensearch.sdk.generated.search.general.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dragons
 * @date 2021/12/30 19:40
 */
public class OpenSearchBaseMapperHandler<T> {

    private final static Logger log = LoggerFactory.getLogger(OpenSearchBaseMapperHandler.class);

    private final Class<T> beanClass;

    private final OpenSearchSqlClient client;

    private final String prefixSql;

    public OpenSearchBaseMapperHandler(Class<?> mapperInterface, Class<T> beanClass, OpenSearchSqlClient client) {
        this.beanClass = beanClass;
        this.client = client;
        this.prefixSql = SqlBuilder.select(beanClass).from(beanClass).build();
    }

    public Object handle(Method method, Object[] args) {
        Object result = handleAnnotation(method, args);
        if (result == null) {
            result = handleMethod(method, args);
        }
        return result;
    }


    private Object handleMethod(Method method, Object[] args) {
        String methodLowerCaseName = method.getName().toUpperCase();
        if (methodLowerCaseName.startsWith(InnerConstants.SELECT)) {
            return handleSelectMethod(method, args);
        } else if (methodLowerCaseName.startsWith(InnerConstants.INSERT)) {
            return handleInsertMethod(method, args);
        } else if (methodLowerCaseName.startsWith(InnerConstants.UPDATE)) {
            return handleUpdateMethod(method, args);
        } else if (methodLowerCaseName.startsWith(InnerConstants.DELETE)) {
            return handleDeleteMethod(method, args);
        }
        return null;
    }

    private Object handleDeleteMethod(Method method, Object[] args) {
        if (args == null || args.length == 0) throw new RuntimeException("There must be at least one delete method parameter.");
        return handleDeleteSql(method, SqlBuilder.delete(beanClass).where((WhereSqlBuilder) args[0]).build());
    }

    private Object handleUpdateMethod(Method method, Object[] args) {
        if (args == null || args.length == 0) throw new RuntimeException("There must be at least one update method parameter.");
        WhereSqlBuilder sqlBuilder = SqlBuilder.update(args[0]);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                if  (args[i] instanceof WhereSqlBuilder) {
                    sqlBuilder = sqlBuilder.and((WhereSqlBuilder) args[i]);
                }
            }
        }
        return handleUpdateSql(method, sqlBuilder.build());
    }

    private Object handleInsertMethod(Method method, Object[] args) {
        if (args == null || args.length == 0) throw new RuntimeException("There must be at least one insert method parameter.");
        return handleInsertSql(method, SqlBuilder.insertInto(args).build());
    }

    private Object handleSelectMethod(Method method, Object[] args) {
        SqlBuilder extensionSqlBuilder = null;
        Set<Distinct> distinctSet = null;
        Set<Aggregate> aggregateSet = null;
        SelectSqlBuilder selectSqlBuilder = null;
        List<Object> remainArgs = new ArrayList<>();
        if (args != null && args.length > 0) {
            for(Object arg : args) {
                if (arg instanceof SelectSqlBuilder) {
                    selectSqlBuilder = (SelectSqlBuilder) arg;
                }
                else if (arg instanceof SqlBuilder && extensionSqlBuilder == null) {
                    extensionSqlBuilder = (SqlBuilder) arg;
                } else if (arg instanceof Distinct[]) {
                    distinctSet = Arrays.stream((Distinct[])arg).collect(Collectors.toSet());
                } else if (arg instanceof Aggregate[]) {
                    aggregateSet = Arrays.stream((Aggregate[])arg).collect(Collectors.toSet());
                } else {
                    remainArgs.add(arg);
                }
            }
        }
        String sql;
        if (extensionSqlBuilder != null) {
            String sqlSuffix = extensionSqlBuilder.build();
            if (selectSqlBuilder != null) {
                sql = selectSqlBuilder.from(beanClass).build() + extensionSqlBuilder.build();
            } else if (sqlSuffix.toUpperCase().contains(InnerConstants.FROM)) {
                sql = sqlSuffix;
            } else {
                sql = prefixSql + extensionSqlBuilder.build();
            }
        } else {
            sql = prefixSql;
        }
        return handleSelectSql(method,  sql, remainArgs.toArray(new Object[0]), distinctSet, aggregateSet);
    }

    private Object handleAnnotation(Method method, Object[] args) {
        Insert insert = method.getAnnotation(Insert.class);
        Update update = method.getAnnotation(Update.class);
        Select select = method.getAnnotation(Select.class);
        Delete delete = method.getAnnotation(Delete.class);
        if (insert != null) {
            client.insert(insert.value());
            return 1;
        } else if (update != null) {
            client.update(update.value());
            return 1;
        } else if (delete != null) {
            client.delete(delete.value());
            return 1;
        } else if (select != null){
            return handleSelectSql(method, select.value(), args, null, null);
        }
        return null;
    }

    private Object handleInsertSql(Method method, String sql) {
        // todo
        client.insert(sql);
        return 1;
    }

    private Object handleUpdateSql(Method method, String sql) {
        // todo
        client.update(sql);
        return 1;
    }

    private Object handleDeleteSql(Method method, String sql) {
        // todo
        client.delete(sql);
        return 1;
    }

    private Object handleSelectSql(Method method, String sql, Object[] args, Set<Distinct> distinctSet, Set<Aggregate> aggregateSet) {
        Class<?> returnType = method.getReturnType();

        // parse
        sql = SqlUtils.parseSql(sql, method, args);
        List result = new ArrayList<>();
        OpenSearchQueryIterator it = client.query(sql, distinctSet, aggregateSet);
        while (it.hasNext()) {
            SearchResult searchResult = it.next();
            if (searchResult == null) {
                log.warn("Part of the \"SearchResult\" is null.");
                continue;
            }
            try {
                ExtensionOpenSearchQueryResult queryResult = JSON.parseObject(searchResult.getResult(), ExtensionOpenSearchQueryResult.class);
                if ("OK".equals(queryResult.getStatus())) {
                    if (!CollectionUtils.isEmpty(aggregateSet) || sql.contains(InnerConstants.GROUP) || sql.contains(InnerConstants.GROUP_LOWER)) {
                        result.addAll(queryResult.getResult().getFacet());
                    } else {
                        result.addAll(queryResult.getResult().getItems().stream().map(e -> JSON.toJavaObject(e.getFields(), beanClass)).collect(Collectors.toList()));
                    }
                } else {
                    log.warn("Part of the \"SearchResult\" status is \"" + queryResult.getStatus() + "\", ignore. Errors:" + queryResult.getErrors());
                }
            } catch (Exception e) {
                log.warn("Part of the \"SearchResult\" is abnormal, ignore. Exception: " + e.getMessage());
            }
        }

        if (returnType == beanClass) {
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } else if (Set.class.isAssignableFrom(returnType)) {
            // todo
            return new HashSet<>(result);
        }
        return result;
    }
}
