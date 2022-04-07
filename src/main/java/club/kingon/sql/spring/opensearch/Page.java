package club.kingon.sql.spring.opensearch;

import club.kingon.sql.opensearch.entry.Error;

import java.util.List;

/**
 * @author dragons
 * @date 2022/4/7 17:40
 */
public class Page<T> extends club.kingon.sql.builder.spring.Page<T> {

    private List<Error> errors;

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public Page(long current, long size) {
        super(current, size);
    }

    public Page(long current, long size, long total) {
        super(current, size, total);
    }

    public static <T> club.kingon.sql.builder.spring.Page<T> of(long current, long size) {
        return of(current, size, 0L);
    }

    public static <T> club.kingon.sql.builder.spring.Page<T> of(long current, long size, long total) {
        return new club.kingon.sql.builder.spring.Page(current, size, total);
    }
}
