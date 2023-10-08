FROM java:8

MAINTAINER wyz980903@163.com

EXPOSE 9527

ADD target/DataSceneContainer-Gateway-1.0-SNAPSHOT.jar /DataSceneContainer-Gateway-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=GMT+8", "-Dfile.encoding=UTF-8","/DataSceneContainer-Gateway-1.0-SNAPSHOT.jar"]