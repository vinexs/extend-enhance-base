# Extend-Enhance-Base
Simply extend the Enhance Class / Base Class and the hard works were done.

# How to use

## Method 1:
Step 1. Add it in root build.gradle at the end of repositories
```
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```
Step 2. Add the dependency
```
dependencies {
	compile 'com.github.vinexs.extend-enhance-base:eeb-core:1.0.6b'
}
```

There are other modules could be use.
```
compile 'com.github.vinexs.extend-enhance-base:eeb-camera:1.0.6b'
compile 'com.github.vinexs.extend-enhance-base:eeb-net:1.0.6b'
```

## Method 2:
Step 1. Clone this project to your local disk.

Step 2. Add the following line to setting.gradle
```
def eebDir = 'C:\\Your\\Workspace\\Location\\Extend-Enhance-Base';
include ':eeb-core'
project(':eeb-core').projectDir = new File(eebDir, 'eeb-core')
include ':eeb-net'
project(':eeb-net').projectDir = new File(eebDir, 'eeb-net')
include ':eeb-camera'
project(':eeb-net').projectDir = new File(eebDir, 'eeb-camera')
```

Step 3. Add those module to your module dependencies.
```
dependencies {
	compile project(':eeb-core')
	compile project(':eeb-net')
}
```

# How to setup

Step 1. Add packagingOption in your module build.gradle to avoid compile error.
```
android {
	...
	packagingOptions {
		exclude 'META-INF/DEPENDENCIES.txt'
		exclude 'META-INF/LICENSE.txt'
		exclude 'META-INF/NOTICE.txt'
		exclude 'META-INF/NOTICE'
		exclude 'META-INF/LICENSE'
		exclude 'META-INF/DEPENDENCIES'
		exclude 'META-INF/notice.txt'
		exclude 'META-INF/license.txt'
		exclude 'META-INF/dependencies.txt'
		exclude 'META-INF/LGPL2.1'
	}
	...
}
```

Step 2. Select a default theme in Manifest.xml. There are some preset theme could use.
```
BaseTheme.Dark
BaseTheme.RoseRed
BaseTheme.Pink
BaseTheme.Purple
BaseTheme.DeepPurple
BaseTheme.Indigo
BaseTheme.Blue
BaseTheme.LightBlue
BaseTheme.Cyan
BaseTheme.Teal
BaseTheme.Green
BaseTheme.LightGreen
BaseTheme.Lime
BaseTheme.Yellow
BaseTheme.Amber
BaseTheme.Orange
BaseTheme.DeepOrange
BaseTheme.Brown
BaseTheme.Grey
BaseTheme.BlueGrey
BaseTheme.Black
```

Step 3. Make your MainActivity extends from com.vinexs.eeb.BaseActivity
```java
public class MainActivity extends BaseActivity {...}
```
Fragment can extends from BaseFragment.
```java
public class MyFragment extends BaseFragment {...}
```








