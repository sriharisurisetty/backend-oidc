package com.security.project.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.security.project.model.UserDTO;

import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	public void sendMessageToTopic(UserDTO userEvent) {
		try {
		String jsonMessage = objectMapper.writeValueAsString(userEvent);
		kafkaTemplate.send("oidctopic", userEvent.getEmail(), jsonMessage);
		LOGGER.info("Message posted to topic: {}", userEvent);
		} catch(Exception e){
			LOGGER.error("Error sending UserDTO to Kafka: {}", e.getMessage(), e);
		}
	}
	
	public void sendUserToTopic(UserDTO userDTO) {
		try {
			String jsonMessage = objectMapper.writeValueAsString(userDTO);
			kafkaTemplate.send("user-events", userDTO.getEmail(), jsonMessage);
			LOGGER.info("UserDTO sent to user-events topic: {}", userDTO);
		} catch (Exception e) {
			LOGGER.error("Error sending UserDTO to Kafka: {}", e.getMessage(), e);
		}
	}

}