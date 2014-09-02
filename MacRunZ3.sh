#!/bin/bash
SPEC=$1

#gcc $SPEC -I"/Users/suliu/Dropbox/SAMToolSet/z3/include/" -L"/Users/suliu/Dropbox/SAMToolSet/z3/bin" -o "Z3Checker" -lz3

gcc $SPEC -I"./z3osx64/include/" -L"./z3osx64/bin" -o "Z3Checker" -lz3

./Z3Checker

#rm -rf Z3Checker


