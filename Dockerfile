FROM openjdk:14
WORKDIR /c0-temp/
COPY ./* ./
RUN javac Main.java
