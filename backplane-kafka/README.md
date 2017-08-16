Flywheel - Kafka Backplane
===
Backplane implementation using Kafka and Scramjet to share messages among all edge nodes.

# Wire Format
Currently **Scramjet** is used as the wire protocol. Scramjet is a simple JSON envelope that takes the following form:
```json
{
  "payload": {
    "$type": "string"
  },
  "messageType": "string",
  "id": "uuid",
  "sentAt": "iso 8601",
  "publisher": "string"
}
```

Scramjet uses the `$type` convention to specify a concrete data type for the `payload`, and may nest further `$type` properties within the payload where type polymorphism is used. Where the type is ambiguous, the `$type` property is _required_; omitting it will result in deserialization errors. 

Flywheel uses the `Scramjet.Messages.Push.Update` payload type to encapsulate the message topic and payload, as well as an upper bound on the useful lifetime of the message. 

## Text messages
A push update with a text payload is shown in the example below.
```json
{
  "payload": {
    "$type": "Scramjet.Messages.Push.Update",
    "topic": "racing/13815910/status",
    "payload": "race started",
    "timeToLive": 30
  },
  "messageType": "PUSH_UPDATE",
  "id": "309be61a-a551-4f6b-9ad6-4fd91c3d495e",
  "sentAt": "2017-08-12T19:53:36.0317039Z",
  "publisher": "flywheel-0"
}
```

If the `payload.payload` value is a string, it is treated as a text message. So the example above will result in the text message `race started` published on the topic `racing/13815910/status`. Alternatively, the payload can be a free-form JSON object, which will be automatically coerced to a string - a major convenience and a readability aid if your payload is natively JSON. So the following two examples will produce an identical outcome:
```json
{
  "payload": {
    "$type": "Scramjet.Messages.Push.Update",
    "topic": "example",
    "payload": {
      "foo": "bar"
    }
    "timeToLive": 30
  },
  ...
}
```

```json
{
  "payload": {
    "$type": "Scramjet.Messages.Push.Update",
    "topic": "example",
    "payload": "{\"foo\": \"bar\"}"
    "timeToLive": 30
  },
  ...
}
```


## Binary messages
If the payload is of type `Scramjet.Messages.Base64`, it will be treated as binary string, decoded from `payload.payload.value`. This is illustrated in the following example, which is just the byte array `[0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07]` in [Base64](https://en.wikipedia.org/wiki/Base64) notation.
```json
{
  "payload": {
    "$type": "Scramjet.Messages.Push.Update",
    "topic": "racing/13815910/status",
    "payload": {
      "$type": "Scramjet.Messages.Base64",
      "value": "AAECAwQFBgc="
    },
    "timeToLive": 30
  },
  ...
}
```

# Kafka Configuration
## Compatibility
This backplane implementation is designed to work with Kafka brokers of version 0.10.x and above. If you need to sources messages from an older Kafka broker, consider using the [Rekafka](https://github.com/william-hill-community/rekafka) tool.

## Topic and partitions
By default the backplane uses the Kafka topic `platform.push` to disseminate messages. This can be edited in the configuration profile.

Messages are keyed by the Flywheel topic name, which provides an even sharding across all available partitions, while preserving message order within any given topic. 

Depending on your Kafka set-up, the backplane may auto-create the Kafka topic with a single partition upon first use. This _is not_ the recommended approach; we suggest that you explicitly create the topic in Kafka prior to connecting the backplane, and have it partitioned accordingly.