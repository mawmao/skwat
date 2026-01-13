
# considerations

- adding field data in `FormEntryEntity` for display in history

# todo

- form history [in progress]

- auth
    - remember feature
        - could check if first time login and show an onboarding
        - could start implementing shared preferences
    - otp feature

- form barcode scanning
    - check if there is already an entry of that form with that mfid when clicking on a form
        - get from current room database entries

- form confirmation
- form wizard:
    - remarks?
    - images handling [solution](#image-handling-solution)
      - image compression
      - image caching & storage optimization
    - remaining form validations
    - image box should expand and show the image as a modal when clicked and has value

- sync
  - improve error handling & rollbacks
  - manual

- settings

# issues

#### high

-

#### medium

- `avg_plant_height` in MonitoringVisit still validating when it is chose then hidden
- image field add error validation message
- permission handling
- coordinates handling (if scanned without turning on location)
- review screen button should be anchored on the bottom
- confirmation on navigate back in form flow
- optimization on wizard fields (recomposes on change of a single field, could be from answers map)

#### low

- visibility of wizard navigation buttons
- disabled ui of textfields
- dropdown menu items ui
- field data location cascading field transition (should come from below last field)

# notes

## image-handling-solution

- [optional] image compression

- create `FormImageEntity` that stores data about local path, remote path, and which form+mfid it is
- the following would also change: `field_activities` table payload

- form flow would be:
    1. insert `FormEntryEntity` in room with payloadJson (optional initial data, empty images array).
    2. save each image to internal app storage
    3. compress / convert to webp (1080–1440px, quality 75–85%).
    4. insert a `FormImageEntity` for each image:
       `FormImageEntity(formId = formId, localPath = "/files/forms/form_$formId/img_1.webp")`
    5. when syncing:
        - fetch all `FormImageEntity` for the form.
        - upload each image to supabase storage.
        - update each `FormImageEntity.remoteUrl` with the returned URL.
        - build image_urls array:
          `val imageUrls = images.mapNotNull { it.remoteUrl }`
        - build supabase payload
        - send json payload to supabase.

    6. post-sync cleanup
        - mark `FormEntryEntity.synced = true`.
        - [Optional] delete local files to save space. (and only show past images when connected on
          internet) or cache max 50 forms (offline) and delete aggressively

