# Spell Meaning — Android App

An app where you type any word, it looks up the meaning using Google search,
and every result is automatically saved so you can review your full search
history later.

## How it works

1. **MainActivity** — type a word, tap Search. The app requests Google's
   search results page for `define <word>` and parses out the dictionary
   snippet Google sometimes shows.
2. **Auto-save** — as soon as a meaning is found, it's saved immediately
   (no extra button press) to:
   - `history.json` inside the app's private storage (used to power the in-app list)
   - `spell_meanings.txt` in your device's **Downloads** folder — a plain text
     file you can open with any file manager / text viewer, share, or back up.
3. **HistoryActivity** — "View Saved List" shows every word you've searched,
   newest first. Tap any entry to open the full meaning in **DetailActivity**,
   where you can also delete it.

## ⚠️ Important — please read before relying on this

Google does **not** provide a free, official API for word definitions. The
only way to get a definition "from Google" is to scrape the HTML of a normal
Google search results page, which this app does. That comes with real
limitations:

- Google's HTML structure/class names change periodically without notice —
  if the app suddenly stops finding meanings, the parsing selectors in
  `GoogleMeaningFetcher.kt` likely need updating.
- Automated/repeated requests can get rate-limited or shown a CAPTCHA page
  instead of results (the app will just report "couldn't find a definition"
  in that case).
- Scraping Google search results this way is against Google's Terms of
  Service for production/commercial use. This is fine for a personal or
  learning project, but **don't publish this to the Play Store** without
  switching to a proper licensed source.
- **Recommended alternative** if you want something reliable long-term: swap
  `GoogleMeaningFetcher` for a real dictionary API such as
  [dictionaryapi.dev](https://dictionaryapi.dev) (free, no key needed) or
  Merriam-Webster's API. The rest of the app (saving, history, detail view)
  doesn't need to change at all — only that one file.

## Opening the project

1. Install **Android Studio** (Hedgehog or newer).
2. Open the `SpellMeaning` folder as a project (File → Open).
3. Let Gradle sync — it will pull in OkHttp, Jsoup, Gson, and Coroutines.
4. Run on an emulator or a real device (minSdk 24 / Android 7.0+).

## Project structure

```
SpellMeaning/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/spellmeaning/
│       │   ├── MainActivity.kt        # search screen
│       │   ├── HistoryActivity.kt     # saved word list
│       │   ├── DetailActivity.kt      # single entry review + delete
│       │   ├── HistoryAdapter.kt      # RecyclerView adapter
│       │   ├── WordEntry.kt           # data model
│       │   ├── StorageHelper.kt       # JSON + Downloads file persistence
│       │   └── GoogleMeaningFetcher.kt# Google scraping logic
│       └── res/layout, values/...
├── build.gradle
└── settings.gradle
```
