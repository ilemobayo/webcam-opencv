@echo off
echo gocr of %1, saving XML to %2

rem gocr049.exe -C "123456789" -d 10 -a 40 -f XML -i %1 -o %2
gocr049.exe -C "123456789" -a 30 -f XML -i %1 -o %2