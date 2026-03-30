import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata = {
  title: 'DrMindit Voice Assistant - AI-Powered Mental Wellness',
  description: 'Experience natural, calming AI voice guidance for mental wellness with voice-guided sessions, voice chat, and instant stress relief.',
  keywords: 'AI voice assistant, mental wellness, voice therapy, stress relief, guided meditation, voice chat',
  authors: [{ name: 'DrMindit' }],
  openGraph: {
    title: 'DrMindit Voice Assistant',
    description: 'Natural AI voice experience for mental wellness and stress relief',
    url: 'https://drmindit.com/voice-assistant',
    siteName: 'DrMindit',
    images: [
      {
        url: '/voice-assistant-og.jpg',
        width: 1200,
        height: 630,
        alt: 'DrMindit Voice Assistant',
      },
    ],
    type: 'website',
  },
  robots: 'index, follow',
  viewport: 'width=device-width, initial-scale=1',
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
