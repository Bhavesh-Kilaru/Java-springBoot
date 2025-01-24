package com.demo.cachingservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.cachingservice.model.PersonEntity;

@Repository
public interface PersonEntityRepository  extends JpaRepository<PersonEntity, String>{

}
