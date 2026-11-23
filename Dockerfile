FROM eclipse-temurin:17-jre
WORKDIR /app
COPY . /app
ENV PORT=8080 DATA_DIR=/app/data
EXPOSE 8080
CMD ["java", "-cp", "campus-library.jar:lib/*", "com.campuslibrary.Main"]
