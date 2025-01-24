package com.demo.cachingservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PersonEntity{
		@Id
	 	private final String id;
	    private final String firstName;
	    private final String lastName;

	    public PersonEntity(String id, String firstName, String lastName) {
	        this.id = id;
	        this.firstName = firstName;
	        this.lastName = lastName;
	    }

	    public String getId() {
	        return id;
	    }

	    public String getFirstName() {
	        return firstName;
	    }

	    public String getLastName() {
	        return lastName;
	    }
}
