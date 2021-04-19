readme.txt
Casey Hird, C12932552
Michael Cole, C16183835

INSTRUCTIONS:
Unzip the project directory and run in the emulator (or test phone).

DESIGN CHOICES:
Note that in each search instance (the capture image and search by text buttons in the main activity and the search button in the results activity) the button is placed either near the right side or the bottom of the screen so that the user can reach it more easily. Text elements are styled consistently, and font size is used to separate headers from details.
Next, in the results activity, text color and bold are used to differentiate between links, titles, and details. Also, in the main activity we use icon (a camera and a question mark) to make their function clear to the user and to make the app's appearance more interesting. Finally, in the confirm activity, we assume that the default choice for the user is to confirm their picture, so we place the confirm button on the right. The retake button (which in this case is like a "cancel" button) is on the left.

REFERENCES:
Zybooks is referenced for many key elements including layouts, widgets, the accelerometer, dialogs, and the recycler view. 
Stackoverflow was also used for implementations of bitmap conversion and for sensing a shake event with the accelerometer.
The google vision label detection api was used to search with images, and the Bing search API was used for text searches.
Finally the icons were collected from various sources referenced in the code.