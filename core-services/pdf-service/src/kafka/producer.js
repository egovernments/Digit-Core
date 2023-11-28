import logger from "../config/logger";
// var kafka = require("kafka-node");
import envVariables from "../EnvironmentVariables";
const { Kafka, logLevel } = require('kafkajs')

let client;
// if (process.env.NODE_ENV === "development") {
// client = new kafka.Client();
// client = new kafka.KafkaClient({ brokers: envVariables.KAFKA_BROKER_HOST, connectRetryOptions: {retries: 1} });
//   console.log("local - ");
// } else {
//   client = new kafka.KafkaClient({ kafkaHost: envVariables.KAFKA_BROKER_HOST });
//   console.log("cloud - ");
// }

let kafkaClient = new Kafka({ 
  clientId: 'pdf-service',
  logLevel: logLevel.INFO,
  brokers: [envVariables.KAFKA_BROKER_HOST], 
  retry: { retries: 1 },
  ssl: false
});


const producer = kafkaClient.producer();


async function initializeProducer() {
  await producer.connect();
}

module.exports = {
  initializeProducer,
  producer
};