# 数据场景容器平台

**后端架构**

![img.png](img.png)

## 系统部署

为了追求一键化部署，减少与服务器的交互，系统采用IDEA的Docker插件进行部署，若不用插件，也可自行上传服务器进行部署 

后端整体分为三个部分：java服务、中间件、数据库




## 网关服务部署

**1、编写Dockerfile**
```dockerfile
FROM java:8

MAINTAINER wyz980903@163.com

EXPOSE 9527

ADD target/DataSceneContainer-Gateway-1.0-SNAPSHOT.jar /DataSceneContainer-Gateway-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=GMT+8", "-Dfile.encoding=UTF-8","/DataSceneContainer-Gateway-1.0-SNAPSHOT.jar"]
```
**2、maven-docker插件构建镜像**

```xml
    <build>
        <directory>${project.basedir}/target</directory>
        <finalName>DataSceneContainer-Gateway-1.0-SNAPSHOT</finalName>
        <plugins>
            <!--maven打包插件-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>2.6.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!--使用docker-maven-plugin插件-->
            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>1.2.2</version>
                <configuration>
                    <!--指定生成的镜像名-->
                    <imageName>dsc-gateway</imageName>
                    <!--指定标签 这里指定的是镜像的版本，我们默认版本是latest-->
                    <imageTags>
                        <imageTag>latest</imageTag>
                    </imageTags>
                    <!-- 指定我们项目中Dockerfile文件的路径-->
                    <dockerDirectory>${project.basedir}</dockerDirectory>

                    <!--指定远程docker 地址-->
                    <dockerHost>http://172.21.213.86:2375</dockerHost>

                    <!-- 这里是复制 jar 包到 docker 容器指定目录配置 -->
                    <resources>
                        <resource>
                            <targetPath>/</targetPath>
                            <!--jar包所在的路径  此处配置的即对应项目中target目录-->
                            <directory>${project.build.directory}</directory>
                            <!-- 需要包含的 jar包 ，这里对应的是 Dockerfile中添加的文件名　-->
                            <include>${project.build.finalName}.jar</include>
                        </resource>
                    </resources>
                </configuration>
            </plugin>

        </plugins>
    </build>
```
**或上传至服务器使用docker命令构建镜像**
```linux
sudo docker built -t dsc-gateway .
```

**3、创建容器，运行该服务**

```linux
sudo docker run --name dsc-gateway -p 9527:9527 -d dsc-gateway
```
## 文件发布服务部署

### 1、首次Docker部署

1)、打包docker镜像，并创建容器\
\
**构建镜像(与网关类似)**
```dockerfile
#设置镜像基础，jdk8
FROM java:8
#维护人员信息
MAINTAINER WYZ
#设置镜像对外暴露端口
EXPOSE 8765
#将当前 target 目录下的 jar 放置在根目录下，命名为 app.jar，推荐使用绝对路径。
ADD target/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar /DataSceneContainer-FileMS-1.0-SNAPSHOT.jar
#执行启动命令
ENTRYPOINT ["java", "-jar", "-Duser.timezone=GMT+8", "-Dfile.encoding=UTF-8","/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar"]
```
**创建容器**
```linux
sudo docker run \
--name dsc-filems -d \
-p 8765:8765 \
-v /home/yzwang/dsc-minio/data:/home/minio-data \
dsc-filems
``` 
\
2)、配置apt国内镜像加速
```linux
#1.进入容器
docker exec -it <容器名称/id> bash
#2.执行命令apt-get update 发现缓慢
#3.进入apt-get 配置目录
cd /ect/apt
#4.执行备份命令 --避免修改失败无法使用
cp sources.list sources.list.bak

#5.同时执行echo下的4行命令，修改成国内镜像源
echo "">sources.list
echo "deb http://ftp2.cn.debian.org/debian/ buster main">>sources.list
echo "deb http://ftp2.cn.debian.org/debian/debian-security buster/updates main">>sources.list
echo "deb http://ftp2.cn.debian.org/debian/debian buster-updates main">>sources.list

#6.查看文件 是否修改成功
cat sources.list
###############################start
root@62be94cc90e7:/etc/apt# cat sources.list

deb http://ftp2.cn.debian.org/debian/ buster main
deb http://ftp2.cn.debian.org/debian/debian-security buster/updates main
deb http://ftp2.cn.debian.org/debian/debian buster-updates main
root@62be94cc90e7:/etc/apt#
###############################end
#7.更新apt-get
apt-get update
#修改完成
#如果发现安装vim 缓慢或者失败，说明本教程不适合你
apt-get install vim
#将备份完成的文件改回来，即可。
rm sources.list
mv sources.list.bak sources.list
```

3)、在容器内使用apt下载postgis、postgresql-client
```linux
apt-get install postgis

apt-get install postgresql-client
```

### 2、更新部署(不改变容器)

```linux
 # 复制jar包，覆盖容器内源文件
 sudo docker cp /home/yzwang/dsc-fileS/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar 80e9bcf5825a:/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar

 # 重启容器
 sudo docker restart 80e9bcf5825a
```

## 认证中心部署

使用了非对称加密，jar包在docker容器中读不到jks证书，故该服务未使用docker部署

```linux
nohup java -jar \
 -Duser.timezone=GMT+08 \
 -Dfile.encoding=UTF-8 \
 /home/yzwang/dsc-auth-central/DataSceneContainer-Auth-Central-1.0-SNAPSHOT.jar \
 >/home/yzwang/dsc-auth-central/log.txt &
 
注:  nohup命令的作用就是让程序在后台运行，不用担心关闭连接进程断掉的问题
```

后期更新...
