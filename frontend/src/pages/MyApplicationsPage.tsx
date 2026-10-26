import { Button, Container, Group, Title } from "@mantine/core";
import UserBar from "../components/UserBar";
import ApplicationList from "../components/ApplicationList"
import { Link } from "react-router";



export default function MyApplicationsPage() {
    return (
        <Container component="main" size="sm" py="md">
            <UserBar />

            <Group justify="flex-end" mb="md">
                <Button component={Link} to="/applications/new">Подать заявку</Button>
            </Group>

            <Title order={1} ta="center" my="xl">Мои заявки</Title>

            <ApplicationList />
        </Container>
    )
}