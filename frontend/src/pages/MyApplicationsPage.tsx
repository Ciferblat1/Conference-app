import { Container, Title } from "@mantine/core";
import UserBar from "../components/UserBar";
import ApplicationList from "../components/ApplicationList"



export default function MyApplicationsPage() {
    return (
        <Container component="main" size="sm" py="md">
            <UserBar />

            <Title order={1} ta="center" my="xl">Мои заявки</Title>

            <ApplicationList />
        </Container>
    )
}