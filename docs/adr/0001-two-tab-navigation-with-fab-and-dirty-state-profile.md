# Two-Tab Navigation with Central FAB and Dirty-State Profile Confirmation

## Context
Originally, the app used a 4-tab bottom navigation bar (`Dashboard`, `MealPortion`, `History`, `Profile`). To streamline user experience and prioritize quick food logging, we consolidated the navigation into a 2-tab structure (`Diet` and `Profile`) with a prominent central Floating Action Button (FAB) for food logging, and embedded history directly into the `Diet` tab as a collapsible section. Additionally, editing physical profile metrics now uses a dirty-state confirmation bar (Confirm/Cancel) to prevent unintentional recalculations of macro targets on the `Diet` tab.

## Decision
1. **Bottom Navigation**: Use a `BottomAppBar` containing two navigation tabs (`Diet` on left, `Profile` on right) and a centered `FloatingActionButton` (+) navigating to the `MealPortion` screen.
2. **Profile Dirty State**: `ProfileViewModel` tracks editing changes against the saved profile. When changes exist, a bottom bar with `Confirm` and `Cancel` buttons appears. `Confirm` persists changes and updates macro targets across the app; `Cancel` resets all fields and hides the buttons.
3. **Height & Read-Only BMI**: Added `heightCm` to `UserProfile` / database schema. BMI is strictly read-only and recalculated in real time whenever height or weight is edited.
4. **Diet Tab Structure**: The `Diet` tab presents today's macro progress at the top and a collapsible accordion for historical meal logs at the bottom.
