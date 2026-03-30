# DrMindit Landing Page

A production-ready, high-converting landing page for the DrMindit mental health application.

## Features

- **Modern Tech Stack**: Next.js 14 with TypeScript
- **Premium UI**: Glassmorphism design inspired by Calm and Headspace
- **SEO Optimized**: Complete meta tags, semantic HTML, and structured data
- **Performance Optimized**: Lazy loading, optimized images, fast rendering
- **Fully Responsive**: Mobile-first design with Tailwind CSS
- **Smooth Animations**: Framer Motion for engaging user experience
- **Analytics Ready**: Google Analytics integration setup

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Styling**: Tailwind CSS with custom design system
- **Animations**: Framer Motion
- **Icons**: Lucide React
- **TypeScript**: Full type safety
- **SEO**: Next SEO package

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

## Design System

### Colors
- **Deep Blue**: `#0D1B2A`
- **Purple**: `#1B263B`
- **Accent Teal**: `#14B8A6`
- **Accent Teal Light**: `#5EEAD4`

### Typography
- **Font**: Inter (system fallback)
- **Headings**: Bold with gradient accents
- **Body**: Clean, readable text

### Components
- **Glass Cards**: Backdrop blur with transparency
- **Pill Buttons**: Rounded, modern CTA buttons
- **Animations**: Fade, slide, and float effects

## SEO Features

- **Meta Tags**: Optimized title and description
- **Open Graph**: Social media sharing
- **Twitter Cards**: Twitter-specific meta
- **Structured HTML**: Semantic markup
- **Alt Text**: All images have descriptions
- **Keywords**: Natural keyword integration

## Performance Features

- **Image Optimization**: Next.js Image component
- **Lazy Loading**: Intersection Observer
- **Code Splitting**: Automatic with Next.js
- **Minification**: SWC minifier
- **Compression**: Gzip/Brotli ready

## Analytics

Google Analytics is configured to track:
- Page views
- CTA clicks
- User engagement
- Conversion events

## Folder Structure

```
landing/
├── app/
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── ui/
│   └── sections/
├── public/
│   ├── images/
│   └── icons/
├── package.json
├── tailwind.config.js
├── next.config.js
└── README.md
```

## Customization

### Colors
Edit `tailwind.config.js` to modify the color scheme:

```javascript
colors: {
  'deep-blue': '#0D1B2A',
  'purple': '#1B263B',
  'accent-teal': '#14B8A6',
}
```

### Content
Update `app/page.tsx` to modify:
- Hero section content
- Feature descriptions
- Use cases
- Testimonials

### Analytics
Add your Google Analytics ID to `app/layout.tsx`.

## License

MIT License - feel free to use this template for your projects.
