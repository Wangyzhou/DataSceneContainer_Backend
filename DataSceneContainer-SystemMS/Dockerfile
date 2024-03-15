FROM java:8

MAINTAINER wyz980903@163.com

EXPOSE 7890

RUN apt-get update && apt-get install -y \
    postgis \
    postgresql-client

ADD DataSceneContainer-SystemMS-1.0-SNAPSHOT.jar /DataSceneContainer-SystemMS-1.0-SNAPSHOT.jar


ENTRYPOINT ["java", "-jar", "-Duser.timezone=GMT+8", "-Dfile.encoding=UTF-8","-Dspring.profiles.active=prod","/DataSceneContainer-SystemMS-1.0-SNAPSHOT.jar"]