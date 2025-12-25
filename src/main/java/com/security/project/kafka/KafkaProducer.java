package com.security.project.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessageToTopic(String message) {
		kafkaTemplate.send("oidctopic", message);
		System.out.println("message posted");
	}

}