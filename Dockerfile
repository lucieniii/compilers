FROM openjdk:12
WORKDIR /c0-temp/
COPY ./* ./
RUN javac Main.java