const kafka = require("kafka-node");
import envVariables from "../EnvironmentVariables";
import logger from "../config/logger";
import { createNoSave } from "../index";
import { runWithContext } from "../utils/requestContext";
const uuidv4 = require("uuid/v4");
const get = require("lodash/get");
var async = require('async'); 


export const listenConsumer = async(topic)=>{
//let receiveJob = envVariables.KAFKA_RECEIVE_CREATE_JOB_TOPIC;

let receiveJob = topic;


var topicList = [];
for (var i in receiveJob) {
  topicList.push(receiveJob[i]);
}

var options = {
  // connect directly to kafka broker (instantiates a KafkaClient)
  kafkaHost: envVariables.KAFKA_BROKER_HOST,
  autoCommit: true,
  groupId: "bulk-pdf",
  // An array of partition assignment protocols ordered by preference. 'roundrobin' or 'range' string for
  // built ins (see below to pass in custom assignment protocol)
  protocol: ["roundrobin"],
  // Offsets to use for new groups other options could be 'earliest' or 'none'
  // (none will emit an error if no offsets were saved) equivalent to Java client's auto.offset.reset
  fromOffset: "latest",
  // how to recover from OutOfRangeOffset error (where save offset is past server retention)
  // accepts same value as fromOffset
  outOfRangeOffset: "earliest"
};

var consumerGroup = new kafka.ConsumerGroup(options, topicList);
// ConsumerGroup owns a private KafkaClient; every in-flight long-poll fetch parks a
// once-'<broker>-longpolling-ready' listener on it, tripping Node's 10-listener heuristic
if (consumerGroup.client && typeof consumerGroup.client.setMaxListeners === "function") {
  consumerGroup.client.setMaxListeners(100);
}

var q = async.queue(function(data, cb) {
  const correlationId =
    get(data, "RequestInfo.correlationId") ||
    get(data, "requestInfo.correlationId") ||
    uuidv4();
  const tenantId = get(data, "RequestInfo.userInfo.tenantId") || null;
  runWithContext({ CORRELATION_ID: correlationId, TENANTID: tenantId }, function() {
    createNoSave(data,null,() => {},() => {}).then(function(ep) {
      cb(); //this marks the completion of the processing by the worker
    }).catch(function(err) {
      logger.error("error while processing consumer record: " + ((err && err.message) || err));
      logger.error((err && err.stack) || err);
      cb();
    });
  });
}, 1);


q.drain(async () => {
  consumerGroup.resume(); //resume listening new messages from the Kafka consumer group
});




consumerGroup.on("ready", function() {
  logger.info("Consumer is ready");
});

consumerGroup.on("message", function(message) {
  logger.info("record received on consumer for create");
  try {
      var data = JSON.parse(message.value);
      //console.log(JSON.stringify(data));
     /* await createNoSave(
        data,
        null,
        () => {},
        () => {}
      )
        .then(() => {
          logger.info("record created for consumer request");
        })
        .catch(error => {
          logger.error(error.stack || error);
        });*/
    q.push(data, function (err, result) {  
      if (err) { logger.error(err); return }      
    });
    consumerGroup.pause();

  } catch (error) {
    logger.error("error in create request by consumer " + error.message);
    logger.error(error.stack || error);
  }
});

consumerGroup.on("error", function(err) {
  logger.error("consumer error: " + ((err && err.message) || err));
  logger.error((err && err.stack) || err);
});

consumerGroup.on("offsetOutOfRange", function(err) {
  logger.error("consumer offsetOutOfRange: " + ((err && err.message) || err));
  logger.error((err && err.stack) || err);
});

}