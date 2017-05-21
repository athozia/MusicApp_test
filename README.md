# MusicApp_test //only function and service

*main source code is in src->main->java->tam->musicplayer
*all in repository is the music application, only build and libs are not include

**only prototype as a part of CPE333

This music app contain 5 main component class 
1. Playlist
2. Track -> base track from stroage
3. MainActivity -> all class are called and ran here
  3.1 GUI_Adapter -> adapt and create some UI that can not be created in MainActivit xml
  3.2 NetworkContent
4. MusicPlayerService -> service for operation of music player
5. MusicController -> music player basic function : play,pause,etc.

But, this is a test work, so only MainActivity, MusicPlayerService and MusicController will be created and implemented (without NetworkContent and others GUI_Adapter)

