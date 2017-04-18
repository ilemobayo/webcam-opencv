@echo off
echo Executing %* with TableLayout, JavaCV, and OpenCV...

java -cp "d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;TableLayout-bin-jdk1.5-2009-08-26.jar;." -Djava.library.path="C:\opencv\build\x86\mingw\bin;." %*

echo Finished.
