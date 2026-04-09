import localFont from 'next/font/local'
import { Metadata } from 'next'
import './globals.css'

const inter = localFont({
  src: [
    {
      path: './fonts/Inter-Regular.woff2',
      weight: '400',
      style: 'normal',
    },
    {
      path: './fonts/Inter-Medium.woff2',
      weight: '500',
      style: 'normal',
    },
    {
      path: './fonts/Inter-Bold.woff2',
      weight: '700',
      style: 'normal',
    },
  ],
  variable: '--font-inter',
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
    handle: '@drmindit',
    site: '@drmindit',
    cardType: 'summary_large_image',
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
        <NextSeo
          title={metadata.title}
          description={metadata.description}
          canonical="https://drmindit.com"
          openGraph={metadata.openGraph}
          twitter={metadata.twitter}
          additionalMetaTags={[
            {
              name: 'keywords',
              content: metadata.keywords,
            },
            {
              name: 'author',
              content: metadata.authors[0].name,
            },
          ]}
        />
        {children}
      </body>
    </html>
  )
}
