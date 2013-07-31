Android-based Jamendo client for Burnstation 3
==============================================

Building
--------

 - Download and unpack the Android SDK to any directory (if you haven't done so already).
 - Create the `local.properties` with a single line like `sdk.dir=/path/to/android/sdk` with the correct path.
 - Fetch zxing libraries with `svn checkout http://zxing.googlecode.com/svn/trunk/core/src/com src/com`
 - Run `ant debug` to build the project and sign it with a debug key.

License
-------

The whole project is licensed under MIT license.
