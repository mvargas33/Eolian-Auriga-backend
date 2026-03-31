#!/bin/bash

while true
t=0.01
do
  echo "cansend can0 064#1b00000000000000"
  sleep $t
  echo "cansend can0 064#1a00000000000000"
  sleep $t
  echo "cansend can0 064#3300000000000000"
  sleep $t
  echo "cansend can0 064#3700000000000000"
  sleep $t
  echo "cansend can0 0C8#1b00000000000000"
  sleep $t
  echo "cansend can0 0C8#1a00000000000000"
  sleep $t
  echo "cansend can0 0C8#3300000000000000"
  sleep $t
  echo "cansend can0 0C8#3700000000000000"
  sleep $t
done

