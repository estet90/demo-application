#!/bin/bash

psql -U postgres -h localhost -d postgres -f 1_init.sql -o 1_init.log
