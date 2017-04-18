@echo off
echo Executing %* with NyARToolkit, Java3D, Portfolio, JavaCV and OpenCV...

java -cp "NyARToolkit.jar;ncsa\portfolio.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." -Djava.library.path="C:\opencv\build\x86\mingw\bin;." %*

echo Finished.
