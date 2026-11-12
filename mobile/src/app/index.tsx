import { Redirect } from 'expo-router'

// Until session restore exists, the app always opens on sign in (as the web does).
export default function Index() {
  return <Redirect href="/login" />
}
