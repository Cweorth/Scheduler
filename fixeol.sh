#!/bin/bash

find . -name "*.java" -exec sed -i "s/\s\+$//" {} \;
