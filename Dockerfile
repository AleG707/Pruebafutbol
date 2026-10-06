FROM eclipse-temurin:17
COPY "./target/CRUD-clubes-apirest-1.jar" "app.jar"
EXPOSE "8098"
ENTRYPOINT ["java","-jar","app.jar"]