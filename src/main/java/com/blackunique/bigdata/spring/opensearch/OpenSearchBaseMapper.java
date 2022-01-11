package com.blackunique.bigdata.spring.opensearch;

import club.kingon.sql.builder.SelectSqlBuilder;
import club.kingon.sql.builder.SqlBuilder;
import club.kingon.sql.builder.entry.Alias;
import club.kingon.sql.builder.spring.BaseMapper;
import club.kingon.sql.opensearch.entry.Group;
import com.aliyun.opensearch.sdk.generated.search.Aggregate;
import com.aliyun.opensearch.sdk.generated.search.Distinct;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author dragons
 * @date 2021/12/30 20:50
 */
public interface OpenSearchBaseMapper<T> extends BaseMapper<T> {

    @NonNull
    List<T> selectList(@Nullable SqlBuilder builder, @Nullable SelectSqlBuilder selectSqlBuilder, @Nullable Distinct[] distinctSet);

    @NonNull
    List<T> selectList(@Nullable SqlBuilder builder, @Nullable Distinct[] distinctSet);

    @NonNull
    List<Group> selectGroup(@Nullable SqlBuilder builder);

    @NonNull
    List<Group> selectGroup(@Nullable SqlBuilder builder, @Nullable SelectSqlBuilder selectSqlBuilder);

    @NonNull
    List<Group> selectGroup(@Nullable SqlBuilder sqlBuilder, @Nullable SelectSqlBuilder selectSqlBuilder, @Nullable Aggregate[] aggregateSet);

    @NonNull
    List<Group> selectGroup(@Nullable SqlBuilder builder, @Nullable SelectSqlBuilder selectSqlBuilder, @Nullable Distinct[] distinctSet, @Nullable Aggregate[] aggregateSet);
}
