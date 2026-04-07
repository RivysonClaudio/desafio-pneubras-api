#!/bin/bash

# Load environment variables from .env file
# and run the application using Maven
# The `xargs` command is used to trim whitespace from the environment variables
# and the `&&` operator is used to run the commands sequentially
export $(cat .env | xargs) && mvn spring-boot:run