package club.kingon.sql.spring.opensearch.filter;

import club.kingon.sql.spring.opensearch.OpenSearchBaseMapper;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

import java.util.Arrays;

/**
 * @author dragons
 * @date 2022/1/12 11:09
 */
public class OpenSearchBaseMapperTypeFilter extends AbstractClassTestingTypeFilter {

    private final String className;

    public OpenSearchBaseMapperTypeFilter() {
        className = OpenSearchBaseMapper.class.getName();
    }

    @Override
    protected boolean match(ClassMetadata classMetadata) {
        if (classMetadata.isInterface() && Arrays.asList(classMetadata.getInterfaceNames()).contains(className)) {
            return true;
        }
        return false;
    }
}
