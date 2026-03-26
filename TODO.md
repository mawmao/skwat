

# todo


- settings on history detail to delete image cache
  
- auth
  - remember feature
    - the user should login with internet on first login
      - the app should store existing user credentials so they can log-in even online
      - could check if first time login and show an onboarding
       
- sync
  - improve error handling & rollbacks
  - manual
  



# issues

#### high

#### medium

- on history detail, check if image is present on internal storage, if not, use remote image
- review screen button should be anchored on the bottom
- confirmation on navigate back in form flow

#### low

- disabled ui of textfields (esp in login)
- dropdown menu items ui (font weight)
- form remarks?

# considerations

- form wizard:
    - [optional] image compression
    - [optional] delete local files to save space. only show past images when connected on internet
      - cache max 50 forms (offline) and delete aggressively
     
- adding field data in `FormEntryEntity` for display in history
- form confirmation: check if there is already an entry of that form with that mfid when clicking on a form
