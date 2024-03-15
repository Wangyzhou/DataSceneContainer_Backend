#设置镜像基础，jdk8
FROM java:8

MAINTAINER wyz980903@163.com

EXPOSE 9400

ADD DataSceneContainer-Auth-Central-1.0-SNAPSHOT.jar /DataSceneContainer-Auth-Central-1.0-SNAPSHOT.jar

COPY ninja.jks /JKS_PATH/ninja.jks

ENV JKS_PATH="/JKS_PATH/ninja.jks"

ENTRYPOINT ["java", "-jar", "-Duser.timezone=GMT+08","-Dfile.encoding=UTF-8","-Dspring.profiles.active=prod","/DataSceneContainer-Auth-Central-1.0-SNAPSHOT.jar"]