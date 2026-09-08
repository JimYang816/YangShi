# YangShi Domain Language

YangShi (央食) is a precise macro-nutrient tracking and food portion balancing application for Android.

## Language

**Diet (饮食)**:
The primary left tab containing daily macronutrient intake progress bars (Carbs, Protein, Fat, Calories) and a collapsible history log.
_Avoid_: Dashboard, Home, Main Screen

**Profile (我的)**:
The secondary right tab displaying user physical metrics (Weight, Height, BMI), macronutrient target intake coefficients (g/kg/day), and meal allocation ratios (%).
_Avoid_: Settings, User Settings, Body Configuration

**BMI (身体质量指数)**:
Read-only body mass index calculated dynamically from height and weight (`weight_kg / (height_m)^2`).
_Avoid_: Body Index, Fat Ratio

**Meal Log (打卡日志)**:
Recorded food logs categorized by meal types (Breakfast, Lunch, Dinner, Snack).
_Avoid_: Food Record, Eating Log

**Portion Auto-Balancing (食物自动配平)**:
Algorithmic computation that determines optimal weights for unlocked foods to hit specific macronutrient targets for a meal.
_Avoid_: Food Calculation, Calorie Solver
