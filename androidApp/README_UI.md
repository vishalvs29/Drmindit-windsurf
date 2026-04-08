# DrMindit Premium UI Design System

A comprehensive, premium mental wellness app UI built with Jetpack Compose, featuring modern glassmorphism design, smooth animations, and a calming dark theme.

## Design Philosophy

The DrMindit UI is designed to create a **calm, premium, and immersive** mental wellness experience inspired by top-tier apps like Calm and Headspace.

## Color System

### Primary Palette
- **Deep Navy**: `#0B1C2C` - Main background
- **Mid Blue**: `#1E3A5F` - Surface elements  
- **Teal Accent**: `#4FD1C5` - Primary actions and highlights
- **Purple Accent**: `#667EEA` - Secondary actions and accents
- **Pink Accent**: `#f093fb` - Gradient endpoints

### Mood Colors
- **Calm**: Teal (`#4FD1C5`)
- **Happy**: Yellow (`#F6E05E`)
- **Focused**: Purple (`#667EEA`)
- **Anxious**: Orange (`#ED8936`)
- **Sad**: Gray (`#718096`)
- **Energetic**: Green (`#48BB78`)

## Typography System

### Display Styles
- **Display Large**: 57sp, Bold
- **Display Medium**: 45sp, Bold
- **Display Small**: 36sp, SemiBold

### Body Styles
- **Body Large**: 16sp, Normal
- **Body Medium**: 14sp, Normal
- **Body Small**: 12sp, Normal

### Custom Styles
- **Timer Display**: 48sp, Monospace, Light
- **Welcome Message**: 18sp, Light
- **Session Title**: 20sp, SemiBold

## Component System

### Core Components

#### GlassCard
Premium glassmorphism card with backdrop blur and transparency
```kotlin
GlassCard(
    cornerRadius = 20.dp,
    backgroundColor = Color(0x0DFFFFFF),
    borderColor = Color(0x1AFFFFFF)
) {
    // Content
}
```

#### GradientButton
Primary action button with gradient background and hover effects
```kotlin
GradientButton(
    text = "Start Session",
    onClick = { /* Handle click */ },
    gradient = Brush.horizontalGradient(...)
)
```

#### SecondaryButton
Outlined secondary button with glass effect
```kotlin
SecondaryButton(
    text = "Learn More",
    onClick = { /* Handle click */ }
)
```

### Screen Components

#### HomeScreen
- Welcome message with user name
- Mood selector with emoji chips
- Featured session card with gradient
- Category filters
- Recent sessions list

#### SessionPlayerScreen
- Animated breathing orb
- Large timer display
- Progress bar
- Playback controls
- Session information

#### ProgressScreen
- Stats overview cards
- Weekly mood chart
- Achievement badges
- Recent activity

#### ExploreScreen
- Search bar with glass effect
- Category filters
- Featured sessions carousel
- Popular sessions list
- Recommendations

#### AnalyticsScreen
- Focus breakdown charts
- Session statistics
- Mood trends
- Progress charts
- Insights

## Animation System

### Breathing Animation
Smooth expand/contract animation for meditation sessions
```kotlin
val breathingScale by animateFloatAsState(
    targetValue = if (isPlaying) 1.2f else 1f,
    animationSpec = tween(4000, easing = EaseInOutCubic)
)
```

### Floating Elements
Subtle floating animations for cards and UI elements
```kotlin
val floatingOffset by rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
        animation = tween(3000, easing = EaseInOutSine),
        repeatMode = RepeatMode.Reverse
    )
)
```

### Particle Effects
Background particles for ambient animation
```kotlin
AnimatedBackground(particles = 20)
```

## Glassmorphism Effects

### Background Blur
```kotlin
.background(
    Color(0x0DFFFFFF) // 5% white
)
.backdropFilter(
    BlurEffect(radius = 10.dp)
)
```

### Border Effects
```kotlin
.border(
    width = 1.dp,
    brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0x33FFFFFF),
            Color(0x1AFFFFFF)
        )
    )
)
```

### Shadow Effects
```kotlin
.shadow(
    elevation = 12.dp,
    shape = RoundedCornerShape(20.dp),
    ambientColor = Color(0x1A4FD1C5),
    spotColor = Color(0x1A4FD1C5)
)
```

## Navigation System

### Bottom Navigation
Glassmorphism bottom navigation with 5 main sections:
- Home
- Explore
- Player
- Progress
- Analytics

### Navigation Transitions
Smooth fade and scale transitions between screens
```kotlin
animatedContent(
    transitionSpec = {
        fadeIn(animationSpec = tween(300)) with
        fadeOut(animationSpec = tween(300))
    }
) { targetState ->
    // Screen content
}
```

## Responsive Design

### Breakpoints
- **Small**: < 600dp
- **Medium**: 600dp - 840dp
- **Large**: > 840dp

### Adaptive Layouts
- Vertical stacking on small screens
- Horizontal scrolling for categories
- Flexible grid layouts for stats

## Performance Optimizations

### Composition Optimization
- Use `remember` for expensive calculations
- `@Composable` functions are skippable by default
- LazyColumn for large lists

### Animation Performance
- Use `rememberInfiniteTransition` for continuous animations
- Optimize animation curves
- Limit concurrent animations

### Image Optimization
- Use Coil for async image loading
- Implement proper caching
- Optimize image sizes for different densities

## Accessibility

### Color Contrast
- All text meets WCAG AA standards
- 4.5:1 contrast ratio for normal text
- 3:1 contrast ratio for large text

### Touch Targets
- Minimum 48dp touch targets
- Proper spacing between interactive elements
- Clear visual feedback on interaction

### Screen Reader Support
- Proper content descriptions
- Semantic markup
- Logical reading order

## Usage Examples

### Creating a Premium Card
```kotlin
GlassCardWithGradient(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    cornerRadius = 24.dp,
    gradient = Brush.radialGradient(
        colors = listOf(
            Color(0x1A4FD1C5),
            Color(0x1A667EEA)
        )
    )
) {
    // Card content
}
```

### Adding Breathing Animation
```kotlin
Box(
    modifier = Modifier
        .size(200.dp)
        .scale(breathingScale)
) {
    // Breathing orb content
}
```

### Custom Theme Usage
```kotlin
DrMinditTheme {
    // App content with premium theme
}
```

## Best Practices

### Design Guidelines
1. **Keep it minimal** - Less is more for mental wellness
2. **Use consistent spacing** - 8dp grid system
3. **Maintain visual hierarchy** - Clear typography scale
4. **Smooth transitions** - All animations should feel natural

### Code Guidelines
1. **Component composition** - Build complex UIs from simple components
2. **State management** - Use Compose state effectively
3. **Performance** - Optimize recomposition
4. **Testing** - Write UI tests for critical flows

This design system creates a cohesive, premium mental wellness experience that feels calm, professional, and engaging.
