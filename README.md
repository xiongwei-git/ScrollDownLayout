# ScrollDownLayout
##Summary
An custom Android Widget，support u use ViewPager in a ScrollView or ListView。
##Screenshot
![1](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/Screenshot_1.png)
![2](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/Screenshot_2.png)
![3](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/Screenshot_3.png)

##Video
[Youtube](https://youtu.be/YVO7dljmwpw)
##Demo
[Download Demo](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/app-debug.apk)

##Usage
### Step 1
#### Gradle
```
dependencies {
    compile 'com.ted.coder.sdlayout:library:1.0.2'
}
```
###Step 2
#### Java Code
```
	{
		mScrollDownLayout.setMinOffset(0);
        mScrollDownLayout.setMaxOffset(800);
        mScrollDownLayout.setExitOffset(1674);
        mScrollDownLayout.setToOpen();
        mScrollDownLayout.setIsSupportExit(true);
        mScrollDownLayout.setAllowHorizontalScroll(true);
        mScrollDownLayout.setOnScrollChangedListener(mOnScrollChangedListener);
    }
		
```
