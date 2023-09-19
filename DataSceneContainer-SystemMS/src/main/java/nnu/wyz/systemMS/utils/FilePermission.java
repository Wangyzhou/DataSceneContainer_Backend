package nnu.wyz.systemMS.utils;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 21:19
 */

public class FilePermission {
    public static final int ALLOW_PREVIEW = 1;//0000 0001
    public static final int ALLOW_DOWNLOAD = 1 << 1;//0000 0010
    public static final int ALLOW_PUBLISH = 1 << 2;//0000 0100
    public static final int ALLOW_SHARE = 1 << 3;//0000 1000
    public static final int ALLOW_EDIT = 1 << 4;//0001 0000

    //目前的权限
    private int flag;
    //设置用户权限
    public void setPer(int per){
        flag = per;
    }
    public int getPer() {
        return this.flag;
    }
    //增加用户权限，一个或多个
    public void addPer(int per){
        flag = flag | per;
    }
    //删除用户某个权限
    public void deletePer(int per){
        flag = flag&~per;
    }
    //判断用户权限
    public boolean isAllow(int per){
        return (flag&per) == per;
    }
    //判断用户没有的权限
    public boolean isNotAllow(int per){
        return (flag&per) == 0;
    }
}
