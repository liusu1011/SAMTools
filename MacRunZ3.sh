#!/bin/bash
SPEC=$1
#gcc $SPEC -L/Users/suliu/Dropbox/SMTResearch/z3/bin/external/ -o"Z3Checker" -lz3

gcc $SPEC -L/lib/ -o "Z3Checker" -lz3


./Z3Checker

rm -rf Z3Checker


