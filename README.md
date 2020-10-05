# File Storage REST service - Java

This is a web-service (REST) with an interface described below

## Concept

### Upload

```

POST /file

{
  "name": "FirstName",
  "size": 123123
}

```

### Delete 

```

DELETE /file/{ID}

```

### Assign tags to file

``` 

POST /file/{ID}/tags

["tag1", "tag2", "tag3"]


```


### Remove tags from file

```

DELETE /file/{ID}/tags

["tag1", "tag3"]

```

### List files with pagination optionally filtered by tags


```

GET /file?tags=tag1,tag2,tag3&page=2&size=3

```

## Addititon

- At the upload automatically add tag "audio" / "video" / "document" / "image" etc. based on extension
- In the listing endpoint handle optional parameter q that will apply a search over file name.

## Requirements

- As a data storage Elasticsearch
- As main framework Spring Boot
- Java 11 and Maven

## How to run

If you can use Docker, then just type in the project directory.

```

make run

```

## Documentation

You can try every operation with Swagger. Just go to 

```
http://localhost:8080/swagger-ui.html

```

You can see the documentation there also.
