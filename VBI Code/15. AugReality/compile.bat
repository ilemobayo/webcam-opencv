@echo off
echo Compiling %* with NyARToolkit, Java3D, Portfolio, JavaCV, and OpenCV...

javac -Xlint:deprecation -encoding ISO-8859-1 -cp "NyARToolkit.jar;ncsa\portfolio.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." %*

echo Finished.
