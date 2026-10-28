# Hallway for Android TV

[The hallway](https://ariamokr.github.io/hallway/) ([AriaMoKr/hallway](https://github.com/AriaMoKr/hallway)),
a corridor whose doors lead to other web environments, on an Android TV, walked with its remote.
The app is the site, full screen, in the TV's own WebView: there is nothing of the hallway in it,
so it is always the published one.

## Installing it

The APK is on this repository's [Releases](https://github.com/AriaMoKr/HallwayApp/releases): each
release has `hallway-tv.apk`, and the latest one is always at

```
https://github.com/AriaMoKr/HallwayApp/releases/latest/download/hallway-tv.apk
```

(`tv-latest`, a prerelease, is the latest build from `main`.) It needs Android 7 or later on the
TV, and was made on Android TV 12.

**With a computer**, over the network, with adb (Android's platform tools):

1. On the TV, turn on the developer options: Settings → System → About, then press OK on
   **Android TV OS build** seven times.
2. In Settings → System → Developer options, turn on **USB debugging**, and **Network debugging**
   (or ADB over network) if the TV lists it separately.
3. Find the TV's address: Settings → Network & Internet, then the network it is on.
4. On the computer:

   ```bash
   adb connect <tv-address>
   ```

   The TV asks whether to allow debugging from this computer; choose to always allow it.
5. Install the APK:

   ```bash
   adb install -r hallway-tv.apk
   ```

**Without a computer**, with a downloader app from the TV's Play Store (one that downloads a file
from an address and opens it, such as Downloader): give it the address above. The first time, the
TV asks whether that app may install apps; allow it, and the APK installs.

The app is **Hallway** in the TV's apps, with a banner of its doorway (the Google TV home screen's
row of apps shows its round icon instead). It opens full screen.

**Updating.** A newer APK signed with the same key installs over the old one, keeping its settings.
A release is signed with the owner's key when one is set up (see "Signing"); without one, each build
is signed with a debug key made for it, and Android refuses to put it over another, so uninstall the
old one first (which forgets the app's settings and your name in shared sessions):

```bash
adb uninstall io.github.ariamokr.hallway
```

## Using it

| Key | Walking | In the card |
|---|---|---|
| ↑ ↓ | walk | from control to control |
| ← → | turn | change a choice (Move, Turn, Motion) |
| OK | use what the dot in the middle is on: a door, a panel button, the floor | press, tick, or bring the keyboard up in a box |
| Menu, or hold Back | the card: the doors list, Together, Settings | walk again |
| Back | back a page; with none to go back to, leave the app | walk again |

From a room behind a door, Back comes back to the hallway in front of that door. The
hallway's README, [The TV app](https://github.com/AriaMoKr/hallway#the-tv-app), says more,
including joining a shared session by typing its room. A gamepad paired with the TV works too:
the WebView passes it to the page through the Gamepad API. With TalkBack on, the arrows are
TalkBack's and don't walk, but the remote's channel up and down walk the hallway, and its rewind
and fast-forward turn; a gamepad walks and turns too (see "The page and the app").

Links to pages on `ariamokr.github.io` stay in the app. Any other link goes to the app on the TV
that opens it (YouTube's, for a YouTube link; a choice when several do). When none does, a message
gives the address, to open on a phone or a computer.

**Behind the doors.** Every site there takes the remote as the hallway does (AriaMoKr/common's
`tv.js`): the arrows walk and turn, OK uses what the dot in the middle is on, Menu goes to the card
(to the control last left there, else the site's first), and Back takes one step back at a time
before it comes back to their door in the hallway; nothing was found that Back skips or takes
twice. The remote steps onto a text box and past it with the TV's keyboard down, and OK on the box
brings it up (see "The keyboard" below). On the remote:

- **vrlbry**: OK takes the book at the dot and reads it, and ← → turn its pages. Menu goes to the
  Controls help, or with a book out to its first button (Read, then Contents once it's open), and
  reaches the book's buttons (Contents, the text size, Theme, 1 page, In front of me, Put back,
  Text view) or, with no book out, the card: the search, the library's Kiwix and ZIM controls, each
  library's Close, Collapse and ← Hallway. ↓ from the search box goes into its results, and OK
  opens the one chosen. With the dot on the catalogue stand's lists or on a book's contents, ↑ ↓
  scroll them and OK chooses. The text view scrolls with ↑ ↓, and ↓ past its toolbar goes to the
  first link on the screen below it, with the view where it was (with no link there, the focus
  stays on the toolbar). Back closes the contents, then the text view, then puts the book back,
  then leaves.
- **Breadboard 8-bit**: OK presses what the dot is on: the panel's buttons (Stop and Run stop and
  run the clock), a switch (SW2 to manual stops the clock), a pot (OK holds RV1, ← → turn it, from
  0.8 to 21 Hz here, and OK or Back lets go). Menu reaches the bar: the two ways back, Controls (the
  panel's buttons), Wires, Comfort and Start shared session. Back leaves the bar, then flies back to
  the table, then leaves. The room is drawn below the bar, from a home view a little further back
  and lower, so the bar's "VR view" no longer sits over the panel. A tap of ← or → turns 1.5°, and
  the dot takes a part just beside it, which then holds it for two taps: from SW1, one tap left
  lands on RV1 and one right on SW2. The label over the dot names the part ("SW2 auto/manual:
  auto"), and a pot's says how the remote turns it: "RV1 speed · OK to hold, then ← and →, OK or
  Back to let go · 4 Hz". Held, ↑ soon goes fast: two seconds took the view past the end of the
  table (Back brings it home).
- **Outside**: OK on the ground walks there, on the bench sits, and sitting opens the book; ← → turn
  its pages, and OK opens its contents (↑ ↓ through them). Menu reaches the card (← Hallway, Sit on
  the bench, Read the book, Start shared session, Settings) and, reading, goes to Contents, the
  first of the book's buttons (Contents, the text size, One page, Theme, Text view, Bring the book
  in front of me, Get up). Sit on the bench and Read the book in the card give the focus back to the
  view, so ← → turn the pages at once. Sitting, the hint says "OK: read the book · Back: stand up ·
  Menu or hold Back: the card". In the text view ↑ ↓ read on, ← → go a page and Menu reaches its
  toolbar. Back leaves the card, then closes the text view or the contents, puts the book down,
  stands up, then leaves. Nothing was found out of reach.
- **music** has vrlbry's remote, and what's said of vrlbry above holds for it too. Its view rests
  tipped down a little, as outside's does, and the dot lands on the gramophone (from 2 m, on its
  horn): OK plays the sounds of a book picked at random. In a book's contents ← → choose Contents
  or ♪ Sounds, and ↑ ↓ go through the rows and Play all, which OK plays. In the text view, OK goes
  to the first link on the screen, ↑ ↓ from link to link, and OK on a Listen link plays it; Next
  then goes on to the book's next sound. While a sound plays, the Now playing bar's Pause (or
  Play), Stop and Next sound are stops of the card, after the search box (in the text view, after
  its toolbar), and OK presses them; Stop gives the focus back to walking. The remote's media keys
  press them wherever the focus is: Play/Pause, Play, Pause, Stop and Next. A Bluetooth remote's
  Pause (HID consumer usage 0xB1, which reaches the page as `Pause`) pauses, and pressed again stays
  paused. The sound goes out as media, so the TV's own volume sets how loud it is: at 15 of 100 the
  app's stream played on the TV's speakers, the page's synth peaking at 0.59 (−18 dB RMS), but
  nobody was in the room to hear it. Back closes the text view, then puts the book back, then
  leaves. In screenshots (not seen from across the room) the tipped view looks right: the shelves
  fill it, with the rug below; at the way in, the scores on the wall are cut by the top edge. Still
  awkward: with the dot on the gramophone, the hint still says OK takes the book at the dot. Some
  Listen links' sounds are a few notes, over in three seconds.
- **art** and **video** have the template's remote. OK on the doorway goes back to the hallway
  (tried in video). Menu reaches ← Hallway, Start shared session and Settings: OK starts a shared
  session (then Leave, Wave, your name and the address to join from) or opens Settings, whose
  choices ↑ ↓ go through and ← → change. Back leaves the card, then leaves.

## The page and the app

The app is one full-screen activity with the system's WebView (`MainActivity`), on
`https://ariamokr.github.io/hallway/`. JavaScript, DOM storage (the settings, and the name a shared
session gives you, are in `localStorage`), WebGL and media without a gesture are on; file and
content access and mixed content are off. The user agent ends with ` HallwayTV/<version>`, which is
how the hallway knows to show its TV view (bigger text, focus rings to be seen across the room, no
Enter VR or Fullscreen, the remote's keys).

The remote's D-pad reaches the page as the arrow keys and its OK as Enter (WebView's own mapping;
OK's key event has no `code`, only `key`). On a text box the app sends OK on as Enter itself, as
the WebView would keep it (see "The keyboard" below). The app keeps Back and Menu, and asks the
page:

- **Back** (a short press) calls `window.hallwayTV.back()`. When the page returns `true` it used the
  key (the hallway's card gives the focus back to walking); otherwise the app goes back a page, or,
  with none to go back to, leaves.
- **Menu**, or holding **Back**, calls `window.hallwayTV.menu()`.

A page without `hallwayTV` gets the plain Back. Every site behind the doors has it
(AriaMoKr/common's `tv.js`, `answerApp`), so Back there goes one step at a time. A link that leaves
the origin opens elsewhere, as above; one that stays is loaded in the app.

What was found on the TV it was made on (Sony BRAVIA 4K VH21, Android TV 12, WebView 155):

- **WebGL 2** works (Mali-G52, textures up to 4096), so the system WebView does; GeckoView, the
  fallback had it not, isn't needed.
- **The Gamepad API** works: a paired gamepad reaches the page, and the hallway's gamepad controls
  work as they do in a browser. The app passes nothing in itself.
- **The frame rate.** The TV's UI is 1920 × 1080 (a 960 × 540 page at a pixel ratio of 2), and
  the hallway holds 60 fps there, standing, walking the hall and facing a doorway close up, with no
  frame dropped. At a pixel ratio of 3 it begins to drop frames facing the doors; the hallway
  renders at 2 at most. Behind the doors (`tools/tv-check.mjs --url`, and the same timing in place,
  standing; a frame over 25 ms counts as dropped):
  - **vrlbry**, at a pixel ratio of 2: 60 fps with a book open, with no frame dropped; among the
    full shelves 57.5 fps (15 of 346 frames dropped), and 60 facing a half-empty bookcase; at the
    way in 53.5 fps (39 of 322 dropped, the slowest 5 % at 33 ms), the one place short of 60.
  - **Breadboard 8-bit**, at a pixel ratio of 2 with the clock running at 4 Hz: from the new home
    view, drawn below the bar (1920 × 838), 60 fps with no frame dropped; from the old one, 59.3
    (4 of 357 dropped), and run up to 64 Hz, 57.5.
  - **Outside**, in its lighter TV mode (a pixel ratio of 1, 960 × 540): 60 fps with no frame
    dropped at the way in, by the pond and reading on the bench.
  - **music**, at a pixel ratio of 2, its view resting tipped down: at the way in 52.1 and 52.3 fps
    (46–47 of about 314 dropped), as before and as in vrlbry; among the shelves 54.5, and 53.5
    turned (55.6 and 52.6 with the gramophone playing); facing the gramophone 59.9 with no frame
    dropped, and 58.4 while it plays (7 of 351 dropped).
  - **art** and **video**, each still an empty room, at a pixel ratio of 2: 59.9 fps with no frame
    dropped, at the way in and turned.
- **TalkBack** (14.2) speaks in the app. The page names the 3D view, which holds the focus while
  walking, with the hint under it as its description, and every control in the card. But with
  TalkBack on, the arrows don't walk or turn the view, and neither the page nor the app can make
  them: on a TV, TalkBack takes the D-pad's arrows before any app's window gets them (its
  `TelevisionNavigationController`, which lets them by only while an on-screen keyboard is up), so
  `dispatchKeyEvent` never sees them, whatever the page's roles say (`role="application"` on the
  view changes nothing). They move TalkBack's own focus through the page instead, in its order (up
  and left back, down and right on), reading each thing, and OK presses what it is on, so the
  card's settings can be read and changed. What still reaches the page with TalkBack on: OK on the
  view (TalkBack passes OK on for a web element it can't click), which uses what the dot is on;
  Menu and Back; the remote's channel and media keys (as `ChannelUp`, `MediaFastForward` and so
  on); and a gamepad, whose sticks walk and turn as they do without TalkBack. So the hallway walks
  with channel up and down and turns with rewind and fast-forward, as its hint says with TalkBack
  on: AriaMoKr/common's `tv.js` (`keyOf`, which its README's "The TV app" asks every site behind
  the doors to read its keys through) takes those four as the arrows. On this TV they reach the
  page, and the app stays in front.
- **TalkBack behind the doors.** In every site there, OK on the hallway's door goes through, and
  TalkBack reads the view's name and the hint as its description, as in the hallway ("A candle-lit
  library…", "Ben Eater's 8-bit computer on a table…", "A small garden…", "A music room…", "art: a
  floor, four walls and a ceiling…", "video: …"). Channel up and down walk and rewind and
  fast-forward turn in all of them, and Menu and Back do what they do without TalkBack; in the card,
  its arrows read each control and OK presses it.
  - **vrlbry**: Menu goes to the Controls help and the keyboard stays down, even with TalkBack's
    focus on the search box; TalkBack's arrows go through the card. Its hint, as the others' now,
    says that the channel and media keys walk and turn with TalkBack on.
  - **music** wasn't tried again with TalkBack on. Before its changes, Menu brought the keyboard up
    as vrlbry's did; once Back had hidden it, TalkBack's ↓ went on from the search box to the help,
    the hint and, while a sound played, the Now playing bar (what plays, its book, Pause, Stop and
    Next sound), which OK pressed. The media keys pressed them with TalkBack on too.
  - **Breadboard 8-bit**: Menu goes to ← Hallway, and TalkBack reads the bar (the two ways back, the
    title, Controls, Wires, Comfort, Start shared session), then the view and the status line.
  - **Outside**: Menu goes to ← Hallway, and TalkBack reads the card; OK on Read the book sits and
    opens it, and once Back has left the card, rewind and fast-forward turn its pages.
  - **art** and **video**: Menu goes to ← Hallway (or the control last left in the card), and
    TalkBack reads the card (the room's line, Start shared session, Settings, which OK opens), then
    the view, where it stays: once the hint has faded, it's no longer a stop.
- **The keyboard.** A text box the remote steps onto keeps the TV's keyboard down (common v0.6.1's
  `tv.js` gives it `inputmode="none"`), and it stays down stepping past: the hallway's Join box and
  your name in a shared session, vrlbry's and music's search and ZIM file address, and your name in
  art's shared session. OK on the box brings it up, without submitting the form, and OK on its
  keys then types. Left to itself, the WebView would keep the remote's OK (DPAD_CENTER) on a text
  box to show the keyboard itself, which `inputmode="none"` stops, and the page would get no key
  event at all, so `tv.js` would never hear the OK it waits for. So when the WebView says a text
  box has the focus (`onCheckIsTextEditor()`), the app sends OK on as Enter (`MainActivity`,
  `dispatchKeyEvent`), and `tv.js` brings the keyboard up. While the keyboard is up it takes OK
  before the app does. Once Back has hidden it, the box keeps the focus but no longer holds the
  keyboard down, so OK there is Enter as on a computer: it submits the box (the hallway's empty
  Join box says how a room is named). Stepping off the box and back, OK brings the keyboard up
  again. Not tried with TalkBack on.

## Building it

It needs JDK 17 and the Android SDK (`ANDROID_HOME`, or `sdk.dir` in `local.properties`); the
workflow's runner has both. With them:

```bash
./gradlew assembleDebug                 # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease test lint     # app-release.apk, the unit tests (Links) and lint
```

The version is `0.0.0-dev` unless given: `-PversionName=1.2.0 -PversionCode=42`. It must start
with a digit (the hallway looks for `HallwayTV/<digit>`).

## Developing

A debug build differs from a release in two ways: its WebView can be inspected (Chrome's
`chrome://inspect`, or `adb forward tcp:9333 localabstract:webview_devtools_remote_<pid>`, the
app's pid from `adb shell pidof io.github.ariamokr.hallway`), and it opens an address given to it,
such as the hallway's dev server on this computer, which it may load over plain http:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb reverse tcp:8089 tcp:8089
adb shell am start -n io.github.ariamokr.hallway/.MainActivity --es url http://127.0.0.1:8089/
```

The dev server must listen on IPv4 for adb reverse: in the hallway, `node tools/serve.mjs --host 127.0.0.1 --port 8089`.

`node tools/tv-check.mjs` checks the app on the TV connected over adb: it starts it (at `--url`, in
a debug build), sees that it is in front and full screen, and, in a debug build, reports what the
page gets (the user agent and TV mode, the viewport and pixel ratio, WebGL 2 and the GPU, the
Gamepad API) and its frame rate standing. It writes a report to `perf/` (`--help` lists the
options). Any site behind the doors can measure itself with it, on the live site or on a dev
server over adb reverse:

```bash
node tools/tv-check.mjs --url https://ariamokr.github.io/outside/
node tools/tv-check.mjs --url http://127.0.0.1:8089/     # a dev server on this computer, on IPv4
```

The hallway (a page with `window.__hallway`) is read from its own renderer, timed walking the hall
and facing a door as well as standing, and must be in TV mode. Any other page is timed as it
stands, with nothing pressed, and its WebGL 2 and GPU are read from a context the check makes and
drops, so the page's own are left alone.

Testing with scripts, mind that:

- `adb shell input keyevent KEYCODE_DPAD_UP` (and `_CENTER`, `KEYCODE_MENU`, `KEYCODE_BACK`) presses
  the remote's keys, but the page gets them with an empty `code`. They also go round TalkBack
  (injected keys skip the accessibility services' key filter), so to see what TalkBack does with
  the remote, make a virtual one: `adb shell hid -` reads JSON commands from its input, `register`
  with a HID descriptor (the consumer page's Menu Up, Down, Left, Right and Pick, Menu and AC Back
  as one-bit fields, and Channel Increment and Decrement, Rewind and Fast Forward for the keys
  TalkBack leaves to the page), then `report` and `delay`. A gamepad (the generic desktop page's Game Pad,
  with buttons and X, Y, Z, Rz) can be made the same way.
- Chromium leaves out of Back's history a page nobody pressed a key on, so a script that follows a
  link must press a real key on the page first, or Back leaves the app instead of coming back.
- On a TV with no touchscreen, WebView moves the focus with the arrows a page doesn't take
  (spatial navigation), and OK then follows the focused link.

## Releases

`.github/workflows/apk.yml` builds the APK, runs the unit tests and lint, and attaches the APK to a
GitHub Release, on every push to `main` that changes the app (the prerelease `tv-latest`, moved to
that commit) and on every tag `tv-v<version>` (a release of its own). To release 1.0.0:

```bash
git tag tv-v1.0.0
git push origin tv-v1.0.0
```

The version is the tag's; a build from `main` is named from the last tag by `git describe`
(`1.0.0-3-gabc1234`). The version code is the workflow's run number.

### Signing

Without a key of the owner's, a release APK is signed with the runner's debug key, made for that
build. To sign them all with one key (so that each installs over the last), make a keystore once,
keep it safe and out of the repository (`.gitignore` leaves out `*.jks`, `*.keystore` and `*.p12`),
and put it in the repository's secrets (Settings → Secrets and variables → Actions):

```bash
keytool -genkeypair -v -keystore hallway-release.jks -alias hallway -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 hallway-release.jks      # the value of HALLWAY_KEYSTORE_BASE64
```

| Secret | Value |
|---|---|
| `HALLWAY_KEYSTORE_BASE64` | the keystore, in base64 |
| `HALLWAY_KEYSTORE_PASSWORD` | its password |
| `HALLWAY_KEY_ALIAS` | the key's alias (`hallway` above) |
| `HALLWAY_KEY_PASSWORD` | the key's password |

A local release build signs with it too when `HALLWAY_KEYSTORE` is the keystore's path and the
other three are in the environment. A TV with a debug-signed build needs it uninstalled once before
the first build signed with the key.

## Licence

[MIT](LICENSE).
