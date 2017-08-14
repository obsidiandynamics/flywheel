package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;
import java.util.concurrent.*;

import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;

public class ConsumerGroupExample {
  private final ConsumerConnector consumer;
  private final String topic;
  private  ExecutorService executor;

  public ConsumerGroupExample(String a_zookeeper, String a_groupId, String a_topic) {
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                                                                   createConsumerConfig(a_zookeeper, a_groupId));
    this.topic = a_topic;
  }

  public void shutdown() {
    if (consumer != null) consumer.shutdown();
    if (executor != null) executor.shutdown();
    try {
      if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
        System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
      }
    } catch (InterruptedException e) {
      System.out.println("Interrupted during shutdown, exiting uncleanly");
    }
  }

  public void run(int a_numThreads) {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, new Integer(a_numThreads));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

    // now launch all the threads
    //
    executor = Executors.newFixedThreadPool(a_numThreads);

    // now create an object to consume the messages
    //
    int threadNumber = 0;
    for (final KafkaStream stream : streams) {
      executor.submit(new ConsumerTest(stream, threadNumber));
      threadNumber++;
    }
  }

  private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
    Properties props = new Properties();
    props.put("zookeeper.connect", a_zookeeper);
    props.put("group.id", a_groupId);
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.enable", "false");
    props.put("auto.offset.reset", "smallest");

    return new ConsumerConfig(props);
  }

  public static void main(String[] args) {
    String zooKeeper = "sitapzoo:2181";//args[0];
    String groupId = "sample2";//args[1];
    String topic = "platform.push";//args[2];
    int threads = 1;//Integer.parseInt(args[3]);

    ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic);
    example.run(threads);

    try {
      Thread.sleep(10000);
    } catch (InterruptedException ie) {

    }
    example.shutdown();
  }

  public static class ConsumerTest implements Runnable {
    private KafkaStream m_stream;
    private int m_threadNumber;

    public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
      m_threadNumber = a_threadNumber;
      m_stream = a_stream;
    }

    public void run() {
      ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
      while (it.hasNext())
        System.out.println("Thread " + m_threadNumber + ": " + new String(it.next().message()));
      System.out.println("Shutting down Thread: " + m_threadNumber);
    }
  }
}
