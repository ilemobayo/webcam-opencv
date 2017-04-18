@echo off
echo Compiling %* with FaceSDK and JavaCV...

javac -cp "C:\Program Files\Luxand\FaceSDK\include\Java\FaceSDK.jar;C:\Program Files\Luxand\FaceSDK\include\Java\jna.jar;d:\javacv-bin\javacv.jar;d:\javacv-bin\javacpp.jar;d:\javacv-bin\javacv-windows-x86.jar;." %*

echo Finished.
