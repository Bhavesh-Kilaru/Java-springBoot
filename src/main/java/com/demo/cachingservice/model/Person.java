package com.demo.cachingservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Person{
		@Id
	 	private final String id;
	    private final String firstName;
	    private final String lastName;
	    
	 // Default constructor
	    public Person() {
	    	this.id = null;
	    	this.firstName = "";
			this.lastName = "";
	    }

	    public Person(String id, String firstName, String lastName) {
	        this.id = id;
	        this.firstName = firstName;
	        this.lastName = lastName;
	    }
	    
	    //getter method to get id
	    public String getId() {
	        return id;
	    }
	   //getter method to get first name
	    public String getFirstName() {
	        return firstName;
	    }
	    
	   //getter method to get last name
	    public String getLastName() {
	        return lastName;
	    }
}
