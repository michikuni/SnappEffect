#!/bin/bash

if [ "$1" = "a" ]; then
  git config user.name "dungtran123-cyber"
  git config user.email "dangminhphuong20042003@gmail.com"
  echo "Đã chuyển sang User dung"
elif [ "$1" = "b" ]; then
  git config user.name "michikuni"
  git config user.email "minhphuonglcby@gmail.com"
  echo "Đã chuyển sang User phuong"
elif [ "$1" = "c" ]; then
  git config user.name "ducnguyen-source"
  git config user.email "minhphuongcy6kma1@gmail.com.com"
  echo "Đã chuyển sang User duc"
else
  echo "Usage: ./set-git-user.sh [a|b|c]"
fi
