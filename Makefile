# start all services defined in docker-compose.yml file
up:
	docker-compose up -d --remove-orphans

# remove all containers defined in docker-compose.yml file
down:
	docker-compose down

# remove all containers, images and volumes defined in docker-compose.yml file
down-all:
	docker-compose down --rmi all --volumes
