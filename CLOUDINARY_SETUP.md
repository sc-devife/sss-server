# Cloudinary Image Storage

Image uploads (organization logos, hotel/activity/destination galleries, and any
future module) are stored in [Cloudinary](https://cloudinary.com) instead of local
disk. This replaces the old `FileStorageController` implementation that wrote
files under `${UPLOAD_DIR}` and served them back via `/files/**`.

## Required environment variables

No defaults are set for these — the app will fail to start without them, same as
`DB_USERNAME`/`DB_PASSWORD`/`MAIL_USERNAME`/`MAIL_PASSWORD`.

| Variable | Description |
|---|---|
| `CLOUDINARY_CLOUD_NAME` | Cloudinary account's cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret — treat as a real secret, never commit it |

Find these under **Dashboard → Product Environment Credentials** in the
Cloudinary console. Set them as real environment variables in every environment
that runs this app (local shell, IDE run configuration, CI, production).

## What changed

- **`CloudinaryConfig`** (`configuration/CloudinaryConfig.java`) builds the
  `Cloudinary` bean from the three properties above.
- **`CloudinaryService`** (`service/files/CloudinaryService.java`) is the single
  reusable entry point for every module:
  - `upload(MultipartFile)` / `upload(MultipartFile, String folder)` — validates
    content type (`image/jpeg`, `image/png`, `image/webp` only) and size (8 MB
    max) before uploading, returns a `CloudinaryUploadResult(secureUrl, publicId)`.
  - `deleteByUrl(String secureUrl)` — derives the `public_id` from a previously
    stored secure URL and deletes that asset. No-ops for null/blank/non-Cloudinary
    URLs (e.g. a legacy local `/files/...` path from before this migration), and
    never throws — a stale asset failing to clean up shouldn't fail the request
    that's replacing it.
  - `deleteRemoved(List<String> previousUrls, List<String> currentUrls)` — for
    image-list fields (hotel/activity/destination galleries): deletes every URL
    present in `previousUrls` but no longer in `currentUrls`.
  - `deleteByPublicId(String)` — lower-level delete if you already have the id.
- **`FileStorageController`** (`POST /api/files/upload`) is unchanged from the
  frontend's point of view — same route, same `multipart/form-data` request, same
  `{"url": "..."}` response shape. It now also returns `"public_id"` alongside
  `"url"` (additive, non-breaking) and `url` is a full `https://res.cloudinary.com/...`
  address instead of a relative `/files/<uuid>.ext` path.
- **Old-image cleanup on replace** — added at every place an image field gets
  overwritten:
  - `OrganizationsHelper.updateOrganizations()` — deletes the previous
    `logo_file` when it changes.
  - `HotelServiceImpl.update()` / `ActivityServiceImpl.update()` /
    `EscapePointsHelper.updateEscapePoint()` — diff the old vs new `images` list
    and delete whatever was dropped.
- **`EscapePointMapper.updateFromDto`** now uses
  `NullValuePropertyMappingStrategy.IGNORE` (matching `HotelMapper`/
  `ActivityMapper`, which already had it) — an update request that omits
  `images` leaves the existing gallery untouched instead of nulling it out. This
  was a pre-existing inconsistency between the three mappers; it became load-bearing
  for correctness once cleanup logic exists, since without it an update that
  omits `images` would look like "every image was removed" and delete them all
  from Cloudinary.

## What did NOT change (by design)

- **Local `/files/**` static serving, `app.upload-dir`, and the
  `spring.servlet.multipart.*` limits are kept** — purely as a read-only fallback
  so any image URL stored *before* this migration (e.g. `/files/<uuid>.jpg`)
  keeps resolving. `deleteByUrl`/`deleteRemoved` correctly skip these (they don't
  match `res.cloudinary.com`), so no error occurs when a legacy value is replaced —
  it's just never deleted from Cloudinary (there was never anything there to delete).
  If you do a full backfill migration of legacy images into Cloudinary later, this
  static-serving path and its `SecurityConfig`/`JwtAuthenticationFilter` public-path
  carve-outs can be removed.
- **The frontend API contract is unchanged.** `resolveFileUrl()` in the frontend
  (`src/lib/files.ts`) already passes through absolute `http(s)://` URLs unmodified,
  so it needed no changes.

## Dependency

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Verifying locally

```bash
curl -X POST http://localhost:8080/sss/api/files/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/image.png;type=image/png"
# => {"url":"https://res.cloudinary.com/<cloud>/image/upload/v.../sss/<id>.png","public_id":"sss/<id>"}
```

Uploads land in the `sss/` folder in your Cloudinary account by default (pass a
different folder to `CloudinaryService.upload(file, folder)` if a module wants
its own).
