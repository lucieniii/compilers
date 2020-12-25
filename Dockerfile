FROM openjdk:14
WORKDIR /c0
COPY ./* ./
RUN pwd
RUN ls
RUN javac src/main/java/c0/Main.java
