import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router'
import { Button, Group, Text } from '@mantine/core'

import { logout } from '../api/auth'
import { useMe } from '../hooks/useMe'

export default function UserBar() {

    const {data: user} = useMe()
    const queryClient = useQueryClient()
    const navigate = useNavigate()

    const mutation = useMutation({
        mutationFn: logout,
        onSuccess: () => {
            queryClient.setQueryData(['me'], null)
            queryClient.removeQueries({queryKey: ['applications']})
            navigate('/login')
        },
    })

    if(!user) {
        return null
    }

    return (
        <Group justify="space-between">
            <Text>{user.fullname} ({user.role})</Text>

            <Button variant="default" loading={mutation.isPending} onClick={() => mutation.mutate()}>
                Выйти
            </Button>
        </Group>
    )
}