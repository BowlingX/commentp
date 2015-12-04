# Base DockerImage for commentp

FROM phusion/baseimage:0.9.17
MAINTAINER David Heidrich (me@bowlingx.com)

ENV COMMENTP_USER commentp

# install java
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# add jobmatic user
RUN adduser --disabled-password --gecos '' $COMMENTP_USER

# enable remote logging
RUN echo "\$ModLoad imudp" >> /etc/rsyslog.conf
RUN echo "\$UDPServerRun 514" >> /etc/rsyslog.conf
