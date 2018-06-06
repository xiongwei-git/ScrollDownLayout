# ScrollDownLayout
## Summary 概要
A custom Android Widget，support u use ViewPager in a ScrollView or ListView。  
帮助你能够在ScrollView或者ListView里面使用ViewPager，支持手势下滑退出页面

## Gif & Usage scenario Gif动画和使用场景
![1](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/2.gif)
![2](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/use.png)

## Video 视频
[Youtube](https://youtu.be/YVO7dljmwpw)

## Demo 例子
[Download Demo](https://github.com/xiongwei-git/ScrollDownLayout/blob/master/Art/app-debug.apk)

## Usage 使用方法
### Step 1
#### Gradle
```
dependencies {
    compile 'com.ted.coder.sdlayout:library:1.0.2'
}
```
### Step 2
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
