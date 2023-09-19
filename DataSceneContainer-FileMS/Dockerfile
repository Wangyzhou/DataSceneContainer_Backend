#设置镜像基础，jdk8
FROM java:8
#维护人员信息
MAINTAINER WYZ
#设置镜像对外暴露端口
EXPOSE 8765
#将当前 target 目录下的 jar 放置在根目录下，命名为 app.jar，推荐使用绝对路径。
ADD target/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar /DataSceneContainer-FileMS-1.0-SNAPSHOT.jar
#执行启动命令
ENTRYPOINT ["java", "-jar", "-Dfile.encoding=UTF-8","/DataSceneContainer-FileMS-1.0-SNAPSHOT.jar"]