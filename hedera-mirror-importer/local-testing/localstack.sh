#!/bin/bash

set -e

endpointUrl=$DOCKER_INTERNAL_LOCALSTACK_ENDPOINT
notificationQueue=$NOTIFIACTION_QUEUE_NAME

echo "Bootstrapping localstack environment using endpoint: $endpointUrl"

# SQS
echo "Bootstrapping SQS"

queues=(
  "$notificationQueue"
)
for queue in ${queues[@]}; do
  aws sqs create-queue \
    --endpoint-url "$endpointUrl" \
    --queue-name "$queue"
  echo "Created queue $queue"
done

echo "Successfully bootstrapped SQS"