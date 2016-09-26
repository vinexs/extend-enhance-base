# Extend-Enhance-Base
Simply extend the Enhance Class / Base Class and the hard works were done.

## How to setup

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

Step 3. Add compileOptions & packagingOption to avoid compile error.
```
compileOptions {
	sourceCompatibility JavaVersion.VERSION_1_8
	targetCompatibility JavaVersion.VERSION_1_8
}
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
```
If gradle notify "Jack is required to support java 8 language feature" error. Add the following to defaultConfig{}
```
defaultConfig {
	...
	jackOptions {
		enabled true
        }
}
```

Step 4. Select a default theme in Manifest.xml. There are some preset theme could use.
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

Step 4. Make your MainActivity extends from com.vinexs.eeb.BaseActivity
```java
public class MainActivity extends BaseActivity {...}
```
Fragment can extends from BaseFragment.
```java
public class MyFragment extends BaseFragment {...}
```








