# Infoza Telegram Bot

## Simple running instructions

Start MySQL:
```
docker-compose up -d
```

Add environment variable BOT_TOKEN and BOT_OWNER and run Spring Boot application:
```
mvn spring-boot:run
```

## Start with docker

Copy .env.example to .env and fill in the required variables.

Start Bot+MySQL:
```
docker compose build bot
# get UID, i.g. 999
docker compose run --rm --entrypoint "id -u app" bot
# get GID, i.g. 999
docker compose run --rm --entrypoint "id -g app" bot
mkdir -p ./log
sudo chown 999:999 ./log
sudo chmod 750 ./log
docker-compose up -d --build
```