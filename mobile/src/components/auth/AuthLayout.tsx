import type { ReactNode } from 'react'
import { KeyboardAvoidingView, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native'
import { StatusBar } from 'expo-status-bar'
import { usePathname } from 'expo-router'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import { Icon } from '@/components/Icon'
import { previewOrder, useAuthNav, type AuthRoute } from '@/components/auth/navigation'
import { colors, sizes } from '@/theme'

export function BrandMark() {
  return (
    <View style={styles.brand}>
      <View style={styles.brandMark}>
        <Icon name="cart" size={20} color={colors.story} strokeWidth={2} />
      </View>
      <Text style={styles.brandName}>ShopOS</Text>
      <View style={styles.brandPill}>
        <Text style={styles.brandPillText}>POS</Text>
      </View>
    </View>
  )
}

/** Faint concentric rings, the mobile echo of .login-story__ring on the web. */
function Rings() {
  return (
    <View pointerEvents="none" style={StyleSheet.absoluteFill}>
      <View style={[styles.ring, styles.ringLarge]} />
      <View style={[styles.ring, styles.ringSmall]} />
      <View style={styles.glow} />
    </View>
  )
}

/** Development-only link that walks every auth screen, like the web mock-up's Next button. */
function PreviewNext() {
  const pathname = usePathname()
  const { replace } = useAuthNav()
  const current = previewOrder.indexOf(pathname.slice(1) as AuthRoute)
  const next = previewOrder[(current + 1) % previewOrder.length]

  return (
    <Pressable onPress={() => replace(next)} hitSlop={8} style={styles.preview} accessibilityRole="button">
      <Text style={styles.previewText}>Preview next screen</Text>
      <Icon name="arrow" size={14} color={colors.subtle} strokeWidth={2} />
    </Pressable>
  )
}

/**
 * Green brand header with a white sheet for the content. `intro` adds extra
 * header content under the brand mark (the login headline).
 */
export function AuthLayout({ children, intro }: { children: ReactNode; intro?: ReactNode }) {
  const insets = useSafeAreaInsets()

  return (
    <View style={styles.screen}>
      <StatusBar style="light" />
      {/* White under the lower half so overscrolling past the sheet never shows green. */}
      <View style={styles.underlay} />
      <Rings />
      <KeyboardAvoidingView behavior="padding" style={styles.flex}>
        <ScrollView
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scroll}
        >
          <View style={[styles.header, { paddingTop: insets.top + 14 }]}>
            <BrandMark />
            {intro}
          </View>
          <View style={[styles.sheet, { paddingBottom: insets.bottom + 20 }]}>
            {children}
            {__DEV__ ? <PreviewNext /> : null}
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  )
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.story },
  flex: { flex: 1 },
  underlay: { position: 'absolute', left: 0, right: 0, bottom: 0, height: '50%', backgroundColor: colors.surface },
  scroll: { flexGrow: 1 },
  header: { paddingHorizontal: sizes.gutter, paddingBottom: 30 },
  sheet: {
    flexGrow: 1,
    paddingHorizontal: sizes.gutter,
    paddingTop: 30,
    borderTopLeftRadius: sizes.sheetRadius,
    borderTopRightRadius: sizes.sheetRadius,
    backgroundColor: colors.surface,
  },
  ring: { position: 'absolute', borderRadius: 999, borderWidth: 1, borderColor: 'rgba(255, 255, 255, 0.07)' },
  ringLarge: { width: 420, height: 420, top: -190, right: -170 },
  ringSmall: { width: 260, height: 260, top: -110, right: -90 },
  glow: {
    position: 'absolute',
    width: 220,
    height: 220,
    top: -60,
    right: -60,
    borderRadius: 999,
    backgroundColor: 'rgba(52, 232, 160, 0.06)',
  },
  brand: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  brandMark: {
    width: 38,
    height: 38,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 11,
    backgroundColor: colors.mint,
  },
  brandName: { color: colors.white, fontSize: 19, fontWeight: '700', letterSpacing: -0.3 },
  brandPill: {
    paddingHorizontal: 7,
    paddingVertical: 2,
    borderRadius: 6,
    backgroundColor: 'rgba(52, 232, 160, 0.16)',
  },
  brandPillText: { color: colors.highlight, fontSize: 10, fontWeight: '700', letterSpacing: 1.5 },
  preview: { marginTop: 18, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6 },
  previewText: { color: colors.subtle, fontSize: 12, fontWeight: '500' },
})
