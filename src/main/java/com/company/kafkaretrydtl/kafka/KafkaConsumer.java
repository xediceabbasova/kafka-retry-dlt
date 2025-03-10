package com.company.kafkaretrydtl.kafka;

import com.company.kafkaretrydtl.exception.AException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @KafkaListener(topics = "message", groupId = "group")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 1.0, maxDelay = 5000),
            autoCreateTopics = "true",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            retryTopicSuffix = "-retrytopic",
            dltTopicSuffix = "-dlttopic",
            include = {AException.class}
    )
    public void kafkaConsumerRetry(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (message.contains("A")) {
            throw new AException("Gondermis oldugunuz deger hatali ! Lutfen " + message + "icerisinde A olmasin !");
        }
        logger.info("Mesaj Consumer tarafindan teslim alindi ! Mesaj: {}", message);
    }

    @DltHandler
    public void dtl(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info("DTL TOPIC message: {},topic name: {}", message, topic);
    }

}
