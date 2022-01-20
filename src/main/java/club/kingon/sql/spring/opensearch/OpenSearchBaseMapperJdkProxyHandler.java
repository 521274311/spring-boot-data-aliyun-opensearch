package club.kingon.sql.spring.opensearch;

import club.kingon.sql.opensearch.OpenSearchSqlClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author dragons
 * @date 2021/12/30 19:37
 */
public class OpenSearchBaseMapperJdkProxyHandler implements InvocationHandler {

    private final OpenSearchBaseMapperHandler handler;

    public OpenSearchBaseMapperJdkProxyHandler(Class<?> mapperInterface, OpenSearchSqlClient client) {
        Class<?> beanClass = getActualTypeArguments0(mapperInterface);
        if (beanClass == null) {
            // todo
            throw new RuntimeException("");
        }
        handler = new OpenSearchBaseMapperHandler(mapperInterface, beanClass, client);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = handler.handle(method, args);
        if (result == null || method.getReturnType() == Void.TYPE) {
            result = Void.class;
        }
        return result;
    }

    private Class<?> getActualTypeArguments0(Class<?> mapperInterface) {
        Class<?> clazz = mapperInterface;
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
            }
        }
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        }
        return null;
    }
}
