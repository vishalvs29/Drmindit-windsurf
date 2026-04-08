# DrMindit Landing Page

A premium, high-converting landing page for the DrMindit mental health application with modern glassmorphism design and smooth animations.

## Features

- **Modern Tech Stack**: Next.js 14 with TypeScript
- **Premium UI**: Glassmorphism design with blue-purple-pink gradients
- **Component Architecture**: Modular, reusable components
- **SEO Optimized**: Complete meta tags, semantic HTML, and structured data
- **Performance Optimized**: Lazy loading, optimized images, fast rendering
- **Fully Responsive**: Mobile-first design with Tailwind CSS
- **Smooth Animations**: Framer Motion for engaging user experience
- **Interactive Elements**: Hover effects, floating animations, micro-interactions

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Styling**: Tailwind CSS with custom design system
- **Animations**: Framer Motion
- **Icons**: Lucide React
- **TypeScript**: Full type safety
- **SEO**: Next SEO package

## Design System

### Colors
- **Primary Blue**: `#667eea`
- **Purple**: `#764ba2`
- **Pink**: `#f093fb`
- **Gradient**: Blue to Purple to Pink

### Typography
- **Font**: Inter (system fallback)
- **Headings**: Bold with gradient accents
- **Body**: Clean, readable text with proper contrast

### Components
- **Glass Cards**: Backdrop blur with transparency and border effects
- **Pill Buttons**: Rounded, modern CTA buttons with hover states
- **Animations**: Fade, slide, float, and scale effects
- **Interactive Elements**: Smooth transitions and micro-interactions

## Component Architecture

### Reusable Components
- `HeroSection` - Main landing section with CTA
- `FeaturesSection` - Feature cards with icons
- `TestimonialsSection` - User testimonials with ratings
- `AudienceSection` - Target audience segments
- `ShowcaseSection` - Product showcase
- `CTASection` - Final call-to-action
- `Footer` - Site footer with links

### UI Components
- `Button` - Reusable button component with variants
- `Card` - Glassmorphism card component

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Run the development server:
```bash
npm run dev
```

3. Open [http://localhost:3000](http://localhost:3000) in your browser.

## Build & Deploy

### Build for Production
```bash
npm run build
```

### Start Production Server
```bash
npm start
```

### Export Static Site
```bash
npm run export
```

## Deployment

This landing page is ready to deploy on Vercel:

1. Push to GitHub
2. Connect to Vercel
3. Deploy automatically

## Folder Structure

```
landing/
|-- app/
|   |-- globals.css
|   |-- layout.tsx
|   |-- page.tsx
|-- components/
|   |-- ui/
|   |   |-- Button.tsx
|   |   |-- Card.tsx
|   |   |-- index.ts
|   |-- HeroSection.tsx
|   |-- FeaturesSection.tsx
|   |-- TestimonialsSection.tsx
|   |-- AudienceSection.tsx
|   |-- ShowcaseSection.tsx
|   |-- CTASection.tsx
|   |-- Footer.tsx
|   |-- index.ts
|-- public/
|   |-- images/
|   |-- icons/
|-- package.json
|-- tailwind.config.js
|-- next.config.js
|-- README.md
```

## Customization

### Colors
Edit `app/globals.css` to modify the color scheme:

```css
body {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}
```

### Content
Update component props in `app/page.tsx` to modify:
- Hero section content
- Feature descriptions
- Use cases
- Testimonials

### Components
Each section is a separate component that can be:
- Reused on other pages
- Modified independently
- Styled with custom props
- Animated differently

## Performance Features

- **Image Optimization**: Next.js Image component
- **Lazy Loading**: Intersection Observer
- **Code Splitting**: Automatic with Next.js
- **Minification**: SWC minifier
- **Animation Performance**: Optimized Framer Motion

## Analytics

CTA clicks are tracked and can be integrated with:
- Google Analytics
- Mixpanel
- Custom analytics solutions

## License

MIT License - feel free to use this template for your projects.
