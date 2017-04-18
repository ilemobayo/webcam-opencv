@echo off
echo Executing %* with JavaCV, OpenCV...

java -cp "d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." -Djava.library.path="C:\opencv\build\x86\mingw\bin;." %*

rem or vc9, vc10
rem C:\WINDOWS\System32;

echo Finished.
