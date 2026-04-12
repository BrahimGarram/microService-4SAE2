# RabbitMQ Integration Guide

## Overview
This guide explains how to set up and test RabbitMQ for asynchronous communication between microservices in your application.

## Architecture
- **Producer**: `service-test` (Test Service) - Sends test notifications
- **Consumer**: `user-service` (User Service) - Receives and processes notifications
- **Queue**: `testNotificationQueue` - Stores messages between producer and consumer

## Installation Steps

### Step 1: Install Erlang (Required for RabbitMQ)
1. Download Erlang from [https://www.erlang.org/downloads](https://www.erlang.org/downloads)
2. Run the installer and follow the on-screen instructions
3. Verify installation: Open CMD and run `erl -version`

**Note**: RabbitMQ requires Erlang to function. Without Erlang, RabbitMQ will not start.

### Step 2: Install RabbitMQ
1. Download RabbitMQ from [https://www.rabbitmq.com/download.html](https://www.rabbitmq.com/download.html)
2. Run the installer and follow the on-screen instructions
3. Add RabbitMQ to PATH:
   - Add `C:\Program Files\RabbitMQ Server\rabbitmq_server-x.x.x\sbin` to your PATH environment variable

### Step 3: Start RabbitMQ
Open Command Prompt as Administrator and run:

```bash
# Enable management plugin
rabbitmq-plugins enable rabbitmq_management

# Start RabbitMQ service
rabbitmq-service start

# Check RabbitMQ status
rabbitmqctl status
```

### Step 4: Access RabbitMQ Management UI
1. Open browser and navigate to: [http://localhost:15672](http://localhost:15672)
2. Default credentials:
   - Username: `guest`
   - Password: `guest`

**Troubleshooting**: If you can't access the management UI:
```bash
# List plugins to verify management plugin is enabled
rabbitmq-plugins list

# Enable management plugin if needed
rabbitmq-plugins enable rabbitmq_management

# Restart RabbitMQ
rabbitmq-service.bat stop
rabbitmq-service.bat start
```

## Docker Setup (Recommended)

The project includes RabbitMQ in `docker-compose.yml`. Simply run:

```bash
docker-compose up -d rabbitmq
```

This will:
- Start RabbitMQ on port 5672 (AMQP)
- Start Management UI on port 15672
- Create persistent volume for data

## Configuration Details

### RabbitMQ Ports
- **5672**: AMQP protocol port (used by applications)
- **15672**: Management UI port (web interface)

### Application Configuration

Both microservices are configured with:
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

In Docker, the host is automatically set to `rabbitmq` (service name).

## Testing the Integration

### Option 1: Using Docker Compose (Recommended)

1. **Start all services**:
```bash
docker-compose up -d
```

2. **Check RabbitMQ is running**:
   - Visit [http://localhost:15672](http://localhost:15672)
   - Login with guest/guest

3. **Send a test notification**:
```bash
curl -X POST http://localhost:18089/service-test/api/test-notifications/send-sample
```

4. **Verify in RabbitMQ UI**:
   - Go to "Queues" tab
   - You should see `testNotificationQueue`
   - Check message statistics

5. **Check received notifications**:
```bash
curl http://localhost:18081/api/notifications
```

### Option 2: Local Development

1. **Start RabbitMQ** (if not using Docker):
```bash
rabbitmq-service start
```

2. **Start Eureka Server**:
```bash
cd Eureka
mvnw spring-boot:run
```

3. **Start User Service** (Consumer - must start first):
```bash
cd services/backend1
mvnw spring-boot:run
```

4. **Start Test Service** (Producer):
```bash
cd services/testService/service-test
mvnw spring-boot:run
```

5. **Send a test notification**:
```bash
# Send sample notification
curl -X POST http://localhost:8089/service-test/api/test-notifications/send-sample

# Or send custom notification
curl -X POST http://localhost:8089/service-test/api/test-notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "testId": 123,
    "testTitle": "Spring Boot Advanced Test",
    "status": "COMPLETED",
    "userId": 456,
    "userName": "Jane Smith"
  }'
```

6. **Check received notifications**:
```bash
# Get all notifications
curl http://localhost:8081/api/notifications

# Get notifications for specific user
curl http://localhost:8081/api/notifications/user/456
```

## Verification Steps

### 1. Check RabbitMQ Management UI
- Navigate to [http://localhost:15672](http://localhost:15672)
- Go to "Queues" tab
- Verify `testNotificationQueue` exists and shows message activity

### 2. Check Application Logs

**Test Service (Producer) logs should show**:
```
INFO ... Test notification envoyée à la queue : testNotificationQueue - Test ID: 123
```

**User Service (Consumer) logs should show**:
```
INFO ... TestNotificationDTO reçu depuis RabbitMQ : TestNotificationDTO{testId=123...}
INFO ... Traitement de la notification de test : Spring Boot Advanced Test
INFO ... Notification ajoutée : Test 'Spring Boot Advanced Test' - Status: COMPLETED
INFO ... Notification envoyée à l'utilisateur Jane Smith : Test 'Spring Boot Advanced Test' - Status: COMPLETED
```

### 3. Test API Endpoints

**Producer Endpoints** (Test Service):
- `POST /service-test/api/test-notifications/send` - Send custom notification
- `POST /service-test/api/test-notifications/send-sample` - Send sample notification

**Consumer Endpoints** (User Service):
- `GET /api/notifications` - Get all received notifications
- `GET /api/notifications/user/{userId}` - Get notifications for specific user

## Architecture Explanation

### Why Consumer Declares the Queue?
The consumer (user-service) declares the queue because:
1. **Responsibility**: Each microservice manages what it needs
2. **Resilience**: If producer crashes, queue still exists
3. **Scalability**: Multiple consumers can declare their own queues
4. **Best Practice**: Follows single responsibility principle

### Message Flow
1. **Queue Declaration** (Consumer - User Service):
   - Consumer creates `testNotificationQueue` in RabbitMQ
   - Consumer subscribes via `@RabbitListener`

2. **Message Publishing** (Producer - Test Service):
   - Test Service converts `TestNotificationDTO` to JSON
   - Publishes message to `testNotificationQueue`
   - RabbitMQ receives and stores the message

3. **Message Storage** (RabbitMQ):
   - Message stored in queue (FIFO order)
   - Persists even if consumer is offline

4. **Message Consumption** (Consumer - User Service):
   - Consumer automatically notified of new message
   - Converts JSON back to `TestNotificationDTO`
   - Processes notification (stores, sends to user, etc.)

### Key Components

**RabbitTemplate** (Producer):
- Main class for sending messages
- Handles connection to RabbitMQ broker
- Uses MessageConverter for serialization

**MessageConverter**:
- Converts Java objects to AMQP messages (JSON)
- Automatically called by Spring AMQP
- Uses Jackson for JSON serialization

**@RabbitListener** (Consumer):
- Listens to specified queue
- Automatically deserializes messages
- Triggers method when message arrives

## Troubleshooting

### Issue: Connection Refused
**Solution**: Ensure RabbitMQ is running
```bash
rabbitmqctl status
```

### Issue: Queue Not Found (404)
**Solution**: Start consumer (user-service) before producer (test-service)

### Issue: Messages Not Being Consumed
**Solution**: Check consumer logs for errors and verify queue name matches

### Issue: Management UI Not Accessible
**Solution**: Enable and restart management plugin
```bash
rabbitmq-plugins enable rabbitmq_management
rabbitmq-service.bat stop
rabbitmq-service.bat start
```

## Additional Resources

- [RabbitMQ Official Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)

## Notes

- Port 5672: AMQP protocol (application communication)
- Port 15672: Management UI (monitoring and administration)
- Producer and Consumer must connect to same broker (same host and port)
- Queue is durable (survives broker restart)
- Messages are persistent by default
