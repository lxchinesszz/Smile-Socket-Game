package smile.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定副号
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public  @interface ActionMapping {
    int sub();
}