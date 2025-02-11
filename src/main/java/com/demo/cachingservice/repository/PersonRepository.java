package com.demo.cachingservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.cachingservice.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, String>{

}


