rm -rf target
mvn clean package
docker build -t docker.tandilserver.com/web-poker/orchestrator .
docker push docker.tandilserver.com/web-poker/orchestrator
docker rm orchestrator
docker run --name orchestrator --env DB_URL="jdbc:mysql://192.168.1.189:3307/poker?autoReconnect=true&useSSL=false" -p 8082:8082 -p 8045:8045 -it docker.tandilserver.com/web-poker/orchestrator