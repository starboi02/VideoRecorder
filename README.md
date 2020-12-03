# VideoRecorder
App for recording video

## Login
I have used Firebase Google Authentication and stored the logged in user's name and uid to Firebase Cloud firestore. 

I am also checking if the current user is admin (used one of my ids for this purpose) or not and storing it using sharedprefrence.

## Video Recording
I have used MediaStore.ACTION_VIDEO_CAPTURE for capturing videos and used it along with MediaStore.EXTRA_OUTPUT to specify the location of the resultant video so that I can delete it after uploading it to the Cloud.

## Storing Video to Cloud
I have stored the file to Firebase Storage and generated the downloadable link which is thus stored to Fireabse Cloud Firestore.

After storing the file to cloud it it then deleted from the phone storage and is not availabe to the user.

## Admin Panel
For admin I have added an edit text along with send button. The message sent is stored to Firebase Cloud Firestore and is hence displayed after fetching it to the users.
