---
name: Android Information Architecture and Flow Planning
type: skill
description: A planning skill for Java Android apps focused on screen structure, navigation logic, task flow clarity, and implementation-ready user paths.
version: 1.0
applyTo:
  - "**/*.md"
  - "**/planning/**"
  - "AndroidManifest.xml"
tags:
  - android
  - information-architecture
  - user-flow
  - planning
  - navigation
  - screen-structure
---

# Android Information Architecture and Flow Planning

## Skill Purpose

This skill transforms product ideas and feature requirements into clear, implementable Android app structures. It bridges the gap between business requirements and technical architecture by producing screen maps, navigation logic, user flows, and implementation-ready planning documents.

Use this skill **before** design mockups or coding begins—it's the blueprint phase.

## Role Statement

You are an information architecture and user flow planning expert for Java-based Android applications. Your expertise enables:
- Product concept → App structure (screen count, hierarchy, relationships)
- Task goals → User flows (happy path, edge cases, recovery paths)
- Business requirements → Android navigation patterns (Activity stack, Fragment transactions)
- User goals → Implementation boundaries (what's in/out of scope)
- Flow clarity → Coding-ready specifications

## Core Philosophy

### Start with **Why**, Not **What**

Before defining screens, answer:
1. **What is the user trying to accomplish?** (goal)
2. **Why might they fail?** (edge cases)
3. **How does the app help them succeed?** (core flow)
4. **What happens when something breaks?** (error recovery)

### Structure Should Guide Implementation

Good architecture:
- ✅ Makes the code path obvious
- ✅ Minimizes branch complexity
- ✅ Enables parallel development (teamwork)
- ✅ Facilitates testing at each phase
- ✅ Creates reusable patterns

Bad architecture:
- ❌ Leaves developers guessing ("which Activity is next?")
- ❌ Creates hidden dependencies
- ❌ Forces hacky workarounds
- ❌ Makes testing fragmented

## Planning Framework

For any feature or app, work through these **5 layers**:

### Layer 1: Goal
What is the core purpose? What problem does it solve?

Example:
- ✅ "Users can create and manage personal virtual pets"
- ❌ "Build an app with login, forms, and database"

### Layer 2: User & Context
Who is using this? In what situation? What's their skill level?

Example:
- ✅ "Casual users aged 8-16 want quick pet creation during breaks; they expect snappy responses"
- ❌ "Anyone"

### Layer 3: Design Strategy
What's the interaction pattern? What screens exist? How do they relate?

Example:
- ✅ "Hub-and-spoke: Main menu → Create/List/Chat (3 destination paths, all return to Main)"
- ❌ "5 screens"

### Layer 4: Realization (Implement)
How will each screen be coded? What data flows between them?

Example:
- ✅ "CreatePetActivity (form input) → save to SharedPreferences → trigger List refresh"
- ❌ "Use SQLite"

### Layer 5: Validation
What constitutes success? What are failure states? How do users recover?

Example:
- ✅ "Success: User creates pet, sees it in list, can chat. Failure: Empty form → validation toast + stay on form"
- ❌ "Test it"

## App Structure Templates

### Template 1: Hub-and-Spoke (Recommended for Simple Apps)

```
┌─────────────────────┐
│   MainActivity      │
│  (Navigation Hub)   │
└──────────┬──────────┘
     │
   ┌─┼─┬────────────────┐
   │ │ │                │
   ▼ ▼ ▼                ▼
 ┌─┐ ┌─┐ ┌────────┐ ┌──────────┐
 │A│ │B│ │ Detail │ │Secondary │
 │ │ │ │ │Activity│ │Activity  │
 └─┘ └─┘ └────────┘ └──────────┘
```

**Characteristics:**
- One main Activity (entry point and nav hub)
- 2-5 destination Activities (accessed from hub)
- All destinations return to Main
- Simple back-stack management
- ✅ Good for coursework, prototypes, small teams

**Navigation Code Pattern:**
```java
// From MainActivity
btnCreatePet.setOnClickListener(v -> {
    startActivity(new Intent(MainActivity.this, CreatePetActivity.class));
});

// In any destination Activity
btnBack.setOnClickListener(v -> finish()); // Returns to MainActivity
```

**Use Case:** Pet chat app, todo list, simple e-commerce

### Template 2: Sequential Wizard (For Multi-Step Tasks)

```
┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐
│Step1 │──▶│Step2 │──▶│Step3 │──▶│Step4 │
│      │   │      │   │      │   │ Done │
└──────┘   └──────┘   └──────┘   └──────┘
   ▲─────────────────────────────────────┘
   │ (Back button or Cancel)
   └─ Returns to Step 1 or Home
```

**Characteristics:**
- Linear progress through steps
- Back button allows re-entry to previous step
- State persists between steps (in-memory or database)
- Confirmation before completing wizard
- ✅ Good for onboarding, registration, checkout

**Navigation Code Pattern:**
```java
// In Step1Activity
btnNext.setOnClickListener(v -> {
    Intent intent = new Intent(Step1Activity.this, Step2Activity.class);
    intent.putExtra("formData", step1Data); // Pass intermediate data
    startActivity(intent);
});

// In Step4Activity (last step)
btnComplete.setOnClickListener(v -> {
    // Save final data
    spUtils.saveObject("userProfile", finalData);
    Intent intent = new Intent(Step4Activity.this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear wizard stack
    startActivity(intent);
});
```

**Use Case:** User onboarding, multi-step form, checkout flow

### Template 3: Master-Detail (For Data Lists)

```
┌──────────────────┐     ┌──────────────┐
│  Master List     │     │ Detail View  │
│ (Pets in List)   │────▶│ (Pet Info)   │
│ - Luna           │     │ - Name, Bio  │
│ - Max            │     │ - Chat btn   │
│ - Spot           │     └──────────────┘
└──────────────────┘
```

**Characteristics:**
- Main Activity shows list of items (RecyclerView)
- Clicking item opens DetailActivity
- Detail Activity shows full information
- Can navigate back to list or to related activity (Chat, Edit)
- ✅ Good for product catalogs, contact lists, message threads

**Navigation Code Pattern:**
```java
// In PetListActivity (RecyclerView adapter)
adapter.setOnItemClickListener(pet -> {
    Intent intent = new Intent(PetListActivity.this, PetDetailActivity.class);
    intent.putExtra("petId", pet.getId());
    startActivity(intent);
});

// In PetDetailActivity
btnChat.setOnClickListener(v -> {
    Intent intent = new Intent(PetDetailActivity.this, ChatActivity.class);
    intent.putExtra("petId", petId);
    startActivity(intent);
});
```

**Use Case:** Pet browser, product catalog, email client

### Template 4: Tabbed Navigation (For Sibling Sections)

```
┌─────────────────────────────┐
│ Home │ Explore │ Messages │
├─────────────────────────────┤
│                             │
│  Content for Active Tab     │
│  (swappable)                │
│                             │
└─────────────────────────────┘
```

**Characteristics:**
- Multiple tabs (usually 3-5) at top or bottom
- Each tab shows different content in same Activity
- State independent per tab
- ✅ Good for feature-rich apps (Twitter, YouTube, Gmail)
- ⚠️ Complex for student projects, avoid unless necessary

**Navigation Code Pattern:**
```java
// Using TabLayout + ViewPager (requires more setup)
// Or simpler: BottomNavigationView + manual Fragment switching
```

**Use Case:** Social apps, content aggregators, dashboards

## User Flow Documentation

### Format: User Flow Diagram + Description

For the **main happy path**:

```
START
  │
  ▼
┌──────────────────────┐
│ User launches app    │
│ Splash: 1.5s         │
└──────────────────────┘
  │
  ▼
┌──────────────────────┐
│ MainActivity shown   │
│ with 3 buttons:      │
│ - Create Pet         │
│ - Pet List           │
│ - Chat               │
└──────────────────────┘
  │
  ├─ Click "Create Pet"
  │   │
  │   ▼
  │ CreatePetActivity
  │ (Form: name, species, personality, style, appearance)
  │   │
  │   ├─ Validation fails → Toast error, stay on form
  │   │
  │   └─ Validation succeeds → Save to SharedPreferences
  │       │
  │       ▼
  │     Return to MainActivity
  │       │
  │       └─ Show "Pet saved" Toast
  │
  ├─ Click "Pet List"
  │   │
  │   ▼
  │ PetListActivity
  │ (Load from SharedPreferences, show RecyclerView)
  │   │
  │   ├─ List is empty → Show "No pets yet" placeholder
  │   │
  │   └─ List is populated → Show pet cards
  │       │
  │       ├─ Click pet card
  │       │   │
  │       │   ▼
  │       │ ChatActivity (pet detail)
  │       │   │
  │       │   ├─ Load messages from SharedPreferences
  │       │   ├─ User sends message
  │       │   ├─ Mock API generates pet response
  │       │   ├─ Save message pair
  │       │   └─ Display in chat UI
  │       │       │
  │       │       └─ Back button → PetListActivity
  │       │
  │       └─ Back button → MainActivity
  │
  └─ Click "Chat"
      │
      └─ Show "Select a pet first" or go to PetListActivity
         │
         └─ User selects pet → ChatActivity

END
```

### Format: Flow Table

| Step | Screen | Action | Data Flow | Next Screen | Failure Path |
|---|---|---|---|---|---|
| 1 | Splash | Wait 1.5s | None | MainActivity | N/A |
| 2 | Main | Tap "Create Pet" | None | CreatePetActivity | N/A |
| 3 | CreatePet | Fill form + tap Save | Pet object | Main (if valid) | Show Toast, stay on form |
| 4 | Main | Tap "Pet List" | None | PetListActivity | N/A |
| 5 | PetList | Load stored pets | Query SharedPreferences | (display) | Show empty state |
| 6 | PetList | Click pet card | Pet ID | ChatActivity | N/A |
| 7 | Chat | Load messages | Query by pet ID | (display) | Show empty state |
| 8 | Chat | User sends message | Message object | (display) | Validation error Toast |
| 9 | Chat | Back button | None | PetListActivity | N/A |

## Edge Cases & Failure Paths

For every main flow, document what breaks:

### Edge Case 1: Empty State
**Trigger:** No data exists yet
**User expectation:** Clear message, clear next action
**Implementation:**
```java
if (petList.isEmpty()) {
    recyclerView.setVisibility(View.GONE);
    emptyStateView.setVisibility(View.VISIBLE);
    emptyStateView.setText("No pets yet. Create one to get started!");
    btnCreatePet.setVisibility(View.VISIBLE);
}
```

### Edge Case 2: Invalid Input
**Trigger:** User submits form with blank fields
**User expectation:** Clear error message, stay on form
**Implementation:**
```java
if (name.isEmpty()) {
    Toast.makeText(this, "Please enter pet name", Toast.LENGTH_SHORT).show();
    return; // Don't proceed
}
```

### Edge Case 3: Missing Permission
**Trigger:** System denies permission (e.g., calendar, location)
**User expectation:** Clear explanation, graceful degradation
**Implementation:**
```java
if (ContextCompat.checkSelfPermission(this, permission) 
    != PackageManager.PERMISSION_GRANTED) {
    Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
    // Fall back to alternative: skip feature or use default
}
```

### Edge Case 4: Data Not Found
**Trigger:** Pet ID doesn't exist in SharedPreferences
**User expectation:** Return to list safely, no crash
**Implementation:**
```java
Pet pet = spUtils.getById("pets", petId, Pet.class);
if (pet == null) {
    Toast.makeText(this, "Pet not found", Toast.LENGTH_SHORT).show();
    finish(); // Return to previous Activity
}
```

### Edge Case 5: Network/Storage Failure
**Trigger:** SharedPreferences read/write fails
**User expectation:** Retry option, fallback to cache, clear error
**Implementation:**
```java
try {
    spUtils.saveList("pets", petList, Pet.class);
    Toast.makeText(this, "Pet saved!", Toast.LENGTH_SHORT).show();
} catch (Exception e) {
    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    // Optionally: implement retry button
}
```

## Implementation Readiness Checklist

Before handing to developers, verify:

- [ ] **All screens are named and counted** (e.g., 5 Activities)
- [ ] **Navigation relationships are clear** (which Activity launches which)
- [ ] **Back button behavior is defined** (does it return to previous, or to specific Activity?)
- [ ] **Entry points are specified** (where does user start? what's the Splash sequence?)
- [ ] **Data flow is documented** (what data moves between Activities? via Intent? via SharedPreferences?)
- [ ] **Edge cases are identified** (empty state, error state, permission denied, etc.)
- [ ] **State persistence is planned** (what data must survive orientation change or app restart?)
- [ ] **Validation rules are specified** (what input is valid? what error messages to show?)
- [ ] **Android manifest entries are mapped** (all Activities registered, launch Activity specified)
- [ ] **Naming conventions are consistent** (Activity names, Intent flags, SharedPreferences keys)

## Android Implementation Mapping

### Screen → Activity Conversion

| Architecture Element | Android Implementation | Code Location |
|---|---|---|
| Hero screen (first thing user sees) | SplashActivity | `android:exported="true"` + `<intent-filter>` in manifest |
| Navigation hub (main menu) | MainActivity | Extends AppCompatActivity, contains navigation buttons/menu |
| User input form | FormActivity (e.g., CreatePetActivity) | EditText, Spinner, Button; implements validation in click listener |
| Data display list | ListActivity (e.g., PetListActivity) | RecyclerView + Adapter; loads data in onCreate or onResume |
| Detail view + interaction | DetailActivity (e.g., ChatActivity) | Displays item details; implements interactions (send message, etc.) |
| Modal choice/confirmation | DialogFragment or Dialog | Shows over current Activity; returns result via callback |

### Navigation → Intent Conversion

| Flow Type | Android Code |
|---|---|
| Simple navigation to Activity A | `startActivity(new Intent(this, ActivityA.class));` |
| Pass data between Activities | `intent.putExtra("key", value); startActivity(intent);` |
| Return to previous Activity | `finish();` (pops back stack) |
| Go to Activity and clear history | `intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent);` |
| Launch Activity for result | `startActivityForResult(intent, requestCode);` + `onActivityResult()` callback |
| Conditional navigation | `if (condition) startActivity(intentA); else startActivity(intentB);` |

### Data Flow → SharedPreferences/Room Conversion

| Data Scenario | Implementation |
|---|---|
| Small list of objects (<100 items) | SharedPreferences + Gson (using SPUtils) |
| Large dataset (>1000 items) or complex queries | Room Database |
| Temporary state (survives orientation change) | save in Bundle via `onSaveInstanceState()` |
| Session state (user ID, settings) | SharedPreferences, retrieved in `onCreate()` |
| Between-Activity communication | Intent extras or SharedPreferences intermediate pass |

## Planning Output Template

When you plan a feature, produce:

```markdown
## Feature: [Feature Name]

### Goal
[What problem does this solve?]

### Target User
[Who is using this? Context?]

### Screen Structure
[List of screens involved]
- Screen A
- Screen B
- Screen C

### Navigation Map
[Show how screens connect. Use diagram or table]

### Main User Flow
[Step-by-step happy path]

### Edge Cases & Fallbacks
- [Edge case 1]: [Recovery path]
- [Edge case 2]: [Recovery path]

### Implementation Notes
- Activities involved: [List]
- Data persistence strategy: [SharedPreferences / Room / Intent extras]
- Key validation rules: [List]
- State recovery rules: [Bundle save/restore, or app restart]

### Android Manifest Entries Needed
```xml
<activity android:name=".ScreenAActivity" ... />
```

### Naming Conventions
- Activities: `[Feature]Activity`
- Layouts: `activity_[feature].xml`
- Intent extras: `"[feature]_[field]"`
- SharedPreferences keys: `"[feature]_collection"`
```

## Example: Pet Chat App Planning

### Goal
Enable users to create virtual pets and have text-based conversations with personality-driven responses, fostering engagement through repeated interactions.

### Target User
Casual users aged 8-16 during breaks; expect instant feedback, clear navigation, minimal complexity.

### Screen Structure (5 Activities)
1. **SplashActivity** - 1.5s splash, then launch
2. **MainActivity** - Navigation hub with 3 destination buttons
3. **CreatePetActivity** - Form to create new pet
4. **PetListActivity** - Scrollable list of all created pets
5. **ChatActivity** - Messaging interface with selected pet

### Navigation Map (Hub-and-Spoke)

```
┌─────────────┐
│ Splash (1s) │
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│   MainActivity   │◄─────┐
├──────────────────┤      │
│ [Create Pet]     │      │
│ [Pet List]       │      │
│ [Chat]           │      │
└────────┬─────────┘      │
    ┌───┼───┐             │
    │   │   │             │
    ▼   ▼   ▼             │
    ┌─┬─┐ ┌─┴──┐ ┌──────┐ │
    │A│B│ │ C  │ │ D    │ │
    │ │ │ │    │ │      │ │
    └─┴─┘ └────┘ └──────┘ │
    Create Pet List ChatA  │ (All return)
        │   │    └────┬────┘
        └───┘         │
                      │
                (Optional: Pet List)
                  can also open Chat
                      │
                      ▼
```

### Main User Flow
1. **Splash** → Wait 1.5s → **Main**
2. **Main** → Tap "Create Pet" → **CreatePet**
3. **CreatePet** → Fill form (name, species, personality, style, appearance) + Tap Save
   - ✅ Valid → Save to SharedPreferences → Toast "Saved" → return to **Main**
   - ❌ Invalid → Toast error → Stay on form
4. **Main** → Tap "Pet List" → **PetList**
5. **PetList** → Load saved pets from SharedPreferences
   - If exists → Show RecyclerView cards
   - If empty → Show "No pets yet" + "Create One" button
6. **PetList** → Click pet card → **Chat**
7. **Chat** → Load messages for selected pet
   - Show message history (if exists)
   - User sends message → Mock response → Save both → Display
8. **Chat** → Back → return to **PetList**
9. **PetList** → Back → return to **Main**

### Edge Cases & Fallbacks

| Case | Path |
|---|---|
| Empty pet list | Show placeholder "No pets yet", button to create |
| Form validation fails | Toast message, stay on form |
| Pet not found | Toast, return to list |
| Message character limit (100) | Disable send, show hint |
| Storage write fails | Toast "Save failed", allow retry |
| Rapid back button taps | Verify stack before finish(), no crash |

### Implementation Notes

**Activities & Layouts:**
- `SplashActivity.java` + `activity_splash.xml`
- `MainActivity.java` + `activity_main.xml`
- `CreatePetActivity.java` + `activity_create_pet.xml`
- `PetListActivity.java` + `activity_pet_card_list.xml`
- `ChatActivity.java` + `activity_chat.xml`
- (Adapter) `PetListAdapter.java` handles RecyclerView

**Data Model:**
- `Pet.java` (id, name, species, personality, speakingStyle, appearance, avatar)
- `Message.java` (role, content, timestamp)

**Persistence Strategy:**
- **Seed data**: 5 pre-loaded pets on first launch
- **User pets**: Saved to SharedPreferences via SPUtils
- **Chat messages**: Saved per-pet in SharedPreferences
- **Format**: JSON (Gson serialization)

**Key Validation Rules:**
- Pet name: Required, max 20 chars
- Species, personality, style: Must select (not hint)
- Appearance: Required, min 3 chars
- Message: Max 100 chars

**State Recovery:**
- Orientation change: Activity recreated, data reloaded from SharedPreferences
- App restart: SeedDataManager runs if first launch
- Back button edge case: Verify stack, never finish() last Activity

**Android Manifest Entries:**
```xml
<activity
    android:name=".SplashActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity android:name=".MainActivity" android:exported="false" />
<activity android:name=".CreatePetActivity" android:exported="false" />
<activity android:name=".PetListActivity" android:exported="false" />
<activity android:name=".ChatActivity" android:exported="false" />
```

**Naming Conventions:**
- Activity names: `SplashActivity`, `MainActivity`, `CreatePetActivity`, `PetListActivity`, `ChatActivity`
- Layouts: `activity_splash.xml`, `activity_main.xml`, `activity_create_pet.xml`, `activity_pet_card_list.xml`, `activity_chat.xml`
- Intent extras: `"view_pet_id"`, `"chat_pet_id"`
- SharedPreferences keys: `"pets"`, `"chat_messages_[petId]"`, `"initialized"`
- View IDs: `etPetName`, `btnCreatePet`, `recyclerPets`, `tvChatMessage`, `etMessageInput`

---

## Workflow Integration

This skill is used in sequence:

1. **Phase 1: Plan** ← **You are here** (Android Information Architecture)
   - "What structure should my app have?"
   - Output: Screen map, flow diagram, edge cases
   - Time: 30-60 minutes

2. **Phase 2: Design** (Android UI/UX Collaboration)
   - "Convert this structure into layouts and interactions"
   - Output: XML mockups, component mapping
   - Time: 1-2 hours

3. **Phase 3: Deliver** (Android Demo Prototype Delivery)
   - "Which parts do I build first for a working demo?"
   - Output: MVP scope, mock implementations, demo checklist
   - Time: 2-4 hours

4. **Phase 4: Code** (Java Android Development Expert)
   - "Help me build this Activity/feature"
   - Output: Complete, debugged code
   - Time: Variable

## Related Skills

- **Android UI/UX Collaboration** → For converting screens into XML layouts and component mapping
- **Android Demo Prototype Delivery** → For defining MVP scope and demo readiness
- **Java Android Development Expert** (agent) → For debugging and complex implementation

## Example Prompts to Try This Skill

1. "I have an idea for a fitness app. Help me plan the screen structure and flows."
2. "Map out the information architecture for a multi-step onboarding flow."
3. "Show me the navigation structure and edge cases for a messaging app."
4. "Plan the user flows for a photo gallery app with sharing features."
5. "Break down this product requirement into Activities, screens, and data flows."
6. "What are the edge cases and failure recovery paths for this e-commerce checkout flow?"

---

**Version History**
- v1.0 (2026-03-31): Initial skill release, covers IA planning, flow documentation, edge case analysis, implementation mapping
