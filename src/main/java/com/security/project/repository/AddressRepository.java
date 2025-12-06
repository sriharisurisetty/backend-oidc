package com.security.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.security.project.model.AddressDTO;

@Repository
public interface AddressRepository extends MongoRepository<AddressDTO, String> {
    
}
