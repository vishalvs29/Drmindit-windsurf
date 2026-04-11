import { Metadata } from 'next'
import localFont from 'next/font/local'
import './globals.css'

const inter = localFont({
  src: [
    {
      path: './fonts/inter-regular.woff2',
      weight: '400',
      style: 'normal',
    },
    {
      path: './fonts/inter-bold.woff2',
      weight: '700',
      style: 'normal',
    },
  ],
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'DrMindit – Mental Health App for Stress, Sleep & Focus',
  description: 'Reduce stress, improve sleep, and stay focused with AI-guided sessions and relaxing audio like rain and 432Hz. Start your mental wellness journey today.',
  keywords: 'mental health app, stress relief, sleep app, focus app, anxiety relief, meditation, wellness, therapy, audio therapy',
  authors: [{ name: 'DrMindit' }],
  robots: 'index, follow',
  viewport: 'width=device-width, initial-scale=1',
  openGraph: {
    title: 'DrMindit – Mental Health App',
    description: 'AI-powered mental wellness for students, professionals, and high-pressure roles',
    url: 'https://drmindit.com',
    siteName: 'DrMindit',
    images: [
      {
        url: '/og-image.jpg',
        width: 1200,
        height: 630,
        alt: 'DrMindit Mental Health App',
      },
    ],
    type: 'website',
  },
  twitter: {
    site: '@drmindit',
    card: 'summary_large_image',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        {children}
      </body>
    </html>
  )
}
