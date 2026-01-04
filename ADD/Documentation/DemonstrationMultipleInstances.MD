# Test Demonstrating Multiple Instances per Service

## Command to run three instances per service
docker compose -p lms up -d --build --scale identity-service=3 --scale catalog-service=3 --scale reader-lending-service=3

![Identity instance 1 load](images/imagem1.png)

---

## Run this command to verify all instances are running

- docker compose -p lms ps

![Identity instance 2 load](images/imagem2.png)

All instances are running, three per service. Running 3 log tails in separate terminals for identity-service:

docker logs -f lms-identity-service-1

docker logs -f lms-identity-service-2

docker logs -f lms-identity-service-3

---

## Then, I run this in another terminal

1..12 | ForEach-Object {
Invoke-RestMethod http://localhost:8081/v3/api-docs
 | Out-Null
Start-Sleep -Milliseconds 200
}


Spamming the identity-service instances:

![Identity instance 3 load](images/imagem3.png)

Terminal 1:

![Identity instance 4 load](images/imagem4.png)

Terminal 2:

![Identity instance 5 load](images/imagem5.png)

Terminal 3:

![Identity instance 6 load](images/imagem6.png)

As seen on the pictures, the traffic was evenly distributed among the three running instances of the service. This test example was also replicated for catalog-service and reader-lending-service.

Compared to the monolithic system, the lms_decentralized is able to handle infinetily more traffic, since the program allows infinetily more instances per service to run simultaneously.
