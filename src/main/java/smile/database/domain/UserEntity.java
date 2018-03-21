package smile.database.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Package: smile.database.domain
 * @Description:
 * @author: liuxin
 * @date: 2018/3/19 下午10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEntity{
    /**
     * 用户id
     */
    private String uid;
    /**
     * 性别
     */
    private String gender;
    /**
     * 要有校验规则
     * 昵称
     */
    private String name;
    /**
     * 用户token
     */
    private String accessToken;

    /**
     * 用户头像
     */
    private String iconurl;
}
