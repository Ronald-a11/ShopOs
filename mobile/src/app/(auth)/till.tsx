import { useState } from 'react'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { ActionButton, OptionList, SecureFooter, StatusIcon, Subtitle, Title, type Option } from '@/components/auth/ui'

// Placeholder data, same as the web mock-up, until GET /branches/{id}/devices is wired in.
const tills: Option[] = [
  { name: 'Counter till 01', code: 'BR-001-T01', status: 'Available' },
  { name: 'Counter till 02', code: 'BR-001-T02', status: 'In use' },
  { name: 'Handheld', code: 'BR-001-H03', status: 'Available' },
  { name: 'Self-service kiosk', code: 'BR-001-K04', status: 'Offline' },
]

export default function SelectTillScreen() {
  const [selected, setSelected] = useState(0)
  return (
    <AuthLayout>
      <StatusIcon name="monitor" />
      <Title>Select a till</Title>
      <Subtitle>Choose the device for General retail at Harare CBD.</Subtitle>
      <OptionList options={tills} selected={selected} onSelect={setSelected} />
      <ActionButton>Open till</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
