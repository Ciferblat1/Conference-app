import {Alert, Badge, Button, Center, Loader, Table, Text } from '@mantine/core'
import { useMyApplications } from '../hooks/useMyApplications';
import { useDirections } from '../hooks/useDirections';
import type { ApplicationStatus } from '../types/api'
import { withdrawApplication } from '../api/applications';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { notifications } from '@mantine/notifications';



const STATUS: Record<ApplicationStatus, {label: string, color: string}> = {
    SUBMITTED: {label: 'Подана', color: 'blue'},
    APPROVED: {label: 'Одобрена', color: 'green'},
    WITHDRAWN: {label: 'Отозвана', color: 'gray'}
}

export default function ApplicationList() {

    const {data: directions = []} = useDirections()
    const {data: applications = [], isPending, isError, error} = useMyApplications()
    const queryClient = useQueryClient()

    const mutation = useMutation({
        mutationFn: withdrawApplication,
        onSuccess: async() => {
            await queryClient.invalidateQueries({queryKey: ['applications'], refetchType: 'all'})
            notifications.show({message: 'Заявка отозвана'})
        },
        onError: (error) => {
            notifications.show({color: 'red', message: error.message})
        }
    })

    if(isPending) {
        return <Center><Loader /></Center>
    }

    if(isError) {
        return <Alert color="red">{error.message}</Alert>
    }

    if(applications.length === 0) {
        return <Text ta="center" c="dimmed">У вас пока нет заявок</Text>
    }

    const titles = new Map(directions.map(direction => [direction.id, direction.title]))

    return (
        <Table>
            <Table.Thead>
                <Table.Tr>
                    <Table.Th>Направление</Table.Th>
                    <Table.Th>Статус</Table.Th>
                    <Table.Th>Дата подачи</Table.Th>
                    <Table.Th>Дата обновления</Table.Th>
                    <Table.Th>Действия</Table.Th>
                </Table.Tr>
            </Table.Thead>

            <Table.Tbody>
                {applications.map(application => (
                    <Table.Tr key={application.id}>
                        <Table.Td>{titles.get(application.directionId) ?? '-'}</Table.Td>
                        <Table.Td>
                            <Badge color={STATUS[application.status].color}>
                                {STATUS[application.status].label}
                            </Badge>
                        </Table.Td>
                        <Table.Td>{new Date(application.createdAt).toLocaleString('ru-RU')}</Table.Td>
                        <Table.Td>{new Date(application.updatedAt).toLocaleString('ru-RU')}</Table.Td>
                        <Table.Td>
                            {application.status === 'SUBMITTED' && (
                                <Button 
                                color='orange'
                                onClick={() => mutation.mutate(application.id)}
                                >   
                                    Отозвать
                                </Button>
                            )
                            }
                        </Table.Td>
                    </Table.Tr>
                ))}
            </Table.Tbody>
        </Table>
    )
}