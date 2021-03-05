
# Android Pdf Viewer
- fork from 'barteksc/AndroidPdfViewer'. but add new features. 
- compile'd native of pdfium from the google source at 2021.
- Had used to real online project.

# New features
- support for transform screen x,y to pdf pages'.
- support add/remove image for pdf page.
- add lot of new methods.  like getPageRotation.

# Gradle
```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  implementation 'com.github.LightSun:AndroidPdfViewer:10.1.4'
```
