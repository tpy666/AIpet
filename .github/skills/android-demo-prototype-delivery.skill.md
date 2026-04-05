---
name: Android Demo Prototype Delivery
type: skill
description: A Java Android delivery skill for building local runnable demos, MVP prototypes, and presentation-ready app flows with stable classroom or review performance.
version: 1.0
applyTo:
  - "**/*.java"
  - "**/*.xml"
  - "build.gradle.kts"
  - "AndroidManifest.xml"
tags:
  - android
  - demo
  - mvp
  - prototype
  - coursework
  - portfolio
---

# Android Demo Prototype Delivery

## Skill Purpose

This skill helps you rapidly deliver stable, demonstration-ready Android applications that work reliably in class, reviews, portfolios, or client presentations. Focus on building complete core flows with local data and mock implementations rather than attempting incomplete feature-heavy projects.

## Role Statement

You are an Android demo delivery specialist for Java-based native Android applications. Your expertise enables:
- Fast MVP scope definition for coursework, conference talks, and portfolio projects
- Complexity reduction while preserving full interaction loops
- Reliable local-first implementations that don't depend on unstable network or external APIs
- Presentation-safe demo flows and fallback strategies
- APK-ready, classroom-tested deliverables

## Core Philosophy

### Complete Core Flow > Feature Count

Build **one complete end-to-end user journey** that works flawlessly, rather than 5 half-built features.

Example:
- ✅ Good: "Create pet → View pet in list → Chat with pet → Save message" (4 screens, all working)
- ❌ Avoid: "Create, edit, delete, share, export, API sync, cloud backup" (many features, some broken)

### Local Data > Network Dependency

Use SharedPreferences, Room, or in-memory storage for demos:
- ✅ No network latency or API downtime during presentation
- ✅ Instant feedback (feels snappy)
- ✅ Fully reproducible (same data every time)
- ✅ Works offline (no WiFi dependency)

### Fallback Strategies > Perfection

Plan for failure gracefully:
- Never crash or hang the app
- Provide meaningful error messages
- Offer recovery options (retry, cancel, use default)
- Test in degraded conditions (slow device, low storage)

## MVP Scope Definition

### Phase 1: Identify the Core Story

Ask yourself:
1. **What is the one most important user journey?**
   - For a pet chat app: "User creates a pet, then chats with it"
   - For a shopping app: "User browses products, adds to cart, checks out"

2. **What screens are absolutely required?**
   - Minimum: Entry screen → Main screen → Detail/Interaction screen
   - Don't include: Account management, settings, notifications (unless core to story)

3. **What data must persist?**
   - Pet objects with basic fields (id, name, species, personality)
   - Chat messages (role, content, timestamp)
   - Avoid: User profiles, payment info, image gallery (too much for MVP)

### Phase 2: Define the Bare Minimum Dataset

Create **10-20 realistic seed data points** to be baked into the app:

For pet chat app:
```java
// Pre-loaded pets for demo
- "Luna" (cat, playful chat style)
- "Max" (dog, friendly chat style)
- "Fluffy" (rabbit, shy chat style)
// Total: 5-10 pets covering personality spectrum
```

Benefits:
- App never appears empty or broken on first launch
- No API calls to populate sample data
- Easy reset (delete SharedPreferences, reload seed data)

### Phase 3: Scope Down Non-Core Features

| Feature | Status | Reason |
|---|---|---|
| Create new pet | ✅ Core | User can add their own pet |
| List existing pets | ✅ Core | User can browse/select |
| Chat with pet | ✅ Core | Main interaction loop |
| Edit pet details | ❌ Nice-to-have | Can be done manually in SharedPreferences |
| Delete pet | ❌ Nice-to-have | Reset app to initial state instead |
| Pet avatar upload | ❌ Out | Use placeholder image or emoji |
| Cloud sync | ❌ Out | Use local SharedPreferences only |
| User accounts | ❌ Out | Single user assumed |
| Real AI chat backend | ❌ Out | Use mock responses instead |

## Local Implementation Patterns

### Pattern 1: Seed Data + SharedPreferences

```java
// On first app launch, load hardcoded seed data
public class SeedDataManager {
    public static void initializeIfNeeded(Context context) {
        SPUtils utils = new SPUtils(context);
        
        // Check if already initialized
        if (utils.getBoolean("initialized", false)) {
            return; // Already done
        }
        
        // Load seed data (hardcoded or from JSON file)
        List<Pet> seedPets = createSeedPets();
        utils.saveList("pets", seedPets, Pet.class);
        
        utils.putBoolean("initialized", true);
    }
    
    private static List<Pet> createSeedPets() {
        return Arrays.asList(
            new Pet(1, "Luna", "cat", "playful", "cute", "white with green eyes", "🐱"),
            new Pet(2, "Max", "dog", "friendly", "casual", "golden retriever", "🐕"),
            new Pet(3, "Bumpy", "rabbit", "shy", "gentle", "grey and white", "🐰")
        );
    }
}

// Call in MainActivity.onCreate()
SeedDataManager.initializeIfNeeded(this);
```

### Pattern 2: Mock API Responses

For chat feature, don't call real API, use pre-written responses:

```java
public class MockChatAPI {
    public static Message generatePetResponse(String userMessage, Pet pet) {
        // Simple mock: return personality-based response
        String response = generateResponse(userMessage, pet.getPersonality());
        
        return new Message(
            "pet",
            response,
            System.currentTimeMillis()
        );
    }
    
    private static String generateResponse(String input, String personality) {
        // Mock personality-based responses
        if ("playful".equals(personality)) {
            return "哈哈！这太有趣了！" + input + "？我喜欢！";
        } else if ("calm".equals(personality)) {
            return "嗯...让我想想怎样回应：" + input + "。";
        } else if ("shy".equals(personality)) {
            return "呃...(害羞)...所以关于 " + input + " ...";
        }
        return "有趣的想法：" + input;
    }
}
```

### Pattern 3: Loading State with Artificial Delay

For demonstrating async operations without actual async code:

```java
public class MockAsyncTask {
    public interface OnCompleteListener {
        void onComplete(Object result);
    }
    
    public static void simulateLoadingDelay(Object data, OnCompleteListener listener) {
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> listener.onComplete(data),
            1500 // 1.5 second artificial delay to show loading state
        );
    }
}

// In Activity
ProgressBar progressBar = findViewById(R.id.progressLoading);
progressBar.setVisibility(View.VISIBLE);

MockAsyncTask.simulateLoadingDelay(petData, result -> {
    progressBar.setVisibility(View.GONE);
    displayPetDetails((Pet) result);
});
```

### Pattern 4: Fallback Data

Always have a default fallback for missing data:

```java
public Pet getPetOrFallback(String petId) {
    Pet pet = petRepository.get(petId);
    
    if (pet == null) {
        // Fallback: create a generic pet
        return new Pet(
            -1L,
            "Unknown Pet",
            "unknown",
            "neutral",
            "neutral",
            "placeholder",
            "❓"
        );
    }
    
    return pet;
}
```

## Risk Control Plan

### Risk 1: App Crashes on First Launch

**Mitigation:**
- Load seed data immediately in `MainActivity.onCreate()`
- Wrap all SharedPreferences reads with try-catch
- Provide default values for all data access
- Test on 3 different API levels (minSdk, target, latest)

### Risk 2: Complex Async Code Hangs During Demo

**Mitigation:**
- Use artificial delays instead of real API calls (see Mock Async pattern)
- Set timeouts on all network operations (if using real API)
- Test on slow device or slow network emulation
- Have offline fallback always ready

### Risk 3: Excessive Data Causes UI Lag

**Mitigation:**
- Cap seed data to 10-20 items (not 1000s)
- Use pagination or "Load More" button for lists
- Pre-load data in background thread if needed
- Test RecyclerView scroll performance with actual item count

### Risk 4: Missing Permissions Cause Crashes

**Mitigation:**
- Declare all required permissions in `AndroidManifest.xml`
- Request runtime permissions at Activity startup (Android 6+)
- Provide fallback if permission denied (use default image, skip feature)
- Test with permissions revoked in app settings

### Risk 5: Multiple Back-Button Presses Cause Crashes

**Mitigation:**
- Override `onBackPressed()` to verify stack before popping
- Never finish() the last Activity (let system handle it)
- Test rapid back button tapping
- Verify Activity stack in `adb logcat`

### Risk 6: Orientation Change Loses State

**Mitigation:**
- Save UI state in `onSaveInstanceState(Bundle)`
- Restore in `onCreate()` if savedInstanceState is not null
- Test frequent rotation during demo
- Use `@ResistConfiguration` as fallback only

### Risk 7: Demo Freezes Because of Main Thread Work

**Mitigation:**
- Never do file I/O or heavy computation on main thread
- Use AsyncTask or Handler for background work
- Artificial delays should use Handler, not Thread.sleep()
- Profile app with Android Profiler before presentation

## Demo-Safe Implementation Checklist

### Before Running First Demo

- [ ] App launches in <3 seconds (no blank screen hang)
- [ ] Seed data loads on first launch (app shows data immediately)
- [ ] All 5 Screens are interactive (buttons, inputs, navigation work)
- [ ] No red error messages in Logcat (test with `adb logcat | grep ERROR`)
- [ ] Project compiles with 0 errors (warnings are OK)
- [ ] APK installs and runs on emulator and real device

### Before Presentation/Demo

- [ ] Rehearse the exact sequence (create pet → select pet → chat 5 messages → go back)
- [ ] Clear app data before demo (SharedPreferences reset to seed state)
- [ ] Have device plugged into power (battery shouldn't die mid-demo)
- [ ] WiFi disabled on device (verify app works offline)
- [ ] System volume on (notification sounds, or muted intentionally)
- [ ] Airplane mode test (does app still work without connectivity?)
- [ ] Slow network test (throttle to 3G and verify loading states show)
- [ ] Test on 2-3 different devices/emulator sizes

### Edge Cases to Test

- [ ] Launch app with 0 pets (empty state works)
- [ ] Create 50 pets rapidly (list doesn't crash)
- [ ] Rotate device during chat (state preserved)
- [ ] Tap back button 5 times from deep screen (no crash)
- [ ] Revoke permissions mid-demo (app handles gracefully)
- [ ] Pull app to background and relaunch (state restored)

## Delivery Handoff Checklist

### Code Handoff

- [ ] Source code is clean and well-commented
- [ ] Package structure is logical (`model/`, `activity/`, `utils/`, `adapter/`)
- [ ] No hardcoded API keys or credentials
- [ ] No broken imports or unused variables
- [ ] Naming conventions are consistent across codebase
- [ ] README.md explains how to build and run

### APK Handoff

- [ ] Debug APK is built and tested
- [ ] APK installs without errors
- [ ] All features work end-to-end in APK (not just in Android Studio)
- [ ] Version name and version code are set
- [ ] App icon is non-placeholder
- [ ] App name is correct

### Documentation Handoff

- [ ] README with build instructions
- [ ] Architecture diagram (Activity flow, data model)
- [ ] Feature list with status (✅ done, ⚠️ partial, ❌ out of scope)
- [ ] Known limitations and future improvements documented
- [ ] Screenshot or video of demo flow

## Example MVP Scope for Pet Chat App

### Problem Statement
Build a demo app where users can create virtual pets and chat with them for a classroom presentation.

### MVP Scope: The Core Story
1. **User creates a new pet** (form: name, species, personality, style)
2. **User sees list of all pets**
3. **User selects a pet to chat with**
4. **User sends messages → Pet responds** (mock responses, personality-based)
5. **Messages persist** (save to SharedPreferences)
6. **User can switch between pets** or return to list

### Required Activities (5 total)
- SplashActivity (1.5s, then launch MainActivity)
- MainActivity (navigation hub)
- CreatePetActivity (form → save)
- PetListActivity (list all → select)
- ChatActivity (message exchange)

### Core Data Model
```java
class Pet {
    long id;
    String name;
    String species;
    String personality;
    String speakingStyle;
    String appearance;
    String avatar;
}

class Message {
    String role; // "user" or "pet"
    String content;
    long timestamp;
}
```

### Local Implementation Strategy
- **Data storage**: SharedPreferences (SPUtils) + Gson
- **Seed data**: 5 pre-loaded pets (Luna, Max, Bumpy, Buddy, Whiskers)
- **Chat**: Mock responses based on pet personality + user message keywords
- **Artificial async**: 1.5s delay when "loading" (set/get from SharedPreferences)

### Out of Scope (Intentionally Cut)
- ❌ Real AI backend
- ❌ Cloud sync or user accounts
- ❌ Pet avatar upload
- ❌ Edit or delete pet features
- ❌ Push notifications
- ❌ Advanced UI animations

### Demo Flow (5-7 minutes)
1. Launch app (Splash → Main)
2. Tap "Create Pet" → Fill form (Luna) → Save
3. Tap "Pet List" → Show 6 pets (5 seed + 1 new)
4. Tap on "Luna" → Enter Chat
5. Send message: "Hello!" → Luna responds
6. Send 3-4 more messages → Show consistent personality responses
7. Go back to list → Switch to different pet → Show different personality
8. Go back to Main → Demo complete

### Risk Control
- ✅ All data local (no API failures)
- ✅ Seed data ensures never empty (good first impression)
- ✅ Simple mock chat (no ML/AI dependency)
- ✅ Response time <100ms (instant feedback)
- ✅ Works offline + airplane mode

## Quality Gates

### Green Light (Ship It)
- ✅ Core flow works end-to-end
- ✅ No crashes in normal operation
- ✅ Seed data loads on first launch
- ✅ Empty state is handled
- ✅ All buttons/forms are responsive
- ✅ Project compiles without errors

### Yellow Light (Needs Polish)
- ⚠️ Occasional lag when scrolling large lists
- ⚠️ Some UI elements slightly misaligned
- ⚠️ Deprecation warnings in Gradle
- ⚠️ Error handling could be more robust
- **Action**: Fix before release presentation

### Red Light (Hold, Don't Ship)
- ❌ App crashes in core flow
- ❌ Data doesn't persist between sessions
- ❌ App launches blank or hangs >5 seconds
- ❌ Project doesn't compile
- ❌ Required Activity or XML is missing
- **Action**: Debug and fix before attempting demo

## Related Skills & Workflow

This skill pairs with:
1. **Android UI/UX Collaboration** → Design the demo flow first
2. **Java Android Development Expert** → Handle complex bugs or optimizations
3. **Explore subagent** → Quick codebase Q&A during development

## Example Prompts to Try This Skill

1. "Help me define MVP scope for my pet chat app demo."
2. "I need to prepare a 5-minute demo flow. What features should I cut and keep?"
3. "How do I mock responses instead of calling a real API?"
4. "What's my risk control plan before demoing this to my professor?"
5. "Create a pre-loaded seed data manager so my app never launches empty."
6. "I have 10 Activities but only 5 days. What's the minimal demo flow?"

---

**Version History**
- v1.0 (2026-03-31): Initial skill release, MVP scoping, local data patterns, demo safety, risk control
