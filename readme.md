# flock-eco-fundraising
This application is a fully integrate solution to support the processes around fundraising. The application is constructed of components from flock-eco and reuses the member, payment, mailchimp and user module.

https://github.com/flock-community/flock-eco 

## Run
````
- mvn clean install
- mvn spring-boot:run
````

## Deploy to the cloud
````
- gcloud config set project <project_name>
- gcloud auth application-default login
- mvn clean appengine:deploy
```