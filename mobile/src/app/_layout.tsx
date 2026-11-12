import { Stack } from 'expo-router'
import { colors } from '@/theme'

export default function RootLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: false,
        // The web auth card slides between screens; the native push slide matches it.
        animation: 'slide_from_right',
        contentStyle: { backgroundColor: colors.bg },
      }}
    />
  )
}
