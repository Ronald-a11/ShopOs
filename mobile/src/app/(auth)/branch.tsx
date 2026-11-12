import { useState } from 'react'
import { AuthLayout } from '@/components/auth/AuthLayout'
import { useAuthNav } from '@/components/auth/navigation'
import { ActionButton, OptionList, SecureFooter, Subtitle, Title, type Option } from '@/components/auth/ui'

// Placeholder data, same as the web mock-up, until GET /branches is wired in.
const branches: Option[] = [
  { name: 'Harare CBD', code: 'BR-001', status: 'Available' },
  { name: 'Borrowdale', code: 'BR-002', status: 'Available' },
  { name: 'Bulawayo Main', code: 'BR-003', status: 'Available' },
  { name: 'Mutare', code: 'BR-004', status: 'Available' },
  { name: 'Gweru', code: 'BR-005', status: 'Available' },
]

export default function SelectBranchScreen() {
  const { go } = useAuthNav()
  const [selected, setSelected] = useState(0)
  return (
    <AuthLayout>
      <Title>Select a branch</Title>
      <Subtitle>Choose the branch you're working at today.</Subtitle>
      <OptionList options={branches} selected={selected} onSelect={setSelected} selectedLabel="Selected" />
      <ActionButton onPress={() => go('till')}>Continue</ActionButton>
      <SecureFooter />
    </AuthLayout>
  )
}
