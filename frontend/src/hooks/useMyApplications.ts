import {useQuery} from '@tanstack/react-query'

import {getApplications} from '../api/applications'


export function useMyApplications() {
    return useQuery({
        queryKey: ['applications', 'me'],
        queryFn: getApplications
    })
}