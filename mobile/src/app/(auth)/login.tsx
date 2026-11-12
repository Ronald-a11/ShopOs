import { useState } from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { Icon } from '@/components/Icon'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, Label, SecureFooter, Subtitle, TextField, Title } from '@/components/auth/ui'
import { colors } from '@/theme'

// Same headline as LoginStoryPanel on the web, set in the green header.
function Intro() {
  return (
    <View style={styles.intro}>
      <Text style={styles.eyebrow}>BUILT FOR MODERN RETAIL</Text>
      <Text style={styles.headline}>
        Smarter checkouts.{'\n'}Better inventory.{'\n'}A <Text style={styles.headlineEm}>stronger</Text> business.
      </Text>
    </View>
  )
}

export default function LoginScreen() {
  const { go } = useAuthNav()
  const [remember, setRemember] = useState(false)

  return (
    <AuthLayout intro={<Intro />}>
      <Title>Welcome back</Title>
      <Subtitle>Sign in to continue managing your store.</Subtitle>

      <Label>Email address</Label>
      <TextField
        icon="mail"
        placeholder="name@yourstore.co.zw"
        keyboardType="email-address"
        autoComplete="email"
        textContentType="emailAddress"
      />

      <View style={styles.labelRow}>
        <Label>Password</Label>
        <Pressable onPress={() => go('forgot')} hitSlop={8}>
          <Text style={styles.forgot}>Forgot password?</Text>
        </Pressable>
      </View>
      <TextField
        icon="lock"
        placeholder="Enter your password"
        secure
        autoComplete="current-password"
        textContentType="password"
      />

      <Pressable
        style={styles.remember}
        onPress={() => setRemember((v) => !v)}
        accessibilityRole="checkbox"
        accessibilityState={{ checked: remember }}
      >
        <View style={[styles.checkbox, remember && styles.checkboxOn]}>
          {remember ? <Icon name="check" size={12} color={colors.white} strokeWidth={3} /> : null}
        </View>
        <Text style={styles.rememberText}>Keep me signed in on this device</Text>
      </Pressable>

      <ActionButton onPress={() => go('branch')}>Sign in</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}

const styles = StyleSheet.create({
  intro: { marginTop: 28 },
  eyebrow: { color: colors.highlight, fontSize: 11, fontWeight: '700', letterSpacing: 2 },
  headline: { marginTop: 10, color: colors.white, fontSize: 30, fontWeight: '700', lineHeight: 36, letterSpacing: -0.8 },
  headlineEm: { color: colors.highlight, fontStyle: 'italic' },
  labelRow: { flexDirection: 'row', alignItems: 'baseline', justifyContent: 'space-between' },
  forgot: { color: colors.accent, fontSize: 13, fontWeight: '600' },
  remember: { marginTop: 16, flexDirection: 'row', alignItems: 'center', gap: 10 },
  checkbox: {
    width: 22,
    height: 22,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: colors.line,
    borderRadius: 7,
    backgroundColor: colors.surface,
  },
  checkboxOn: { borderColor: colors.accent, backgroundColor: colors.accent },
  rememberText: { color: colors.muted, fontSize: 14 },
})
