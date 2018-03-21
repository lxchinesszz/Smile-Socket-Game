package smile.service.home;

import smile.tool.StdRandom;

public class HomeGeneratorImpl implements HomeGenerator {
    @Override
    public Home createHome(HomeInfo homeType) {
        return new Home(homeType,homeKey());
    }


    @Override
    public Integer homeKey() {
        return generate6BitInt();
    }

    private static int generate6BitInt() {
        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        // 将数组随机打乱，据算法原理可知：
        // 重复概率 = 1/10 * 1/9 * 1/8 * 1/7 * 1/6 * 1/5 * 1/4 * 1/3 * 1/2 * 1/1 = 1/3628800，
        // 即重复概率为三百多万分之一，满足要求。
        for(int num = 10; num > 1; --num) {
            int idx = StdRandom.uniform(num);
            int temp = arr[idx];
            arr[idx] = arr[num - 1];
            arr[num - 1] = temp;
        }
        // 第一个元素不能为0，否则位数不够
        if(0 == arr[0]) {
            int ndx = StdRandom.uniform(10);
            arr[0] = arr[ndx];
            arr[ndx] = 0;
        }
        // 将数组前六位转化为整数
        int rs = 0;
        for(int idx = 0; idx < 6; ++idx) {
            rs = rs * 10 + arr[idx];
        }
        return rs;
    }

}
