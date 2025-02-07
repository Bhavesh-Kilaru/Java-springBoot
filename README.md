### Description
This application is designed to implement a Caching Service that stores the person entities. The person has following characteristics 
ID
FirstName
LastName

This applications supports the below functionalities.

#### add
This functionality takes a person which has all characteristics. Initially, it checks whether an person entity with id exists in cache and database. If it doesn't exists, it will insert the entity otherwise will show the appropriate message.

#### Get Entity
This functionality takes an input argument ID and returns the entity details if it exists in cache and Database.

#### Get All Entities
This functionality return all entities in cache and Database.

### Get All Entities From cache
This functionality return all entities in cache.

#### Remove Id
This functionality takes an input argument ID and removes the entity if it exists in cache and Database.

#### Remove All
This functionality removes all entities that exists in cache and Database.

#### Clear
This functionality removes all entities that exists in cache.

__The Swagger is available in link :- swagger url : http://localhost:8080/swagger-ui/index.html#/ 

Java version: 1.8__