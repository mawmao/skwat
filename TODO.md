
# TODO

- barcode scanning
- confirmation

## forms: 
1. field data - validation
2. cultural management - validation, conditional entries (low prio), specific review ui
3. nutrient management - validation
4. production - validation
5. monitoring visit - validation
6. damage assessment - validation

todo: remarks?
todo: images on all forms
    - create `FormImageEntity` that stores data about local path, remote path, and which form+mfid it is
    the following would also change:
        - field_activities table payload
        - 
    research: image compression
    
form flow would be:
1. Insert FormEntryEntity in Room with payloadJson (optional initial data, empty images array).
2. Save each image to internal app storage under /forms/form_<formId>/.
3. Compress / convert to WebP (1080–1440px, quality 75–85%).
4. Insert a FormImageEntity for each image:
   `FormImageEntity(formId = formId, localPath = "/files/forms/form_$formId/img_1.webp")`
5. When syncing:
   - Fetch all FormImageEntity for the form.
   - Upload each image to Supabase Storage.
   - Update each FormImageEntity.remoteUrl with the returned URL.
   - Build image_urls array:
     `val imageUrls = images.mapNotNull { it.remoteUrl }`
   - Build Supabase payload 
   - Send JSON payload to Supabase.

6. Post-sync cleanup
   - Mark FormEntryEntity.synced = true.
   - [Optional] delete local files to save space. (and only show past images when connected on internet)
        or cache max 50 forms (offline) and delete aggressively

## review screen

- form-specific review ui
- button should be anchored on the bottom


## less priority bugs

- confirmation on navigate back in form flow

- visibility of wizard navigation buttons
 
- disabled ui of textfields
 
- confirmation
 
- dropdown menu items ui
 
- field data location cascading field transition (should come from below last field)



## to test intensively

- permission handling

- coordinates handling (if scanned without turning on location)
 
