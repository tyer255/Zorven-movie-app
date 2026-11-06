# ZORVÉN — Catalogue Deduplication, Clean Details Header, Taller Animated Hero & Skeleton Shimmer Upgrade

This update resolves content duplication across Home discovery rows, eliminates the overlapping duplicate thumbnail bug on the Movie/Series Details page, increases the Home hero banner height by 25% with auto-cycling zoom motion, removes the Continue Watching row and all demo/mock fallbacks, fixes 16:9 Search landscape thumbnail cropping, and replaces every circular loading spinner with a smooth skeleton shimmer system.

## User Review & Critical Decisions

> [!IMPORTANT]
> Both design decisions from the clarification step have been locked in and incorporated into this plan:

- **Confirmed Decision 1 — Home Hero Banner Motion**: The taller Home hero banner (~520px responsive height, +24% increase) will **auto-cycle through featured hero titles** every 6 seconds with a subtle Ken Burns zoom-and-crossfade animation and interactive progress indicator pills.
- **Confirmed Decision 2 — Details Page Header Layout**: The floating duplicate poster thumbnail overlay (`.pthumb`) is completely removed. Instead, the Details screen uses a **single taller 300px hero backdrop with an overlaid title, metadata badges, and seamless bottom gradient**, followed immediately by full-width **Play** and **Download** action buttons.

---

## 1. Overview & Core Concept

- **What It Does**: Transforms the ZORVÉN discovery, search, and details experience into a clean, zero-duplication streaming interface powered exclusively by live Unified V1 API data (`https://anymovie.ai.studio/api/v1`).
- **Target Audience / Persona**: Mobile streaming viewers browsing movies, web series, and anime across compact and standard Android viewports (320px–412px+).
- **Key Value**:
  - Every Home discovery rail displays a genuinely distinct collection of titles with strict canonical ID deduplication.
  - Opening any title renders a single, unambiguous Details view with zero duplicated or overlapping poster layers.
  - App startup, category filtering, search, and details navigation feel instantaneous and cohesive using layout-matched skeleton shimmer placeholders instead of jarring circular spinners.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Startup & Home Discovery**: Immediately upon entering Home after the ZORVÉN splash, the app renders a full layout-matched skeleton shimmer (taller hero placeholder, filter chip bar, and poster rails). As live data arrives, the hero crossfades in and begins auto-cycling through featured titles with subtle zoom motion, while each discovery row (**Top 10 Today**, **Trending Now**, **Top on ZORVÉN**, **Popular Movies**, **Popular Web Series**, **Critically Acclaimed**, **New Releases**, and category sections) renders strictly deduplicated titles.
  2. **Search & Filtering**: Navigating to Search immediately shows 16:9 landscape row skeletons while fetching results, then transitions into uniformly aligned result rows with crisp 16:9 landscape backdrops (`aspect-ratio: 16 / 9`, `object-fit: cover`), structured center metadata (title, year, rating badge, genre/type, 2-line overview), and a right-aligned Play trigger that never overlaps text even on 320px screens or when the keyboard is open.
  3. **Movie & Series Details**: Tapping any card navigates cleanly to a single Details view featuring one 300px cinematic backdrop with overlaid title and metadata, full-width Play and Download buttons, cast rail, and episode/trailer/related tabs—with skeleton shimmer placeholders during both initial details loading and season episode switching.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Deep ambient obsidian-plum cinema (`#17171a` to `#25233a`) with high-contrast typography and zero visual clutter.
  - *Color Palette & Mood*:
    - `--background`: `#17171a` / `#25233a` deep theatre surface
    - `--primary`: `#e50914` signature crimson brand accent
    - `--accent`: `#ffd166` warm rating gold & `#46d369` match emerald
    - `--muted`: `#b4b2c2` secondary metadata slate
  - *Typography & Hierarchy*: Bold condensed display titles with crisp letter-spacing on hero banners, paired with high-legibility sans-serif metadata rows and clamped 2-line synopses.
  - *Component Styling & Layout*: Responsive mobile-first containers supporting 320px through 412px+ widths without horizontal overflow or layout shift.
- **Interactive Feedback & Motion**:
  - **Skeleton Shimmer**: Linear sweep gradient (`110deg`, `#1f1d30` -> `#322f4a` -> `#1f1d30`) running at 1.4s infinite ease, automatically paused when `prefers-reduced-motion: reduce` is active.
  - **Hero Auto-Cycle & Ken Burns Zoom**: Smooth 6-second interval rotation across up to 5 hero titles with a subtle `scale(1.0) -> scale(1.06)` transform and tap-to-pause/resume behavior.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Canonical Cross-Section Deduplication Engine**:
  - *Chosen Approach*: Normalize every media item to a canonical key (`imdbId || tmdbId || id || normalizedTitle+year`) and pass Home sections through a deterministic priority allocator (`HeroFeatured -> Top 10 Today -> Trending Now -> Top on ZORVÉN -> Category/Genre Rails`).
  - *Why*: The upstream `/home` endpoint returns overlapping items across `trending`, `sections[0]` ("Trending Now"), `topRated`, and `/discover?platform=netflix`. Without a cross-section deduplicator and supplemental `/discover` pages, identical cards appear in consecutive rows.
  - *Alternatives Considered*: Shuffling arrays client-side was rejected because it still shows the same movies across adjacent rows and looks like demo data.
- **Decision 2 — Eliminating Duplicate Artwork on the Details Screen**:
  - *Chosen Approach*: Remove the secondary `.pthumb` poster element and `.head-row` negative-margin overlap altogether, clear `#zd` state immediately on route/selection changes, and render a single 300px `.hero` container with overlaid title and metadata.
  - *Why*: For many catalogue titles, `backdrop` and `poster` resolve to the same artwork URL. Rendering both `.hero img.himg` and an overlapping `.pthumb img` caused the same image to appear twice in the top viewport.
- **Decision 3 — Complete Removal of Continue Watching & Demo Strings**:
  - *Chosen Approach*: Remove the "Continue Watching" row, its `.tile.w` fake progress bars (`(25 + idx*13)%85%`), hardcoded fallback IDs (`tt4574334`), and demo account/help toasts, replacing empty or unavailable states with clean, honest UI messages.
  - *Why*: Ensures 100% of visible content comes from real API responses or actual user actions (`My List`, `Liked`, `Downloads`).

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                 Production Unified V1 API (anymovie.ai.studio)           │
│  /home?limit=20  │ /discover (Netflix, Movies, TV, Pages) │ /movie & /series │
└────────┬─────────┴───────────────────┬────────────────────┴────────┬─────┘
         │                             │                             │
         ▼                             ▼                             ▼
┌──────────────────────────────────────────────────────────────────────────┐
│              Canonical Deduplication & Catalogue Normalizer              │
│  • Canonical ID resolver: imdbId -> tmdbId -> id -> slug(title + year)   │
│  • Seen-ID registry across Home sections with deterministic priority     │
│  • Supplemental page fetcher when a row has <6 unique items after filter │
└────────┬─────────────────────────────┬─────────────────────────────┬─────┘
         │                             │                             │
         ▼                             ▼                             ▼
┌──────────────────────┐   ┌────────────────────────┐   ┌──────────────────┐
│   Home Discovery UI  │   │   Search & Filter UI   │   │  Details Screen  │
│ • 520px Auto-Cycling │   │ • 16:9 Landscape Box   │   │ • Single 300px   │
│   Hero + Subtle Zoom │   │   (aspect-ratio: 16/9) │   │   Hero + Overlay │
│ • Top 10 Today       │   │ • Backdrop -> Poster   │   │   Title & Meta   │
│ • Trending Now       │   │   Fallback Chain       │   │ • Full-Width     │
│ • Top on ZORVÉN      │   │ • Debounced Live Query │   │   Play/Download  │
│ • Zero Mock Rows     │   │ • Shimmer Row Loader   │   │ • Shimmer Loader │
└──────────────────────┘   └────────────────────────┘   └──────────────────┘
```

- **Data Model & State**:
  - **Canonical Identity (`getCanonicalId(item)`)**: Extracts `item.imdbId || item.tmdbId || item.id` first, falling back to ` normalizeTitle(item.title) + '_' + (item.year || '')`.
  - **Home Section Priority Pipeline**:
    1. **Hero Banner Pool**: Top 5 unique items with high-resolution backdrops from `home.hero` / `home.trending`.
    2. **Top 10 Today**: Ranked 1–10 using genuine `topRated` / ranked catalogue items, deduplicated internally so no title repeats within Top 10.
    3. **Trending Now**: Populated from `home.trending` + `home.sections` trending feed, excluding items already shown in Top 10 Today and the active Hero card, backfilled from `/discover?page=2` if needed.
    4. **Top on ZORVÉN**: Populated from `/discover?platform=netflix&status=RELEASED` + `popularSeries`/`popularMovies`, strictly excluding IDs already claimed by Top 10 Today and Trending Now.
    5. **Category & Genre Rows**: Each subsequent row filters out intra-row duplicates and prioritizes unseen catalogue items; any section with fewer than 3 valid unique items is cleanly hidden.
- **Interactive Component & State Mapping**:
  - **Skeleton Shimmer Lifecycle**:
    - `renderHomeSkeleton()`, `renderSearchSkeleton()`, `renderDetailsSkeleton()`, and `renderEpisodesSkeleton()` render exact-dimension structural placeholders with `.shimmer` CSS animation.
    - All `.spin` circular loader elements are removed from Home, Search, Clips, Filter views, and Details screens.
  - **Details Single-Instance Guarantee**:
    - Navigating to Details or tapping a "More Like This" card increments `requestToken`, immediately resets `DATA`, replaces `#zd` with `renderDetailsSkeleton()`, cancels any stale callbacks, and renders exactly one `.hero` backdrop (300px tall) with the title and metadata overlaid on the bottom gradient.
  - **Search 16:9 Landscape Thumbnail Container**:
    - `.sr .th` uses `width: 136px; aspect-ratio: 16 / 9; flex: 0 0 136px; overflow: hidden; border-radius: 6px;` (scaling cleanly down to `112px` on 320px viewports) with `img { width: 100%; height: 100%; object-fit: cover; object-position: center; }`, preferring `item.backdrop` and falling back to `item.poster`.
