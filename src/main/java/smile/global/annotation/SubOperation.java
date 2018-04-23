package smile.global.annotation;

import smile.protocol.impl.UserDatagram;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Package: smile.global.annotation
 * @Description: 副操作
 * @author: mac
 * @date: 2018/4/16 下午4:19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubOperation {
    byte[] sub();
    Class model() default UserDatagram.class;
}
