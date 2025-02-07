# Caching Service  

## Description  
This application implements a **Caching Service** that stores **Person** entities. Each **Person** entity has the following attributes:  
- **ID**  
- **First Name**  
- **Last Name**  

The application provides various functionalities for managing cached entities efficiently.  

## Features  

### **Add Person**  
- Accepts a **Person** entity with all attributes.  
- Checks whether an entity with the given **ID** already exists in **cache** or **database**.  
- If the entity **does not exist**, it inserts the new entity.  
- If the entity **already exists**, an appropriate message is displayed.  

### **Get Person by ID**  
- Takes an **ID** as input.  
- Returns the entity details if it exists in **cache** or **database**.  

### **Get All Entities**  
- Retrieves all **Person** entities stored in both **cache** and **database**.  

### **Get All Entities from Cache**  
- Retrieves only the **Person** entities currently stored in the **cache**.  

### **Remove Person by ID**  
- Takes an **ID** as input.  
- Removes the entity from both **cache** and **database** if it exists.  

### **Remove All Entities**  
- Removes **all** entities stored in both **cache** and **database**.  

### **Clear Cache**  
- Clears **all** entities stored in the **cache** without affecting the database.  

## API Documentation  
The Swagger API documentation is available at:  
**[Swagger UI](http://localhost:8080/swagger-ui/index.html#/)**  

## Environment Details  
- **Java Version:** 1.8  
