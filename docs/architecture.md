# Clean Architecture (KMP)
This repo is structured as a reusable boilerplate that enforces Clean Architecture boundaries at the Gradle-module level.

## Modules
`shared-domain`
Pure domain logic.
- Entities, models, repository interfaces
- UseCases
- Cross-layer abstractions (e.g. `AuthNotifier`)
Rules:
- Must not depend on data/presentation modules.

`shared-data`
Data layer implementations.
- Ktor networking, DTOs
- Repository implementations
- TokenManager/AuthBootstrapper
Rules:
- Depends on `shared-domain`
- Must not depend on `shared-presentation`

`shared-presentation`
Presentation layer.
- ViewModels
- UI state, reducers
- `AuthStore` (StateFlow<AuthState> + SharedFlow<AuthEffect>)
Rules:
- Depends on `shared-domain`
- Must not depend on `shared-data`

`shared` (composition root + iOS framework)
- Wires the dependency graph (Koin modules)
- Exposes Swift-friendly factories and stores
- Builds the iOS framework `MashupShared`
Rules:
- Can depend on all shared modules.

## iOS integration
The iOS app links only one artifact: `MashupShared.xcframework` built from `:shared`.

Key points:
- `:shared` re-exports (`api`) and `export(...)` all layer modules so their public APIs appear in the generated ObjC/Swift headers.
- Keep Swift-friendly entrypoints in `:shared` (composition root) to avoid presentation/data importing each other.

## Auth routing
Shared exposes:
- `AuthStore.authState: StateFlow<AuthState>` (source of truth for root flow)
- `AuthStore.effects: SharedFlow<AuthEffect>` (one-shot messages)

Native:
- Android collects these flows.
- iOS uses `AuthStore.watchState` / `AuthStore.watchEffects` (callback bridge) to avoid Flow bridging complexity.

## Enforcement
Enforcement is done in two layers:
1) Gradle module boundaries: wrong imports typically do not compile because the module is not on the classpath.
2) `./gradlew archCheck`: a repo-level task that scans source for forbidden imports and fails the build.

## Adding a new feature
- Add domain interfaces/usecases to `shared-domain`.
- Implement data in `shared-data`.
- Create viewmodels/state in `shared-presentation`.
- Wire everything in `shared` (Koin modules + factories exported to Swift).
