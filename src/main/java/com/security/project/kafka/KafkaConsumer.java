package com.security.project.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

	@KafkaListener(topics = "oidctopic", groupId = "oidc-group")
	public void listenToCodeDecodeKafkaTopic(String messageReceived) {
		System.out.println("Message received is " + messageReceived);
	}
}
