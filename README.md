# lms-decentralized

## Docker commands
### Wipe everything
- docker compose down -v

### Build
- docker compose build

### Run
- docker compose up

### Command for running multiple instances per service, in this case three
- docker compose -p lms up -d --build --scale identity-service=3 --scale catalog-service=3 --scale reader-lending-service=3
