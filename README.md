
# Looking for new maintainer!
- fork from 'barteksc/AndroidPdfViewer'. but add support for transform screen x,y to pdf pages'.
- support add/remove image for pdf page
- compile'd pdfium from the google source. 2021

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
