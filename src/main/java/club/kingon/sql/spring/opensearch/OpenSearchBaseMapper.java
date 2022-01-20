package club.kingon.sql.spring.opensearch;


import club.kingon.sql.builder.SQLBuilder;
import club.kingon.sql.builder.SelectSQLBuilder;
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
    List<T> selectList(@Nullable SQLBuilder builder, @Nullable SelectSQLBuilder selectSqlBuilder, @Nullable Distinct[] distinctSet);

    @NonNull
    List<T> selectList(@Nullable SQLBuilder builder, @Nullable Distinct[] distinctSet);

    @NonNull
    List<Group> selectGroup(@Nullable SQLBuilder builder);

    @NonNull
    List<Group> selectGroup(@Nullable SQLBuilder builder, @Nullable SelectSQLBuilder selectSqlBuilder);

    @NonNull
    List<Group> selectGroup(@Nullable SQLBuilder sqlBuilder, @Nullable SelectSQLBuilder selectSqlBuilder, @Nullable Aggregate[] aggregateSet);

    @NonNull
    List<Group> selectGroup(@Nullable SQLBuilder builder, @Nullable SelectSQLBuilder selectSqlBuilder, @Nullable Distinct[] distinctSet, @Nullable Aggregate[] aggregateSet);
}
