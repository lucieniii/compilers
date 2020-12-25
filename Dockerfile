FROM openjdk:14
WORKDIR /c0
COPY ./* ./
RUN javac src/main/java/c0/Main.java
