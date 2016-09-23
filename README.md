# osusume-java-spring
Osusume back-end built in Java Spring Boot.

## Configuration
A environment variable for the database URL needs to be configured for PostgreSQL, such as:

`OSUSUME_DATABASE_URL=jdbc:postgresql://localhost/osusume-dev`

For accessing the Google Places API, an API key is necessary (see https://console.developers.google.com/):

`GOOGLE_PLACES_KEY=<Google Places API Key>`

For accessing to AWS S3 bucket:

`AWS_ACCESS_KEY=<AWS Access Key>`

`AWS_SECRET_KEY=<AWS Secret Access Key>`

`AWS_S3_BUCKET_NAME=<S3 Bucket Name>`

## Makefile
Please use the makefile which contains a few useful commands:

**refreshdb** Creates database. Run this to setup the DB for the first time or to wipe out all data.

**migrate** Refreshes the database scheme. Run this to apply migrations to the database.

**loadsampledata** Loads the base set of sample data that can be used with the application.

**tests** Runs all tests and then loads all sample data. ***Please note that this will remove all existing data and re-load the sample data!***

## Setting up the Project
1.) Set environment variables:

  * OSUSUME_DATABASE_URL=`OSUSUME_DATABASE_URL=jdbc:postgresql://localhost/osusume-dev` #(reset to 'osusume-test' for tests)
  * OSUSUME_DATABASE_USER
  * OSUSUME_DATABASE_PASSWORD

2.) Set up the development database:
```
# Create db
make refreshdb

# Run db migrations
make migrate

# Load sample data
make loadsampledata
```

3.) Set up the test database. You will need to change the OSUSUME_DATABASE_URL to point at the test instance (i.e. osusume-test)
```
# Create db
make refreshdb

# Run db migrations
make migrate

# Load sample data
make loadsampledata
```

4.) Build application. Tests will need to run and pass in order to successfully build the application.
```
make tests
```

5.) Change OSUSUME_DATABASE_URL to point back at the dev instance (i.e. osusume-dev). You can now start the application.
```
make start
```

## Starting the Server
The project is broken down into an "application" and a "components" directory, therefore to start the server please locate the jar under the 'applications' directory:

`java -jar build/libs/osusume-java-spring-0.0.1-SNAPSHOT.jar`

## Migrations
To run migrations on Cloud Foundry:

`OSUSUME_DATABASE_URL=<jdbc-url-cf-sql> OSUSUME_DATABASE_USER=<jdbc-user-cf-sql> OSUSUME_DATABASE_PASSWORD=<jdbc-password-cf-sql> ./gradlew flywayMigrate`

`flywayInfo` can be used instead to show which migrations have been run.

To run migrations on local:

`OSUSUME_DATABASE_URL=jdbc:postgresql://localhost/osusume-test; make refreshdb; ./gradlew flywayMigrate`

## Sample curl statements

Create a session (logon) and receive a token (please pass in a valid username and password):

`curl -i -X POST http://localhost:8080/session -H "content-type: application/json" -d '{"email":"name","password":"password"}'`

Retrieve a list of restaurants (please replace `<token>` with a valid token):

`curl http://localhost:8080/restaurants -H "Authorization: Bearer <token>" | jq .`

Retrieve details for a specific restaurant (please replace `<token>` with a valid token):

`curl http://localhost:8080/restaurants/14 -H "Authorization: Bearer <token>" | jq .`

## Docker

### Docker Install

For MacOS, we recommend brew
```
brew install docker 
```

You will also need to provision a Linux VM within docker-machine in order to run docker. Please make sure that you are running the latest version of Virtualbox.
This step is not required if you are running docker directly in Linux. 
```
docker-machine create --driver virtualbox default
```

### Osusume Setup

**macOS NOTE**: if you are running docker on macOS, then you will need to first run the following command in order to communicate with the docker daemon in the Linux VM in your shell session:
```
eval $(docker-machine env)
```

1.) Build image from project root.
```
docker build -f ./docker/Dockerfile -t osusume-java .
```

2.) Start container from image "osusume-java" and map container port 8080 to host 8080
```
docker run -p 8080:8080 osusume-java 
```

3.) Attach to running container
```
docker ps  # shows list of active containers by id
docker exec -it <container id> /bin/bash
```

**macOS NOTE**: if you are running docker on macOS, then you need to remember that it is actually running on a small Linux VM. 
This means that mapped ports from the container to the host must pass through the Linux VM on macOS.
You can query the IP address for the VM as follows:
```
echo $DOCKER_HOST
tcp://192.168.99.100:2376
```

Other applications/services could now access Osusume Java Spring on http://192.168.99.100:8080 (this is the IP address for the Linux VM, and the port that we mapped from the container into the host).
This step is not required when running directly in Linux.
