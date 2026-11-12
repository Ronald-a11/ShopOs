import type { ReactNode } from 'react'
import Svg, { Circle, Path, Rect } from 'react-native-svg'
import { colors } from '@/theme'

// Same 24x24 stroke paths as the web auth screens (frontend/src/auth).
const paths = {
  clock: <><Circle cx="12" cy="12" r="9" /><Path d="M12 7v5l3.5 2" /></>,
  logout: <Path d="M10 5H5v14h5M13 8l4 4-4 4M17 12H9" />,
  shield: <><Path d="M12 3 20 6v5c0 5-3.2 8.3-8 10-4.8-1.7-8-5-8-10V6l8-3Z" /><Path d="m9 12 2 2 4-4" /></>,
  block: <><Circle cx="12" cy="12" r="8" /><Path d="m7.5 16.5 9-9" /></>,
  monitor: <><Rect x="3" y="4" width="18" height="13" rx="2" /><Path d="M8 21h8M12 17v4" /></>,
  restore: <Path d="M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7" />,
  lock: <><Rect x="5" y="10" width="14" height="11" rx="2" /><Path d="M8 10V7a4 4 0 0 1 8 0v3" /></>,
  padlock: <><Rect x="6" y="10" width="12" height="10" rx="2" /><Path d="M9 10V7a3 3 0 0 1 6 0v3M12 14v2" /></>,
  mail: <><Rect x="3" y="5" width="18" height="14" rx="2" /><Path d="m4 7 8 6 8-6" /></>,
  info: <><Circle cx="12" cy="12" r="9" /><Path d="M12 11v6M12 7.5v.5" /></>,
  arrow: <Path d="M5 12h13M13 7l5 5-5 5" />,
  check: <Path d="m5 12 5 5 9-10" />,
  eye: <><Path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" /><Circle cx="12" cy="12" r="2.5" /></>,
  cart: <><Path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.5h7.9a2 2 0 0 0 1.9-1.4L20.5 8H7" /><Circle cx="9" cy="19" r="1.2" /><Circle cx="17" cy="19" r="1.2" /></>,
  checkout: <Path d="M2 4h3l2 11h10l2-7H6M9 19h8M8 19a1 1 0 1 0 0 2 1 1 0 0 0 0-2ZM17 19a1 1 0 1 0 0 2 1 1 0 0 0 0-2Z" />,
  inventory: <Path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Zm0 9 8-4.5M12 12 4 7.5M12 12v9" />,
  secure: <Path d="M12 3 20 6v5c0 5-3.4 8.4-8 10-4.6-1.6-8-5-8-10V6l8-3Zm-3 9 2 2 4-4" />,
  store: <Path d="M3 11h18l-2-6H5l-2 6Zm2 0v8h14v-8M7 15h10M8 19v2m8-2v2" />,
} satisfies Record<string, ReactNode>

export type IconName = keyof typeof paths

export function Icon({
  name,
  size = 18,
  color = colors.accent,
  strokeWidth = 1.7,
}: {
  name: IconName
  size?: number
  color?: string
  strokeWidth?: number
}) {
  return (
    <Svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {paths[name]}
    </Svg>
  )
}
