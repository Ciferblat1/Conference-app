import { Navigate, Outlet } from 'react-router'
import { Loader } from '@mantine/core'

import { useMe } from '../hooks/useMe'

export default function RequireAuth() {

    const {data: user, isPending} = useMe()

    if(isPending) {
        return <Loader mt="xl" />
    }

    if(user === null) {
        return <Navigate to="/login" replace />
    }

    return <Outlet />
}