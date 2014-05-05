MPC
===

<a href="http://mpd.wikia.com/wiki/Music_Player_Daemon_Wiki">MPD</a> client for android with an 
emphasis on aesthetics and simplicity. This app is not a typical MPD client, in fact it doesn't currently support manual playlist creation. Playlists are handled in the background, all you have to do to play a song is select it and the list you are viewing it in will be enqued.  

Available free on the app store (https://play.google.com/store/apps/details?id=thelollies.mpc) 

Using the Source
================

This app relies on two libraries:
  - <a href="https://github.com/thelollies/MPCLibrary">MPClient</a>
  - <a href="http://actionbarsherlock.com/">ActionBarSherlock</a>

Setting them up in Eclipse requires the following:
  1. Follow the instructions on <a href="http://actionbarsherlock.com/usage.html">this page</a> to add ActionBarSherlock (use method 1).
  2. Create a new Java Project and import MPClient. Right-click the MPC project and select Properties. Then navigate to "Java Build Path" and select the "Projects" tab. Select "Add..." and add the MPCLibrary project. Then select the "Order and Export" tab, tick the box next to the MPCLibrary project and move it to the top using the "Up" button.
  


