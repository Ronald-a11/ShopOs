import { useState, type ReactNode } from 'react'
import {
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
  type KeyboardTypeOptions,
  type TextInputProps,
} from 'react-native'
import { Icon, type IconName } from '@/components/Icon'
import { colors, sizes } from '@/theme'

// Mobile counterparts of the helpers and .auth-* classes in
// frontend/src/auth/AuthScreens.tsx and LoginScreen.css.

export function Title({ children }: { children: ReactNode }) {
  return <Text style={styles.title}>{children}</Text>
}

export function Subtitle({ children }: { children: ReactNode }) {
  return <Text style={styles.subtitle}>{children}</Text>
}

export function BackButton({ label = 'Back to sign in', onPress }: { label?: string; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} hitSlop={10} style={styles.back} accessibilityRole="button">
      <Text style={styles.backText}>← {label}</Text>
    </Pressable>
  )
}

export function StatusIcon({ name }: { name: IconName }) {
  return (
    <View style={styles.statusIcon}>
      <Icon name={name} size={24} />
    </View>
  )
}

export function Label({ children }: { children: ReactNode }) {
  return <Text style={styles.label}>{children}</Text>
}

export function TextField({
  icon,
  prefix,
  secure = false,
  keyboardType,
  ...input
}: {
  icon?: IconName
  prefix?: string
  secure?: boolean
  keyboardType?: KeyboardTypeOptions
} & Pick<TextInputProps, 'placeholder' | 'value' | 'onChangeText' | 'autoComplete' | 'textContentType'>) {
  const [focused, setFocused] = useState(false)
  const [visible, setVisible] = useState(false)
  const tint = focused ? colors.accent : colors.subtle

  return (
    <View style={[styles.input, focused && styles.inputFocused]}>
      {icon ? <Icon name={icon} color={tint} /> : null}
      {prefix ? <Text style={[styles.prefix, { color: tint }]}>{prefix}</Text> : null}
      <TextInput
        {...input}
        style={styles.inputText}
        placeholderTextColor={colors.placeholder}
        selectionColor={colors.accent}
        keyboardType={keyboardType}
        secureTextEntry={secure && !visible}
        autoCapitalize="none"
        autoCorrect={false}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
      />
      {secure ? (
        <Pressable
          onPress={() => setVisible((v) => !v)}
          hitSlop={10}
          accessibilityRole="button"
          accessibilityLabel={visible ? 'Hide password' : 'Show password'}
        >
          <Icon name="eye" color={visible ? colors.accent : colors.subtle} />
        </Pressable>
      ) : null}
    </View>
  )
}

export function ActionButton({
  children,
  onPress,
  secondary = false,
}: {
  children: ReactNode
  onPress?: () => void
  secondary?: boolean
}) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      style={({ pressed }) => [
        styles.action,
        secondary ? styles.actionSecondary : styles.actionPrimary,
        pressed && styles.pressed,
      ]}
    >
      <Text style={[styles.actionText, secondary && styles.actionTextSecondary]}>{children}</Text>
      {secondary ? null : <Icon name="arrow" color={colors.white} strokeWidth={2} />}
    </Pressable>
  )
}

export function Divider() {
  return (
    <View style={styles.divider}>
      <View style={styles.dividerLine} />
      <Text style={styles.dividerText}>OR</Text>
      <View style={styles.dividerLine} />
    </View>
  )
}

export function TextLink({ children, onPress }: { children: ReactNode; onPress?: () => void }) {
  return (
    <Pressable onPress={onPress} hitSlop={8} accessibilityRole="link" style={styles.textLink}>
      <Text style={styles.textLinkText}>{children}</Text>
    </Pressable>
  )
}

export function Notice({ icon = 'info', title, children }: {
  icon?: IconName
  title?: string
  children: ReactNode
  /** Kept for parity with the web's .auth-notice--info; both render the same on mobile. */
  info?: boolean
}) {
  return (
    <View style={styles.notice}>
      <View style={styles.noticeIcon}>
        <Icon name={icon} size={16} />
      </View>
      <View style={styles.noticeBody}>
        {title ? <Text style={styles.noticeTitle}>{title}</Text> : null}
        <Text style={styles.noticeText}>{children}</Text>
      </View>
    </View>
  )
}

export function SecureFooter() {
  return (
    <View style={styles.footer}>
      <Icon name="padlock" size={13} color={colors.subtle} strokeWidth={1.8} />
      <Text style={styles.footerText}>SECURE &amp; ENCRYPTED</Text>
    </View>
  )
}

export function Requirements({ items }: { items: string[] }) {
  return (
    <View style={styles.requirements}>
      {items.map((item) => (
        <View key={item} style={styles.requirement}>
          <View style={styles.requirementDot} />
          <Text style={styles.requirementText}>{item}</Text>
        </View>
      ))}
    </View>
  )
}

export function Segmented<T extends string>({ options, value, onChange }: {
  options: { value: T; label: string }[]
  value: T
  onChange: (value: T) => void
}) {
  return (
    <View style={styles.segmented}>
      {options.map((option) => {
        const active = option.value === value
        return (
          <Pressable
            key={option.value}
            onPress={() => onChange(option.value)}
            accessibilityRole="radio"
            accessibilityState={{ selected: active }}
            style={[styles.segment, active && styles.segmentActive]}
          >
            <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{option.label}</Text>
          </Pressable>
        )
      })}
    </View>
  )
}

export type Option = { name: string; code: string; status: string }

export function OptionList({ options, selected, onSelect, selectedLabel }: {
  options: Option[]
  selected: number
  onSelect: (index: number) => void
  selectedLabel?: string
}) {
  return (
    <View style={styles.options}>
      {options.map((option, index) => {
        const active = index === selected
        return (
          <Pressable
            key={option.code}
            onPress={() => onSelect(index)}
            accessibilityRole="radio"
            accessibilityState={{ selected: active }}
            style={[styles.option, active && styles.optionActive]}
          >
            <View style={styles.optionBody}>
              <Text style={styles.optionName}>{option.name}</Text>
              <Text style={styles.optionCode}>{option.code}</Text>
            </View>
            <Text style={[styles.optionStatus, active && styles.optionStatusActive]}>
              {active && selectedLabel ? selectedLabel : option.status}
            </Text>
            <View style={[styles.check, active && styles.checkActive]}>
              {active ? <Icon name="check" size={12} color={colors.white} strokeWidth={3} /> : null}
            </View>
          </Pressable>
        )
      })}
    </View>
  )
}

export function Steps({ steps }: { steps: { icon: IconName; label: string; state?: 'done' | 'active' }[] }) {
  return (
    <View style={styles.steps}>
      {steps.map((step, index) => (
        <View
          key={step.label}
          style={[styles.step, index === steps.length - 1 && styles.stepLast, step.state && styles.stepHighlighted]}
        >
          <Icon name={step.state === 'done' ? 'check' : step.icon} size={16} color={step.state ? colors.accent : colors.subtle} />
          <Text style={[styles.stepText, step.state && styles.stepTextHighlighted]}>{step.label}</Text>
        </View>
      ))}
    </View>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '700', letterSpacing: -0.6 },
  subtitle: { marginTop: 6, marginBottom: 6, color: colors.muted, fontSize: 15, lineHeight: 22 },
  back: { alignSelf: 'flex-start', marginBottom: 14 },
  backText: { color: colors.accent, fontSize: 14, fontWeight: '600' },
  statusIcon: {
    width: 52,
    height: 52,
    marginBottom: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 16,
    backgroundColor: colors.mint,
  },
  label: { marginTop: 18, marginBottom: 8, color: colors.ink, fontSize: 13, fontWeight: '600' },
  input: {
    height: sizes.control,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    paddingHorizontal: 16,
    borderWidth: 1.5,
    borderColor: 'transparent',
    borderRadius: sizes.radius,
    backgroundColor: colors.field,
  },
  inputFocused: { borderColor: colors.accent, backgroundColor: colors.surface },
  inputText: { flex: 1, height: '100%', color: colors.ink, fontSize: 16 },
  prefix: { fontSize: 16, fontWeight: '600' },
  action: {
    height: sizes.control,
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderRadius: sizes.radius,
  },
  actionPrimary: {
    backgroundColor: colors.ink,
    shadowColor: colors.story,
    shadowOpacity: 0.25,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 6 },
    elevation: 4,
  },
  actionSecondary: { borderWidth: 1.5, borderColor: colors.line, backgroundColor: colors.surface },
  actionText: { color: colors.white, fontSize: 16, fontWeight: '600' },
  actionTextSecondary: { color: colors.accent },
  pressed: { opacity: 0.85, transform: [{ scale: 0.99 }] },
  divider: { height: 44, marginTop: 4, flexDirection: 'row', alignItems: 'center', gap: 12 },
  dividerLine: { flex: 1, height: 1, backgroundColor: colors.line },
  dividerText: { color: colors.subtle, fontSize: 11, fontWeight: '600', letterSpacing: 2 },
  textLink: { alignSelf: 'center', paddingVertical: 6 },
  textLinkText: { color: colors.accent, fontSize: 14, fontWeight: '600' },
  notice: {
    marginTop: 16,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
    borderRadius: sizes.radius,
    backgroundColor: colors.mintSoft,
  },
  noticeIcon: {
    width: 30,
    height: 30,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 9,
    backgroundColor: colors.surface,
  },
  noticeBody: { flex: 1, paddingTop: 1 },
  noticeTitle: { marginBottom: 3, color: colors.ink, fontSize: 14, fontWeight: '700' },
  noticeText: { color: colors.muted, fontSize: 14, lineHeight: 20 },
  footer: { marginTop: 22, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6 },
  footerText: { color: colors.subtle, fontSize: 10, fontWeight: '600', letterSpacing: 2 },
  requirements: { marginTop: 12, gap: 6 },
  requirement: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  requirementDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: colors.line },
  requirementText: { color: colors.muted, fontSize: 13 },
  segmented: { flexDirection: 'row', padding: 4, borderRadius: sizes.radius, backgroundColor: colors.field },
  segment: { flex: 1, height: 44, alignItems: 'center', justifyContent: 'center', borderRadius: 10 },
  segmentActive: {
    backgroundColor: colors.surface,
    shadowColor: colors.story,
    shadowOpacity: 0.1,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  segmentText: { color: colors.muted, fontSize: 14, fontWeight: '600' },
  segmentTextActive: { color: colors.ink },
  options: { marginTop: 14, gap: 10 },
  option: {
    minHeight: 68,
    paddingHorizontal: 16,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    borderWidth: 1.5,
    borderColor: 'transparent',
    borderRadius: sizes.radius,
    backgroundColor: colors.field,
  },
  optionActive: { borderColor: colors.accent, backgroundColor: colors.mintSoft },
  optionBody: { flex: 1 },
  optionName: { color: colors.ink, fontSize: 16, fontWeight: '600' },
  optionCode: { marginTop: 3, color: colors.subtle, fontSize: 12, fontWeight: '500', letterSpacing: 0.5 },
  optionStatus: { color: colors.subtle, fontSize: 12, fontWeight: '500' },
  optionStatusActive: { color: colors.accent, fontWeight: '600' },
  check: {
    width: 22,
    height: 22,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: colors.line,
    borderRadius: 11,
    backgroundColor: colors.surface,
  },
  checkActive: { borderColor: colors.accent, backgroundColor: colors.accent },
  steps: { marginTop: 14, overflow: 'hidden', borderRadius: sizes.radius, backgroundColor: colors.field },
  step: {
    height: sizes.control,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.surface,
  },
  stepLast: { borderBottomWidth: 0 },
  stepHighlighted: { backgroundColor: colors.mintSoft },
  stepText: { color: colors.subtle, fontSize: 14 },
  stepTextHighlighted: { color: colors.accent, fontWeight: '600' },
})
