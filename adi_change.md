# Changes Made to SVC Intercom Plugin

## Overview
Implemented a comprehensive speaker system with **dual broadcast modes** for the intercom plugin:
- **Global broadcasts**: Everyone in the world hears equally (original behavior)
- **Speaker broadcasts**: Positional audio from defined speaker locations with limited range

This allows admins to choose between world-wide announcements or realistic, location-based PA systems.

---

## New Files Created

### 1. `src/main/java/eu/projnull/spelis/svci/voice/Speaker.java`
**Purpose:** Data model representing a virtual speaker in the world

**Features:**
- Stores speaker location (world, x, y, z coordinates)
- Configurable broadcast range (1-1000 blocks)
- Named speakers for easy management
- Distance calculation to check if players are in range
- Helper method to get Bukkit Location object
- Thread-safe design

---

### 2. `src/main/java/eu/projnull/spelis/svci/voice/SpeakerManager.java`
**Purpose:** Singleton manager for speaker storage and persistence

**Features:**
- Manages all speakers across all worlds using ConcurrentHashMap
- JSON-based persistence (saves to `plugins/SVCIntercom/speakers.json`)
- Add/remove/list speakers by world
- Find speakers within range of a location
- Thread-safe operations
- Automatic save on modifications
- Automatic load on plugin initialization
- Uses Gson for serialization

---

### 3. `src/main/java/eu/projnull/spelis/svci/commands/handlers/SpeakerCommand.java`
**Purpose:** Command handler for managing speakers with flexible coordinate options

**Commands Implemented:**
- `/intercom speaker add <name> <range>` - Add speaker at player's current location (simplest)
- `/intercom speaker add <name> <range> <world>` - Add speaker using player's X/Y/Z in different world
- `/intercom speaker add <name> <world> <x> <y> <z> <range>` - Add speaker at specific coordinates (full control)
- `/intercom speaker remove <world> <name>` - Remove a speaker
- `/intercom speaker list [world]` - List all speakers (defaults to player's world)

**Features:**
- **Coordinates are truly optional** - 3 different ways to add speakers
- Tab completion for world names and speaker names
- Permission checks (`svcintercom.speaker.add`, `svcintercom.speaker.remove`, `svcintercom.speaker.list`)
- Validation for duplicate names and world existence
- Range validation (1.0 to 1000.0 blocks)
- Clear error messages and feedback

---

### 4. `gradlew`
**Purpose:** Gradle wrapper script for building the project
- Standard Gradle wrapper shell script for Unix-based systems
- Made executable for building the project without system Gradle

---

## Modified Files

### 1. `src/main/java/eu/projnull/spelis/svci/voice/BroadcasterState.java`
**Major Changes:**
- Added `BroadcastMode` enum with two values:
  - `GLOBAL` - Everyone hears equally, speakers ignored
  - `SPEAKER` - Only audible near speakers with positional audio
- Updated `Broadcaster` constructor to require a `mode` parameter
- Added `getMode()` getter method
- Mode is now stored alongside broadcast type and validated on creation

**Why:** Separates global broadcasts from speaker-based broadcasts, giving admins full control

---

### 2. `src/main/java/eu/projnull/spelis/svci/commands/handlers/LiveCommand.java`
**Major Rewrite:**
- Added optional `mode` parameter: `/intercom live <player> <world> <duration> [mode]`
- Mode can be `global` or `speaker`
- **Auto-detection**: If mode not specified, uses speaker mode if speakers exist, otherwise global
- Refactored to use helper method `executeLiveBroadcast()` for cleaner code
- Added import for `SpeakerManager` for mode detection
- Warning if speaker mode selected but no speakers exist
- Shows mode in success message: `(global)` or `(speaker)`

**Usage Examples:**
- `/intercom live Player world 60` - Auto-detect
- `/intercom live Player world 60 global` - Force global
- `/intercom live Player world 60 speaker` - Force speaker mode

---

### 3. `src/main/java/eu/projnull/spelis/svci/commands/handlers/FileCommand.java`
**Major Rewrite:**
- Added optional `mode` parameter: `/intercom file <filename> <world> [mode]`
- Mode can be `global` or `speaker`
- **Auto-detection**: If mode not specified, uses speaker mode if speakers exist, otherwise global
- Refactored to use helper method `executeFileBroadcast()` for cleaner code
- **Global mode**: Creates static audio channels for each player (everyone hears equally)
- **Speaker mode**: Creates locational audio channels at each speaker position
- Warning if speaker mode selected but no speakers exist
- Shows mode and speaker count in success message

**Behavior:**
- Global: Uses `createStaticAudioChannel()` - no positional audio
- Speaker: Uses `createLocationalAudioChannel()` with `setDistance()` for each speaker

---

### 4. `src/main/java/eu/projnull/spelis/svci/voice/VoicePlugin.java`
**Major Changes:**
- Modified `onMicPacket()` to respect broadcast mode
- **Global mode path**: Uses static sound packets, everyone hears equally (ignores speakers)
- **Speaker mode path**: 
  - Checks for speakers in the world
  - Finds nearest speaker to each listener
  - Sends locational sound packets from speaker position
  - Players only hear if within range of a speaker
- Removed auto-fallback - now strictly follows the configured mode
- Better separation of concerns between modes

**Key Logic:**
```java
if (mode == GLOBAL) {
    // Send static packets to all players
} else {
    // Find nearest speaker per listener
    // Send locational packets from speaker position
}
```

---

### 5. `src/main/java/eu/projnull/spelis/svci/commands/IntercomCommand.java`
**Changes Made:**
- Added `SpeakerCommand` to the list of registered handlers
- New line: `registerHandler(new SpeakerCommand());`

---

### 6. `src/main/java/eu/projnull/spelis/svci/Intercom.java`
**Changes Made:**
- Added import for `SpeakerManager`
- Modified `onEnable()` method to initialize the speaker system
- Added: `SpeakerManager.inst().initialize(this.getDataFolder());`
- This runs before everything else to load saved speakers from disk

---

### 7. `build.gradle.kts`
**Changes Made:**
- Added Gson dependency for JSON serialization: `implementation("com.google.code.gson:gson:2.10.1")`
- Required for saving/loading speaker configuration to disk

---

### 8. `README.md`
**Major Update:**
- Completely rewritten with comprehensive documentation
- Added "Broadcast Modes Explained" section with detailed behavior
- Expanded command documentation with examples
- Added auto-detection explanation
- Added permissions list
- Added setup example with real-world scenarios
- Clarified all three ways to add speakers

---

## Key Improvements from Previous Version

### 1. **Separated Global and Speaker Broadcasts**
- **Before**: Speakers would override behavior for entire world (all-or-nothing)
- **After**: Choose mode per broadcast - use global for world announcements, speaker for localized PA

### 2. **Truly Optional Coordinates**
- **Before**: Two options - full coords or player location
- **After**: Three flexible options:
  1. Just name + range (uses player's location)
  2. Name + range + world (uses player's XYZ in different world)
  3. Full specification with all coordinates

### 3. **Better Command Structure**
- Optional mode parameters with auto-detection
- Tab completion for modes
- Clearer command syntax
- More helpful error messages

### 4. **Improved Logic**
- No more auto-fallback confusion
- Explicit mode selection
- Warning system for invalid configurations
- Cleaner code with helper methods

---

## Technical Details

### Broadcast Mode Logic

#### Global Mode:
1. Broadcast is created with `BroadcastMode.GLOBAL`
2. VoicePlugin/FileCommand checks the mode
3. For live: Sends static sound packets to all players
4. For file: Creates static audio channels for each player
5. **Speakers are completely ignored** - audio is non-positional

#### Speaker Mode:
1. Broadcast is created with `BroadcastMode.SPEAKER`
2. VoicePlugin/FileCommand checks the mode
3. Retrieves speakers from SpeakerManager
4. If no speakers exist, no audio is heard (with warning)
5. For live: Finds nearest speaker per listener, sends locational packets
6. For file: Creates locational audio channels at each speaker position
7. **Only players near speakers hear the audio**

#### Auto-Detection:
```java
boolean hasSpeakers = !SpeakerManager.inst().getSpeakers(worldId).isEmpty();
mode = hasSpeakers ? SPEAKER : GLOBAL;
```

### Data Persistence

- Speakers saved to `plugins/SVCIntercom/speakers.json`
- Format: Array of speaker objects with worldId, coordinates, range, and name
- Uses Gson for clean JSON serialization
- Automatically loaded on plugin enable
- Automatically saved after any speaker modification
- Thread-safe operations with ConcurrentHashMap

### Permission Nodes

**Existing:**
- `svcintercom.broadcast` - Base permission
- `svcintercom.broadcast.start` - Start broadcasts

**New:**
- `svcintercom.speaker` - Base permission for speaker commands
- `svcintercom.speaker.add` - Permission to add speakers
- `svcintercom.speaker.remove` - Permission to remove speakers
- `svcintercom.speaker.list` - Permission to list speakers

---

## Summary of Changes

**Files Created:** 4
- Speaker.java (data model)
- SpeakerManager.java (persistence & management)
- SpeakerCommand.java (command handlers with 3 flexible add options)
- gradlew (build script)

**Files Modified:** 8
- BroadcasterState.java (added BroadcastMode enum)
- LiveCommand.java (added mode selection with auto-detect)
- FileCommand.java (added mode selection with auto-detect)
- VoicePlugin.java (respects broadcast mode, no fallback)
- IntercomCommand.java (register speaker commands)
- Intercom.java (initialize speaker system)
- build.gradle.kts (add Gson dependency)
- README.md (comprehensive documentation rewrite)

**Total Lines Added:** ~900+

**Key Features:**
1. ✅ Dual broadcast modes (Global vs Speaker)
2. ✅ Truly optional coordinates (3 ways to add speakers)
3. ✅ Auto-detection with explicit mode override
4. ✅ Better separation of concerns
5. ✅ Comprehensive documentation
6. ✅ Warning system for invalid configurations
7. ✅ Persistent speaker storage
8. ✅ Thread-safe operations
