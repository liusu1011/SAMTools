#!/bin/bash

SPEC=$1

spin -a $SPEC

#gedit $SPEC

gcc -DMEMLIM=1024 -O2 -DXUSAFE -w -o pan pan.c
./pan -m10000  -a -N f

#gedit output.txt

spin -t -r -s $SPEC>output.txt

