#!/bin/bash

psql -U todo -h localhost -d todo -f 2_create_schema.sql -o 2_create_schema.log
