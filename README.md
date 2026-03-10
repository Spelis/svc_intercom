# SVC Intercom

**SVC Intercom** is a plugin for Simple Voice Chat that adds broadcasting functionality, meaning you can talk to everyone
inside a world in a one-way system. Now with **dual broadcast modes**: global broadcasts (everyone hears equally) or speaker-based broadcasts (only near speakers).

## Features:

- **Live Broadcasts**: Stream a player's microphone to everyone in a world
- **File Playback**: Play audio files to everyone in a world  
- **Dual Broadcast Modes**:
  - **Global Mode**: Everyone in the world hears equally, regardless of location
  - **Speaker Mode**: Audio only plays from defined speaker locations with positional audio
- **Speaker System**: Create virtual speakers with positional audio and limited range
  - Audio positioned at speaker locations for realistic sound
  - Players must be near a speaker to hear speaker-mode broadcasts
  - Support for multiple speakers per world
  - Persistent storage of speakers

## Commands:

### Broadcast Commands

`/intercom live <player> <world> <duration> [mode]`
- Start broadcasting a player's microphone to everyone in a world for a duration (1-300 seconds)
- Optional `mode`: `global` or `speaker` (auto-detects if not specified)
- Examples:
  - `/intercom live PlayerName world 60` - Auto-detect mode based on speakers
  - `/intercom live PlayerName world 60 global` - Force global broadcast
  - `/intercom live PlayerName world 60 speaker` - Force speaker-based broadcast

`/intercom file <filename> <world> [mode]`
- Plays an audio file for everyone in a world for the duration of the file
- Optional `mode`: `global` or `speaker` (auto-detects if not specified)
- Audio files go in `plugins/SVCIntercom/sounds/`

`/intercom info <world>` 
- Shows active broadcasts in a world

`/intercom stop <world>` 
- Stops the broadcast in a world

### Speaker Commands

`/intercom speaker add <name> <range>`
- Add a speaker at your current location (players only)
- `range`: How far the speaker broadcasts (1-1000 blocks)
- Example: `/intercom speaker add spawn_plaza 100`

`/intercom speaker add <name> <range> <world>`
- Add a speaker using your current X/Y/Z coordinates but in a different world
- Useful for copying speaker positions across worlds

`/intercom speaker add <name> <world> <x> <y> <z> <range>`
- Add a speaker at specific coordinates
- Example: `/intercom speaker add main_square world 0 64 0 150`

`/intercom speaker remove <world> <name>` 
- Remove a speaker by name
- Example: `/intercom speaker remove world spawn_plaza`

`/intercom speaker list [world]` 
- List all speakers in a world (defaults to your current world)

## Broadcast Modes Explained:

### Global Mode
- **Best for**: World-wide announcements, emergency broadcasts
- **Behavior**: All players in the world hear the audio equally, regardless of location
- **Speaker requirement**: None - speakers are ignored in global mode

### Speaker Mode  
- **Best for**: Realistic PA systems, localized announcements, immersive experiences
- **Behavior**: 
  - Audio appears to come from speaker locations using 3D positional audio
  - Players only hear if they're within range of at least one speaker
  - Closer speakers play louder
- **Speaker requirement**: At least one speaker must exist in the world

### Auto-Detection
If you don't specify a mode, the plugin will:
- Use **speaker mode** if speakers exist in the world
- Use **global mode** if no speakers exist
- Warn you if speaker mode is selected but no speakers exist

## Permissions:

- `svcintercom.broadcast` - Access to broadcast commands
- `svcintercom.broadcast.start` - Start broadcasts (live/file)
- `svcintercom.speaker` - Access to speaker commands
- `svcintercom.speaker.add` - Add speakers
- `svcintercom.speaker.remove` - Remove speakers
- `svcintercom.speaker.list` - List speakers

## Setup Example:

```
# Set up speakers in a world
/intercom speaker add town_square 100
/intercom speaker add market_district world 200 65 -150 80
/intercom speaker add residential_area 75

# Start a global announcement (everyone hears)
/intercom live Admin world 30 global

# Start a speaker-based broadcast (only near speakers)
/intercom live Admin world 60 speaker

# Play a file through speakers
/intercom file announcement.ogg world speaker
```