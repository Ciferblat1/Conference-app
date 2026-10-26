import { Container, Title } from '@mantine/core';
import DirectionList from '../components/DirectionList'
import UserBar from '../components/UserBar';

export default function DirectionsPage() {
    return (
        <Container component="main" size="sm" py="md">
            <UserBar />

            <Title order={1} ta="center" my="xl">Направления конференции</Title>

            <DirectionList />
        </Container>
    )
}