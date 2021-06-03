# Hour Calculator
 App for calculating hours

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/K3K64AQVM)

# Status
[![GitHub issues](https://img.shields.io/github/issues-raw/corylowry12/Hour-Calculator?style=for-the-badge)](https://github.com/corylowry12/Hour-Calculator/issues)

# Whats New in Version 7.1.1
* Fixed issue with text not displaying in history activity on some devices
* Tweaked left and right padding for warning text view in trash activity

# Whats New in Version 7.1.0
* Fixed version info not going back if the user clicked a link and went into the web view and the backed out using the back button and then tried to exit the version info activity using the back button
* Fixed the rare occasion the app would round wrong
* Now a toast message pops up saying the hour was not saved if you exit the edit activity without clicking the save button
* Splash screen length is now shorter if you have performance mode enabled
* Fixed issue where if the user went to the edit activity then used the menu to go to settings and change the theme, and then hit the back button to go back to the edit activity, the theme would not apply
* Fixed issue where graph activity would not fade in when you went back to it using the back button
* Fixed issue where there is a slight flash from the activity recreating in the graph activity if you went to it using back button
* Made app more lightweight by preventing activities from having multiple instances in a stack
* Added toast messages that pop up when you manage your stored hours. _Ex. *Click Delete* Toast message will pop up saying item deleted_
* Removed some deprecated dependencies
* Fixed issue with app showing wrong toast message if the hour you were trying to edit does not contain am or pm
* Wrapped each hour entry in a card view to make it more consistent to match the settings activity
* Changed card view shade of gray for dark theme only
* Removed the line between each entry in history and trash activities
* Changed the way text is set in history resulting in more optimization and less code
* All new animation when changing themes. Now it will fade to the other theme
* Fixed issue with stuff in settings still having a different vibration intensity
* Tweaked about me dialog text
* Redesigned settings activity
* In the process of redesigning settings, the end padding for the subtitle text views are now increased
* Fixed text views not sliding up in trash on animation if performance mode is disabled
* Fixed scroll animation in trash being different than history scroll animation
* Better error checking for wages calculation in history activity to prevent any app crashes
* Fixed the dialog showing up saying the app needed permissions to export not vibrating when you clicked the buttons
* Fixed dialog explaining why permissions are needed being different colors than all the other dialogs
* Fixed the app not re-requesting for permissions if the user denied them. Now it takes you to the settings to manually set permissions
* Fixed issue with hint in search bar in history not supporting multiple languages
* Fixed save button in edit activity not supporting multiple languages
* Added ads to edit activity
* Now if you deny file permissions in settings, and try to export later, it will take you to settings and you can manually change permissions
* Tweaked left and right padding for output text view on main activity so it now no longer goes all the way to the edge of the screen
* Fixed issue with app crashing if you entered a dash in the text boxes in main activity
* Fixed issue with app crashing if you entered a dash in the text boxes in edit activity
* Fixed app crashing when launching activity on some devices
* Will now automatically set focus back to in time text box on clear button clicked whether they're empty or not
* Removed the everything is cleared toast message if all text boxes were empty
* Webview for websites will now go to dark them if dark theme is enabled in the app, but only if you have android 11 or higher
* Fixed search hint in trash and history not being white or black depending on the theme

# Whats New in Version 7.0.2
* Fixed crashing in webview when it would launch on some devices
* Now links in version info activity vibrate when you click on them
* Tweaked left padding for certain text views in version info activity in every layout size
* Fixed people not receiving update notifications even when update notifications were enabled

# Whats New in Version 7.0.0
* Added ability to click on item in the graph activity and have a textview display contents of that SQLite row
* Fixed txt export not supporting multiple languages
* Fixed csv export not supporting multiple languages
* Changed position of **Clear** and **Calculate** buttons
* Fixed issue with text not wrapping properly for **Calculate** button
* Added animated splash screen
* Fixed spinners _(drop down menu to select **AM** or **PM**)_ having a slight delay before it loads contents upon app startup
* Fixed issue causing black/white screen for a second before app actually loaded
* Fixed graph having double tap to zoom on entries _(it is removed)_
* Changed Firebase notification icon
* _Tweaked_ text size of the text views on the main activity in every layout
     * Main Activity should now scale better on _all_ screen sizes
* Removed animation when changing theme in Settings Activity
     * _Resulting in smoother theme change and less frame drops_
* Helped add support for multiple screen sizes by adding ability to scale, even though the app technically already supported it
* Removed some unused code resulting in smaller app size
* Wages edit text in the settings activity will now clear focus when you hit the enter key after you've entered your wages
* Optimized the code that sets the value for the update notification switch
     * _Resulting in less code_
* Optimized the code that sets the value for the _"Trash Automatic Deletion"_ switch
     * _Resulting in less code_
* Optimized the code that sets the value for the "_Toggle History_" Switch
     * _Resulting in less code_
* Optimized the code that sets the value for the "_Toggle Dark Mode_" Switch
     * _Resulting in less code_
* Optimized the code that sets the value for the "_Toggle Vibration_" switch
     * _Resulting in less code_
* Optimized the code that sets the value for the "_Toggle Break Text Box_" Switch
     * _Resulting in less code_
* Fixed the "_Bug Report_" subject data for feedback emails not supporting multiple languages
* The change theme action when switch is checked is now delayed by 200 milliseconds to prevent lag and frame drops upon activity recreation
* Fixed text color of radio button on donate dialog being white if theme is set to light
* Fixed text color of text on about me dialog being white if the theme is set to light
* Different icon for enter key on wages edit text in settings activity
* Fixed wages edit text in settings not clearing focus or hiding keyboard properly. _Should_ work better now.
* Fixed issue with focus on break text box not clearing if it was active and you hit the back button
* Fixed issue where keyboard would disappear for a second then reappear if you hit enter and the break time text box was visible
* Fixed app crashing if you entered the wrong type of  value in the break text box _(Example: "5:00" or "5.0")_
* Removed the shortcuts if you long pressed on the icon on the home screen
* Tweaked layout on some of the _"Other Apps"_ layouts to make links better align with their images
* Added **X** (Close Button) to the "Other Apps" bottom sheet to give you another way to close the dialog
* Fixed the slight delay where if you changed the theme, or toggled break text box and hit the back button, it would go to main activity and there would be a slight delay from recreating the activity. That is fixed now.
* Added **Copy** and **Share** buttons to the web view in which you can view the dependencies I use.
* Fixed floating action button in trash activity not being animated when made visible on scroll change
* Fixed floating action button in history activity not being animated when made visible on scroll change
* Slightly increased text size for some layouts on the version info activity
* Slightly increased bar chart animation when loading data in graph activity
* Added better formatting for output text on main activity if you had a break. Now total hours with and without break will be on separate lines.
* Changed the way the animations for the graph are ran, resulting in less code. Instead of 8
     ```kotlin 
     barChart.setAnimateY(600)
     ```
There is only one global, one.
* Now when you toggle history switch in settings, a snackbar message will pop up
* Now when you toggle break text box switch in settings, a snackbar message will pop up
* Newly added dialog in settings if you try to toggle history. If you're changing it to off, it will ask you what you would like to do with your hours, nothing, trash, or delete.
* Now you can view change log/release notes on this github, not in app as it was previously
* Fixed issue with delete in history activity deleting the wrong entry
* Fixed issue with move to trash in history activity moving the wrong hour to trash
* Fixed issue with delete in trash activity deleting wrong hour
* Fixed issue with restore in trash activity restoring the wrong hour
* Optimized the way hours are deleted, moved to trash, and restored in trash and history resulting in it being lighter weight
* App now automatically deletes cache upon app startup to prevent app size being larger than needed
* Removed all firebase logging to make app more lightweight 
* Fixed the hint in the search box in history and trash not supporting multiple languages
 * Fixed web view constantly trying to redirect to your browser
* Fixed issue with you being able to input minutes as long as three numbers. That's no longer an issue
* Fixed graph not display the last entered as the first bar. It would display the first item as the first bar. Now that's fixed
* Fixed issue with text box allowing you to enter 5 digits without a colon
* Fixed the issue with break time textbox not vibrating when clicked
* Added ability to edit stored hours
* Break text box is now enabled by default
* All new animations when switching activities
* Fixed issue with double back press not closing the app
* Fixed issue with "Yes" on delete all from history alert dialog not being translated to other locales
* Now break time text box does not allow you to enter more than 3 numbers
* App will now automatically remember if you previously selected am or pm for the times on both input and output
* Break text box now only allows you to enter 3 numbers instead of 5 like previously
* Removed requirement for the user to click the text view to enter a colon, now it does it automatically
* Fixed issue where the screen would flash if you went to trash activity using the back button
* Now the back arrow in the title bar will go to previous activity. Doesn't take you back to main activity any more
* Fixed issue with app calculating hours wrong if out time hours was 12 and spinner was set to the opposite value as in time spinner
* Fixed issue with slight flash when going back to patch notes activity via the back button
* Now whenever you move item to trash, it also transfers date, doesn't store time it was moved to trash any more
* Now you can drag scroll bars in history activity
* Now you can drag scroll bars in trash activity
* Fixed issue with 3 dot menu for edit, move to trash, etc in the history icon in history being red in certain layouts
* Fixed issue with 3 dot menu for delete, restore, etc in the trash icon being white in certain layouts no matter the theme
* Fixed issue with 3 dot menu for delete, move to trash, etc in the history icon being white in certain layouts no matter the theme
* Fixed issues with webview loading if you tried to load webview and leave webview and go back using back button
* Added progress bar to webview
* Added refresh button in the menu to webview
* Added swipe to refresh to webview
* Added Dynamic subtitles to action bar in webview
* Fixed 3 dot menu for history or trash management not vibrating when clicked
* Items in "Other Apps" bottom fragment now vibrate when you click on them
* Now all buttons in all dialogs that pop up should vibrate when you click them, if vibration is enabled
* Now the options menu in every activity should vibrate when you click it
* Now there's a cancel button on the "donate" dialog
* Fixed issue with 3 dot menu for delete, restore, etc in the trash icon being white in certain layouts no matter the theme
* Fixed issue with 3 dot menu for delete, move to trash, etc in the history icon being white in certain layouts no matter the theme
* Now if the wages edit text will lose focus if you press the back button while it has focus
* Now the 3 dot menu in history and trash will vibrate when clicked

Use this app to take input time, out time time and/or break time, to display your time in a decimal format, rounded to the second decimal place.

This app will also store the hours in a SQLite database, as well as let you view it and calculate total pay, and how many hours are stored, and the total hours of all the hours combined.

It also has various customization options to make the app suit your needs. 

Has an activity that displays a graph to visualize the hours yuo have worked on a bar graph.

I created this app in order to help me calculate my hours at work.

# Screenshots
<p float="left">
<img src=/Screenshots/screenshot1.png width="200" height="400"/>
<img src=/Screenshots/screenshot2.png width="200" height="400"/>
<img src=/Screenshots/screenshot3.png width="200" height="400"/>
<img src=/Screenshots/screenshot4.png width="200" height="400"/>
<img src=/Screenshots/screenshot5.png width="200" height="400"/>
</p>
