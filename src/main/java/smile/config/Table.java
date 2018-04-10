package smile.config;

import java.lang.annotation.*;

/**
 * @Package: smile.config
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/22 下午5:33
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    String name();
}
