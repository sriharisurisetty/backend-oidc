package com.security.project.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.security.project.model.UserDTO;


@RestController
@RequestMapping("/kafka/api")
public class KafkaController {

	@Autowired
    KafkaProducer kafkaProducer;


    @GetMapping(value = "/producer")
    public String sendMessage(@RequestParam("message") UserDTO message)
    {
        kafkaProducer.sendMessageToTopic(message);
        return "Message sent Successfully to the your OIDC topic ";
    }
	
}
