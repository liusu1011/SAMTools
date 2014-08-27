#!/bin/bash
SPEC=$1
gcc $SPEC -I"C:\z3\include" -L"C:\z3\bin" -o"Z3Checker.exe" -lz3

./Z3Checker.exe 

rm -rf Z3Checker


